package com.brendigo.skypeep

import org.junit.Assert.*
import org.junit.Test

class SkyPeepMenuTouchTest {
    @Test fun allSevenIllustratedControlsHaveDistinctCentres() {
        val expected = listOf(
            Triple(470f, 1019f, SkyPeepMenuTouch.Action.PLAY),
            Triple(277f, 1235f, SkyPeepMenuTouch.Action.LEVELS),
            Triple(653f, 1235f, SkyPeepMenuTouch.Action.SKINS),
            Triple(277f, 1420f, SkyPeepMenuTouch.Action.SHOP),
            Triple(653f, 1420f, SkyPeepMenuTouch.Action.SETTINGS),
            Triple(290f, 65f, SkyPeepMenuTouch.Action.COINS),
            Triple(870f, 65f, SkyPeepMenuTouch.Action.GEMS)
        )
        for ((x, y, action) in expected)
            assertEquals(action, SkyPeepMenuTouch.actionAt(x, y))
    }
    @Test fun illustratedButtonGuttersAreInert() {
        assertEquals(SkyPeepMenuTouch.Action.LEVELS, SkyPeepMenuTouch.actionAt(277f, 1327f))
        assertEquals(SkyPeepMenuTouch.Action.SKINS, SkyPeepMenuTouch.actionAt(653f, 1330f))
        assertNull(SkyPeepMenuTouch.actionAt(277f, 1335f))
        assertNull(SkyPeepMenuTouch.actionAt(653f, 1340f))
        assertEquals(SkyPeepMenuTouch.Action.SHOP, SkyPeepMenuTouch.actionAt(277f, 1348f))
        assertEquals(SkyPeepMenuTouch.Action.SETTINGS, SkyPeepMenuTouch.actionAt(653f, 1348f))
        assertNull(SkyPeepMenuTouch.actionAt(470f, 1420f))
        assertNull(SkyPeepMenuTouch.actionAt(470f, 1160f))
        assertNull(SkyPeepMenuTouch.actionAt(120f, 80f))
        assertNull(SkyPeepMenuTouch.actionAt(Float.NaN, 1200f))
        assertNull(SkyPeepMenuTouch.actionAt(450f, Float.POSITIVE_INFINITY))
    }
}
