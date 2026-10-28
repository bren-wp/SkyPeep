package com.brendigo.skypeep

/**
 * The score remains centred on the original 941px illustrated play area.
 * Wallets and pause stay against the ACTUAL visible display edges, including
 * wide Android tablets and foldables. Coordinates are in LibGDX world units.
 *
 * The pause touch area is intentionally slightly larger than the 125px drawn
 * control, exactly as on the original 941x1672 reference screen.
 */
internal object SkyPeepHudLayout {
    fun coinX(frame: SkyPeepScreenFrame.Frame): Float = frame.left + 14f
    fun gemX(frame: SkyPeepScreenFrame.Frame): Float = frame.right - 306f
    fun pauseX(frame: SkyPeepScreenFrame.Frame): Float = frame.right - 130f


    // Keep new status layers clear of the ORIGINAL winged score/ribbon artwork
    // (x=313..628, top-down y=8..188). Unlike the former centre badge, the
    // 10,001-pickup goal now sits under the coin/lives column at x<=286.
    // Rebuilt PNG sources are 269x113 (coin), 191x115 (gem) and
    // 367x210 (winged score). Keep draw aspect within ~0.4% of source.
    const val COIN_PLATE_WIDTH = 235f
    const val COIN_PLATE_HEIGHT = 99f
    const val GEM_PLATE_WIDTH = 170f
    const val GEM_PLATE_HEIGHT = 102f
    const val SCORE_WIDTH = 315f
    const val SCORE_HEIGHT = 180f

    const val GOAL_WIDTH = 260f
    const val GOAL_HEIGHT = 50f
    const val SCORE_LEFT = 313f
    const val SCORE_RIGHT = 628f
    const val SCORE_BOTTOM_TOP_DOWN = 188f

    fun goalX(frame: SkyPeepScreenFrame.Frame): Float = frame.left + 14f
    fun goalBottom(frame: SkyPeepScreenFrame.Frame): Float = frame.top - 255f

    fun heartCenterX(frame: SkyPeepScreenFrame.Frame, index: Int): Float {
        require(index in 0 until SkyPeepLives.MAX)
        return frame.left + 47f + index * 51f
    }
    fun heartCenterY(frame: SkyPeepScreenFrame.Frame): Float = frame.top - 156f

    // Brand-coloured, deliberately inert preflight instruction. The user
    // starts the flight by tapping ANY gameplay sky, not by finding a button.
    const val PROMPT_X = 249f
    const val PROMPT_Y = 522f
    const val PROMPT_WIDTH = 443f
    const val PROMPT_HEIGHT = 108f

    /** topY uses the reference image's top-down coordinates (H-worldY). */
    fun pauseContains(frame: SkyPeepScreenFrame.Frame,
                      x: Float, topY: Float): Boolean =
        x >= frame.right - 135f && x <= frame.right &&
        topY >= -frame.extraY && topY <= 155f - frame.extraY
}
