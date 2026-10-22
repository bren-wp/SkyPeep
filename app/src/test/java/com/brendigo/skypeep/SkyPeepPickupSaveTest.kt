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
    @Test fun physicalPickupWritesOnlyThreeFieldsFromOneWorld() {
        val writes = linkedMapOf<String, Int>()
        SkyPeepPickupSave.write(4, 111, 1_260, 77) { key, value ->
            writes[key] = value
        }
        assertEquals(listOf("collected_world_4", "coins", "gems"), writes.keys.toList())
        assertEquals(listOf(111, 1260, 77), writes.values.toList())
    }

    @Test fun invalidWorldNeverStartsAPartialTransaction() {
        val writes = mutableListOf<String>()
        try {
            SkyPeepPickupSave.write(0, 100, 5, 6) { key, _ -> writes.add(key) }
            fail("invalid world accepted")
        } catch (_: IllegalArgumentException) { }
        assertTrue(writes.isEmpty())
    }

    @Test fun corruptSavedAmountsAreClampedInTheActualWritePath() {
        val writes = mutableMapOf<String, Int>()
        SkyPeepPickupSave.write(6, Int.MAX_VALUE, -5, -4) { key, value ->
            writes[key] = value
        }
        assertEquals(10_001, writes["collected_world_6"])
        assertEquals(0, writes["coins"])
        assertEquals(0, writes["gems"])
    }
}
