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
        val low = if (previous != null && previous.isFinite()) max(minCenter, previous - step)
            else minCenter
        val high = if (previous != null && previous.isFinite()) min(maxCenter, previous + step)
            else maxCenter
        // A stored centre is always inside these bounds. Defensive handling
        // also allows fixtures outside the normal range without an exception.
        val lower = min(low, high)
        val upper = max(low, high)
        return lower + (upper - lower) * sample.coerceIn(0f, 1f)
    }
}
