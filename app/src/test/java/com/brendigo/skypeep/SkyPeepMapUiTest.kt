package com.brendigo.skypeep

import org.junit.Assert.*
import org.junit.Test

class SkyPeepMapUiTest {
    @Test fun sixWorldsHaveShortReadableStatusWithoutRepeatedProgressBadges() {
        for(world in 1..6) {
            assertEquals("PLAY", SkyPeepMapUi.status(world,world,0))
            assertEquals("DONE", SkyPeepMapUi.status(world,world,10001))
            val other=if(world==1)2 else 1
            assertEquals("OPEN", SkyPeepMapUi.status(world,other,0))
            assertEquals("DONE", SkyPeepMapUi.status(world,other,10001))
        }
    }

    @Test fun lockStatusOverlaysAvoidOriginalWorldOneNumber() {
        assertNull(SkyPeepMapUi.lockChip(1))
        for(world in 2..6) {
            val r = SkyPeepMapUi.lockChip(world)!!
            assertTrue(r.width <= 88f)
            assertTrue(r.height <= 70f)
            assertTrue(r.left >= 0f)
            assertTrue(r.top >= 0f)
            assertTrue(r.top + r.height < 1500f)
        }
    }

    @Test fun oneFooterShowsTheSelectedWorldCollectionProgress() {
        assertEquals("WORLD 1  0 / 10001", SkyPeepMapUi.footer(1,0))
        assertEquals("WORLD 6  10001 / 10001",
            SkyPeepMapUi.footer(6,Int.MAX_VALUE))
    }
}
