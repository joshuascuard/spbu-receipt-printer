package com.spbu.receiptprinter.ui.common

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

// Warna tema merah Pertamina
private val PertaminaRed = Color(0xFFE31837)
private val PertaminaRedDark = Color(0xFFB01129)
private val PertaminaRedLight = Color(0xFFFF5252)

private val DarkColorScheme = darkColorScheme(
    primary = PertaminaRedLight,
    onPrimary = Color.White,
    primaryContainer = PertaminaRedDark,
    onPrimaryContainer = Color.White,
    secondary = Color(0xFFFF8A65),
    onSecondary = Color.Black,
    background = Color(0xFF1C1B1F),
    onBackground = Color(0xFFE6E1E5),
    surface = Color(0xFF2B2A2F),
    onSurface = Color(0xFFE6E1E5),
    surfaceVariant = Color(0xFF3A383F),
    onSurfaceVariant = Color(0xFFCAC4D0),
    error = Color(0xFFCF6679),
    outline = Color(0xFF938F99)
)

private val LightColorScheme = lightColorScheme(
    primary = PertaminaRed,
    onPrimary = Color.White,
    primaryContainer = Color(0xFFFFDAD6),
    onPrimaryContainer = Color(0xFF410002),
    secondary = Color(0xFFFF6E40),
    onSecondary = Color.White,
    background = Color(0xFFFFFBFE),
    onBackground = Color(0xFF1C1B1F),
    surface = Color(0xFFFFFBFE),
    onSurface = Color(0xFF1C1B1F),
    surfaceVariant = Color(0xFFF5DDDB),
    onSurfaceVariant = Color(0xFF534341),
    error = Color(0xFFBA1A1A),
    outline = Color(0xFF857371)
)

/**
 * Tema utama aplikasi SPBU Receipt Printer.
 * Mendukung dark mode dan Material You (Android 12+).
 */
@Composable
fun SPBUTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false, // Nonaktifkan dynamic color untuk menjaga branding Pertamina
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = colorScheme.primary.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !darkTheme
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        content = content
    )
}
