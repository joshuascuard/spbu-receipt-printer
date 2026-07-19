package com.spbu.receiptprinter

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.*
import androidx.compose.ui.platform.LocalContext
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.compose.rememberNavController
import com.spbu.receiptprinter.data.repository.SettingRepository
import com.spbu.receiptprinter.ui.AppNavGraph
import com.spbu.receiptprinter.ui.common.SPBUTheme
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

/**
 * MainActivity - satu-satunya Activity dalam aplikasi.
 * Menggunakan Jetpack Compose secara penuh (single Activity architecture).
 */
@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @Inject
    lateinit var settingRepository: SettingRepository

    override fun onCreate(savedInstanceState: Bundle?) {
        // Pasang splash screen sebelum super.onCreate()
        installSplashScreen()

        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            // Observe dark mode dari database setting
            val setting by settingRepository.setting.collectAsStateWithLifecycle(initialValue = null)
            val isDarkMode = setting?.darkMode ?: isSystemInDarkTheme()

            SPBUTheme(darkTheme = isDarkMode) {
                val navController = rememberNavController()
                AppNavGraph(navController = navController)
            }
        }
    }
}
