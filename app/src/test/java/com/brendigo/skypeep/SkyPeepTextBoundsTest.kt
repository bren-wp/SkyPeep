package com.brendigo.skypeep

import org.junit.Assert.*
import org.junit.Test

class SkyPeepTextBoundsTest {
    private fun assertInside(rect: SkyPeepUiTouch.Rect,
                             proposedBaseline: Float, proposedCentre: Float,
                             originalWidth: Float, originalHeight: Float) {
        val scale=SkyPeepTextBounds.scaleLimit(originalWidth,originalHeight,rect)
        val width=originalWidth*scale
        val height=originalHeight*scale
        val final=SkyPeepTextBounds.position(
            rect,proposedCentre,proposedBaseline,width,height)
        val bottom=1672f-rect.top-rect.height
        val top=bottom+rect.height
        assertTrue("text outside left edge",final.centerX-width*.5f >= rect.left+6.9f)
        assertTrue("text outside right edge",final.centerX+width*.5f <= rect.left+rect.width-6.9f)
        assertTrue("text below button",final.baseline >= bottom+6.9f)
        assertTrue("text above button",final.baseline+height <= top-6.9f)
    }

    @Test fun liveWalletTotalsCannotRiseAboveTheirIllustratedPanels() {
        // Old skins number baseline (1637) was above the upper panel edge
        // (1634) before its glyph height was even added.
        assertInside(SkyPeepUiTouch.Rect(120f,38f,164f,65f),
            1637f,199f,160f,60f)
        assertInside(SkyPeepUiTouch.Rect(733f,38f,111f,65f),
            1637f,788f,130f,55f)
        assertInside(SkyPeepUiTouch.Rect(119f,38f,136f,65f),
            1618f,186f,245f,72f)
        assertInside(SkyPeepUiTouch.Rect(743f,38f,76f,65f),
            1618f,781f,175f,72f)
    }

    @Test fun equippedBuyShopAndVictoryRemainWithinActualTouchTargets() {
        val default=SkyPeepUiTouch.defaultEquipRect
        assertInside(default,1672f-default.top-default.height/2f+13f,
            default.left+default.width/2f,320f,89f)
        for (skin in 1..4) {
            val r=SkyPeepUiTouch.skinPurchaseRect(skin)
            assertInside(r,1672f-r.top-r.height/2f+13f,
                r.left+r.width/2f,230f,96f)
        }
        for (id in 0..3) {
            val r=SkyPeepUiTouch.shopPurchaseRect(id)
            assertInside(r,1060f-id*195f+103f,706f,250f,105f)
        }
        for (button in SkyPeepResultTouch.victoryButtons) {
            val r=SkyPeepUiTouch.Rect(button.left,button.top,button.width,button.height)
            assertInside(r,303f,button.left+button.width/2f,380f,170f)
        }
    }

    @Test fun alreadyFittingTextKeepsItsPositionAndScale() {
        val b=SkyPeepUiTouch.Rect(279f,1290f,384f,137f)
        assertEquals(1f,SkyPeepTextBounds.scaleLimit(180f,55f,b),.001f)
        val p=SkyPeepTextBounds.position(b,471f,310f,180f,55f)
        assertEquals(471f,p.centerX,.001f)
        assertEquals(310f,p.baseline,.001f)
    }

    @Test fun clippingProtectionWorksOnAllDeviceAspectRatios() {
        val viewports=listOf(360 to 640,720 to 1280,1080 to 1920,
            1080 to 2400,1200 to 1920,1600 to 2000,2048 to 1536)
        for((w,h) in viewports) {
            val visible=SkyPeepScreenFrame.forPixels(w,h)
            for(skin in 1..4) {
                val r=SkyPeepUiTouch.skinPurchaseRect(skin)
                assertTrue(r.left>=visible.left)
                assertTrue(r.left+r.width<=visible.right)
                assertInside(r,1672f-r.top-r.height/2f+13f,
                    r.left+r.width/2f,260f,78f)
            }
        }
    }
}
