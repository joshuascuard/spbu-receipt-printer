package com.spbu.receiptprinter.data.dao

import androidx.room.*
import com.spbu.receiptprinter.data.model.Setting
import kotlinx.coroutines.flow.Flow

/**
 * Data Access Object untuk tabel setting.
 */
@Dao
interface SettingDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(setting: Setting)

    @Update
    suspend fun update(setting: Setting)

    /** Ambil setting sebagai Flow (reactive) */
    @Query("SELECT * FROM setting WHERE id = 1")
    fun getSettingFlow(): Flow<Setting?>

    /** Ambil setting sekali (untuk use case non-reactive) */
    @Query("SELECT * FROM setting WHERE id = 1")
    suspend fun getSetting(): Setting?

    /** Update hanya printer default */
    @Query("UPDATE setting SET printerDefault = :mac, namaPrinter = :nama WHERE id = 1")
    suspend fun updatePrinterDefault(mac: String, nama: String)

    /** Update dark mode */
    @Query("UPDATE setting SET darkMode = :darkMode WHERE id = 1")
    suspend fun updateDarkMode(darkMode: Boolean)
}
