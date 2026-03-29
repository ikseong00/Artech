package org.ikseong.artech.data.repository

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.datetime.Instant
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
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

    suspend fun clearScrollPosition() {
        dataStore.edit { preferences ->
            preferences.remove(SCROLL_POSITION_KEY)
            preferences.remove(SCROLL_OFFSET_KEY)
        }
    }

    val skippedOptionalVersion: Flow<String?> = dataStore.data.map { it[SKIPPED_OPTIONAL_VERSION_KEY] }

    suspend fun setSkippedOptionalVersion(version: String) {
        dataStore.edit { it[SKIPPED_OPTIONAL_VERSION_KEY] = version }
    }

    suspend fun getRecommendRefreshRemaining(): Int {
        val prefs = dataStore.data.first()
        val savedDate = prefs[RECOMMEND_REFRESH_DATE_KEY]
        return if (savedDate == todayDateString()) {
            MAX_RECOMMEND_REFRESHES - (prefs[RECOMMEND_REFRESH_COUNT_KEY] ?: 0)
        } else {
            MAX_RECOMMEND_REFRESHES
        }
    }

    suspend fun useRecommendRefresh(): Int {
        val today = todayDateString()
        val prefs = dataStore.data.first()
        val savedDate = prefs[RECOMMEND_REFRESH_DATE_KEY]
        val currentCount = if (savedDate == today) {
            prefs[RECOMMEND_REFRESH_COUNT_KEY] ?: 0
        } else {
            0
        }
        val newCount = currentCount + 1
        dataStore.edit {
            it[RECOMMEND_REFRESH_DATE_KEY] = today
            it[RECOMMEND_REFRESH_COUNT_KEY] = newCount
        }
        return MAX_RECOMMEND_REFRESHES - newCount
    }

    private fun todayDateString(): String =
        Instant.fromEpochMilliseconds(kotlin.time.Clock.System.now().toEpochMilliseconds())
            .toLocalDateTime(TimeZone.currentSystemDefault()).date.toString()

    companion object {
        const val MAX_RECOMMEND_REFRESHES = 5
        private val THEME_MODE_KEY = stringPreferencesKey("theme_mode")
        private val LAST_VISIT_TIME_KEY = longPreferencesKey("last_visit_time")
        private val SCROLL_POSITION_KEY = intPreferencesKey("scroll_position")
        private val SCROLL_OFFSET_KEY = intPreferencesKey("scroll_offset")
        private val SKIPPED_OPTIONAL_VERSION_KEY = stringPreferencesKey("skipped_optional_version")
        private val RECOMMEND_REFRESH_DATE_KEY = stringPreferencesKey("recommend_refresh_date")
        private val RECOMMEND_REFRESH_COUNT_KEY = intPreferencesKey("recommend_refresh_count")
    }
}
