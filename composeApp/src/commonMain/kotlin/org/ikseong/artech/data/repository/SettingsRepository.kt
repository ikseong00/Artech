package org.ikseong.artech.data.repository

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.core.stringSetPreferencesKey
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.datetime.Instant
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import org.ikseong.artech.data.model.ThemeMode

class SettingsRepository(
    private val dataStore: DataStore<Preferences>,
) : VisitSessionStorage {
    val themeMode: Flow<ThemeMode> = dataStore.data.map { preferences ->
        val name = preferences[THEME_MODE_KEY]
        name?.let {
            runCatching { ThemeMode.valueOf(it) }.getOrNull()
        } ?: ThemeMode.SYSTEM
    }

    override val lastVisitTime: Flow<Instant?> = dataStore.data.map { preferences ->
        preferences[LAST_VISIT_TIME_KEY]?.let { Instant.fromEpochMilliseconds(it) }
    }

    suspend fun setThemeMode(mode: ThemeMode) {
        dataStore.edit { preferences ->
            preferences[THEME_MODE_KEY] = mode.name
        }
    }

    override suspend fun updateLastVisitTime() {
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
            preferences.remove(LEGACY_SCROLL_POSITION_KEY)
            preferences.remove(LEGACY_SCROLL_OFFSET_KEY)
        }
    }

    val skippedOptionalVersion: Flow<String?> = dataStore.data.map { it[SKIPPED_OPTIONAL_VERSION_KEY] }

    val interestCategories: Flow<List<String>> = dataStore.data.map { preferences ->
        val orderedCategories = preferences[INTEREST_CATEGORIES_ORDERED_KEY]
            ?.let(::decodeInterestCategories)
            .orEmpty()
        orderedCategories.ifEmpty {
            preferences[LEGACY_INTEREST_CATEGORIES_KEY]
                .orEmpty()
                .toList()
                .normalizedInterestCategories()
        }
    }

    suspend fun setSkippedOptionalVersion(version: String) {
        dataStore.edit { it[SKIPPED_OPTIONAL_VERSION_KEY] = version }
    }

    suspend fun setInterestCategories(categories: List<String>) {
        dataStore.edit { preferences ->
            val normalizedCategories = categories.normalizedInterestCategories()
            if (normalizedCategories.isEmpty()) {
                preferences.remove(INTEREST_CATEGORIES_ORDERED_KEY)
                preferences.remove(LEGACY_INTEREST_CATEGORIES_KEY)
            } else {
                preferences[INTEREST_CATEGORIES_ORDERED_KEY] = encodeInterestCategories(normalizedCategories)
                preferences.remove(LEGACY_INTEREST_CATEGORIES_KEY)
            }
        }
    }

    suspend fun toggleInterestCategory(category: String) {
        val normalizedCategory = category.trim()
        if (normalizedCategory.isEmpty()) return

        dataStore.edit { preferences ->
            val currentCategories = preferences[INTEREST_CATEGORIES_ORDERED_KEY]
                ?.let(::decodeInterestCategories)
                .orEmpty()
                .ifEmpty {
                    preferences[LEGACY_INTEREST_CATEGORIES_KEY]
                        .orEmpty()
                        .toList()
                        .normalizedInterestCategories()
                }
            val nextCategories = if (normalizedCategory in currentCategories) {
                currentCategories - normalizedCategory
            } else {
                currentCategories + normalizedCategory
            }
            if (nextCategories.isEmpty()) {
                preferences.remove(INTEREST_CATEGORIES_ORDERED_KEY)
            } else {
                preferences[INTEREST_CATEGORIES_ORDERED_KEY] = encodeInterestCategories(nextCategories)
            }
            preferences.remove(LEGACY_INTEREST_CATEGORIES_KEY)
        }
    }

    suspend fun getRecommendRefreshRemaining(): Int {
        val prefs = dataStore.data.first()
        val savedDate = prefs[RECOMMEND_REFRESH_DATE_KEY]
        return if (savedDate == todayDateString()) {
            (MAX_RECOMMEND_REFRESHES - (prefs[RECOMMEND_REFRESH_COUNT_KEY] ?: 0)).coerceAtLeast(0)
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
        val newCount = (currentCount + 1).coerceAtMost(MAX_RECOMMEND_REFRESHES)
        dataStore.edit {
            it[RECOMMEND_REFRESH_DATE_KEY] = today
            it[RECOMMEND_REFRESH_COUNT_KEY] = newCount
        }
        return MAX_RECOMMEND_REFRESHES - newCount
    }

    suspend fun getRecommendedArticleIdsSeenToday(): Set<Long> {
        val today = todayDateString()
        val prefs = dataStore.data.first()
        return if (prefs[RECOMMEND_SEEN_ARTICLE_IDS_DATE_KEY] == today) {
            prefs[RECOMMEND_SEEN_ARTICLE_IDS_KEY]
                .orEmpty()
                .mapNotNull { it.toLongOrNull() }
                .toSet()
        } else {
            emptySet()
        }
    }

    suspend fun addRecommendedArticleIdsSeenToday(ids: Collection<Long>) {
        if (ids.isEmpty()) return

        val today = todayDateString()
        dataStore.edit { preferences ->
            val existingIds = if (preferences[RECOMMEND_SEEN_ARTICLE_IDS_DATE_KEY] == today) {
                preferences[RECOMMEND_SEEN_ARTICLE_IDS_KEY].orEmpty()
            } else {
                emptySet()
            }
            preferences[RECOMMEND_SEEN_ARTICLE_IDS_DATE_KEY] = today
            preferences[RECOMMEND_SEEN_ARTICLE_IDS_KEY] = existingIds + ids.map { it.toString() }
        }
    }

    private fun todayDateString(): String =
        Instant.fromEpochMilliseconds(kotlin.time.Clock.System.now().toEpochMilliseconds())
            .toLocalDateTime(TimeZone.currentSystemDefault()).date.toString()

    private fun encodeInterestCategories(categories: List<String>): String =
        categories.normalizedInterestCategories().joinToString("\n")

    private fun decodeInterestCategories(value: String): List<String> =
        value.lineSequence().toList().normalizedInterestCategories()

    private fun List<String>.normalizedInterestCategories(): List<String> =
        map { it.trim() }
            .filter { it.isNotEmpty() }
            .distinct()

    companion object {
        const val MAX_RECOMMEND_REFRESHES = 5
        private val THEME_MODE_KEY = stringPreferencesKey("theme_mode")
        private val LAST_VISIT_TIME_KEY = longPreferencesKey("last_visit_time")
        private val SCROLL_POSITION_KEY = intPreferencesKey(HomeScrollPositionKeys.POSITION)
        private val SCROLL_OFFSET_KEY = intPreferencesKey(HomeScrollPositionKeys.OFFSET)
        private val LEGACY_SCROLL_POSITION_KEY = intPreferencesKey(HomeScrollPositionKeys.LEGACY_POSITION)
        private val LEGACY_SCROLL_OFFSET_KEY = intPreferencesKey(HomeScrollPositionKeys.LEGACY_OFFSET)
        private val SKIPPED_OPTIONAL_VERSION_KEY = stringPreferencesKey("skipped_optional_version")
        private val RECOMMEND_REFRESH_DATE_KEY = stringPreferencesKey("recommend_refresh_date")
        private val RECOMMEND_REFRESH_COUNT_KEY = intPreferencesKey("recommend_refresh_count")
        private val RECOMMEND_SEEN_ARTICLE_IDS_DATE_KEY = stringPreferencesKey("recommend_seen_article_ids_date")
        private val RECOMMEND_SEEN_ARTICLE_IDS_KEY = stringSetPreferencesKey("recommend_seen_article_ids")
        private val INTEREST_CATEGORIES_ORDERED_KEY = stringPreferencesKey("interest_categories_ordered")
        private val LEGACY_INTEREST_CATEGORIES_KEY = stringSetPreferencesKey("interest_categories")
    }
}

internal object HomeScrollPositionKeys {
    const val POSITION = "home_feed_v2_scroll_position"
    const val OFFSET = "home_feed_v2_scroll_offset"
    const val LEGACY_POSITION = "scroll_position"
    const val LEGACY_OFFSET = "scroll_offset"
}
