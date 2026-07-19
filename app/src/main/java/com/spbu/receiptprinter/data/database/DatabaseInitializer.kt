package com.spbu.receiptprinter.data.database

import com.spbu.receiptprinter.data.dao.ProdukBbmDao
import com.spbu.receiptprinter.data.dao.SettingDao
import com.spbu.receiptprinter.data.model.ProdukBbm
import com.spbu.receiptprinter.data.model.Setting
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Menginisialisasi data default saat aplikasi pertama kali dijalankan.
 * Data BBM default Pertamina beserta harga standar.
 */
@Singleton
class DatabaseInitializer @Inject constructor(
    private val produkBbmDao: ProdukBbmDao,
    private val settingDao: SettingDao
) {
    /**
     * Panggil saat aplikasi pertama kali dibuka.
     * Hanya mengisi data jika tabel masih kosong.
     */
    suspend fun initialize() {
        initProdukBbm()
        initSetting()
    }

    /** Inisialisasi daftar produk BBM dengan harga default */
    private suspend fun initProdukBbm() {
        if (produkBbmDao.count() > 0) return // Sudah ada data, skip

        val defaultProduk = listOf(
            ProdukBbm(namaProduk = "Pertalite",      harga = 10_000.0, urutan = 1),
            ProdukBbm(namaProduk = "Pertamax",       harga = 12_300.0, urutan = 2),
            ProdukBbm(namaProduk = "Pertamax Turbo", harga = 13_700.0, urutan = 3),
            ProdukBbm(namaProduk = "Dexlite",        harga = 13_950.0, urutan = 4),
            ProdukBbm(namaProduk = "Pertamina Dex",  harga = 14_500.0, urutan = 5),
            ProdukBbm(namaProduk = "Solar",          harga = 6_800.0,  urutan = 6),
            ProdukBbm(namaProduk = "Custom",         harga = 0.0,      urutan = 7)
        )
        defaultProduk.forEach { produkBbmDao.insert(it) }
    }

    /** Inisialisasi setting default jika belum ada */
    private suspend fun initSetting() {
        if (settingDao.getSetting() == null) {
            settingDao.insert(Setting())
        }
    }
}
