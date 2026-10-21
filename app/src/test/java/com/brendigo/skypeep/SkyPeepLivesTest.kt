package com.brendigo.skypeep

import org.junit.Assert.*
import org.junit.Test

class SkyPeepLivesTest {
    @Test fun startsAtThreeAndRequiresThreeSeparateHitsToLose() {
        var count = SkyPeepLives.MAX
        for (expected in 2 downTo 0) {
            val hit = SkyPeepLives.hit(count,0f)
            assertTrue(hit.tookDamage)
            assertEquals(expected,hit.remaining)
            count = hit.remaining
        }
        assertFalse(SkyPeepLives.hit(0,0f).tookDamage)
    }

    @Test fun shieldPreventsMultiFrameDoubleDamage() {
        assertEquals(SkyPeepLives.Hit(2,false),SkyPeepLives.hit(2,1.0f))
        assertEquals(SkyPeepLives.Hit(1,true),SkyPeepLives.hit(2,0f))
        assertEquals(0f,SkyPeepLives.tickShield(.01f,.02f),0f)
        assertEquals(1.5f,SkyPeepLives.tickShield(2f,.5f),.0001f)
    }

    @Test fun recoveryHeartRestoresOnlyOneLifeUpToThree() {
        for (i in 0..3) assertEquals((i+1).coerceAtMost(3),SkyPeepLives.heal(i))
        assertEquals(3,SkyPeepLives.heal(Int.MAX_VALUE))
    }

    @Test fun heartsAppearPeriodicallyOnlyWhenInjured() {
        assertFalse(SkyPeepLives.spawnHeart(3,25f,0f,12))
        assertFalse(SkyPeepLives.spawnHeart(1,11.9f,-16f,8))
        assertFalse(SkyPeepLives.spawnHeart(1,20f,5f,8))
        assertFalse(SkyPeepLives.spawnHeart(1,20f,-16f,7))
        assertTrue(SkyPeepLives.spawnHeart(2,20f,-16f,8))
        assertFalse(SkyPeepLives.spawnHeart(0,20f,-16f,8))
        assertFalse(SkyPeepLives.spawnHeart(2,Float.NaN,-16f,8))
        assertTrue(SkyPeepLives.spawnHeart(1,40f,20f,16))
    }
}
