package com.brendigo.skypeep

import org.junit.Assert.*
import org.junit.Test

class SkyPeepMapUiTest {
    @Test fun sixWorldsHaveShortReadableStatusWithoutRepeatedProgressBadges() {
        for(world in 1..6) {
            assertEquals("SELECTED", SkyPeepMapUi.status(world,world,0))
            assertEquals("DONE", SkyPeepMapUi.status(world,world,10001))
            val other=if(world==1)2 else 1
            assertEquals("OPEN", SkyPeepMapUi.status(world,other,0))
            assertEquals("DONE", SkyPeepMapUi.status(world,other,10001))
        }
    }

    @Test fun oneFooterShowsTheSelectedWorldCollectionProgress() {
        assertEquals("WORLD 1  0 / 10001", SkyPeepMapUi.footer(1,0))
        assertEquals("WORLD 6  10001 / 10001",
            SkyPeepMapUi.footer(6,Int.MAX_VALUE))
    }
}
