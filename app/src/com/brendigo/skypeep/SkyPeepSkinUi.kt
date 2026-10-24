package com.brendigo.skypeep

/** Preserve original illustrated BUY buttons; paint only changed UI state. */
internal object SkyPeepSkinUi {
    fun dynamicCaption(skin: Int, selected: Int, owned: Boolean): String? {
        require(skin in 1..4)
        return when {
            skin == selected -> "EQUIPPED"
            owned -> "EQUIP"
            else -> null // The original source JPEG already contains BUY.
        }
    }
}
