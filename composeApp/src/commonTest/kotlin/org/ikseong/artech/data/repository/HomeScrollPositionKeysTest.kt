package org.ikseong.artech.data.repository

import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import okio.Path.Companion.toPath
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotEquals
import kotlin.test.assertTrue

class HomeScrollPositionKeysTest {

    @Test
    fun homeScrollKeys_are_versioned_away_from_legacy_long_feed_keys() {
        assertEquals("scroll_position", HomeScrollPositionKeys.LEGACY_POSITION)
        assertEquals("scroll_offset", HomeScrollPositionKeys.LEGACY_OFFSET)
        assertNotEquals(HomeScrollPositionKeys.LEGACY_POSITION, HomeScrollPositionKeys.POSITION)
        assertNotEquals(HomeScrollPositionKeys.LEGACY_OFFSET, HomeScrollPositionKeys.OFFSET)
        assertTrue(HomeScrollPositionKeys.POSITION.startsWith("home_feed_v2_"))
        assertTrue(HomeScrollPositionKeys.OFFSET.startsWith("home_feed_v2_"))
    }

    @Test
    fun settingsRepository_ignores_legacy_scroll_position_and_restores_v2_position() = runBlocking {
        val dataStore = PreferenceDataStoreFactory.createWithPath(
            produceFile = { "/tmp/artech-scroll-position-${kotlin.time.Clock.System.now().toEpochMilliseconds()}.preferences_pb".toPath() },
        )
        val repository = SettingsRepository(dataStore)

        dataStore.edit { preferences ->
            preferences[intPreferencesKey(HomeScrollPositionKeys.LEGACY_POSITION)] = 99
            preferences[intPreferencesKey(HomeScrollPositionKeys.LEGACY_OFFSET)] = 24
        }

        assertEquals(0 to 0, repository.getScrollPosition())

        repository.saveScrollPosition(index = 3, offset = 12)

        assertEquals(3 to 12, repository.getScrollPosition())

        repository.clearScrollPosition()

        val preferences = dataStore.data.first()
        assertFalse(intPreferencesKey(HomeScrollPositionKeys.POSITION) in preferences)
        assertFalse(intPreferencesKey(HomeScrollPositionKeys.OFFSET) in preferences)
        assertFalse(intPreferencesKey(HomeScrollPositionKeys.LEGACY_POSITION) in preferences)
        assertFalse(intPreferencesKey(HomeScrollPositionKeys.LEGACY_OFFSET) in preferences)
    }
}
