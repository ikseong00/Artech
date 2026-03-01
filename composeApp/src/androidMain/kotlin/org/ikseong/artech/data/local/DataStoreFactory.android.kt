package org.ikseong.devnews.data.local

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.core.Preferences
import okio.Path.Companion.toPath

actual class DataStoreFactory(private val context: Context) {
    private val dataStore: DataStore<Preferences> by lazy {
        val path = context.filesDir.resolve(DATASTORE_FILE_NAME).absolutePath
        PreferenceDataStoreFactory.createWithPath(
            produceFile = { path.toPath() },
        )
    }

    actual fun create(): DataStore<Preferences> = dataStore
}
