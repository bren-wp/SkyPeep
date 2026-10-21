package com.brendigo.skypeep

import org.junit.Assert.*
import org.junit.Test

class SkyPeepHudTextCacheTest {
    @Test fun stringsAreReusedBetweenUnchangedFrames() {
        val cache = SkyPeepHudTextCache()
        val score = cache.score(234)
        val coins = cache.coins(12345)
        val gems = cache.gems(1000000)
        repeat(200) {
            assertSame(score,cache.score(234))
            assertSame(coins,cache.coins(12345))
            assertSame(gems,cache.gems(1000000))
        }
        assertEquals("234",score)
        assertEquals("12.3K",coins)
        assertEquals("1M",gems)
    }

    @Test fun counterChangesAndCorruptedNegativeValuesAreHandled() {
        val cache = SkyPeepHudTextCache()
        assertEquals("0",cache.score(-50))
        assertEquals("0",cache.coins(-1))
        assertEquals("0",cache.gems(-999))
        assertEquals("1",cache.score(1))
        assertEquals("5",cache.coins(5))
        assertEquals("13",cache.gems(13))
    }

    @Test fun liveHudCountersNeverBleedIntoAnotherInstance() {
        val first=SkyPeepHudTextCache()
        val second=SkyPeepHudTextCache()
        assertEquals("400",first.score(400))
        assertEquals("0",second.score(0))
        assertEquals("400",first.score(400))
    }
}
