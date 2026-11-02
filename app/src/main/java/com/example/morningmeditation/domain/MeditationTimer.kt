package com.example.morningmeditation.domain

fun interface MonotonicClock {
    fun nowMillis(): Long
}

class MeditationTimer(
    private val clock: MonotonicClock = MonotonicClock { System.nanoTime() / 1_000_000L },
) {
    private var current: SessionState = SessionState.Idle
    private var deadlineMillis: Long = 0L

    fun start(duration: MeditationDuration, soundscape: Soundscape) {
        deadlineMillis = clock.nowMillis() + duration.millis
        current = SessionState.Running(
            duration = duration,
            soundscape = soundscape,
            soundEnabled = soundscape != Soundscape.NONE,
            remainingMillis = duration.millis,
        )
    }

    fun pause() {
        val running = current as? SessionState.Running ?: return
        val remaining = remainingAt(clock.nowMillis())
        current = if (remaining == 0L) {
            SessionState.Completed(running.duration, running.soundscape, running.soundEnabled)
        } else {
            SessionState.Paused(running.duration, running.soundscape, running.soundEnabled, remaining)
        }
    }

    fun resume() {
        val paused = current as? SessionState.Paused ?: return
        if (paused.remainingMillis == 0L) {
            current = SessionState.Completed(paused.duration, paused.soundscape, paused.soundEnabled)
        } else {
            deadlineMillis = clock.nowMillis() + paused.remainingMillis
            current = SessionState.Running(
                paused.duration,
                paused.soundscape,
                paused.soundEnabled,
                paused.remainingMillis,
            )
        }
    }

    fun setSoundEnabled(enabled: Boolean) {
        current = when (val state = stateAt(clock.nowMillis())) {
            is SessionState.Running -> state.copy(soundEnabled = enabled)
            is SessionState.Paused -> state.copy(soundEnabled = enabled)
            is SessionState.Completed -> state.copy(soundEnabled = enabled)
            SessionState.Idle -> SessionState.Idle
        }
    }

    fun finish() {
        current = when (val state = current) {
            is SessionState.Running -> SessionState.Completed(state.duration, state.soundscape, state.soundEnabled)
            is SessionState.Paused -> SessionState.Completed(state.duration, state.soundscape, state.soundEnabled)
            else -> state
        }
    }

    fun stateAt(nowMillis: Long): SessionState = when (val state = current) {
        is SessionState.Running -> {
            val remaining = remainingAt(nowMillis)
            if (remaining == 0L) {
                SessionState.Completed(state.duration, state.soundscape, state.soundEnabled).also { current = it }
            } else {
                state.copy(remainingMillis = remaining)
            }
        }
        else -> state
    }

    private fun remainingAt(nowMillis: Long): Long = (deadlineMillis - nowMillis).coerceAtLeast(0L)
}
