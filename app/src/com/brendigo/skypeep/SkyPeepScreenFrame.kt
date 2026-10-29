package com.brendigo.skypeep

/**
 * Portrait game art is authored at 941 × 1672. A plain FitViewport shows
 * letterbox bands on taller/wider devices; stretching the whole UI instead
 * would deform the bird, labels and clickable button geometry.
 *
 * ExtendViewport expands the world in the spare dimension. This pure helper
 * computes the visible frame centred on the original, unscaled artwork.
 * Only illustrated edge scenery is painted in the expanded margins.
 */
internal object SkyPeepScreenFrame {
    const val REFERENCE_WIDTH = 941f
    const val REFERENCE_HEIGHT = 1672f

    internal data class Frame(
        val left: Float, val right: Float,
        val bottom: Float, val top: Float,
        val extraX: Float, val extraY: Float
    )


    /**
     * Native Android fullscreen still has unsafe pixels around camera cutouts
     * and transient system bars. Move only HUD controls into this safe area;
     * the background, camera, obstacle physics and original art remain full bleed.
     */
    internal data class PixelInsets(
        val left: Int, val top: Int, val right: Int, val bottom: Int
    )

    fun hudSafeFrame(frame: Frame, widthPx: Int, heightPx: Int,
                     inset: PixelInsets): Frame {
        require(widthPx > 0 && heightPx > 0)
        val xScale = (frame.right - frame.left) / widthPx
        val yScale = (frame.top - frame.bottom) / heightPx
        // Malformed OEM inset values cannot invert the visible viewport.
        val l = frame.left + inset.left.coerceIn(0, widthPx / 3) * xScale
        val r = frame.right - inset.right.coerceIn(0, widthPx / 3) * xScale
        val b = frame.bottom + inset.bottom.coerceIn(0, heightPx / 3) * yScale
        val t = frame.top - inset.top.coerceIn(0, heightPx / 3) * yScale
        return Frame(l, r, b, t, -l, t - REFERENCE_HEIGHT)
    }

    // Spawn outside the ACTUAL full-screen viewport, not just the legacy
    // 941px photo. Otherwise new pillars pop into view on wider tablets.
    fun pillarSpawnX(frame: Frame): Float = frame.right + 80f
    fun pillarDespawnX(frame: Frame): Float = frame.left - 400f

    fun forPixels(screenWidth: Int, screenHeight: Int): Frame {
        require(screenWidth > 0 && screenHeight > 0)
        val ratio = screenWidth.toFloat() / screenHeight
        val referenceRatio = REFERENCE_WIDTH / REFERENCE_HEIGHT
        val expandedWidth = if (ratio > referenceRatio)
            REFERENCE_HEIGHT * ratio else REFERENCE_WIDTH
        val expandedHeight = if (ratio < referenceRatio)
            REFERENCE_WIDTH / ratio else REFERENCE_HEIGHT
        val extraX = (expandedWidth - REFERENCE_WIDTH).coerceAtLeast(0f) * .5f
        val extraY = (expandedHeight - REFERENCE_HEIGHT).coerceAtLeast(0f) * .5f
        return Frame(-extraX, REFERENCE_WIDTH + extraX,
            -extraY, REFERENCE_HEIGHT + extraY, extraX, extraY)
    }
}
