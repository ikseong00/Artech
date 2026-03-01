package org.ikseong.artech.data.local

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences

expect class DataStoreFactory {
    fun create(): DataStore<Preferences>
}

internal const val DATASTORE_FILE_NAME = "artech_settings.preferences_pb"
