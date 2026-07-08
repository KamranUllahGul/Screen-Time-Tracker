package com.kamran.screentimetracker.data.local

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore by preferencesDataStore(name = "settings")

enum class ThemeConfig {
    FOLLOW_SYSTEM, LIGHT, DARK
}

class SettingsManager(private val context: Context) {
    companion object {
        val THEME_CONFIG = stringPreferencesKey("theme_config")
        val USE_24_HOUR_FORMAT = booleanPreferencesKey("use_24_hour_format")
    }

    val themeConfig: Flow<ThemeConfig> = context.dataStore.data.map { preferences ->
        val themeName = preferences[THEME_CONFIG] ?: ThemeConfig.FOLLOW_SYSTEM.name
        ThemeConfig.valueOf(themeName)
    }

    val use24HourFormat: Flow<Boolean> = context.dataStore.data.map { preferences ->
        preferences[USE_24_HOUR_FORMAT] ?: true
    }

    suspend fun setThemeConfig(theme: ThemeConfig) {
        context.dataStore.edit { preferences ->
            preferences[THEME_CONFIG] = theme.name
        }
    }

    suspend fun setUse24HourFormat(use24Hour: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[USE_24_HOUR_FORMAT] = use24Hour
        }
    }
}
