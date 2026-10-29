package com.brendigo.skypeep

import org.junit.Assert.*
import org.junit.Test

class SkyPeepVictoryLayoutTest {
    @Test fun headlineAndNumbersHaveNonOverlappingSourceArtZones() {
        for((left,width) in listOf(137f to 313f,492f to 311f)) {
            val caption=SkyPeepVictoryLayout.heading(left,width)
            val value=SkyPeepVictoryLayout.value(left,width)
            assertTrue("SCORE/BEST must sit above its value",
                caption.top+caption.height+7f <= value.top)
            assertTrue(value.top+value.height<=966f)
            assertTrue(caption.top>=820f)
            for((textW,textH) in listOf(210f to 48f,340f to 85f)) {
                val hs=SkyPeepTextBounds.scaleLimit(textW,textH,caption)
                val vs=SkyPeepTextBounds.scaleLimit(textW,textH,value)
                val a=SkyPeepTextBounds.position(
                    caption,left+width/2f,826f,textW*hs,textH*hs)
                val b=SkyPeepTextBounds.position(
                    value,left+width/2f,781f,textW*vs,textH*vs)
                val captionBottom=1672f-caption.top-caption.height
                val valueTop=1672f-value.top
                assertTrue(a.baseline-textH*hs>=captionBottom+6.9f)
                assertTrue(b.baseline<=valueTop-6.9f)
                assertTrue(
                    "Caption glyph must remain ABOVE the numeric glyph",
                    a.baseline-textH*hs>b.baseline)
            }
        }
    }

    @Test fun collectedCoinsHeadingAndProgressCannotCollide() {
        val h=SkyPeepVictoryLayout.coinHeading
        val v=SkyPeepVictoryLayout.coinValue
        assertTrue(h.top+h.height+12f<=v.top)
        assertTrue(h.left==v.left && h.width==v.width)
        assertTrue(v.top+v.height<=1164f)
    }
}
