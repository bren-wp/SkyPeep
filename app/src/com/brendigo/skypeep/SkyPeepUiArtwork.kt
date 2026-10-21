package com.brendigo.skypeep

import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.Pixmap
import com.badlogic.gdx.graphics.Texture
import kotlin.math.max

/**
 * One-time generated illustrations for SkyPeep's premium control chrome and
 * victory stars. Extracted from the large game class to keep runtime physics
 * separate from artwork rasterization.
 *
 * Reusing a mutable pixel Color avoids ~107,000 startup Color allocations.
 * Source artwork, bevel geometry and palette values stay unchanged.
 */
internal object SkyPeepUiArtwork {
    fun createVictoryStar(): Texture {
        // Render an antialiased-looking five-point star as one reusable texture.
        val side=128
        val pix=Pixmap(side,side,Pixmap.Format.RGBA8888)
        pix.setColor(0f,0f,0f,0f)
        pix.fill()
        fun star(cx:Int,cy:Int,outer:Int,inner:Int,tint:Color) {
            val xx=IntArray(10)
            val yy=IntArray(10)
            for(i in 0..9) {
                val angle=-Math.PI/2+i*Math.PI/5
                val r=if(i%2==0)outer else inner
                xx[i]=cx+(kotlin.math.cos(angle)*r).toInt()
                yy[i]=cy+(kotlin.math.sin(angle)*r).toInt()
            }
            pix.setColor(tint)
            for(i in 0..9) {
                val next=(i+1)%10
                pix.fillTriangle(cx,cy,xx[i],yy[i],xx[next],yy[next])
            }
        }
        star(64,67,59,29,Color.valueOf("49220F"))
        star(64,61,56,27,Color.valueOf("FFB52D"))
        star(60,56,47,22,Color.valueOf("FFE34D"))
        val tx=SkyPeepManagedTexture.fromPixmap(pix)
        tx.setFilter(Texture.TextureFilter.Linear,Texture.TextureFilter.Linear)
        return tx
    }


    fun createChrome(): Map<String, Texture> {
        val textures = linkedMapOf<String, Texture>()
        val palette=mapOf(
            "dark" to Color.valueOf("103963"),
            "green" to Color.valueOf("48C62D"),
            "cream" to Color.valueOf("F6E4C4"),
            "wood" to Color.valueOf("92552F"),
            "wallet" to Color.valueOf("573729"),
            "blue" to Color.valueOf("169CE8"),
            "red" to Color.valueOf("C83A40")
        )
        for ((name,base) in palette) {
            val pix=Pixmap(192,80,Pixmap.Format.RGBA8888)
            pix.setColor(0f,0f,0f,0f)
            pix.fill()
            val pixel=Color()
            // Render the same premium illustration treatment used by
            // reference PLAY/PAUSE buttons: thick chocolate silhouette,
            // coloured inset bevel, broad glossy shoulder and shaded base.
            // The border is antialiased and the patch remains 9-slice safe.
            for(y in 0 until 80) for(x in 0 until 192) {
                val dx=max(0f,kotlin.math.abs(x-95.5f)-64.5f)
                val dy=max(0f,kotlin.math.abs(y-39.5f)-9.5f)
                val d=kotlin.math.sqrt(dx*dx+dy*dy)
                if(d>30f) continue
                val edgeAlpha=((30f-d)/1.65f).coerceIn(0f,1f)
                val vertical=y/79f
                when {
                    d>=25f -> pixel.set(.17f,.075f,.055f,edgeAlpha)
                    d>=22f -> pixel.set(.39f,.20f,.12f,edgeAlpha)
                    d>=19f -> pixel.set(
                        (base.r*.66f).coerceIn(0f,1f),
                        (base.g*.61f).coerceIn(0f,1f),
                        (base.b*.57f).coerceIn(0f,1f),edgeAlpha)
                    else -> {
                        var brightness=1.19f-.29f*vertical
                        if(y<=17) brightness+=.07f
                        if(y>=66) brightness-=.12f
                        if(y in 13..17 && x in 28..154)
                            brightness+=.085f
                        if(d>=15f) brightness-=.045f
                        pixel.set((base.r*brightness).coerceIn(0f,1f),
                                  (base.g*brightness).coerceIn(0f,1f),
                                  (base.b*brightness).coerceIn(0f,1f),edgeAlpha)
                    }
                }
                pix.drawPixel(x,y,Color.rgba8888(pixel))
            }
            val image=SkyPeepManagedTexture.fromPixmap(pix).also {
                it.setFilter(Texture.TextureFilter.Linear,Texture.TextureFilter.Linear)
            }
            textures[name]=image
        }
        return textures
    }

}
