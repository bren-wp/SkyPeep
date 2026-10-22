package com.brendigo.skypeep

/**
 * Compact map status chips preserve original illustrated world names and
 * stars. Collection numbers appear in only one selected-world footer.
 */
internal object SkyPeepMapUi {
    fun status(world: Int, selectedWorld: Int, collected: Int): String {
        require(world in 1..SkyPeepCoinGoal.WORLDS)
        return when {
            SkyPeepCoinGoal.isComplete(collected) -> "DONE"
            world == selectedWorld -> "PLAY"
            else -> "OPEN"
        }
    }

    /**
     * Match only the five lock icons painted in levels.jpg, not the numbered
     * island banners underneath. Sky Start (world 1) has no painted lock.
     * Coordinates are on the original 941 × 1672 reference canvas.
     */
    fun lockChip(world: Int): SkyPeepUiTouch.Rect? = when (world) {
        1 -> null
        2 -> SkyPeepUiTouch.Rect(644f, 1048f, 84f, 69f)
        3 -> SkyPeepUiTouch.Rect(300f, 847f, 84f, 70f)
        4 -> SkyPeepUiTouch.Rect(595f, 692f, 84f, 70f)
        5 -> SkyPeepUiTouch.Rect(309f, 444f, 86f, 69f)
        6 -> SkyPeepUiTouch.Rect(688f, 307f, 88f, 67f)
        else -> throw IllegalArgumentException("world must be 1..6")
    }

    fun footer(world: Int, collected: Int): String {
        require(world in 1..SkyPeepCoinGoal.WORLDS)
        return "WORLD $world  ${SkyPeepCoinGoal.label(collected)}"
    }
}
