package com.example.morningmeditation.playback

import androidx.annotation.RawRes
import com.example.morningmeditation.R
import com.example.morningmeditation.domain.Soundscape

object AudioCatalog {
    @RawRes
    fun resourceFor(soundscape: Soundscape): Int? = when (soundscape) {
        Soundscape.NONE -> null
        Soundscape.FOREST -> R.raw.forest_loop
        Soundscape.WAVES -> R.raw.waves_loop
        Soundscape.RAIN -> R.raw.rain_loop
        Soundscape.SINGING_BOWL -> R.raw.singing_bowl
    }
}
