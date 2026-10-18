package com.brendigo.skypeep

import kotlin.math.max
import kotlin.math.min

/**
 * Deterministic route generation: neighbouring gates must be reachable.
 * The first centre preserves the original range; later gates vary within
 * a difficulty-aware vertical travel budget, never teleporting 540 units.
 */
internal object SkyPeepGatePlacement {
    private const val MIN_CENTRE = 570f
    private const val MAX_CENTRE = 1110f

    fun maxShift(world: Int, difficulty: SkyDifficulty): Float =
        min(225f, SkyRouteRules.spawnSeconds(world, difficulty) * 150f)

    fun next(previous: Float?, world: Int, difficulty: SkyDifficulty, sample: Float): Float {
        require(world in 1..6)
        require(sample.isFinite())
        val halfGap = SkyRouteRules.gap(world, difficulty) * .5f
        val safety = SkyPeepContact.BODY_RADIUS_Y + 8f
        val minCenter = max(MIN_CENTRE, halfGap + safety)
        val maxCenter = min(MAX_CENTRE, SkyPeepScreenFrame.REFERENCE_HEIGHT - halfGap - safety)
        require(minCenter <= maxCenter)
        val step = maxShift(world, difficulty)
        // Malformed or old fixtures are clamped before computing the next
        // route; an invalid previous centre cannot spawn an unreachable gate.
        val anchor = previous?.takeIf { it.isFinite() }?.coerceIn(minCenter, maxCenter)
        val low = if (anchor != null) max(minCenter, anchor - step) else minCenter
        val high = if (anchor != null) min(maxCenter, anchor + step) else maxCenter
        return low + (high - low) * sample.coerceIn(0f, 1f)
    }
}
