package com.spbu.receiptprinter.ui.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.spbu.receiptprinter.bluetooth.BluetoothPrinterManager
import com.spbu.receiptprinter.bluetooth.PrinterStatus
import com.spbu.receiptprinter.data.repository.SettingRepository
import com.spbu.receiptprinter.data.repository.TransaksiRepository
import com.spbu.receiptprinter.util.FormatUtil
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class DashboardUiState(
    val jumlahTransaksi: Int = 0,
    val totalLiter: Double = 0.0,
    val totalPenjualan: Double = 0.0,
    val namaSpbu: String = "",
    val printerStatus: PrinterStatus = PrinterStatus.Disconnected,
    val isLoading: Boolean = false
)

@HiltViewModel
class DashboardViewModel @Inject constructor(
    private val transaksiRepository: TransaksiRepository,
    private val settingRepository: SettingRepository,
    val bluetoothPrinterManager: BluetoothPrinterManager
) : ViewModel() {

    private val tanggalHariIni = FormatUtil.tanggalHariIni()

    val uiState: StateFlow<DashboardUiState> = combine(
        transaksiRepository.countHariIni(tanggalHariIni),
        transaksiRepository.totalLiterHariIni(tanggalHariIni),
        transaksiRepository.totalPenjualanHariIni(tanggalHariIni),
        settingRepository.setting,
        bluetoothPrinterManager.printerStatus
    ) { count, liter, penjualan, setting, printerStatus ->
        DashboardUiState(
            jumlahTransaksi = count,
            totalLiter = liter ?: 0.0,
            totalPenjualan = penjualan ?: 0.0,
            namaSpbu = setting?.namaSpbu ?: "",
            printerStatus = printerStatus
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = DashboardUiState()
    )

    /** Auto-reconnect ke printer terakhir saat buka dashboard */
    fun tryAutoReconnect() {
        viewModelScope.launch {
            val setting = settingRepository.getSetting() ?: return@launch
            val mac = setting.printerDefault
            val nama = setting.namaPrinter
            if (mac.isNotBlank() && !bluetoothPrinterManager.isConnected()) {
                bluetoothPrinterManager.connect(mac, nama)
            }
        }
    }
}
