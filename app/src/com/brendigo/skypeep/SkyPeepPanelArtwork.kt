package com.brendigo.skypeep

/** Pure contract between selected map island and actually loaded GL world. */
internal object SkyPeepPanelArtwork {
    private const val WORLD_COUNT = SkyPeepCoinGoal.WORLDS

    /** Selecting a map island does not itself load its large background JPG. */
    fun needsStage(selectedWorld: Int, loadedWorld: Int): Boolean {
        require(selectedWorld in 1..WORLD_COUNT)
        return selectedWorld != loadedWorld
    }

    /**
     * Draw only the last successfully staged world. If an incoming decode
     * fails, the existing texture remains valid and prevents an empty panel.
     */
    fun backgroundFor(loadedWorld: Int): String? =
        if (loadedWorld in 1..WORLD_COUNT) "bg_$loadedWorld" else null
}
