package com.brendigo.skypeep

import org.junit.Assert.*
import org.junit.Test

class SkyPeepRecordSaveTest {
    @Test fun everyDifficultyUsesOnlyItsOwnScoreKeyAndGlobalBest() {
        for (route in SkyDifficulty.entries.indices) {
            val written = linkedMapOf<String, Int>()
            SkyPeepRecordSave.write(route, 500, 17) { key, value -> written[key] = value }
            assertEquals(listOf("best", "route_best_$route"), written.keys.toList())
            assertEquals(listOf(500, 17), written.values.toList())
        }
    }

    @Test fun invalidRouteIsRejectedBeforeAnyWrite() {
        val entries = mutableListOf<String>()
        try {
            SkyPeepRecordSave.write(100, 9, 10) { key, _ -> entries.add(key) }
            fail("invalid difficulty accepted")
        } catch (_: IllegalArgumentException) { }
        assertTrue(entries.isEmpty())
    }

    @Test fun corruptedNegativeRecordsCannotBePersisted() {
        val values = mutableMapOf<String, Int>()
        SkyPeepRecordSave.write(
            SkyDifficulty.EXPLORER.ordinal, -5, -2
        ) { key, value -> values[key] = value }
        assertEquals(0, values["best"])
        assertEquals(0, values["route_best_1"])
    }
}
