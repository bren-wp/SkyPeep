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
            world == selectedWorld -> "SELECTED"
            else -> "OPEN"
        }
    }

    fun footer(world: Int, collected: Int): String {
        require(world in 1..SkyPeepCoinGoal.WORLDS)
        return "WORLD $world  ${SkyPeepCoinGoal.label(collected)}"
    }
}
