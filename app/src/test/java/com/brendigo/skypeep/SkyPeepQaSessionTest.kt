package com.brendigo.skypeep

import org.junit.Assert.*
import org.junit.Test

class SkyPeepQaSessionTest {
    @Test fun regularGameplayMayPersist() {
        assertTrue(SkyPeepQaSession.shouldPersist(0, -1))
        assertTrue(SkyPeepQaSession.shouldPersist(-1, 0))
    }

    @Test fun allDebugWorldAndMapFixturesCannotOverwritePlayerProgress() {
        for (world in 1..6) {
            assertFalse(SkyPeepQaSession.shouldPersist(world, -1))
            assertFalse(SkyPeepQaSession.shouldPersist(0, world))
        }
    }
}
