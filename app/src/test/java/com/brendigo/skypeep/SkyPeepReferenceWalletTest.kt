package com.brendigo.skypeep

import org.junit.Assert.*
import org.junit.Test

class SkyPeepReferenceWalletTest {
    @Test fun menuPreservesOriginalWalletPixelsOnlyAtExactValues() {
        assertFalse(SkyPeepReferenceWallet.menuNeedsCoinOverlay(1250))
        assertFalse(SkyPeepReferenceWallet.menuNeedsGemOverlay(75))
        assertTrue(SkyPeepReferenceWallet.menuNeedsCoinOverlay(1249))
        assertTrue(SkyPeepReferenceWallet.menuNeedsCoinOverlay(1251))
        assertTrue(SkyPeepReferenceWallet.menuNeedsGemOverlay(89))
        assertTrue(SkyPeepReferenceWallet.menuNeedsGemOverlay(0))
    }

    @Test fun skinsUseTheirOwnReferenceGemValue() {
        assertFalse(SkyPeepReferenceWallet.skinsNeedsCoinOverlay(1250))
        assertFalse(SkyPeepReferenceWallet.skinsNeedsGemOverlay(89))
        assertTrue(SkyPeepReferenceWallet.skinsNeedsGemOverlay(75))
        assertTrue(SkyPeepReferenceWallet.skinsNeedsGemOverlay(90))
        assertTrue(SkyPeepReferenceWallet.skinsNeedsCoinOverlay(0))
        assertTrue(SkyPeepReferenceWallet.skinsNeedsCoinOverlay(Int.MAX_VALUE))
    }
}
