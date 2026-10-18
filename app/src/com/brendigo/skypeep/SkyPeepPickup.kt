package com.brendigo.skypeep

import kotlin.math.abs
import kotlin.math.hypot
import kotlin.math.max

/**
 * Testable collision of the actual illustrated pickup circle with the
 * bird's solid torso/head ellipse. Unlike the old 128x102 pickup ellipse,
 * this does not award coins while an entire sprite-width away.
 */
internal object SkyPeepPickup {
    const val COIN_RADIUS = 37f
    const val GEM_RADIUS = 41f

    fun overlaps(body: SkyPeepContact.Body, itemX: Float, itemY: Float, radius: Float): Boolean {
        if (!itemX.isFinite() || !itemY.isFinite() ||
            !radius.isFinite() || radius <= 0f ||
            !body.cx.isFinite() || !body.cy.isFinite() ||
            !body.rx.isFinite() || !body.ry.isFinite() ||
            body.rx <= 0f || body.ry <= 0f) return false

        val dx = abs(itemX.toDouble() - body.cx)
        val dy = abs(itemY.toDouble() - body.cy)
        val rx = body.rx.toDouble()
        val ry = body.ry.toDouble()
        val r = radius.toDouble()

        // Fast exact rejection, and a point already inside the body ellipse.
        if (dx > rx + r || dy > ry + r) return false
        if ((dx / rx) * (dx / rx) + (dy / ry) * (dy / ry) <= 1.0) return true

        // Nearest point on an axis-aligned ellipse, solved with a bounded
        // scalar bisection. The circle touches iff its radius reaches that point.
        val rx2 = rx * rx
        val ry2 = ry * ry
        fun ellipseEquation(lambda: Double): Double {
            val x = rx * dx / (lambda + rx2)
            val y = ry * dy / (lambda + ry2)
            return x * x + y * y
        }
        var low = 0.0
        var high = max(rx * dx, ry * dy).coerceAtLeast(1.0)
        while (ellipseEquation(high) > 1.0) high *= 2.0
        repeat(40) {
            val mid = (low + high) * 0.5
            if (ellipseEquation(mid) > 1.0) low = mid else high = mid
        }
        val nearX = rx2 * dx / (high + rx2)
        val nearY = ry2 * dy / (high + ry2)
        return hypot(dx - nearX, dy - nearY) <= r + 0.00001
    }
}
