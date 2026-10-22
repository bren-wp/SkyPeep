package com.brendigo.skypeep

/** Stable key/normalization contract for frequent real collectible writes. */
internal object SkyPeepPickupSave {
    fun worldKey(world: Int): String {
        require(world in 1..SkyPeepCoinGoal.WORLDS)
        return "collected_world_$world"
    }
    fun collected(value: Int): Int = SkyPeepCoinGoal.collected(value)
    fun currency(value: Int): Int = value.coerceAtLeast(0)

    /** Emit exactly three changed integers; caller controls one guarded flush. */
    inline fun write(
        world: Int, collected: Int, coins: Int, gems: Int,
        put: (String, Int) -> Unit
    ) {
        // Resolve the validated key before making the first write.
        val key = worldKey(world)
        put(key, collected(collected))
        put("coins", currency(coins))
        put("gems", currency(gems))
    }
}
