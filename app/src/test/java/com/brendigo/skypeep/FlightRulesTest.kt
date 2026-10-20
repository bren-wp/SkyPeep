package com.brendigo.skypeep

import org.junit.Assert.*
import org.junit.Test

class FlightRulesTest {
    @Test fun victoryBonusesAreConsistentWithWorldProgression() {
        assertEquals(115, FlightRules.winBonus(1))
        assertEquals(190, FlightRules.winBonus(6))
        assertEquals(115, FlightRules.winBonus(0))
        assertEquals(190, FlightRules.winBonus(100))
    }

    @Test fun levelDifficultyIsBounded() {
        assertEquals(11, FlightRules.targetScore(1))
        assertEquals(26, FlightRules.targetScore(6))
        assertEquals(585f, FlightRules.gap(1), .001f)
        assertEquals(395f, FlightRules.gap(6), .001f)
    }

    // Real hazard and pickup geometry are now covered by
    // SkyPeepContactTest and SkyPeepPickupTest, not legacy AABB/halo tests.
}
