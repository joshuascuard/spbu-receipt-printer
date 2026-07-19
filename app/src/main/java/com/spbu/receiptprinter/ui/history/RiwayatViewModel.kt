package com.spbu.receiptprinter.ui.history

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.spbu.receiptprinter.bluetooth.BluetoothPrinterManager
import com.spbu.receiptprinter.bluetooth.EscPosFormatter
import com.spbu.receiptprinter.bluetooth.PrintResult
import com.spbu.receiptprinter.data.model.Transaksi
import com.spbu.receiptprinter.data.repository.SettingRepository
import com.spbu.receiptprinter.data.repository.TransaksiRepository
import com.spbu.receiptprinter.util.ExportUtil
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class RiwayatUiState(
    val daftarTransaksi: List<Transaksi> = emptyList(),
    val isLoading: Boolean = false,
    val queryPencarian: String = "",
    val filterDariTanggal: String = "",
    val filterSampaiTanggal: String = "",
    val pesanInfo: String = "",
    val pesanError: String = "",
    val transaksiDipilih: Transaksi? = null,
    val showHapusDialog: Boolean = false
)

@HiltViewModel
class RiwayatViewModel @Inject constructor(
    private val transaksiRepository: TransaksiRepository,
    private val settingRepository: SettingRepository,
    private val bluetoothPrinterManager: BluetoothPrinterManager,
    private val escPosFormatter: EscPosFormatter
) : ViewModel() {

    private val _query = MutableStateFlow("")
    private val _filterDari = MutableStateFlow("")
    private val _filterSampai = MutableStateFlow("")
    private val _pesanInfo = MutableStateFlow("")
    private val _pesanError = MutableStateFlow("")
    private val _transaksiDipilih = MutableStateFlow<Transaksi?>(null)
    private val _showHapusDialog = MutableStateFlow(false)

    /** Flow list transaksi berdasarkan filter aktif */
    @OptIn(ExperimentalCoroutinesApi::class)
    private val transaksiFlow: Flow<List<Transaksi>> = combine(
        _query, _filterDari, _filterSampai
    ) { q, dari, sampai -> Triple(q, dari, sampai) }
        .flatMapLatest { (query, dari, sampai) ->
            when {
                dari.isNotBlank() && sampai.isNotBlank() ->
                    transaksiRepository.filterByDate(dari, sampai)
                query.isNotBlank() ->
                    transaksiRepository.search(query)
                else ->
                    transaksiRepository.semuaTransaksi
            }
        }

    val uiState: StateFlow<RiwayatUiState> = combine(
        transaksiFlow,
        _pesanInfo,
        _pesanError,
        _transaksiDipilih,
        _showHapusDialog
    ) { transaksiList, info, error, dipilih, showHapus ->
        RiwayatUiState(
            daftarTransaksi = transaksiList,
            queryPencarian = _query.value,
            filterDariTanggal = _filterDari.value,
            filterSampaiTanggal = _filterSampai.value,
            pesanInfo = info,
            pesanError = error,
            transaksiDipilih = dipilih,
            showHapusDialog = showHapus
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = RiwayatUiState(isLoading = true)
    )

    fun setPencarian(q: String) { _query.value = q }

    fun setFilter(dari: String, sampai: String) {
        _filterDari.value = dari
        _filterSampai.value = sampai
    }

    fun resetFilter() {
        _filterDari.value = ""
        _filterSampai.value = ""
        _query.value = ""
    }

    fun pilihHapus(t: Transaksi) {
        _transaksiDipilih.value = t
        _showHapusDialog.value = true
    }

    fun batalHapus() {
        _transaksiDipilih.value = null
        _showHapusDialog.value = false
    }

    fun konfirmasiHapus() {
        val t = _transaksiDipilih.value ?: return
        viewModelScope.launch {
            transaksiRepository.delete(t)
            _pesanInfo.value = "Transaksi dihapus"
            batalHapus()
        }
    }

    /** Cetak ulang transaksi dari riwayat */
    fun cetakUlang(transaksi: Transaksi) {
        if (!bluetoothPrinterManager.isConnected()) {
            _pesanError.value = "Printer tidak terhubung"
            return
        }
        viewModelScope.launch {
            val setting = settingRepository.getSetting() ?: run {
                _pesanError.value = "Pengaturan tidak ditemukan"
                return@launch
            }
            val escPosText = escPosFormatter.formatStruk(transaksi, setting, setting.lebarKertas)
            val result = bluetoothPrinterManager.print(escPosText, setting.lebarKertas, setting.jumlahCopy)
            when (result) {
                is PrintResult.Success -> _pesanInfo.value = "Berhasil mencetak ulang"
                is PrintResult.Failure -> _pesanError.value = result.message
            }
        }
    }

    /** Export semua transaksi ke Excel */
    fun exportExcel(context: Context) {
        viewModelScope.launch {
            val list = transaksiRepository.getAll()
            val uri = ExportUtil.exportTransaksiToExcel(context, list)
            if (uri != null) {
                ExportUtil.shareFile(
                    context, uri,
                    "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet",
                    "Export Excel Transaksi"
                )
            } else {
                _pesanError.value = "Gagal membuat file Excel"
            }
        }
    }

    fun clearPesanInfo() { _pesanInfo.value = "" }
    fun clearPesanError() { _pesanError.value = "" }
}
