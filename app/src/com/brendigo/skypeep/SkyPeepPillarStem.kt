package com.brendigo.skypeep

import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.Pixmap
import com.badlogic.gdx.graphics.Texture
import kotlin.math.max

/** The illustrated pillars must visually connect to the screen boundaries. */
internal object PillarGeometry {
    private const val TILE_HEIGHT = 144
    const val STEM_INSET = 46f
    const val STEM_WIDTH = 183f

    fun bottomTailHeight(bottomY: Float, spriteHeight: Float): Float =
        max(0f,bottomY-spriteHeight+24f)

    fun topTailStart(topY: Float, spriteHeight: Float): Float =
        topY+spriteHeight*.76f

    /**
     * Shafts grow upward from the visible cap to a screen edge. The final
     * partial repeat must take the *trailing* rows of the source tile so its
     * bottom edge still joins the previous full repeat without a pattern jump.
     */
    fun trailingSourceY(visibleHeight: Float): Int {
        val rows = kotlin.math.ceil(visibleHeight.toDouble()).toInt().coerceIn(1, TILE_HEIGHT)
        return TILE_HEIGHT - rows
    }
}

/**
 * The extension tile repeats vertically hundreds of times. Any horizontal
 * stroke across a single tile is a visible seam in flight. Its shade therefore
 * depends ONLY on x, so row 0 and row 143 are bit-identical.
 */
internal object PillarStemProfile {
    const val WIDTH = 128
    const val HEIGHT = 144

    /** Per-column brightness (percentage of the original world's stone). */
    fun brightness(x: Int): Int {
        require(x in 0 until WIDTH)
        return when {
            x < 6 || x >= 122 -> 72  // illustrated outer edge
            x < 12 -> 98
            x < 26 -> 116           // narrow illuminated bevel
            x < 101 -> 100          // continuous stone face
            x < 117 -> 84           // shaded bevel
            else -> 92
        }
    }
}

/** Repeatable, vertically continuous extension below original illustrated caps. */
internal object SkyPeepPillarStem {
    private fun theme(world:Int):Color=when(world) {
        2 -> Color.valueOf("E7DCBA")
        3 -> Color.valueOf("344A79")
        4 -> Color.valueOf("B8B6A7")
        5 -> Color.valueOf("45317D")
        6 -> Color.valueOf("F5D78A")
        else -> Color.valueOf("E6D7B4")
    }

    fun create(world:Int):Texture {
        require(world in 1..6)
        val pix=Pixmap(PillarStemProfile.WIDTH,PillarStemProfile.HEIGHT,
            Pixmap.Format.RGBA8888)
        val base=theme(world)
        val tint=Color()
        // One vertical line per x, with no y-dependent decoration. Keeping
        // the last and first rows identical eliminates the old 144px stripe.
        for(x in 0 until PillarStemProfile.WIDTH) {
            tint.set(base).mul(PillarStemProfile.brightness(x)/100f)
            tint.a=1f
            pix.setColor(tint)
            pix.drawLine(x,0,x,PillarStemProfile.HEIGHT-1)
        }
        val tex=SkyPeepManagedTexture.fromPixmap(pix)
        tex.setFilter(Texture.TextureFilter.Linear,Texture.TextureFilter.Linear)
        return tex
    }
}
