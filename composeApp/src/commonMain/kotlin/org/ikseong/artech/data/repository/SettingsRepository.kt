package org.ikseong.artech.data.repository

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.datetime.Instant
import org.ikseong.artech.data.model.ThemeMode

class SettingsRepository(
    private val dataStore: DataStore<Preferences>,
) {
    val themeMode: Flow<ThemeMode> = dataStore.data.map { preferences ->
        val name = preferences[THEME_MODE_KEY]
        name?.let {
            runCatching { ThemeMode.valueOf(it) }.getOrNull()
        } ?: ThemeMode.SYSTEM
    }

    val lastVisitTime: Flow<Instant?> = dataStore.data.map { preferences ->
        preferences[LAST_VISIT_TIME_KEY]?.let { Instant.fromEpochMilliseconds(it) }
    }

    suspend fun setThemeMode(mode: ThemeMode) {
        dataStore.edit { preferences ->
            preferences[THEME_MODE_KEY] = mode.name
        }
    }

    suspend fun updateLastVisitTime() {
        dataStore.edit { preferences ->
            preferences[LAST_VISIT_TIME_KEY] = kotlin.time.Clock.System.now().toEpochMilliseconds()
        }
    }

    val scrollPopupShown: Flow<Boolean> = dataStore.data.map { preferences ->
        preferences[SCROLL_POPUP_SHOWN_KEY] ?: false
    }

    val scrollRestorationEnabled: Flow<Boolean> = dataStore.data.map { preferences ->
        preferences[SCROLL_RESTORATION_KEY] ?: false
    }

    suspend fun setScrollPopupShown(shown: Boolean) {
        dataStore.edit { preferences ->
            preferences[SCROLL_POPUP_SHOWN_KEY] = shown
        }
    }

    suspend fun setScrollRestorationEnabled(enabled: Boolean) {
        dataStore.edit { preferences ->
            preferences[SCROLL_RESTORATION_KEY] = enabled
            preferences[SCROLL_POPUP_SHOWN_KEY] = true
        }
    }

    suspend fun saveScrollPosition(index: Int, offset: Int) {
        dataStore.edit { preferences ->
            preferences[SCROLL_POSITION_KEY] = index
            preferences[SCROLL_OFFSET_KEY] = offset
        }
    }

    suspend fun getScrollPosition(): Pair<Int, Int> {
        val prefs = dataStore.data.first()
        return Pair(
            prefs[SCROLL_POSITION_KEY] ?: 0,
            prefs[SCROLL_OFFSET_KEY] ?: 0,
        )
    }

    companion object {
        private val THEME_MODE_KEY = stringPreferencesKey("theme_mode")
        private val LAST_VISIT_TIME_KEY = longPreferencesKey("last_visit_time")
        private val SCROLL_POPUP_SHOWN_KEY = booleanPreferencesKey("scroll_popup_shown")
        private val SCROLL_RESTORATION_KEY = booleanPreferencesKey("scroll_restoration_enabled")
        private val SCROLL_POSITION_KEY = intPreferencesKey("scroll_position")
        private val SCROLL_OFFSET_KEY = intPreferencesKey("scroll_offset")
    }
}
