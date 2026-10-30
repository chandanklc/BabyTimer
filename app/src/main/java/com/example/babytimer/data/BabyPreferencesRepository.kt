package com.example.babytimer.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "baby_preferences")

interface BabyPreferencesRepository {
    val babyName: Flow<String>
    val photoUri: Flow<String?>
    val normalTimerEndTime: Flow<Long>
    val reusableTimerEndTime: Flow<Long>
    suspend fun updateBabyName(name: String)
    suspend fun updatePhotoUri(uri: String?)
    suspend fun updateNormalTimerEndTime(time: Long)
    suspend fun updateReusableTimerEndTime(time: Long)
}

class BabyPreferencesRepositoryImpl(private val dataStore: DataStore<Preferences>) : BabyPreferencesRepository {

    private object PreferencesKeys {
        val BABY_NAME = stringPreferencesKey("baby_name")
        val PHOTO_URI = stringPreferencesKey("photo_uri")
        val NORMAL_TIMER_END_TIME = longPreferencesKey("normal_timer_end_time")
        val REUSABLE_TIMER_END_TIME = longPreferencesKey("reusable_timer_end_time")
    }

    override val babyName: Flow<String> = dataStore.data
        .map { preferences ->
            preferences[PreferencesKeys.BABY_NAME] ?: "Baby"
        }

    override val photoUri: Flow<String?> = dataStore.data
        .map { preferences ->
            preferences[PreferencesKeys.PHOTO_URI]
        }

    override val normalTimerEndTime: Flow<Long> = dataStore.data
        .map { preferences ->
            preferences[PreferencesKeys.NORMAL_TIMER_END_TIME] ?: 0L
        }

    override val reusableTimerEndTime: Flow<Long> = dataStore.data
        .map { preferences ->
            preferences[PreferencesKeys.REUSABLE_TIMER_END_TIME] ?: 0L
        }

    override suspend fun updateBabyName(name: String) {
        dataStore.edit { preferences ->
            preferences[PreferencesKeys.BABY_NAME] = name
        }
    }

    override suspend fun updatePhotoUri(uri: String?) {
        dataStore.edit { preferences ->
            if (uri != null) {
                preferences[PreferencesKeys.PHOTO_URI] = uri
            } else {
                preferences.remove(PreferencesKeys.PHOTO_URI)
            }
        }
    }

    override suspend fun updateNormalTimerEndTime(time: Long) {
        dataStore.edit { preferences ->
            if (time > 0L) {
                preferences[PreferencesKeys.NORMAL_TIMER_END_TIME] = time
            } else {
                preferences.remove(PreferencesKeys.NORMAL_TIMER_END_TIME)
            }
        }
    }

    override suspend fun updateReusableTimerEndTime(time: Long) {
        dataStore.edit { preferences ->
            if (time > 0L) {
                preferences[PreferencesKeys.REUSABLE_TIMER_END_TIME] = time
            } else {
                preferences.remove(PreferencesKeys.REUSABLE_TIMER_END_TIME)
            }
        }
    }
}
