package com.brendigo.skypeep

/**
 * Allocation-light labels for the realtime premium HUD. Formatting is
 * performed only when a number actually changes, never on every GL frame.
 * The independently styled menu and result screens keep their source text.
 */
internal class SkyPeepHudTextCache {
    private var lastScore = Int.MIN_VALUE
    private var lastCoins = Int.MIN_VALUE
    private var lastGems = Int.MIN_VALUE
    private var scoreLabel = "0"
    private var coinLabel = "0"
    private var gemLabel = "0"

    fun score(value: Int): String {
        val safe = value.coerceAtLeast(0)
        if (lastScore != safe) {
            lastScore = safe
            scoreLabel = safe.toString()
        }
        return scoreLabel
    }

    fun coins(value: Int): String {
        val safe = value.coerceAtLeast(0)
        if (lastCoins != safe) {
            lastCoins = safe
            coinLabel = SkyPeepHudNumbers.compact(safe)
        }
        return coinLabel
    }

    fun gems(value: Int): String {
        val safe = value.coerceAtLeast(0)
        if (lastGems != safe) {
            lastGems = safe
            gemLabel = SkyPeepHudNumbers.compact(safe)
        }
        return gemLabel
    }
}
