package com.brendigo.skypeep

/**
 * Pure recovery of the painted bird body after striking the visible sky edge.
 * Recompute the real contact ellipse AFTER this response in the game loop;
 * the stale pre-bounce body must never be used for coins, pillars or hearts.
 */
internal object SkyPeepWorldBounds {
    private const val BOUNCE_SPEED = 235f
    private const val EDGE_INSET = 12f

    data class Recovery(val birdY: Float, val speedY: Float)

    fun recover(birdY: Float, bottom: Float, top: Float,
                birdHeight: Float): Recovery {
        require(birdY.isFinite() && bottom.isFinite() && top.isFinite())
        require(birdHeight.isFinite() && birdHeight > 0f && top > bottom)
        val anchor = birdHeight * SkyPeepContact.BODY_CENTRE_Y
        val minY = bottom + SkyPeepContact.BODY_RADIUS_Y + EDGE_INSET - anchor
        val maxY = top - SkyPeepContact.BODY_RADIUS_Y - EDGE_INSET - anchor
        require(minY <= maxY) { "Visible sky too small for bird" }
        return Recovery(birdY.coerceIn(minY, maxY),
            if (birdY < minY) BOUNCE_SPEED else -BOUNCE_SPEED)
    }
}
