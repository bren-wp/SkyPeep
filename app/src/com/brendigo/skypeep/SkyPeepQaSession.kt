package com.brendigo.skypeep

/**
 * Debug Intent fixtures may deliberately unlock every world or grant a skin.
 * They must NEVER persist their artificial state into a real player's save.
 */
internal object SkyPeepQaSession {
    fun shouldPersist(world: Int, mapUnlocked: Int): Boolean =
        world !in 1..6 && mapUnlocked !in 1..6
}
