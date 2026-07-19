package com.spbu.receiptprinter.ui.printer

import android.annotation.SuppressLint
import android.bluetooth.BluetoothDevice
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.spbu.receiptprinter.bluetooth.BluetoothPrinterManager
import com.spbu.receiptprinter.bluetooth.PrintResult
import com.spbu.receiptprinter.bluetooth.PrinterStatus
import com.spbu.receiptprinter.data.repository.SettingRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class PrinterUiState(
    val printerStatus: PrinterStatus = PrinterStatus.Disconnected,
    val pairedDevices: List<BluetoothDevice> = emptyList(),
    val printerDefault: String = "",
    val namaPrinterDefault: String = "",
    val isLoading: Boolean = false,
    val pesanInfo: String = "",
    val pesanError: String = "",
    val lebarKertas: Int = 58,
    val isBluetoothEnabled: Boolean = true
)

@HiltViewModel
class PrinterViewModel @Inject constructor(
    private val bluetoothPrinterManager: BluetoothPrinterManager,
    private val settingRepository: SettingRepository
) : ViewModel() {

    private val _isLoading = MutableStateFlow(false)
    private val _pesanInfo = MutableStateFlow("")
    private val _pesanError = MutableStateFlow("")
    private val _pairedDevices = MutableStateFlow<List<BluetoothDevice>>(emptyList())

    val uiState: StateFlow<PrinterUiState> = combine(
        bluetoothPrinterManager.printerStatus,
        settingRepository.setting,
        _pairedDevices,
        _isLoading,
        _pesanInfo,
        _pesanError
    ) { status, setting, devices, loading, info, error ->
        PrinterUiState(
            printerStatus = status,
            pairedDevices = devices,
            printerDefault = setting?.printerDefault ?: "",
            namaPrinterDefault = setting?.namaPrinter ?: "",
            isLoading = loading,
            pesanInfo = info,
            pesanError = error,
            lebarKertas = setting?.lebarKertas ?: 58,
            isBluetoothEnabled = bluetoothPrinterManager.isBluetoothEnabled()
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = PrinterUiState()
    )

    /** Scan/load daftar perangkat yang sudah di-pair */
    fun loadPairedDevices() {
        _pairedDevices.value = bluetoothPrinterManager.getPairedDevices()
    }

    /** Connect ke printer tertentu */
    @SuppressLint("MissingPermission")
    fun connect(device: BluetoothDevice) {
        viewModelScope.launch {
            _isLoading.value = true
            val nama = try { device.name ?: device.address } catch (e: SecurityException) { device.address }
            val berhasil = bluetoothPrinterManager.connect(device.address, nama)
            if (berhasil) {
                // Simpan sebagai printer default
                settingRepository.updatePrinterDefault(device.address, nama)
                _pesanInfo.value = "Berhasil terhubung ke $nama"
            } else {
                _pesanError.value = "Gagal terhubung ke $nama"
            }
            _isLoading.value = false
        }
    }

    /** Disconnect dari printer saat ini */
    fun disconnect() {
        bluetoothPrinterManager.disconnect()
        _pesanInfo.value = "Printer diputuskan"
    }

    /** Test print untuk verifikasi koneksi */
    fun testPrint() {
        val lebar = uiState.value.lebarKertas
        viewModelScope.launch {
            _isLoading.value = true
            val result = bluetoothPrinterManager.testPrint(lebar)
            when (result) {
                is PrintResult.Success -> _pesanInfo.value = "Test print berhasil!"
                is PrintResult.Failure -> _pesanError.value = "Test print gagal: ${result.message}"
            }
            _isLoading.value = false
        }
    }

    fun clearPesanInfo() { _pesanInfo.value = "" }
    fun clearPesanError() { _pesanError.value = "" }
}
