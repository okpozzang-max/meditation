package com.example.morningmeditation.domain

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class MeditationTimerTest {
    private class FakeClock(var current: Long = 0L) : MonotonicClock {
        override fun nowMillis(): Long = current
    }

    @Test
    fun startsWithSelectedDurationAndSound() {
        val clock = FakeClock()
        val timer = MeditationTimer(clock)

        timer.start(MeditationDuration.FIVE_MINUTES, Soundscape.FOREST)

        val state = timer.stateAt(clock.current) as SessionState.Running
        assertEquals(300_000L, state.remainingMillis)
        assertEquals(Soundscape.FOREST, state.soundscape)
    }

    @Test
    fun pauseFreezesRemainingTime() {
        val clock = FakeClock()
        val timer = MeditationTimer(clock)
        timer.start(MeditationDuration.FIVE_MINUTES, Soundscape.RAIN)
        clock.current = 60_000L

        timer.pause()
        clock.current = 90_000L

        assertEquals(240_000L, timer.stateAt(clock.current).remainingMillis)
        assertTrue(timer.stateAt(clock.current) is SessionState.Paused)
    }

    @Test
    fun resumeContinuesFromRemainingTime() {
        val clock = FakeClock()
        val timer = MeditationTimer(clock)
        timer.start(MeditationDuration.FIVE_MINUTES, Soundscape.WAVES)
        clock.current = 60_000L
        timer.pause()
        clock.current = 90_000L

        timer.resume()
        clock.current = 100_000L

        assertEquals(230_000L, timer.stateAt(clock.current).remainingMillis)
    }

    @Test
    fun finishesAtZeroAndDoesNotGoNegative() {
        val clock = FakeClock()
        val timer = MeditationTimer(clock)
        timer.start(MeditationDuration.ONE_MINUTE, Soundscape.NONE)
        clock.current = 60_001L

        assertTrue(timer.stateAt(clock.current) is SessionState.Completed)
        assertEquals(0L, timer.stateAt(clock.current).remainingMillis)
    }

    @Test
    fun soundCanBeMutedAndRestored() {
        val timer = MeditationTimer(FakeClock())
        timer.start(MeditationDuration.ONE_MINUTE, Soundscape.SINGING_BOWL)

        timer.setSoundEnabled(false)
        assertEquals(false, timer.stateAt(0L).soundEnabled)
        timer.setSoundEnabled(true)

        assertEquals(true, timer.stateAt(0L).soundEnabled)
        assertEquals(Soundscape.SINGING_BOWL, timer.stateAt(0L).soundscape)
    }
}
