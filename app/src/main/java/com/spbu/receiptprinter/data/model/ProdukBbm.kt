package com.spbu.receiptprinter.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Entity Room Database untuk daftar produk BBM dan harganya.
 * Admin dapat menambah, edit, dan hapus produk dari menu Pengaturan.
 */
@Entity(tableName = "produk_bbm")
data class ProdukBbm(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,

    /** Nama produk BBM, misal: Pertalite, Pertamax, dst */
    val namaProduk: String,

    /** Harga per liter dalam rupiah */
    val harga: Double,

    /** Urutan tampil di dropdown, semakin kecil semakin atas */
    val urutan: Int = 0,

    /** Apakah produk ini aktif/tersedia */
    val aktif: Boolean = true
)
