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
        Target(Action.PLAY, 170f, 895f, 590f, 248f),
        // The old LEVELS/SKINS row overlapped SHOP/SETTINGS by six pixels.
        Target(Action.LEVELS, 95f, 1155f, 364f, 162f),
        Target(Action.SKINS, 471f, 1155f, 370f, 162f),
        Target(Action.SHOP, 95f, 1318f, 364f, 173f),
        Target(Action.SETTINGS, 471f, 1318f, 370f, 173f),
        Target(Action.COINS, 243f, 0f, 100f, 130f),
        Target(Action.GEMS, 815f, 0f, 110f, 130f)
    )
    fun actionAt(x: Float, yTop: Float): Action? {
        if (!x.isFinite() || !yTop.isFinite()) return null
        return targets.firstOrNull { it.contains(x, yTop) }?.action
    }
}
