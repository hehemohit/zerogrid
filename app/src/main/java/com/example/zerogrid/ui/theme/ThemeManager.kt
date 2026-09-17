package com.example.zerogrid.ui.theme

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue

enum class AppThemeMode {
    SYSTEM, DARK, LIGHT
}

object ThemeManager {
    var themeMode by mutableStateOf(AppThemeMode.SYSTEM)

    fun toggleTheme(isDark: Boolean) {
        themeMode = if (isDark) AppThemeMode.DARK else AppThemeMode.LIGHT
    }
}
