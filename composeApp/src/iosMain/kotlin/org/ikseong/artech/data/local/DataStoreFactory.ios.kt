@file:OptIn(kotlinx.cinterop.ExperimentalForeignApi::class)

package org.ikseong.devnews.data.local

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.core.Preferences
import okio.Path.Companion.toPath
import platform.Foundation.NSDocumentDirectory
import platform.Foundation.NSFileManager
import platform.Foundation.NSUserDomainMask

actual class DataStoreFactory {
    private val dataStore: DataStore<Preferences> by lazy {
        val documentDirectory = NSFileManager.defaultManager.URLForDirectory(
            directory = NSDocumentDirectory,
            inDomain = NSUserDomainMask,
            appropriateForURL = null,
            create = false,
            error = null,
        )
        val path = requireNotNull(documentDirectory?.path) + "/$DATASTORE_FILE_NAME"
        PreferenceDataStoreFactory.createWithPath(
            produceFile = { path.toPath() },
        )
    }

    actual fun create(): DataStore<Preferences> = dataStore
}
