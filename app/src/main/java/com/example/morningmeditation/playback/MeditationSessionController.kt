package com.example.morningmeditation.playback

import android.content.Context
import android.content.Intent
import androidx.core.content.ContextCompat
import com.example.morningmeditation.domain.MeditationDuration
import com.example.morningmeditation.domain.SessionState
import com.example.morningmeditation.domain.Soundscape
import kotlinx.coroutines.flow.StateFlow

class MeditationSessionController(context: Context) {
    private val appContext = context.applicationContext
    val state: StateFlow<SessionState> = SessionStateStore.state

    fun start(duration: MeditationDuration, soundscape: Soundscape) {
        command(MeditationSessionService.ACTION_START) {
            putExtra(MeditationSessionService.EXTRA_DURATION, duration.name)
            putExtra(MeditationSessionService.EXTRA_SOUNDSCAPE, soundscape.name)
        }
    }

    fun pause() = command(MeditationSessionService.ACTION_PAUSE)
    fun resume() = command(MeditationSessionService.ACTION_RESUME)
    fun setSoundEnabled(enabled: Boolean) = command(
        if (enabled) MeditationSessionService.ACTION_SOUND_ON else MeditationSessionService.ACTION_SOUND_OFF,
    )
    fun finish() = command(MeditationSessionService.ACTION_FINISH)

    private fun command(action: String, extras: Intent.() -> Unit = {}) {
        val intent = Intent(appContext, MeditationSessionService::class.java).setAction(action).apply(extras)
        if (action == MeditationSessionService.ACTION_START) {
            ContextCompat.startForegroundService(appContext, intent)
        } else {
            appContext.startService(intent)
        }
    }
}
