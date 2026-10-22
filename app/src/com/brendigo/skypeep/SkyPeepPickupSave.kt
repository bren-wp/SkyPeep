package com.brendigo.skypeep

/** Stable key/normalization contract for frequent real collectible writes. */
internal object SkyPeepPickupSave {
    fun worldKey(world: Int): String {
        require(world in 1..SkyPeepCoinGoal.WORLDS)
        return "collected_world_$world"
    }
    fun collected(value: Int): Int = SkyPeepCoinGoal.collected(value)
    fun currency(value: Int): Int = value.coerceAtLeast(0)
}
