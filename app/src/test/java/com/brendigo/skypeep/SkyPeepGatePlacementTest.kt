package com.brendigo.skypeep

import org.junit.Assert.*
import org.junit.Test
import kotlin.math.abs

class SkyPeepGatePlacementTest {
    @Test fun consecutiveGatesNeverJumpBeyondTravelBudget() {
        for (mode in SkyDifficulty.entries)
            for (world in 1..6) {
                var previous: Float? = null
                for (i in 0..99) {
                    val next = SkyPeepGatePlacement.next(previous, world, mode,
                        (i % 7) / 6f)
                    assertTrue(next in 570f..1110f)
                    if (previous != null) {
                        assertTrue(abs(next - previous) <=
                            SkyPeepGatePlacement.maxShift(world, mode) + 0.001f)
                    }
                    val half = SkyRouteRules.gap(world, mode) * .5f
                    assertTrue(next - half > SkyPeepContact.BODY_RADIUS_Y)
                    assertTrue(next + half <
                        SkyPeepScreenFrame.REFERENCE_HEIGHT - SkyPeepContact.BODY_RADIUS_Y)
                    previous = next
                }
            }
    }

    @Test fun spawnExtremesAndInvalidPreviousRemainBounded() {
        for (mode in SkyDifficulty.entries) {
            assertEquals(570f, SkyPeepGatePlacement.next(null, 1, mode, 0f), 0.001f)
            assertEquals(1110f, SkyPeepGatePlacement.next(null, 1, mode, 1f), 0.001f)
            assertTrue(SkyPeepGatePlacement.next(Float.NaN, 6, mode, .5f)
                in 570f..1110f)
            assertTrue(SkyPeepGatePlacement.next(999999f, 6, mode, .5f)
                in 570f..1110f)
            assertTrue(SkyPeepGatePlacement.next(-999999f, 6, mode, .5f)
                in 570f..1110f)
            assertEquals(SkyPeepGatePlacement.next(900f, 6, mode, 1f),
                SkyPeepGatePlacement.next(900f, 6, mode, 12f), 0.001f)
        }
    }
}
