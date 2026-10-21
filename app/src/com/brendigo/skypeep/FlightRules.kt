package com.brendigo.skypeep

import kotlin.math.max

/** Canonical world balance. Hazard/pickup contacts live in SkyPeepContact
 * and SkyPeepPickup; obsolete rectangle / distant-halo rules were removed.
 */
internal object FlightRules {
    fun gap(level: Int): Float = max(355f, 585f - (level.coerceIn(1, 6) - 1) * 38f)

}
