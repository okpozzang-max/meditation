package com.example.morningmeditation.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.example.morningmeditation.domain.MeditationDuration
import com.example.morningmeditation.domain.Soundscape
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

data class SessionSelection(
    val duration: MeditationDuration = MeditationDuration.FIVE_MINUTES,
    val soundscape: Soundscape = Soundscape.FOREST,
)

class SessionPreferences(private val dataStore: DataStore<Preferences>) {
    constructor(context: Context) : this(context.sessionPreferencesDataStore)

    fun observe(): Flow<SessionSelection> = dataStore.data.map { preferences ->
        SessionSelection(
            duration = preferences[DURATION]?.let { stored ->
                MeditationDuration.entries.firstOrNull { it.name == stored }
            } ?: MeditationDuration.FIVE_MINUTES,
            soundscape = Soundscape.fromStorage(preferences[SOUNDSCAPE]),
        )
    }

    suspend fun save(selection: SessionSelection) {
        dataStore.edit { preferences ->
            preferences[DURATION] = selection.duration.name
            preferences[SOUNDSCAPE] = selection.soundscape.name
        }
    }

    private companion object {
        val DURATION = stringPreferencesKey("duration")
        val SOUNDSCAPE = stringPreferencesKey("soundscape")
    }
}

private val Context.sessionPreferencesDataStore: DataStore<Preferences> by preferencesDataStore(
    name = "session.preferences_pb",
)
