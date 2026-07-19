package com.spbu.receiptprinter.data.database

import androidx.room.Database
import androidx.room.RoomDatabase
import com.spbu.receiptprinter.data.dao.ProdukBbmDao
import com.spbu.receiptprinter.data.dao.SettingDao
import com.spbu.receiptprinter.data.dao.TransaksiDao
import com.spbu.receiptprinter.data.model.ProdukBbm
import com.spbu.receiptprinter.data.model.Setting
import com.spbu.receiptprinter.data.model.Transaksi

/**
 * Room Database utama aplikasi SPBU Receipt Printer.
 * Versi database: tambahkan migration jika ada perubahan schema.
 */
@Database(
    entities = [
        Transaksi::class,
        ProdukBbm::class,
        Setting::class
    ],
    version = 1,
    exportSchema = true
)
abstract class AppDatabase : RoomDatabase() {

    abstract fun transaksiDao(): TransaksiDao
    abstract fun produkBbmDao(): ProdukBbmDao
    abstract fun settingDao(): SettingDao

    companion object {
        const val DATABASE_NAME = "spbu_receipt_printer.db"
    }
}
