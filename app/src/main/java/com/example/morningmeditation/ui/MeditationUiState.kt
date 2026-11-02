package com.example.morningmeditation.ui

import com.example.morningmeditation.data.SessionSelection
import com.example.morningmeditation.domain.SessionState

data class HomeUiState(
    val selection: SessionSelection = SessionSelection(),
)

data class SessionUiState(
    val state: SessionState = SessionState.Idle,
)

data class MeditationUiState(
    val home: HomeUiState = HomeUiState(),
    val session: SessionUiState = SessionUiState(),
)
