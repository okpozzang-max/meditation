package com.example.morningmeditation.ui

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import com.example.morningmeditation.domain.MeditationDuration
import com.example.morningmeditation.domain.SessionState
import com.example.morningmeditation.domain.Soundscape
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test

class SessionScreenTest {
    @get:Rule
    val compose = createComposeRule()

    @Test
    fun showsRemainingTimeAndSelectedSound() {
        compose.setContent {
            SessionScreen(
                SessionState.Running(MeditationDuration.FIVE_MINUTES, Soundscape.FOREST, true, 299_000L),
                onPauseResume = {}, onSoundToggle = {}, onFinish = {},
            )
        }

        compose.onNodeWithText("04:59").assertIsDisplayed()
        compose.onNodeWithText("숲 소리").assertIsDisplayed()
    }

    @Test
    fun pauseResumeLabelsTrackSessionState() {
        var paused = false
        compose.setContent {
            SessionScreen(
                SessionState.Running(MeditationDuration.FIVE_MINUTES, Soundscape.RAIN, true, 120_000L),
                onPauseResume = { paused = true }, onSoundToggle = {}, onFinish = {},
            )
        }

        compose.onNodeWithText("일시정지").performClick()
        assertEquals(true, paused)
    }

    @Test
    fun muteCanBeReversed() {
        var enabled: Boolean? = null
        compose.setContent {
            SessionScreen(
                SessionState.Paused(MeditationDuration.FIVE_MINUTES, Soundscape.WAVES, true, 120_000L),
                onPauseResume = {}, onSoundToggle = { enabled = it }, onFinish = {},
            )
        }

        compose.onNodeWithText("소리 끄기").performClick()
        assertEquals(false, enabled)
    }

    @Test
    fun finishReturnsControlToHome() {
        var finished = false
        compose.setContent {
            SessionScreen(
                SessionState.Running(MeditationDuration.ONE_MINUTE, Soundscape.NONE, false, 60_000L),
                onPauseResume = {}, onSoundToggle = {}, onFinish = { finished = true },
            )
        }

        compose.onNodeWithText("명상 마치기").performClick()
        assertEquals(true, finished)
    }
}
