package com.spbu.receiptprinter.ui.settings

import android.content.Context
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.spbu.receiptprinter.data.model.Setting
import com.spbu.receiptprinter.data.repository.SettingRepository
import com.spbu.receiptprinter.util.BackupUtil
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class PengaturanUiState(
    val setting: Setting = Setting(),
    val isLoading: Boolean = false,
    val pesanInfo: String = "",
    val pesanError: String = "",
    val isSimpanBerhasil: Boolean = false
)

@HiltViewModel
class PengaturanViewModel @Inject constructor(
    private val settingRepository: SettingRepository
) : ViewModel() {

    val uiState: StateFlow<PengaturanUiState> = settingRepository.setting
        .filterNotNull()
        .map { setting -> PengaturanUiState(setting = setting) }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = PengaturanUiState()
        )

    private val _pesanInfo = MutableStateFlow("")
    val pesanInfo: StateFlow<String> = _pesanInfo.asStateFlow()

    private val _pesanError = MutableStateFlow("")
    val pesanError: StateFlow<String> = _pesanError.asStateFlow()

    /** Simpan semua pengaturan */
    fun simpan(setting: Setting) {
        viewModelScope.launch {
            try {
                settingRepository.update(setting)
                _pesanInfo.value = "Pengaturan berhasil disimpan"
            } catch (e: Exception) {
                _pesanError.value = "Gagal menyimpan: ${e.message}"
            }
        }
    }

    /** Update logo path setelah user memilih gambar */
    fun updateLogo(uri: Uri) {
        viewModelScope.launch {
            val setting = settingRepository.getSetting() ?: return@launch
            settingRepository.update(setting.copy(logoPath = uri.toString()))
            _pesanInfo.value = "Logo berhasil diperbarui"
        }
    }

    /** Backup database */
    fun backupDatabase(context: Context) {
        viewModelScope.launch {
            val uri = BackupUtil.backupDatabase(context)
            if (uri != null) {
                _pesanInfo.value = "Backup berhasil disimpan"
            } else {
                _pesanError.value = "Gagal membuat backup"
            }
        }
    }

    /** Restore database */
    fun restoreDatabase(context: Context, uri: Uri) {
        viewModelScope.launch {
            val berhasil = BackupUtil.restoreDatabase(context, uri)
            if (berhasil) {
                _pesanInfo.value = "Restore berhasil. Restart aplikasi untuk memuat data."
            } else {
                _pesanError.value = "Gagal restore database"
            }
        }
    }

    /** Toggle dark mode */
    fun toggleDarkMode(darkMode: Boolean) {
        viewModelScope.launch {
            settingRepository.updateDarkMode(darkMode)
        }
    }

    fun clearPesanInfo() { _pesanInfo.value = "" }
    fun clearPesanError() { _pesanError.value = "" }
}
