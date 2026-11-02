package com.example.morningmeditation.playback

import com.example.morningmeditation.domain.Soundscape
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertNull
import org.junit.Test

class AudioCatalogTest {
    @Test
    fun mapsEachSoundscapeToBundledAudioAndLeavesNoneSilent() {
        val soundscapes = listOf(
            Soundscape.FOREST,
            Soundscape.WAVES,
            Soundscape.RAIN,
            Soundscape.SINGING_BOWL,
        )

        val resources = soundscapes.map { AudioCatalog.resourceFor(it) }

        assertEquals(4, resources.toSet().size)
        resources.forEach { assertNotEquals(0, it) }
        assertNull(AudioCatalog.resourceFor(Soundscape.NONE))
    }
}
