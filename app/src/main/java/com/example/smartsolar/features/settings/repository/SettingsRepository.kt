package com.example.smartsolar.features.settings.repository

import android.content.Context
import android.content.SharedPreferences
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class SettingsRepository(context: Context) {
    private val prefs: SharedPreferences = context.getSharedPreferences("smart_solar_settings", Context.MODE_PRIVATE)

    private val _isDarkMode = MutableStateFlow(prefs.getBoolean("dark_mode", false))
    val isDarkMode: StateFlow<Boolean> = _isDarkMode.asStateFlow()

    private val _pushEnabled = MutableStateFlow(prefs.getBoolean("push_enabled", true))
    val pushEnabled: StateFlow<Boolean> = _pushEnabled.asStateFlow()

    fun setDarkMode(enabled: Boolean) {
        prefs.edit().putBoolean("dark_mode", enabled).apply()
        _isDarkMode.value = enabled
    }

    fun setPushEnabled(enabled: Boolean) {
        prefs.edit().putBoolean("push_enabled", enabled).apply()
        _pushEnabled.value = enabled
    }
}
