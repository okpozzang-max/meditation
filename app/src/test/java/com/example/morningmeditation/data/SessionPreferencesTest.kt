package com.example.morningmeditation.data

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.core.Preferences
import com.example.morningmeditation.domain.MeditationDuration
import com.example.morningmeditation.domain.Soundscape
import java.nio.file.Files
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Test

class SessionPreferencesTest {
    @Test
    fun remembersLastDurationAndSoundButStoresNoSessions() = runBlocking {
        val directory = Files.createTempDirectory("morning-preferences").toFile()
        val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
        val dataStore: DataStore<Preferences> = PreferenceDataStoreFactory.create(
            scope = scope,
            produceFile = { directory.resolve("session.preferences_pb") },
        )
        try {
            val preferences = SessionPreferences(dataStore)
            assertEquals(
                SessionSelection(MeditationDuration.FIVE_MINUTES, Soundscape.FOREST),
                preferences.observe().first(),
            )

            val chosen = SessionSelection(MeditationDuration.TEN_MINUTES, Soundscape.SINGING_BOWL)
            preferences.save(chosen)

            assertEquals(chosen, preferences.observe().first())
            assertEquals(
                setOf("duration", "soundscape"),
                dataStore.data.first().asMap().keys.map { it.name }.toSet(),
            )
        } finally {
            scope.cancel()
            directory.deleteRecursively()
        }
    }
}
