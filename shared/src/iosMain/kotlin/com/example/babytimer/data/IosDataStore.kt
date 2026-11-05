package com.example.babytimer.data

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import kotlinx.cinterop.ExperimentalForeignApi
import okio.Path.Companion.toPath
import platform.Foundation.NSDocumentDirectory
import platform.Foundation.NSFileManager

@OptIn(ExperimentalForeignApi::class)
actual fun createDataStore(): DataStore<Preferences> {
    val documentDirectory = NSFileManager.defaultManager.URLForDirectory(
        directory = NSDocumentDirectory,
        appropriateForURL = null,
        create = true,
        error = null
    )
    val path = requireNotNull(documentDirectory?.path) + "/$DATA_STORE_FILE_NAME"
    return PreferenceDataStoreFactory.createWithPath(
        produceFile = { path.toPath() }
    )
}
