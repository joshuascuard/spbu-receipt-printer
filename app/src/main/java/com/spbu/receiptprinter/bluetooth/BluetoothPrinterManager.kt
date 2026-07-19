package com.spbu.receiptprinter.bluetooth

import android.annotation.SuppressLint
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothManager
import android.content.Context
import android.util.Log
import com.dantsu.escposprinter.EscPosPrinter
import com.dantsu.escposprinter.connection.bluetooth.BluetoothConnection
import com.dantsu.escposprinter.connection.bluetooth.BluetoothPrintersConnections
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Status koneksi printer Bluetooth.
 */
sealed class PrinterStatus {
    object Disconnected : PrinterStatus()
    object Connecting : PrinterStatus()
    data class Connected(val deviceName: String, val macAddress: String) : PrinterStatus()
    data class Error(val message: String) : PrinterStatus()
    object Printing : PrinterStatus()
}

/**
 * Hasil operasi print.
 */
sealed class PrintResult {
    object Success : PrintResult()
    data class Failure(val message: String) : PrintResult()
}

/**
 * Manager utama untuk koneksi Bluetooth dan cetak ESC/POS.
 * Menggunakan library DantSu/ESCPOS-ThermalPrinter-Android.
 *
 * Library: https://github.com/DantSu/ESCPOS-ThermalPrinter-Android
 */
@Singleton
class BluetoothPrinterManager @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val TAG = "BluetoothPrinterManager"

    /** Status printer saat ini */
    private val _printerStatus = MutableStateFlow<PrinterStatus>(PrinterStatus.Disconnected)
    val printerStatus: StateFlow<PrinterStatus> = _printerStatus.asStateFlow()

    /** Koneksi Bluetooth aktif */
    private var currentConnection: BluetoothConnection? = null

    /** Bluetooth Adapter sistem */
    private val bluetoothAdapter: BluetoothAdapter? by lazy {
        val bluetoothManager = context.getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
        bluetoothManager.adapter
    }

    /** Cek apakah Bluetooth tersedia dan aktif */
    fun isBluetoothEnabled(): Boolean = bluetoothAdapter?.isEnabled == true

    /**
     * Mendapatkan daftar perangkat Bluetooth yang sudah di-pair.
     * Membutuhkan permission BLUETOOTH_CONNECT.
     */
    @SuppressLint("MissingPermission")
    fun getPairedDevices(): List<BluetoothDevice> {
        return try {
            bluetoothAdapter?.bondedDevices?.toList() ?: emptyList()
        } catch (e: SecurityException) {
            Log.e(TAG, "Permission denied untuk mengakses paired devices", e)
            emptyList()
        }
    }

    /**
     * Mendapatkan daftar printer Bluetooth yang tersedia (sudah paired).
     * Menggunakan helper dari library DantSu.
     */
    @SuppressLint("MissingPermission")
    fun getAvailablePrinters(): Array<BluetoothConnection>? {
        return try {
            BluetoothPrintersConnections.selectFirstPaired()?.let { arrayOf(it) }
                ?: BluetoothPrintersConnections().list
        } catch (e: Exception) {
            Log.e(TAG, "Error mendapatkan daftar printer", e)
            null
        }
    }

    /**
     * Menghubungkan ke printer Bluetooth berdasarkan MAC address.
     *
     * @param macAddress MAC address printer Bluetooth
     * @param deviceName Nama perangkat untuk ditampilkan
     */
    @SuppressLint("MissingPermission")
    suspend fun connect(macAddress: String, deviceName: String): Boolean {
        return withContext(Dispatchers.IO) {
            try {
                _printerStatus.value = PrinterStatus.Connecting

                // Disconnect dulu jika masih ada koneksi
                disconnect()

                val device = bluetoothAdapter?.getRemoteDevice(macAddress)
                    ?: throw Exception("Perangkat tidak ditemukan: $macAddress")

                val connection = BluetoothConnection(device)
                connection.connect()
                currentConnection = connection

                _printerStatus.value = PrinterStatus.Connected(deviceName, macAddress)
                Log.d(TAG, "Berhasil terhubung ke $deviceName ($macAddress)")
                true

            } catch (e: SecurityException) {
                val msg = "Permission Bluetooth ditolak"
                Log.e(TAG, msg, e)
                _printerStatus.value = PrinterStatus.Error(msg)
                false
            } catch (e: Exception) {
                val msg = "Gagal terhubung: ${e.message}"
                Log.e(TAG, msg, e)
                _printerStatus.value = PrinterStatus.Error(msg)
                false
            }
        }
    }

    /**
     * Memutus koneksi dari printer Bluetooth saat ini.
     */
    fun disconnect() {
        try {
            currentConnection?.disconnect()
        } catch (e: Exception) {
            Log.w(TAG, "Error saat disconnect", e)
        }
        currentConnection = null
        _printerStatus.value = PrinterStatus.Disconnected
    }

    /**
     * Mencetak struk menggunakan format ESC/POS dari library DantSu.
     *
     * @param escPosText Teks format ESC/POS dengan tag HTML-like dari library DantSu
     * @param lebarKertas Lebar kertas: 58 atau 80 mm
     * @param jumlahCopy Jumlah copy cetak
     */
    @SuppressLint("MissingPermission")
    suspend fun print(
        escPosText: String,
        lebarKertas: Int = 58,
        jumlahCopy: Int = 1
    ): PrintResult {
        return withContext(Dispatchers.IO) {
            try {
                val connection = currentConnection
                    ?: return@withContext PrintResult.Failure("Printer tidak terhubung")

                _printerStatus.value = PrinterStatus.Printing

                // Karakter per baris tergantung lebar kertas
                // 58mm: ~32 karakter, 80mm: ~48 karakter
                val charPerLine = if (lebarKertas == 80) 48 else 32

                // DPI printer thermal standar
                val dpi = 203
                val mmFeedPaper = 20

                val printer = EscPosPrinter(connection, dpi, lebarKertas.toFloat(), charPerLine)

                repeat(jumlahCopy) {
                    printer.printFormattedText(escPosText, mmFeedPaper)
                }

                // Kembalikan status ke Connected setelah selesai print
                val statusSebelumnya = _printerStatus.value
                if (statusSebelumnya is PrinterStatus.Printing) {
                    // Ambil info dari status sebelumnya (kita perlu menyimpannya)
                    _printerStatus.value = PrinterStatus.Connected(
                        getCurrentDeviceName(),
                        getCurrentMacAddress()
                    )
                }

                Log.d(TAG, "Print berhasil ($jumlahCopy copy)")
                PrintResult.Success

            } catch (e: Exception) {
                val msg = "Gagal mencetak: ${e.message}"
                Log.e(TAG, msg, e)
                _printerStatus.value = PrinterStatus.Error(msg)
                // Coba reconnect otomatis
                PrintResult.Failure(msg)
            }
        }
    }

    /**
     * Test print untuk memverifikasi koneksi printer.
     */
    suspend fun testPrint(lebarKertas: Int = 58): PrintResult {
        val testText = buildString {
            append("[C]<b>TEST PRINT</b>\n")
            append("[C]SPBU Receipt Printer\n")
            append("[C]--------------------------------\n")
            append("[C]Printer terhubung dengan baik!\n")
            append("[C]--------------------------------\n")
            append("[C]${java.text.SimpleDateFormat("dd/MM/yyyy HH:mm:ss", java.util.Locale.getDefault()).format(java.util.Date())}\n")
            append("[C]\n\n\n")
        }
        return print(testText, lebarKertas)
    }

    /** Mendapatkan nama perangkat dari status saat ini */
    private fun getCurrentDeviceName(): String {
        return when (val s = _printerStatus.value) {
            is PrinterStatus.Connected -> s.deviceName
            else -> ""
        }
    }

    /** Mendapatkan MAC address dari status saat ini */
    private fun getCurrentMacAddress(): String {
        return when (val s = _printerStatus.value) {
            is PrinterStatus.Connected -> s.macAddress
            else -> ""
        }
    }

    /** Cek apakah printer sedang terhubung */
    fun isConnected(): Boolean = _printerStatus.value is PrinterStatus.Connected
}
