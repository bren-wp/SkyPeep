package com.brendigo.skypeep

import org.junit.Assert.*
import org.junit.Test

class SkyPeepQaSessionTest {
    @Test fun regularGameplayCanSaveNormally() {
        assertTrue(SkyPeepQaSession.shouldPersist())
        assertTrue(SkyPeepQaSession.shouldPersist(world=0,mapUnlocked=-1))
    }

    @Test fun worldAndMapScreenshotsNeverSaveSyntheticProgress() {
        for (world in 1..6) {
            assertFalse(SkyPeepQaSession.shouldPersist(world=world))
            assertFalse(SkyPeepQaSession.shouldPersist(mapUnlocked=world))
        }
    }

    @Test fun allOtherIndependentFixtureFlagsPreventWrites() {
        assertFalse(SkyPeepQaSession.shouldPersist(skin=2))
        assertFalse(SkyPeepQaSession.shouldPersist(victory=true))
        assertFalse(SkyPeepQaSession.shouldPersist(pillars=true))
        assertFalse(SkyPeepQaSession.shouldPersist(score=9999))
        assertFalse(SkyPeepQaSession.shouldPersist(loss=true))
        assertFalse(SkyPeepQaSession.shouldPersist(backgroundMotion=true))
        assertFalse(SkyPeepQaSession.shouldPersist(difficulty=2))
        assertFalse(SkyPeepQaSession.shouldPersist(world=-1))
        assertFalse(SkyPeepQaSession.shouldPersist(mapUnlocked=0))
        assertFalse(SkyPeepQaSession.shouldPersist(score=-2))
        assertFalse(SkyPeepQaSession.shouldPersist(
            world=1,skin=3,victory=true,score=50,difficulty=2))
    }
}
