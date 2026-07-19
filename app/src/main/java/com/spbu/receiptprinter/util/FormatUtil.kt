package com.spbu.receiptprinter.util

import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Utility class untuk format tampilan angka, tanggal, dan teks.
 */
object FormatUtil {

    private val locale = Locale("id", "ID")

    private val symbolsRupiah = DecimalFormatSymbols(locale).apply {
        groupingSeparator = '.'
        decimalSeparator = ','
    }

    /** Format rupiah: Rp. 12.300 */
    fun formatRupiah(nominal: Double): String {
        val formatter = DecimalFormat("#,###", symbolsRupiah)
        return "Rp. ${formatter.format(nominal)}"
    }

    /** Format angka besar tanpa "Rp.": 150.000 */
    fun formatAngkaBesar(nominal: Double): String {
        val formatter = DecimalFormat("#,###", symbolsRupiah)
        return formatter.format(nominal)
    }

    /** Format volume BBM: 12.195 */
    fun formatVolume(volume: Double): String {
        val formatter = DecimalFormat("#,##0.000", symbolsRupiah)
        return formatter.format(volume)
    }

    /** Tanggal hari ini format dd/MM/yyyy */
    fun tanggalHariIni(): String {
        return SimpleDateFormat("dd/MM/yyyy", locale).format(Date())
    }

    /** Jam sekarang format HH:mm:ss */
    fun jamSekarang(): String {
        return SimpleDateFormat("HH:mm:ss", locale).format(Date())
    }

    /** Timestamp sekarang format dd/MM/yyyy HH:mm:ss */
    fun timestampSekarang(): String {
        return SimpleDateFormat("dd/MM/yyyy HH:mm:ss", locale).format(Date())
    }

    /** Parse Double dari string, return 0.0 jika gagal */
    fun parseDouble(text: String): Double {
        return text.replace(",", ".").toDoubleOrNull() ?: 0.0
    }

    /** Parse Long dari string, return 0 jika gagal */
    fun parseLong(text: String): Long {
        return text.replace(".", "").toLongOrNull() ?: 0L
    }

    /** Hitung total dari harga/liter dan volume */
    fun hitungTotal(hargaLiter: Double, volume: Double): Double {
        return hargaLiter * volume
    }

    /** Hitung volume dari harga/liter dan total */
    fun hitungVolume(hargaLiter: Double, total: Double): Double {
        if (hargaLiter == 0.0) return 0.0
        return total / hargaLiter
    }

    /** Format nama file untuk export (tanpa karakter spesial) */
    fun namaFileExport(prefix: String): String {
        val timestamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
        return "${prefix}_$timestamp"
    }

    /** Format harga untuk input field (hanya angka) */
    fun formatHargaInput(nominal: Double): String {
        if (nominal == 0.0) return ""
        val formatter = DecimalFormat("#,###", symbolsRupiah)
        return formatter.format(nominal)
    }

    /** Bersihkan format rupiah menjadi double */
    fun bersihkanHarga(text: String): Double {
        return text.replace(".", "").replace(",", ".").toDoubleOrNull() ?: 0.0
    }
}
