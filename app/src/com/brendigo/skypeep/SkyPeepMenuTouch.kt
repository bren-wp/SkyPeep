package com.brendigo.skypeep

/** Artwork-aligned tap areas on the 941 × 1672 reference canvas. */
internal object SkyPeepMenuTouch {
    enum class Action { PLAY, LEVELS, SKINS, SHOP, SETTINGS, COINS, GEMS }

    private data class Target(
        val action: Action, val left: Float, val top: Float,
        val width: Float, val height: Float
    ) {
        fun contains(x: Float, y: Float): Boolean =
            x >= left && x <= left + width && y >= top && y <= top + height
    }
    private val targets = listOf(
        // Reference artwork actual button frames; empty space is not tappable.
        Target(Action.PLAY, 150f, 914f, 647f, 235f),
        Target(Action.LEVELS, 95f, 1177f, 366f, 150f),
        Target(Action.SKINS, 483f, 1177f, 365f, 153f),
        Target(Action.SHOP, 95f, 1348f, 367f, 151f),
        Target(Action.SETTINGS, 483f, 1348f, 365f, 153f),
        Target(Action.COINS, 261f, 27f, 78f, 78f),
        Target(Action.GEMS, 846f, 27f, 74f, 78f)
    )
    fun actionAt(x: Float, yTop: Float): Action? {
        if (!x.isFinite() || !yTop.isFinite()) return null
        return targets.firstOrNull { it.contains(x, yTop) }?.action
    }
}
