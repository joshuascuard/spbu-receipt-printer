package com.spbu.receiptprinter.data.repository

import com.spbu.receiptprinter.data.dao.SettingDao
import com.spbu.receiptprinter.data.model.Setting
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Repository untuk pengaturan aplikasi.
 */
@Singleton
class SettingRepository @Inject constructor(
    private val settingDao: SettingDao
) {
    val setting: Flow<Setting?> = settingDao.getSettingFlow()

    suspend fun getSetting(): Setting? = settingDao.getSetting()

    suspend fun update(setting: Setting) = settingDao.update(setting)

    suspend fun insert(setting: Setting) = settingDao.insert(setting)

    suspend fun updatePrinterDefault(mac: String, nama: String) =
        settingDao.updatePrinterDefault(mac, nama)

    suspend fun updateDarkMode(darkMode: Boolean) =
        settingDao.updateDarkMode(darkMode)
}
