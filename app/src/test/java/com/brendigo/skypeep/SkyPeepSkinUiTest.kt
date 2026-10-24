package com.brendigo.skypeep

import org.junit.Assert.*
import org.junit.Test

class SkyPeepSkinUiTest {
    @Test fun allOriginalBuyButtonsStayPixelIdenticalForNewPlayers() {
        for (i in 1..4) assertNull(SkyPeepSkinUi.dynamicCaption(i, 0, false))
    }

    @Test fun purchasedButUnequippedCardShowsEquipWithoutNewCharges() {
        assertEquals("EQUIP", SkyPeepSkinUi.dynamicCaption(1, 2, true))
        assertEquals("EQUIPPED", SkyPeepSkinUi.dynamicCaption(2, 2, true))
        assertEquals("EQUIPPED", SkyPeepSkinUi.dynamicCaption(4, 4, false))
        assertNull(SkyPeepSkinUi.dynamicCaption(3, 2, false))
    }

    @Test(expected = IllegalArgumentException::class)
    fun defaultMainBirdIsNotABuyCard() {
        SkyPeepSkinUi.dynamicCaption(0, 0, true)
    }
}
