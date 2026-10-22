package com.brendigo.skypeep

import org.junit.Assert.*
import org.junit.Test

class SkyPeepSkinCatalogTest {
    @Test fun everySkinHasUniqueStableIdAndName() {
        val skins = SkyPeepSkinCatalog.all
        assertEquals(listOf(0,1,2,3,4), skins.map { it.id })
        assertEquals(5, skins.map { it.name }.distinct().size)
        assertEquals(listOf("Default","Captain","Forest","Cosmic","Royal"),
            skins.map { it.name })
    }

    @Test fun cosmeticPricesMatchIllustratedShop() {
        assertEquals(listOf(0,500,800,120,1000),
            SkyPeepSkinCatalog.all.map { it.price })
        assertEquals(SkinCurrency.GEMS, SkyPeepSkinCatalog.get(3).currency)
        assertTrue(SkyPeepSkinCatalog.all.filter { it.id != 3 }
            .all { it.currency == SkinCurrency.COINS })
    }

    @Test fun purchasesRespectCurrencyAndExactBalance() {
        assertFalse(SkyPeepSkinCatalog.get(3).affordable(10_000, 119))
        assertTrue(SkyPeepSkinCatalog.get(3).affordable(0, 120))
        assertFalse(SkyPeepSkinCatalog.get(2).affordable(799, 999))
        assertTrue(SkyPeepSkinCatalog.get(2).affordable(800, 0))
        assertTrue(SkyPeepSkinCatalog.get(0).affordable(0, 0))
    }
    @Test fun costumePurchaseUsesCorrectCurrencyAndExactBalance() {
        val forest = SkyPeepSkinCatalog.purchase(2, false, 800, 5)
        assertTrue(forest.success)
        assertFalse(forest.alreadyOwned)
        assertEquals(0, forest.coins)
        assertEquals(5, forest.gems)
        val cosmic = SkyPeepSkinCatalog.purchase(3, false, 100, 120)
        assertTrue(cosmic.success)
        assertEquals(100, cosmic.coins)
        assertEquals(0, cosmic.gems)
    }

    @Test fun costumeAlreadyOwnedCannotChargeAgain() {
        val again = SkyPeepSkinCatalog.purchase(4, true, 0, 0)
        assertTrue(again.success)
        assertTrue(again.alreadyOwned)
        assertEquals(0, again.coins)
        assertEquals(0, again.gems)
    }

    @Test fun costumePurchaseRejectsInvalidAndInsufficientBalances() {
        val denied = SkyPeepSkinCatalog.purchase(3, false, 50_000, 119)
        assertFalse(denied.success)
        assertEquals(50_000, denied.coins)
        assertEquals(119, denied.gems)
        assertFalse(SkyPeepSkinCatalog.purchase(-1, false, 5_000, 500).success)
        assertFalse(SkyPeepSkinCatalog.purchase(99, false, 5_000, 500).success)
        assertFalse(SkyPeepSkinCatalog.purchase(1, false, -1, 500).success)
        assertFalse(SkyPeepSkinCatalog.purchase(0, false, 0, -1).success)
    }
}
