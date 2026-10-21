package com.brendigo.skypeep

import org.junit.Assert.*
import org.junit.Test

class SkyPeepGatePassTest {
    @Test fun precisionSamplingOccursAtIllustratedObstacleMidpoint() {
        assertFalse(SkyPeepGatePass.reachedBirdCentre(211f, 275f, 347.5f))
        assertTrue(SkyPeepGatePass.reachedBirdCentre(210f, 275f, 347.5f))
        assertTrue(SkyPeepGatePass.reachedBirdCentre(100f, 275f, 347.5f))
        assertFalse(SkyPeepGatePass.reachedBirdCentre(0f, 0f, 300f))
    }

    @Test fun scoreArrivesWhenRealBodyClearsTrailingPillarCap() {
        val bodyLeft = 301f
        val grace = 5f
        val gateWidth = 275f
        assertFalse(SkyPeepGatePass.hasClearedBody(31f,gateWidth,bodyLeft,grace))
        assertTrue(SkyPeepGatePass.hasClearedBody(30f,gateWidth,bodyLeft,grace))
        // Old fixed BIRD_X+40 threshold waited almost another hundred units.
        assertTrue(30f + gateWidth > 212f)
        assertFalse(SkyPeepGatePass.hasClearedBody(
            Float.NaN,gateWidth,bodyLeft,grace))
        assertFalse(SkyPeepGatePass.hasClearedBody(
            0f,gateWidth,bodyLeft,Float.NEGATIVE_INFINITY))
    }

    @Test fun spawnTimingsRetainFractionalFrameOverrun() {
        assertEquals(1.995f, SkyPeepGatePass.nextSpawnCountdown(-.005f, 2f), .0001f)
        assertEquals(2f, SkyPeepGatePass.nextSpawnCountdown(0f, 2f), .0001f)
        assertTrue(SkyPeepGatePass.nextSpawnCountdown(-50f, 1.5f) > 0f)
    }

    @Test fun precisionPassIsFixedAtCrossingEvenWhenBirdFliesAwayLater() {
        val crossingY = 805f
        val centerY = 810f
        val gap = 420f
        assertTrue(SkyRouteRules.isPrecisionPass(crossingY,centerY,gap))
        // Sample is immutable after midpoint crossing; the bird's later
        // movement must not remove an already earned accurate passage.
        val sampledPrecise = SkyRouteRules.isPrecisionPass(crossingY,centerY,gap)
        assertFalse(SkyRouteRules.isPrecisionPass(1100f,centerY,gap))
        assertTrue(sampledPrecise)
    }

    @Test fun invalidGeometryCannotSample() {
        assertFalse(SkyPeepGatePass.reachedBirdCentre(Float.NaN,275f,400f))
        assertFalse(SkyPeepGatePass.reachedBirdCentre(10f,275f,Float.POSITIVE_INFINITY))
    }
}
