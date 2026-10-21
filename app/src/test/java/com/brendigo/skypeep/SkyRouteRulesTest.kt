package com.brendigo.skypeep

import org.junit.Assert.*
import org.junit.Test

class SkyRouteRulesTest {
    @Test fun flightDifficultiesPreserveExistingPhysicalBalance() {
        for (world in 1..6) {
            assertTrue(SkyRouteRules.gap(world,SkyDifficulty.BREEZE) >
                SkyRouteRules.gap(world,SkyDifficulty.TEMPEST))
            assertTrue(SkyRouteRules.speed(world,SkyDifficulty.BREEZE) <
                SkyRouteRules.speed(world,SkyDifficulty.TEMPEST))
            assertEquals(FlightRules.gap(world),
                SkyRouteRules.gap(world,SkyDifficulty.EXPLORER),.001f)
            for (difficulty in SkyDifficulty.entries)
                assertEquals(10_001,SkyPeepCoinGoal.REQUIRED)
        }
    }
}
