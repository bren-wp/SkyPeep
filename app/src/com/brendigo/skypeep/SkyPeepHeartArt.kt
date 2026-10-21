package com.brendigo.skypeep

import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.Pixmap
import com.badlogic.gdx.graphics.Texture

/** Small, transparent, procedurally rasterised heart. Never a screenshot overlay. */
internal object SkyPeepHeartArt {
    fun create(): Texture {
        val pixels = Pixmap(96,96,Pixmap.Format.RGBA8888)
        pixels.setColor(0f,0f,0f,0f)
        pixels.fill()
        for (y in 0 until 96) {
            for (x in 0 until 96) {
                val u = (x-48f)/36f
                val v = (48f-y)/36f
                val q = u*u+v*v-1f
                val inside = q*q*q-u*u*v*v*v
                if (inside <= 0f) {
                    pixels.setColor(if (inside > -.09f) Color(.75f,.09f,.20f,1f)
                                    else Color(.99f,.28f,.40f,1f))
                    pixels.drawPixel(x,y)
                }
            }
        }
        val texture = SkyPeepManagedTexture.fromPixmap(pixels)
        texture.setFilter(Texture.TextureFilter.Linear,Texture.TextureFilter.Linear)
        return texture
    }
}
