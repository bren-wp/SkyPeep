package com.brendigo.skypeep

import org.junit.Assert.*
import org.junit.Test

class SkyPeepPickupTest {
    private val body = SkyPeepContact.body(172f, 800f, 295f, 232f)

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
