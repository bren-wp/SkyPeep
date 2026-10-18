package com.brendigo.skypeep

import com.badlogic.gdx.graphics.Pixmap
import com.badlogic.gdx.graphics.Texture
import com.badlogic.gdx.graphics.glutils.PixmapTextureData

/**
 * LibGDX cannot restore Texture(Pixmap) after Android destroys the EGL context.
 * Keep only these tiny generated sprites' source pixels while their managed
 * textures live; release both the GL texture and native Pixmap on dispose.
 *
 * File-backed original artwork remains loaded normally and unchanged.
 */
internal object SkyPeepManagedTexture {
    fun fromPixmap(pixmap: Pixmap): Texture {
        return try {
            object : Texture(PixmapTextureData(
                pixmap, pixmap.format, false, false, true
            )) {
                private var pixmapReleased = false

                override fun dispose() {
                    super.dispose()
                    if (!pixmapReleased) {
                        pixmapReleased = true
                        pixmap.dispose()
                    }
                }
            }
        } catch (error: Throwable) {
            pixmap.dispose()
            throw error
        }
    }
}
