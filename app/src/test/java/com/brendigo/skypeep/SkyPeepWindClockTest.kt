package com.brendigo.skypeep

import org.junit.Assert.*
import org.junit.Test
import kotlin.math.abs

class SkyPeepWindClockTest {
    private fun impulse(world: Int, mode: SkyDifficulty, hz: Int): Float {
        val step = 1f/hz
        var elapsed = 0f
        var sum = 0f
        repeat(hz * 9) {
            sum += SkyPeepWindClock.windAtStepMidpoint(world,mode,elapsed,step)*step
            elapsed = SkyPeepWindClock.advance(elapsed,step)
        }
        return sum
    }

    @Test fun gustImpulseRemainsStableAcrossDifferentDisplayRefreshRates() {
        for (difficulty in SkyDifficulty.entries)
            for (world in 1..6) {
                val hz60 = impulse(world,difficulty,60)
                val hz120 = impulse(world,difficulty,120)
                assertTrue("wind drift world=$world mode=$difficulty: $hz60 vs $hz120",
                    abs(hz60-hz120) < 1.1f)
            }
    }

    @Test fun frozenGameplayClockCannotAccumulateBackgroundGusts() {
        assertEquals(0f,SkyPeepWindClock.advance(0f,0f),0f)
        assertEquals(0f,SkyPeepWindClock.windAtStepMidpoint(
            4,SkyDifficulty.TEMPEST,2f,0f),0f)
        assertEquals(2f,SkyPeepWindClock.advance(2f,Float.NaN),0f)
        assertEquals(0f,SkyPeepWindClock.advance(Float.NaN,.01f),0f)
    }

    @Test fun firstWorldNeverReceivesWindAfterLongFlights() {
        for (d in SkyDifficulty.entries) for (t in 0..100) {
            assertEquals(0f,SkyPeepWindClock.windAtStepMidpoint(
                1,d,t.toFloat(),.008f),0f)
        }
    }
}
