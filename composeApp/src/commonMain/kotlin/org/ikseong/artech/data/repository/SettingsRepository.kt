package org.ikseong.devnews.data.repository

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import org.ikseong.devnews.data.model.ThemeMode

class SettingsRepository(
    private val dataStore: DataStore<Preferences>,
) {
    val themeMode: Flow<ThemeMode> = dataStore.data.map { preferences ->
        val name = preferences[THEME_MODE_KEY]
        name?.let {
            runCatching { ThemeMode.valueOf(it) }.getOrNull()
        } ?: ThemeMode.SYSTEM
    }

    suspend fun setThemeMode(mode: ThemeMode) {
        dataStore.edit { preferences ->
            preferences[THEME_MODE_KEY] = mode.name
        }
    }

    companion object {
        private val THEME_MODE_KEY = stringPreferencesKey("theme_mode")
    }
}
