package com.brendigo.skypeep

/**
 * Three attempt points per FLIGHT, not paid lives, with collectible recovery.
 * Progress, wallet and purchases are unaffected by hits or temporary shields.
 */
internal object SkyPeepLives {
    const val MAX = 3
    const val SHIELD_SECONDS = 2.25f
    const val HEART_RADIUS = 35f
    const val FIRST_HEART_SECONDS = 12f
    const val HEART_INTERVAL_SECONDS = 16f

    data class Hit(val remaining: Int, val tookDamage: Boolean)

    fun hit(lives: Int, shieldSeconds: Float): Hit {
        val safe = lives.coerceIn(0, MAX)
        return if (safe <= 0 || (shieldSeconds.isFinite() && shieldSeconds > 0f))
            Hit(safe, false) else Hit(safe - 1, true)
    }

    fun heal(lives: Int): Int = (lives.coerceIn(0, MAX) + 1).coerceAtMost(MAX)

    fun tickShield(seconds: Float, delta: Float): Float =
        if (seconds.isFinite() && delta.isFinite() && delta > 0f)
            (seconds - delta).coerceAtLeast(0f)
        else if (seconds.isFinite()) seconds.coerceAtLeast(0f) else 0f

    fun spawnHeart(
        lives: Int, elapsedSeconds: Float, lastHeartTime: Float,
        gateNumber: Int
    ): Boolean = lives in 1 until MAX &&
        elapsedSeconds.isFinite() && lastHeartTime.isFinite() &&
        elapsedSeconds >= FIRST_HEART_SECONDS &&
        elapsedSeconds - lastHeartTime >= HEART_INTERVAL_SECONDS &&
        gateNumber > 0 && gateNumber % 4 == 0
}
