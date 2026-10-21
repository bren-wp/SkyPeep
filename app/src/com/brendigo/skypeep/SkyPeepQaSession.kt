package com.brendigo.skypeep

/** Debug-only fixtures must never flush artificial progress or preferences. */
internal object SkyPeepQaSession {
    fun shouldPersist(
        world: Int = 0,
        mapUnlocked: Int = -1,
        skin: Int = -1,
        victory: Boolean = false,
        pillars: Boolean = false,
        score: Int = -1,
        loss: Boolean = false,
        backgroundMotion: Boolean = false,
        difficulty: Int = -1
    ): Boolean = world == 0 && mapUnlocked == -1 && skin == -1 &&
        !victory && !pillars && score == -1 && !loss &&
        !backgroundMotion && difficulty == -1
}
