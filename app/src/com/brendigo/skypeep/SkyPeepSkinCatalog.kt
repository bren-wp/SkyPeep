package com.brendigo.skypeep

/** Prices and currency are defined once for preview, purchase and regression tests. */
internal enum class SkinCurrency { COINS, GEMS }

internal data class BirdSkin(
    val id: Int,
    val name: String,
    val price: Int,
    val currency: SkinCurrency
) {
    fun affordable(coins: Int, gems: Int): Boolean = when(currency) {
        SkinCurrency.COINS -> coins >= price
        SkinCurrency.GEMS -> gems >= price
    }
}

internal data class SkinPurchase(
    val success: Boolean, val alreadyOwned: Boolean,
    val coins: Int, val gems: Int
)

internal object SkyPeepSkinCatalog {
    val all = listOf(
        BirdSkin(0, "Default", 0, SkinCurrency.COINS),
        BirdSkin(1, "Captain", 500, SkinCurrency.COINS),
        BirdSkin(2, "Forest", 800, SkinCurrency.COINS),
        BirdSkin(3, "Cosmic", 120, SkinCurrency.GEMS),
        BirdSkin(4, "Royal", 1000, SkinCurrency.COINS)
    )
    /** Return a validated transaction; the caller persists it only once. */
    fun purchase(id: Int, alreadyOwned: Boolean, coins: Int, gems: Int): SkinPurchase {
        val offer = all.getOrNull(id) ?: return SkinPurchase(false, false, coins, gems)
        if (coins < 0 || gems < 0) return SkinPurchase(false, false, coins, gems)
        if (alreadyOwned || offer.price == 0) return SkinPurchase(true, true, coins, gems)
        if (!offer.affordable(coins, gems)) return SkinPurchase(false, false, coins, gems)
        return when (offer.currency) {
            SkinCurrency.COINS -> SkinPurchase(true, false, coins - offer.price, gems)
            SkinCurrency.GEMS -> SkinPurchase(true, false, coins, gems - offer.price)
        }
    }

    fun get(id: Int): BirdSkin = all[id.coerceIn(0, all.lastIndex)]
}
