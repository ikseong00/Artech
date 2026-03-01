package org.ikseong.devnews.data.local

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences

expect class DataStoreFactory {
    fun create(): DataStore<Preferences>
}

internal const val DATASTORE_FILE_NAME = "devnews_settings.preferences_pb"
