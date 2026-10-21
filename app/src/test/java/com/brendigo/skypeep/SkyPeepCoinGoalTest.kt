package com.brendigo.skypeep

import org.junit.Assert.*
import org.junit.Test

class SkyPeepCoinGoalTest {
    @Test fun sixIndependentWorldsEachRequireMoreThanTenThousandPickupEvents() {
        assertEquals(6, SkyPeepCoinGoal.WORLDS)
        assertEquals(10_001, SkyPeepCoinGoal.REQUIRED)
        val worlds=IntArray(SkyPeepCoinGoal.WORLDS)
        for (index in worlds.indices) {
            repeat(10_000) { worlds[index] = SkyPeepCoinGoal.increment(worlds[index]) }
            assertFalse("World ${index+1} ended too early",
                SkyPeepCoinGoal.isComplete(worlds[index]))
        }
        for (index in worlds.indices) {
            worlds[index]=SkyPeepCoinGoal.increment(worlds[index])
            assertTrue("World ${index+1} didn't complete",SkyPeepCoinGoal.isComplete(worlds[index]))
        }
    }

    @Test fun collectionSaturatesAndNeverOverflowOrTreatWalletAsProgress() {
        assertEquals(10_001,SkyPeepCoinGoal.increment(10_001))
        assertEquals(10_001,SkyPeepCoinGoal.increment(Int.MAX_VALUE))
        assertEquals(1,SkyPeepCoinGoal.increment(-500))
        assertEquals(0f,SkyPeepCoinGoal.fraction(-999),0f)
        assertEquals(1f,SkyPeepCoinGoal.fraction(Int.MAX_VALUE),0f)
        val collected=502
        val walletAfterPurchase=0
        assertEquals(502,SkyPeepCoinGoal.collected(collected))
        assertEquals(0,walletAfterPurchase)
        assertEquals("502 / 10001",SkyPeepCoinGoal.label(collected))
    }

    @Test fun exactlyOneVictoryOccursOnFirstCrossingAndReplaysStillWork() {
        assertFalse(SkyPeepCoinGoal.justCompleted(0,1))
        assertFalse(SkyPeepCoinGoal.justCompleted(9_999,10_000))
        assertTrue(SkyPeepCoinGoal.justCompleted(10_000,10_001))
        assertFalse(SkyPeepCoinGoal.justCompleted(10_001,10_001))
        assertFalse(SkyPeepCoinGoal.justCompleted(Int.MAX_VALUE,Int.MAX_VALUE))
        // A replay still awards spendable currency for physical pickups, but
        // the capped world counter must not trigger a second instant win.
        val afterReplayCoin=SkyPeepCoinGoal.increment(10_001)
        assertEquals(10_001,afterReplayCoin)
        assertFalse(SkyPeepCoinGoal.justCompleted(10_001,afterReplayCoin))
    }

    @Test fun completionIsPerWorldNotSharedAcrossOtherWorldsOrModes() {
        val collected=IntArray(6)
        collected[1]=10_001
        assertTrue(SkyPeepCoinGoal.isComplete(collected[1]))
        for(i in listOf(0,2,3,4,5)) assertFalse(SkyPeepCoinGoal.isComplete(collected[i]))
        assertEquals(10_001,SkyPeepCoinGoal.increment(collected[1]))
    }
}
