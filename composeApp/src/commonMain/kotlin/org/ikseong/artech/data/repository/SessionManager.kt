package org.ikseong.artech.data.repository

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class SessionManager(
    private val dataStore: DataStore<Preferences>,
) {
    val isLoggedIn: Flow<Boolean> = dataStore.data.map { preferences ->
        preferences[SESSION_USER_ID_KEY] != null
    }

    suspend fun saveSession(userId: String, accessToken: String, refreshToken: String) {
        dataStore.edit { preferences ->
            preferences[SESSION_USER_ID_KEY] = userId
            preferences[SESSION_ACCESS_TOKEN_KEY] = accessToken
            preferences[SESSION_REFRESH_TOKEN_KEY] = refreshToken
        }
    }

    suspend fun clearSession() {
        dataStore.edit { preferences ->
            preferences.remove(SESSION_USER_ID_KEY)
            preferences.remove(SESSION_ACCESS_TOKEN_KEY)
            preferences.remove(SESSION_REFRESH_TOKEN_KEY)
        }
    }

    companion object {
        private val SESSION_USER_ID_KEY = stringPreferencesKey("session_user_id")
        private val SESSION_ACCESS_TOKEN_KEY = stringPreferencesKey("session_access_token")
        private val SESSION_REFRESH_TOKEN_KEY = stringPreferencesKey("session_refresh_token")
    }
}
