package com.brendigo.skypeep

import org.junit.Assert.*
import org.junit.Test

class SkyPeepFlightHudStateTest {
    @Test fun gameplayCountersRepresentRealPickupsNotSavedWalletBalances() {
        val hud = SkyPeepFlightHudState()
        val savedCoins = 1250
        val savedGems = 75
        assertEquals(0, hud.coinsCollected)
        assertEquals(0, hud.gemsCollected)
        repeat(63) { hud.collectCoin() }
        repeat(4) { hud.collectGem() }
        assertEquals(63, hud.coinsCollected)
        assertEquals(4, hud.gemsCollected)
        // Original menu/shop wallet remains untouched. Gameplay counters are
        // display-only state and cannot award or spend persistent currency.
        assertEquals(1250, savedCoins)
        assertEquals(75, savedGems)
    }

    @Test fun eachNewFlightResetsOnlyTransientHudAndKeepsWalletProgressSeparate() {
        val hud = SkyPeepFlightHudState()
        hud.collectCoin()
        hud.collectGem()
        hud.reset()
        assertEquals(0, hud.coinsCollected)
        assertEquals(0, hud.gemsCollected)
        hud.collectCoin()
        assertEquals(1, hud.coinsCollected)
        assertEquals(0, hud.gemsCollected)
    }

    @Test fun repeatedPickupCountersSaturateInsteadOfOverflowing() {
        val hud = SkyPeepFlightHudState()
        repeat(100) { hud.collectCoin(); hud.collectGem() }
        assertEquals(100, hud.coinsCollected)
        assertEquals(100, hud.gemsCollected)
    }
}
