package com.brendigo.skypeep

import org.junit.Assert.*
import org.junit.Test

class SkyPeepHudTextCacheTest {
    @Test fun stringsAreReusedBetweenUnchangedFrames() {
        val cache = SkyPeepHudTextCache()
        val score = cache.score(234)
        val coins = cache.coins(12345)
        val gems = cache.gems(1000000)
        val progress = cache.progress(27)
        repeat(200) {
            assertSame(score,cache.score(234))
            assertSame(coins,cache.coins(12345))
            assertSame(gems,cache.gems(1000000))
            assertSame(progress,cache.progress(27))
        }
        assertEquals("234",score)
        assertEquals("12.3K",coins)
        assertEquals("1M",gems)
        assertEquals("27 / 10001",progress)
    }

    @Test fun counterChangesAndCorruptedNegativeValuesAreHandled() {
        val cache = SkyPeepHudTextCache()
        assertEquals("0",cache.score(-50))
        assertEquals("0",cache.coins(-1))
        assertEquals("0",cache.gems(-999))
        assertEquals("1",cache.score(1))
        assertEquals("5",cache.coins(5))
        assertEquals("13",cache.gems(13))
        assertEquals("0 / 10001",cache.progress(-1))
        assertEquals("10001 / 10001",cache.progress(Int.MAX_VALUE))
        assertEquals("1 / 10001",cache.progress(1))
    }

    @Test fun liveHudCountersNeverBleedIntoAnotherInstance() {
        val first=SkyPeepHudTextCache()
        val second=SkyPeepHudTextCache()
        assertEquals("400",first.score(400))
        assertEquals("0",second.score(0))
        assertEquals("400",first.score(400))
    }
}
