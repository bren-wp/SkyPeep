package com.brendigo.skypeep

/**
 * Physics elapsed time advances per bounded simulation substep, not per
 * graphics render call. Wind uses the midpoint of that substep to reduce
 * refresh-rate-dependent drift without allocating per-frame objects.
 */
internal object SkyPeepWindClock {
    fun windAtStepMidpoint(
        world: Int, difficulty: SkyDifficulty,
        elapsedSeconds: Float, stepSeconds: Float
    ): Float {
        if (!elapsedSeconds.isFinite() || elapsedSeconds < 0f ||
            !stepSeconds.isFinite() || stepSeconds <= 0f) return 0f
        return SkyRouteRules.windAcceleration(
            world, difficulty, elapsedSeconds + stepSeconds * .5f)
    }

    fun advance(elapsedSeconds: Float, stepSeconds: Float): Float {
        if (!elapsedSeconds.isFinite() || elapsedSeconds < 0f) return 0f
        if (!stepSeconds.isFinite() || stepSeconds <= 0f) return elapsedSeconds
        val next = elapsedSeconds + stepSeconds
        return if (next.isFinite()) next else elapsedSeconds
    }
}
