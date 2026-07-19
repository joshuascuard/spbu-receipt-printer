package com.spbu.receiptprinter.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Entity Room Database untuk data transaksi SPBU.
 * Setiap record mewakili satu transaksi pengisian BBM.
 */
@Entity(tableName = "transaksi")
data class Transaksi(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,

    /** Nomor transaksi custom dari user, bukan auto-generate */
    val nomorTransaksi: String,

    /** Tanggal transaksi format: dd/MM/yyyy */
    val tanggal: String,

    /** Jam transaksi format: HH:mm:ss */
    val jam: String,

    /** Nomor shift (1, 2, 3, dst) */
    val shift: String,

    /** Nama operator yang bertugas */
    val operator: String,

    /** Jenis pembayaran: CASH, QRIS, Debit, Kredit, Lainnya */
    val pembayaran: String,

    /** Nomor plat kendaraan */
    val nomorPlat: String,

    /** Nama produk BBM */
    val produk: String,

    /** Harga per liter saat transaksi (bisa berbeda dari harga default) */
    val hargaLiter: Double,

    /** Volume BBM dalam liter */
    val volume: Double,

    /** Total harga transaksi */
    val total: Double,

    /** Timestamp Unix saat transaksi dibuat, untuk sorting */
    val createdAt: Long = System.currentTimeMillis()
)
