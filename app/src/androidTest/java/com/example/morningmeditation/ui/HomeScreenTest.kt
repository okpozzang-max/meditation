package com.example.morningmeditation.ui

import androidx.compose.ui.test.assertHeightIsAtLeast
import androidx.compose.ui.test.assertIsSelected
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.unit.dp
import com.example.morningmeditation.data.SessionSelection
import com.example.morningmeditation.domain.MeditationDuration
import com.example.morningmeditation.domain.Soundscape
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test

class HomeScreenTest {
    @get:Rule
    val compose = createComposeRule()

    @Test
    fun showsFiveDurationsAndFiveSoundChoices() {
        compose.setContent { HomeScreen(SessionSelection(), onStart = { _, _ -> }) }

        listOf("1분", "5분", "10분", "15분", "20분", "소리 없음", "숲", "파도", "비", "싱잉볼")
            .forEach { compose.onNodeWithText(it).assertIsDisplayed() }
    }

    @Test
    fun selectedChoicesExposeSelectedSemantics() {
        compose.setContent { HomeScreen(SessionSelection(), onStart = { _, _ -> }) }

        compose.onNodeWithText("5분").assertIsSelected()
        compose.onNodeWithText("숲").assertIsSelected()
    }

    @Test
    fun touchTargetsMeetAndroidMinimum() {
        compose.setContent { HomeScreen(SessionSelection(), onStart = { _, _ -> }) }

        compose.onNodeWithText("5분").assertHeightIsAtLeast(48.dp)
        compose.onNodeWithText("숲").assertHeightIsAtLeast(48.dp)
    }

    @Test
    fun startPassesSelectedDurationAndSound() {
        var started: SessionSelection? = null
        compose.setContent {
            HomeScreen(
                selection = SessionSelection(),
                onStart = { duration, sound -> started = SessionSelection(duration, sound) },
            )
        }

        compose.onNodeWithText("10분").performClick()
        compose.onNodeWithText("비").performClick()
        compose.onNodeWithText("명상 시작").performClick()

        assertEquals(SessionSelection(MeditationDuration.TEN_MINUTES, Soundscape.RAIN), started)
    }

    @Test
    fun defaultSelectionStartsWithOneTap() {
        var starts = 0
        compose.setContent { HomeScreen(SessionSelection(), onStart = { _, _ -> starts++ }) }

        compose.onNodeWithText("명상 시작").performClick()

        assertEquals(1, starts)
    }
}
