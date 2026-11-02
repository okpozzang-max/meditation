package com.example.morningmeditation.domain

sealed interface SessionState {
    val duration: MeditationDuration?
    val soundscape: Soundscape
    val soundEnabled: Boolean
    val remainingMillis: Long

    data object Idle : SessionState {
        override val duration: MeditationDuration? = null
        override val soundscape: Soundscape = Soundscape.NONE
        override val soundEnabled: Boolean = false
        override val remainingMillis: Long = 0L
    }

    data class Running(
        override val duration: MeditationDuration,
        override val soundscape: Soundscape,
        override val soundEnabled: Boolean,
        override val remainingMillis: Long,
    ) : SessionState

    data class Paused(
        override val duration: MeditationDuration,
        override val soundscape: Soundscape,
        override val soundEnabled: Boolean,
        override val remainingMillis: Long,
    ) : SessionState

    data class Completed(
        override val duration: MeditationDuration,
        override val soundscape: Soundscape,
        override val soundEnabled: Boolean,
    ) : SessionState {
        override val remainingMillis: Long = 0L
    }
}
