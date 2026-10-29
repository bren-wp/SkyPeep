package com.brendigo.skypeep

/**
 * Pure UI geometry. LibGDX BitmapFont.draw uses a bottom-up baseline, with
 * glyphs rising above that baseline. Keeping only a maximum text WIDTH is
 * insufficient: live wallet totals and EQUIPPED/REPLAY can protrude above
 * their illustrated panels. This helper bounds both text dimensions while
 * preserving the original desired alignment whenever it already fits.
 */
internal object SkyPeepTextBounds {
    data class Position(val centerX: Float, val baseline: Float)

    fun scaleLimit(width: Float, height: Float, frame: SkyPeepUiTouch.Rect,
                   padding: Float = 7f): Float {
        require(width >= 0f && height >= 0f && padding >= 0f)
        require(frame.width > padding * 2f && frame.height > padding * 2f)
        val x = if (width == 0f) 1f else (frame.width - 2f * padding) / width
        val y = if (height == 0f) 1f else (frame.height - 2f * padding) / height
        return minOf(1f, x, y).coerceAtLeast(.001f)
    }

    fun position(frame: SkyPeepUiTouch.Rect, requestedCenterX: Float,
                 requestedBaseline: Float, textWidth: Float, textHeight: Float,
                 canvasHeight: Float = SkyPeepScreenFrame.REFERENCE_HEIGHT,
                 padding: Float = 7f): Position {
        require(textWidth >= 0f && textHeight >= 0f)
        val minCenter = frame.left + padding + textWidth / 2f
        val maxCenter = frame.left + frame.width - padding - textWidth / 2f
        val bottom = canvasHeight - frame.top - frame.height
        val minBaseline = bottom + padding
        val maxBaseline = bottom + frame.height - padding - textHeight
        require(minCenter <= maxCenter + .01f && minBaseline <= maxBaseline + .01f) {
            "Text size must be fitted into the button before positioning"
        }
        return Position(
            requestedCenterX.coerceIn(minCenter, maxOf(minCenter,maxCenter)),
            requestedBaseline.coerceIn(minBaseline, maxOf(minBaseline,maxBaseline))
        )
    }
}
