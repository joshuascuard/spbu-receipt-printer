package com.spbu.receiptprinter.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Entity Room Database untuk pengaturan aplikasi.
 * Hanya ada satu baris (id=1) yang selalu diupdate.
 */
@Entity(tableName = "setting")
data class Setting(
    @PrimaryKey
    val id: Int = 1,

    /** Nama SPBU tampil di struk */
    val namaSpbu: String = "SPBU RY SOLO-KLATEN",

    /** Nomor SPBU Pertamina */
    val nomorSpbu: String = "44.574.14",

    /** Alamat SPBU tampil di struk */
    val alamat: String = "Jl. Raya Solo-Klaten",

    /** Teks footer bawah struk */
    val footer: String = "Terima Kasih\nSelamat Jalan",

    /** Path file logo SPBU (URI string dari internal storage) */
    val logoPath: String = "",

    /** MAC address printer Bluetooth default */
    val printerDefault: String = "",

    /** Nama printer Bluetooth default */
    val namaPrinter: String = "",

    /** Ukuran font cetak: SMALL, MEDIUM, LARGE */
    val ukuranFont: String = "MEDIUM",

    /** Jumlah copy cetak per transaksi (1-10) */
    val jumlahCopy: Int = 1,

    /** Lebar kertas: 58 atau 80 (mm) */
    val lebarKertas: Int = 58,

    /** Dark mode aktif atau tidak */
    val darkMode: Boolean = false,

    /** Margin kiri printer dalam karakter */
    val marginKiri: Int = 0
)
