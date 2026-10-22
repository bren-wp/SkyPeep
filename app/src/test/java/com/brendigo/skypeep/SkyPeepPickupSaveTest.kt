package com.brendigo.skypeep

import org.junit.Assert.*
import org.junit.Test

class SkyPeepPickupSaveTest {
    @Test fun storesOnlyActiveWorldWithExistingStablePreferenceKeys() {
        val expected = (1..6).map { "collected_world_$it" }
        assertEquals(expected, (1..6).map(SkyPeepPickupSave::worldKey))
    }

    @Test fun rejectsUnrecognizedWorldsInsteadOfCorruptingProgress() {
        for (world in listOf(-1, 0, 7, Int.MAX_VALUE)) {
            try {
                SkyPeepPickupSave.worldKey(world)
                fail("invalid world was accepted: $world")
            } catch (_: IllegalArgumentException) { }
        }
    }

    @Test fun clampsCorruptSavesWithoutChangingPickupCompletionRules() {
        assertEquals(0, SkyPeepPickupSave.collected(-10))
        assertEquals(10_001, SkyPeepPickupSave.collected(Int.MAX_VALUE))
        assertEquals(10_000, SkyPeepPickupSave.collected(10_000))
        assertEquals(0, SkyPeepPickupSave.currency(Int.MIN_VALUE))
        assertEquals(Int.MAX_VALUE, SkyPeepPickupSave.currency(Int.MAX_VALUE))
    }
}
