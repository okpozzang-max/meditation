package com.example.morningmeditation.playback

import android.content.Context
import android.content.Intent
import android.app.ActivityManager
import androidx.core.content.ContextCompat
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.example.morningmeditation.domain.MeditationDuration
import com.example.morningmeditation.domain.SessionState
import com.example.morningmeditation.domain.Soundscape
import java.util.concurrent.TimeUnit
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withTimeout
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class MeditationSessionServiceTest {
    private val context: Context = InstrumentationRegistry.getInstrumentation().targetContext

    @After
    fun stopSession() = runBlocking {
        context.startService(command(MeditationSessionService.ACTION_FINISH))
        awaitState("cleanup") { it == SessionState.Idle }
        withTimeout(TimeUnit.SECONDS.toMillis(5L)) {
            val activityManager = context.getSystemService(ActivityManager::class.java)
            while (activityManager.getRunningServices(Int.MAX_VALUE).any {
                    it.service.className == MeditationSessionService::class.java.name
                }
            ) {
                delay(50L)
            }
        }
    }

    @Test
    fun muteKeepsCountdownActive() = runBlocking {
        start(Soundscape.RAIN)
        val playing = awaitState("start") { it is SessionState.Running && it.soundEnabled }

        context.startService(command(MeditationSessionService.ACTION_SOUND_OFF))
        val muted = awaitState("mute") { it is SessionState.Running && !it.soundEnabled }
        delay(1_500L)
        val continued = SessionStateStore.state.value

        assertTrue(playing.remainingMillis > muted.remainingMillis)
        assertTrue(muted.remainingMillis > continued.remainingMillis)
    }

    @Test
    fun notificationActionsPauseResumeAndFinish() = runBlocking {
        start(Soundscape.NONE)
        awaitState("start") { it is SessionState.Running }

        context.startService(command(MeditationSessionService.ACTION_PAUSE))
        val paused = awaitState("pause") { it is SessionState.Paused }
        delay(750L)
        assertEquals(paused.remainingMillis, SessionStateStore.state.value.remainingMillis)

        context.startService(command(MeditationSessionService.ACTION_RESUME))
        awaitState("resume") { it is SessionState.Running }
        context.startService(command(MeditationSessionService.ACTION_FINISH))
        awaitState("finish") { it == SessionState.Idle }
        Unit
    }

    private fun start(soundscape: Soundscape) {
        val intent = command(MeditationSessionService.ACTION_START)
            .putExtra(MeditationSessionService.EXTRA_DURATION, MeditationDuration.ONE_MINUTE.name)
            .putExtra(MeditationSessionService.EXTRA_SOUNDSCAPE, soundscape.name)
        ContextCompat.startForegroundService(context, intent)
    }

    private fun command(action: String) = Intent(context, MeditationSessionService::class.java).setAction(action)

    private suspend fun awaitState(stage: String, predicate: (SessionState) -> Boolean): SessionState = try {
        withTimeout(TimeUnit.SECONDS.toMillis(8L)) {
            SessionStateStore.state.first(predicate)
        }
    } catch (error: TimeoutCancellationException) {
        throw AssertionError("Timed out during $stage; current state=${SessionStateStore.state.value}", error)
    }
}
