package com.brendigo.skypeep

import org.junit.Assert.*
import org.junit.Test

class SkyPeepPickupTest {
    private val body = SkyPeepContact.body(172f, 800f, 295f, 232f)

    @Test fun fastBroadPhaseNeverCreatesPhantomPickups() {
        // Deterministic sample sweep checks distances around all angles,
        // including exact cardinal-axis tangencies and diagonal near misses.
        val radii=listOf(SkyPeepPickup.COIN_RADIUS,
                        SkyPeepPickup.GEM_RADIUS,SkyPeepLives.HEART_RADIUS)
        for (radius in radii)
            for (angleStep in 0 until 72)
                for (distance in listOf(0f,30f,40f,60f,80f,90f,95f,120f)) {
                    val angle=angleStep*Math.PI/36.0
                    val x=body.cx+Math.cos(angle).toFloat()*distance
                    val y=body.cy+Math.sin(angle).toFloat()*distance
                    val contact=SkyPeepPickup.overlaps(body,x,y,radius)
                    // Proven exact outer bounds; all points beyond the
                    // containing disk must be rejected.
                    val maxDistance=kotlin.math.max(body.rx,body.ry)+radius
                    if (distance>maxDistance) assertFalse(contact)
                    val minDistance=kotlin.math.min(body.rx,body.ry)+radius
                    if (distance<=minDistance) assertTrue(contact)
                }
    }

    @Test fun genuineContactAndExactHorizontalTangencyCount() {
        assertTrue(SkyPeepPickup.overlaps(body, body.cx, body.cy, SkyPeepPickup.COIN_RADIUS))
        assertTrue(SkyPeepPickup.overlaps(body,
            body.cx + body.rx + SkyPeepPickup.COIN_RADIUS,
            body.cy, SkyPeepPickup.COIN_RADIUS))
        assertFalse(SkyPeepPickup.overlaps(body,
            body.cx + body.rx + SkyPeepPickup.COIN_RADIUS + 1f,
            body.cy, SkyPeepPickup.COIN_RADIUS))
    }

    @Test fun visibleAirGapAndDiagonalSeparationDoNotAwardCurrencies() {
        assertFalse(SkyPeepPickup.overlaps(body, body.cx + 100f, body.cy,
            SkyPeepPickup.COIN_RADIUS))
        assertFalse(SkyPeepPickup.overlaps(body, body.cx,
            body.cy + 87f, SkyPeepPickup.GEM_RADIUS))
        assertFalse(SkyPeepPickup.overlaps(body, body.cx + 74f,
            body.cy + 76f, SkyPeepPickup.GEM_RADIUS))
        assertTrue(SkyPeepPickup.overlaps(body, body.cx + 30f,
            body.cy + 52f, SkyPeepPickup.COIN_RADIUS))
    }

    @Test fun allModesUseSamePhysicalPickupPixels() {
        for (mode in SkyDifficulty.entries)
            for (world in 1..6) {
                assertFalse(SkyPeepPickup.overlaps(body, body.cx + 120f,
                    body.cy, SkyPeepPickup.COIN_RADIUS))
                assertTrue(SkyPeepPickup.overlaps(body, body.cx + 10f,
                    body.cy, SkyPeepPickup.GEM_RADIUS))
                assertTrue(SkyRouteRules.gap(world, mode) > 0f)
            }
    }

    @Test fun invalidCoordinatesAndRadiiNeverCredit() {
        assertFalse(SkyPeepPickup.overlaps(body, Float.NaN, 0f, SkyPeepPickup.COIN_RADIUS))
        assertFalse(SkyPeepPickup.overlaps(body, Float.POSITIVE_INFINITY, 0f,
            SkyPeepPickup.COIN_RADIUS))
        assertFalse(SkyPeepPickup.overlaps(body, 0f, 0f, -1f))
    }
}
