package com.example.zerogrid.ui.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val DarkColorScheme = darkColorScheme(
    primary = PrimaryCyan,
    onPrimary = DarkBackground,
    primaryContainer = DarkSurfaceDarker,
    onPrimaryContainer = PrimaryCyanGlow,
    secondary = StatusActive,
    background = DarkBackground,
    onBackground = DarkTextPrimary,
    surface = DarkCardBackground,
    onSurface = DarkTextPrimary,
    surfaceVariant = DarkSurfaceDarker,
    onSurfaceVariant = DarkTextSecondary,
    outline = DarkDivider,
    error = SosRed,
    onError = Color.White
)

private val LightColorScheme = lightColorScheme(
    primary = PrimaryCyanLight,
    onPrimary = Color.White,
    primaryContainer = LightSurfaceDarker,
    onPrimaryContainer = PrimaryCyanLight,
    secondary = PrimaryCyan,
    background = LightBackground,
    onBackground = LightTextPrimary,
    surface = LightCardBackground,
    onSurface = LightTextPrimary,
    surfaceVariant = LightSurfaceDarker,
    onSurfaceVariant = LightTextSecondary,
    outline = LightDivider,
    error = SosRed,
    onError = Color.White
)

@Composable
fun ZeroGridTheme(
    themeMode: AppThemeMode = ThemeManager.themeMode,
    content: @Composable () -> Unit
) {
    val darkTheme = when (themeMode) {
        AppThemeMode.SYSTEM -> isSystemInDarkTheme()
        AppThemeMode.DARK -> true
        AppThemeMode.LIGHT -> false
    }

    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = colorScheme.background.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !darkTheme
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        content = content
    )
}
