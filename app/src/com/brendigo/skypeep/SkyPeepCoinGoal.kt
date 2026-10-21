package com.brendigo.skypeep

/**
 * All six worlds are playable from the beginning. Each world is completed
 * by collecting more than 10,000 actual coin pickups across any number of
 * attempts. Wallet balances, purchases and scoring never affect this tally.
 */
internal object SkyPeepCoinGoal {
    const val REQUIRED = 10_001
    const val WORLDS = 6

    fun collected(value: Int): Int = value.coerceIn(0, REQUIRED)
    fun increment(current: Int): Int =
        (collected(current) + if (collected(current) < REQUIRED) 1 else 0)
    fun isComplete(value: Int): Boolean = value >= REQUIRED

    /** A finished world may be replayed without a new instant victory. */
    fun justCompleted(before: Int, after: Int): Boolean =
        !isComplete(before) && isComplete(after)

    fun fraction(value: Int): Float = collected(value).toFloat() / REQUIRED
    fun label(value: Int): String = "${collected(value)} / ${REQUIRED}"
    fun compact(value: Int): String = "${collected(value)} / 10001"
}
