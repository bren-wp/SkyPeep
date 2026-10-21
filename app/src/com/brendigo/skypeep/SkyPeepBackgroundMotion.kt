package com.brendigo.skypeep

import kotlin.math.roundToInt
import kotlin.math.sin

/**
 * Small, seam-free parallax crop. The actual GL draw loop uses a reusable
 * MutableCrop instead of allocating a new Kotlin data class every frame.
 */
internal object SkyPeepBackgroundMotion {
    internal data class Crop(val sourceX: Int, val sourceWidth: Int)
    internal class MutableCrop(var sourceX: Int = 0, var sourceWidth: Int = 0)

    fun crop(textureWidth: Int, elapsedSeconds: Float, playing: Boolean): Crop {
        val result = MutableCrop()
        cropInto(textureWidth,elapsedSeconds,playing,result)
        return Crop(result.sourceX,result.sourceWidth)
    }

    fun cropInto(textureWidth: Int, elapsedSeconds: Float,
                 playing: Boolean, result: MutableCrop) {
        require(textureWidth > 0)
        val maximumMargin = (textureWidth * 0.018f).roundToInt()
            .coerceAtMost((textureWidth - 1) / 2)
        if (!playing || maximumMargin == 0) {
            result.sourceX = 0
            result.sourceWidth = textureWidth
            return
        }

        // Preserve exact reference pixels while idle; ease very gently
        // after the first input instead of snapping to a moving camera.
        val elapsed = if (elapsedSeconds.isFinite())
            elapsedSeconds.coerceIn(0f,1_000_000f) else 0f
        val eased = (elapsed / 1.4f).coerceIn(0f,1f)
        val margin = (maximumMargin * eased).roundToInt()
        if (margin == 0) {
            result.sourceX = 0
            result.sourceWidth = textureWidth
            return
        }
        val drift = (margin * sin(elapsed.toDouble() * 0.13).toFloat()).roundToInt()
        result.sourceX = (margin + drift).coerceIn(0,margin*2)
        result.sourceWidth = textureWidth - margin*2
    }
}
