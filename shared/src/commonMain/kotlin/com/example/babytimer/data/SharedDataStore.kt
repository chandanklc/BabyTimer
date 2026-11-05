package com.example.babytimer.data

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences

const val DATA_STORE_FILE_NAME = "baby_preferences.preferences_pb"

expect fun createDataStore(): DataStore<Preferences>
