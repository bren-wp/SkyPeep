package com.brendigo.skypeep

/** Existing stable keys for a best-score snapshot, without unrelated writes. */
internal object SkyPeepRecordSave {
    fun write(
        route: Int, best: Int, routeBest: Int,
        put: (String, Int) -> Unit
    ) {
        require(route in SkyDifficulty.entries.indices)
        val global = best.coerceAtLeast(0)
        val perRoute = routeBest.coerceAtLeast(0)
        // Validate everything before the first preference mutation.
        put("best", global)
        put("route_best_$route", perRoute)
    }
}
