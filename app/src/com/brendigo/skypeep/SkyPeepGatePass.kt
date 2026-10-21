package com.brendigo.skypeep

/**
 * Pure timing and scoring boundaries for the moving illustrated gates.
 * A precision pass is sampled when the *middle of the obstacle* crosses the
 * real bird torso centre, not when the obstacle has long passed behind it.
 */
internal object SkyPeepGatePass {
    fun reachedBirdCentre(gateX: Float, gateWidth: Float, birdCentreX: Float): Boolean =
        gateX.isFinite() && gateWidth.isFinite() && birdCentreX.isFinite() &&
        gateWidth > 0f && gateX + gateWidth * .5f <= birdCentreX

    /**
     * Credit a pass once the trailing illustrated stone is behind the actual
     * solid torso, using the same contact grace as hazard collision checks.
     */
    fun hasClearedBody(
        gateX: Float, gateWidth: Float, bodyLeft: Float, contactGrace: Float
    ): Boolean = gateX.isFinite() && gateWidth.isFinite() &&
        bodyLeft.isFinite() && contactGrace.isFinite() &&
        gateWidth > 0f && contactGrace >= 0f &&
        gateX + gateWidth < bodyLeft + contactGrace

    /**
     * Carry short physics-step overrun forward to the next spawn. Discarding
     * this remainder introduces device-FPS-dependent obstacle spacing.
     * A normal game substep is far shorter than the minimum spawn interval.
     */
    fun nextSpawnCountdown(overdue: Float, interval: Float): Float {
        require(overdue.isFinite() && overdue <= 0f)
        require(interval.isFinite() && interval > 0f)
        return (interval + overdue.coerceAtLeast(-interval)).coerceAtLeast(.0001f)
    }
}
