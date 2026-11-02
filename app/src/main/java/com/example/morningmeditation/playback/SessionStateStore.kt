package com.example.morningmeditation.playback

import com.example.morningmeditation.domain.SessionState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

object SessionStateStore {
    private val mutableState = MutableStateFlow<SessionState>(SessionState.Idle)
    val state: StateFlow<SessionState> = mutableState.asStateFlow()

    fun publish(state: SessionState) {
        mutableState.value = state
    }
}
