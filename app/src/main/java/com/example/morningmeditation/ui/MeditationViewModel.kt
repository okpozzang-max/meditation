package com.example.morningmeditation.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.morningmeditation.data.SessionPreferences
import com.example.morningmeditation.data.SessionSelection
import com.example.morningmeditation.domain.MeditationDuration
import com.example.morningmeditation.domain.SessionState
import com.example.morningmeditation.domain.Soundscape
import com.example.morningmeditation.playback.MeditationSessionController
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class MeditationViewModel(application: Application) : AndroidViewModel(application) {
    private val preferences = SessionPreferences(application)
    private val controller = MeditationSessionController(application)

    val uiState = combine(preferences.observe(), controller.state) { selection, session ->
        MeditationUiState(HomeUiState(selection), SessionUiState(session))
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000L), MeditationUiState())

    fun saveSelection(selection: SessionSelection) {
        viewModelScope.launch { preferences.save(selection) }
    }

    fun start(duration: MeditationDuration, soundscape: Soundscape) {
        viewModelScope.launch {
            preferences.save(SessionSelection(duration, soundscape))
            controller.start(duration, soundscape)
        }
    }

    fun pause() = controller.pause()
    fun resume() = controller.resume()
    fun setSoundEnabled(enabled: Boolean) = controller.setSoundEnabled(enabled)
    fun finish() = controller.finish()
}
