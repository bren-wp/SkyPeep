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
        assertTrue("glyphs below illustrated panel",
            final.baseline-height >= bottom+6.9f)
        assertTrue("glyphs above illustrated panel",
            final.baseline <= top-6.9f)
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

    @Test fun shopAndSettingsHeadersDoNotSinkBelowTheirWoodPanels() {
        val shop=SkyPeepUiTouch.Rect(117f,185f,708f,122f)
        val h=84f
        val result=SkyPeepTextBounds.position(
            shop,471f,1444f,442f,h)
        // Shop title stays within y=1365..1487 (game bottom-up pixels).
        assertTrue(result.baseline-h>=1365f+7f)
        assertTrue(result.baseline<=1487f-7f)
        val settings=SkyPeepUiTouch.Rect(132f,319f,677f,139f)
        val label=SkyPeepTextBounds.position(
            settings,471f,1340f,380f,98f)
        assertTrue(label.baseline-98f>=1214f+7f)
        assertTrue(label.baseline<=1353f-7f)
    }

    @Test fun clampingUsesActualGlyphDownwardExtentNotIncorrectUpwardExtent() {
        val panel=SkyPeepUiTouch.Rect(100f,200f,300f,100f)
        val bottom=1672f-200f-100f
        val p=SkyPeepTextBounds.position(
            panel,250f,bottom+3f,150f,58f)
        assertEquals(bottom+58f+7f,p.baseline,.01f)
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

    @Test fun levelShopSettingsAndResultPanelLabelsStayInsideIllustratedBounds() {
        val panels=listOf(
            SkyPeepUiTouch.Rect(110f,194f,721f,103f), // toast
            SkyPeepUiTouch.Rect(137f,820f,666f,119f), // settings last row
            SkyPeepUiTouch.Rect(132f,319f,677f,139f), // settings title
            SkyPeepUiTouch.Rect(117f,185f,708f,122f), // shop headline
            SkyPeepUiTouch.Rect(118f,330f,338f,90f), // shop coin wallet
            SkyPeepUiTouch.Rect(483f,330f,338f,90f), // shop gems
            SkyPeepUiTouch.Rect(201f,857f,153f,90f), // game over score
            SkyPeepUiTouch.Rect(402f,857f,160f,90f), // game over best
            SkyPeepUiTouch.Rect(665f,857f,122f,90f), // game over coins
            SkyPeepUiTouch.Rect(293f,1043f,463f,57f), // game over 10,001
            SkyPeepUiTouch.Rect(137f,820f,313f,146f), // victory score
            SkyPeepUiTouch.Rect(492f,820f,311f,146f), // victory best
            SkyPeepUiTouch.Rect(137f,1008f,666f,156f), // victory coins
            SkyPeepUiTouch.Rect(14f,205f,260f,50f)  // world goal
        )
        for ((index,p) in panels.withIndex()) {
            assertTrue("panel ${index} crosses left canvas",p.left>=0f)
            assertTrue("panel ${index} crosses right canvas",
                p.left+p.width<=941f)
            assertTrue("panel ${index} crosses top canvas",p.top>=0f)
            assertTrue("panel ${index} crosses bottom canvas",
                p.top+p.height<=1672f)
            // Exercise very large dynamic values and multilingual labels.
            assertInside(p,1672f-p.top-7f,
                p.left+p.width/2f,p.width*2.7f,p.height*1.6f)
        }
    }

    @Test fun worldLockOverlaysDoNotExtendOutsideReferenceAndLongStatusFits() {
        for(world in 2..6) {
            val r=SkyPeepMapUi.lockChip(world)!!
            assertTrue(r.left>=0f && r.left+r.width<=941f)
            assertTrue(r.top>=0f && r.top+r.height<=1672f)
            assertInside(r,1672f-r.top-r.height/2f+12f,
                r.left+r.width/2f,176f,69f)
        }
        val renamed=SkyPeepUiTouch.Rect(644f-119f,1048f+40f,238f,49f)
        // The Cloud Drift renaming uses measured marker coordinates in
        // production; no new large label may trespass on nearby star rows.
        assertInside(renamed,1672f-renamed.top-10f,
            renamed.left+renamed.width/2f,320f,80f)
    }
}
