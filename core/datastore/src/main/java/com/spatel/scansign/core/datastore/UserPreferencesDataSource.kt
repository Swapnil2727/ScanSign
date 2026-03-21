package com.spatel.scansign.core.datastore

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class UserPreferencesDataSource(
    private val dataStore: DataStore<Preferences>,
) {
    val userPreferences: Flow<UserPreferences> = dataStore.data.map { prefs ->
        UserPreferences(
            appTheme = prefs[THEME_KEY]?.let { runCatching { AppTheme.valueOf(it) }.getOrNull() }
                ?: AppTheme.SYSTEM,
            scanQuality = prefs[SCAN_QUALITY_KEY]?.let { runCatching { ScanQuality.valueOf(it) }.getOrNull() }
                ?: ScanQuality.STANDARD,
        )
    }

    suspend fun setAppTheme(theme: AppTheme) {
        dataStore.edit { it[THEME_KEY] = theme.name }
    }

    suspend fun setScanQuality(quality: ScanQuality) {
        dataStore.edit { it[SCAN_QUALITY_KEY] = quality.name }
    }

    companion object {
        private val THEME_KEY = stringPreferencesKey("app_theme")
        private val SCAN_QUALITY_KEY = stringPreferencesKey("scan_quality")
    }
}
