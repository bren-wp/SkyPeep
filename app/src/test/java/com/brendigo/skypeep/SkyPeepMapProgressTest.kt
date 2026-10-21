package com.brendigo.skypeep

import org.junit.Assert.*
import org.junit.Test

class SkyPeepMapProgressTest {
    @Test fun allWorldsAreAvailableFromFirstRunWithoutStars() {
        for (world in 1..6) {
            assertEquals(MapWorldStatus.OPEN,SkyPeepMapProgress.status(world,0))
            assertEquals(MapWorldStatus.OPEN,SkyPeepMapProgress.status(world,10_000))
            assertEquals(MapWorldStatus.COMPLETED,SkyPeepMapProgress.status(world,10_001))
        }
    }
    @Test fun corruptCoinProgressCannotLockTheMap() {
        assertEquals(MapWorldStatus.OPEN,SkyPeepMapProgress.status(6,-100))
        assertEquals(MapWorldStatus.COMPLETED,
            SkyPeepMapProgress.status(1,Int.MAX_VALUE))
    }
    @Test fun playHitAreaDoesNotInterceptFirstIslandOrItsStars() {
        assertEquals(1,SkyPeepMapLayout.worldAt(600f,1475f))
        assertFalse(SkyPeepMapLayout.play.contains(600f,1475f))
        assertTrue(SkyPeepMapLayout.play.contains(760f,1580f))
        assertTrue(SkyPeepMapLayout.back.contains(100f,1540f))
    }
    @Test fun overlappingIslandHitsPreferTheNearestPaintedMarker() {
        assertEquals(1,SkyPeepMapLayout.worldAt(600f,1265f))
        assertEquals(2,SkyPeepMapLayout.worldAt(649f,1260f))
        assertEquals(5,SkyPeepMapLayout.worldAt(490f,585f))
        assertEquals(4,SkyPeepMapLayout.worldAt(500f,600f))
    }
    @Test fun invalidAndOutsideTouchesDoNotSelectWorld() {
        assertNull(SkyPeepMapLayout.worldAt(Float.NaN,1200f))
        assertNull(SkyPeepMapLayout.worldAt(1200f,Float.POSITIVE_INFINITY))
        assertNull(SkyPeepMapLayout.worldAt(-1f,-1f))
    }
    @Test fun sixIllustratedIslandsAllHaveTouchRegions() {
        for((world,x,y) in listOf(Triple(1,500f,1360f),Triple(2,730f,1140f),
            Triple(3,350f,900f),Triple(4,660f,710f),
            Triple(5,340f,450f),Triple(6,730f,300f))) {
            assertEquals(world,SkyPeepMapLayout.worldAt(x,y))
        }
    }
}
