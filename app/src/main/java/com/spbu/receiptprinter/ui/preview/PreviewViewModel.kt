package com.spbu.receiptprinter.ui.preview

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.spbu.receiptprinter.bluetooth.BluetoothPrinterManager
import com.spbu.receiptprinter.bluetooth.EscPosFormatter
import com.spbu.receiptprinter.bluetooth.PrintResult
import com.spbu.receiptprinter.bluetooth.PrinterStatus
import com.spbu.receiptprinter.data.model.Setting
import com.spbu.receiptprinter.data.model.Transaksi
import com.spbu.receiptprinter.data.repository.SettingRepository
import com.spbu.receiptprinter.data.repository.TransaksiRepository
import com.spbu.receiptprinter.util.ExportUtil
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class PreviewUiState(
    val transaksi: Transaksi? = null,
    val setting: Setting? = null,
    val previewText: String = "",
    val isLoading: Boolean = false,
    val isCetak: Boolean = false,
    val cetakBerhasil: Boolean = false,
    val pesanError: String = "",
    val pesanInfo: String = "",
    val printerStatus: PrinterStatus = PrinterStatus.Disconnected
)

@HiltViewModel
class PreviewViewModel @Inject constructor(
    private val transaksiRepository: TransaksiRepository,
    private val settingRepository: SettingRepository,
    private val bluetoothPrinterManager: BluetoothPrinterManager,
    private val escPosFormatter: EscPosFormatter
) : ViewModel() {

    private val _uiState = MutableStateFlow(PreviewUiState())
    val uiState: StateFlow<PreviewUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            bluetoothPrinterManager.printerStatus.collect { status ->
                _uiState.update { it.copy(printerStatus = status) }
            }
        }
    }

    /** Load transaksi dan setting untuk ditampilkan di preview */
    fun load(transaksiId: Long) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            val transaksi = transaksiRepository.getById(transaksiId)
            val setting = settingRepository.getSetting()

            if (transaksi != null && setting != null) {
                val previewText = escPosFormatter.formatPreview(
                    transaksi, setting, setting.lebarKertas
                )
                _uiState.update { state ->
                    state.copy(
                        transaksi = transaksi,
                        setting = setting,
                        previewText = previewText,
                        isLoading = false
                    )
                }
            } else {
                _uiState.update { it.copy(
                    isLoading = false,
                    pesanError = "Data tidak ditemukan"
                )}
            }
        }
    }

    /** Cetak struk ke printer Bluetooth */
    fun cetak(context: Context) {
        val state = _uiState.value
        val transaksi = state.transaksi ?: return
        val setting = state.setting ?: return

        if (!bluetoothPrinterManager.isConnected()) {
            _uiState.update { it.copy(pesanError = "Printer tidak terhubung. Silakan hubungkan printer terlebih dahulu.") }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isCetak = true) }

            val escPosText = escPosFormatter.formatStruk(
    transaksi, setting, setting.lebarKertas, context
)

            val result = bluetoothPrinterManager.print(
                escPosText = escPosText,
                lebarKertas = setting.lebarKertas,
                jumlahCopy = setting.jumlahCopy
            )

            when (result) {
                is PrintResult.Success -> {
                    _uiState.update { it.copy(
                        isCetak = false,
                        cetakBerhasil = true,
                        pesanInfo = "Berhasil mencetak ${setting.jumlahCopy} copy"
                    )}
                }
                is PrintResult.Failure -> {
                    _uiState.update { it.copy(
                        isCetak = false,
                        pesanError = result.message
                    )}
                }
            }
        }
    }

    /** Export transaksi ke PDF dan return Uri */
    fun exportPdf(context: Context) {
        val state = _uiState.value
        val t = state.transaksi ?: return
        val s = state.setting ?: return

        viewModelScope.launch {
            val uri = ExportUtil.exportTransaksiToPdf(
                context = context,
                transaksi = t,
                namaSpbu = s.namaSpbu,
                nomorSpbu = s.nomorSpbu,
                alamat = s.alamat
            )
            if (uri != null) {
                ExportUtil.shareFile(context, uri, "application/pdf", "Bagikan Struk PDF")
            } else {
                _uiState.update { it.copy(pesanError = "Gagal membuat PDF") }
            }
        }
    }

    fun clearPesanError() = _uiState.update { it.copy(pesanError = "") }
    fun clearPesanInfo() = _uiState.update { it.copy(pesanInfo = "") }
    fun resetCetakBerhasil() = _uiState.update { it.copy(cetakBerhasil = false) }
}
