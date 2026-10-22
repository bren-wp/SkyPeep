package com.brendigo.skypeep

import org.junit.Assert.*
import org.junit.Test

class SkyPeepPreferenceReadTest {
    @Test fun validNumbersAndFlagsAreLoadedNormally() {
        val values = mapOf("coins" to 250, "level" to 4)
        assertEquals(250, SkyPeepPreferenceRead.integer("coins", 1250,
            { key, default -> values[key] ?: default }))
        assertEquals(4, SkyPeepPreferenceRead.integer("level", 1,
            { key, default -> values[key] ?: default }))
        assertFalse(SkyPeepPreferenceRead.boolean("music", true, { _, _ -> false }))
    }

    @Test fun oneMalformedKeyDoesNotHideIndependentProgress() {
        val failures = mutableListOf<String>()
        val lookup: (String, Int) -> Int = { key, _ ->
            if (key == "coins") throw ClassCastException("invalid integer")
            if (key == "level") 6 else 3
        }
        assertEquals(1250, SkyPeepPreferenceRead.integer("coins", 1250, lookup,
            { key, _ -> failures.add(key) }))
        assertEquals(6, SkyPeepPreferenceRead.integer("level", 1, lookup,
            { key, _ -> failures.add(key) }))
        assertEquals(listOf("coins"), failures)
    }

    @Test fun wrongBooleanTypeUsesOnlyThatKeysFallback() {
        var failed = false
        val value = SkyPeepPreferenceRead.boolean("music", true,
            { _, _ -> throw IllegalStateException("wrong stored type") },
            { key, _ -> failed = key == "music" })
        assertTrue(value)
        assertTrue(failed)
    }
}
