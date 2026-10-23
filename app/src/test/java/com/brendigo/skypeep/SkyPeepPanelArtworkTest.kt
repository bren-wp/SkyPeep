package com.brendigo.skypeep

import org.junit.Assert.*
import org.junit.Test

class SkyPeepPanelArtworkTest {
    @Test fun allSixSelectedWorldsStageOnlyWhenNeeded() {
        for (world in 1..6) {
            assertFalse(SkyPeepPanelArtwork.needsStage(world, world))
            assertEquals("bg_$world", SkyPeepPanelArtwork.backgroundFor(world))
            assertTrue(SkyPeepPanelArtwork.needsStage(world, 0))
        }
    }

    @Test fun changingMapSelectionMustNotRenderAnUnloadedBackground() {
        // The map can select 6 while world 1 remains loaded in memory.
        assertTrue(SkyPeepPanelArtwork.needsStage(6, 1))
        assertEquals("bg_1", SkyPeepPanelArtwork.backgroundFor(1))
        // On successful stage, both panel and overscan render world 6.
        assertFalse(SkyPeepPanelArtwork.needsStage(6, 6))
        assertEquals("bg_6", SkyPeepPanelArtwork.backgroundFor(6))
        // After failure, staged world remains 1 and thus still renderable.
        assertEquals("bg_1", SkyPeepPanelArtwork.backgroundFor(1))
    }

    @Test fun corruptedSelectedWorldIsRejectedBeforeStaging() {
        for (world in listOf(0, -1, 7, Int.MAX_VALUE)) {
            try {
                SkyPeepPanelArtwork.needsStage(world, 1)
                fail("invalid selected world accepted")
            } catch (_: IllegalArgumentException) { }
        }
        assertNull(SkyPeepPanelArtwork.backgroundFor(0))
        assertNull(SkyPeepPanelArtwork.backgroundFor(99))
    }
}
