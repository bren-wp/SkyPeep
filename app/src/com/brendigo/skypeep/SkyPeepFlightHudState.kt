package com.brendigo.skypeep

/**
 * LIVE gameplay pickup counters, intentionally separate from the wallet.
 *
 * The reference menu/shop show saved, spendable totals, while the illustrated
 * flight HUD shows real pickups within the active flight. A wallet purchase
 * must never change these on-screen pickup counters or world-goal progress.
 * This state is transient: durable coin/gem totals and 10,001 pickup progress
 * remain saved through the existing Preferences keys.
 */
internal class SkyPeepFlightHudState {
    var coinsCollected: Int = 0
        private set
    var gemsCollected: Int = 0
        private set

    fun reset() {
        coinsCollected = 0
        gemsCollected = 0
    }

    fun collectCoin() {
        coinsCollected = SkyPeepEconomy.credit(coinsCollected, 1)
    }

    fun collectGem() {
        gemsCollected = SkyPeepEconomy.credit(gemsCollected, 1)
    }
}
