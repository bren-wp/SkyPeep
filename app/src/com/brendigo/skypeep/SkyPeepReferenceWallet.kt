package com.brendigo.skypeep

/**
 * The original Brendigo 941x1672 paintings contain sample wallet amounts.
 * When saved balances equal those illustrated values, the source pixels are
 * already correct and must not be needlessly obscured by dynamic badges.
 * All nonmatching balances still receive genuinely live wallet text.
 */
internal object SkyPeepReferenceWallet {
    private const val REFERENCE_COINS = 1250
    private const val MENU_GEMS = 75
    private const val SKINS_GEMS = 89

    fun menuNeedsCoinOverlay(coins: Int) = coins != REFERENCE_COINS
    fun menuNeedsGemOverlay(gems: Int) = gems != MENU_GEMS
    fun skinsNeedsCoinOverlay(coins: Int) = coins != REFERENCE_COINS
    fun skinsNeedsGemOverlay(gems: Int) = gems != SKINS_GEMS
}
