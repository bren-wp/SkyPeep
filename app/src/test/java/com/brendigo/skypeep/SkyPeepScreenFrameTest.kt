package com.brendigo.skypeep

import org.junit.Assert.*
import org.junit.Test

class SkyPeepScreenFrameTest {
    @Test fun originalScreenshotRatioStaysExactAndCentered() {
        val frame = SkyPeepScreenFrame.forPixels(941,1672)
        assertEquals(0f,frame.extraX,.001f)
        assertEquals(0f,frame.extraY,.001f)
        assertEquals(941f,frame.right-frame.left,.001f)
        assertEquals(1672f,frame.top-frame.bottom,.001f)
    }

    @Test fun tallPhoneFillsFullHeightWithoutSquashingArtwork() {
        val frame = SkyPeepScreenFrame.forPixels(1080,2400)
        assertEquals(0f,frame.extraX,.001f)
        assertTrue(frame.extraY > 0f)
        assertEquals(941f,frame.right-frame.left,.001f)
        assertEquals(941f*2400f/1080f,
            frame.top-frame.bottom,.02f)
    }

    @Test fun widePhoneAndTabletFillFullWidthWithoutStretchingBird() {
        for((width,height) in listOf(1280 to 720,2048 to 1536,
                2400 to 1080,1024 to 768)) {
            val frame = SkyPeepScreenFrame.forPixels(width,height)
            assertEquals(0f,frame.extraY,.001f)
            assertTrue(frame.extraX >= 0f)
            assertEquals(1672f*width.toFloat()/height,
                frame.right-frame.left,.02f)
            assertEquals(1672f,frame.top-frame.bottom,.001f)
        }
    }

    @Test fun compactPortraitKeepsOriginalButtonGeometryVisible() {
        for((width,height) in listOf(360 to 640,720 to 1280,
                1080 to 1920,1080 to 2340,800 to 1280)) {
            val f=SkyPeepScreenFrame.forPixels(width,height)
            assertTrue(f.left <= 0f && f.right >= 941f)
            assertTrue(f.bottom <= 0f && f.top >= 1672f)
            assertEquals(941f*.5f,(f.left+f.right)*.5f,.01f)
            assertEquals(1672f*.5f,(f.bottom+f.top)*.5f,.01f)
        }
    }

    @Test fun pillarsSpawnOutsideVisibleRegionEvenOnVeryWideTablets() {
        for((w,h) in listOf(941 to 1672,1080 to 2400,
                1200 to 1920,2048 to 1536,2400 to 1080)) {
            val frame=SkyPeepScreenFrame.forPixels(w,h)
            assertEquals(frame.right+80f,
                SkyPeepScreenFrame.pillarSpawnX(frame),.001f)
            assertTrue(SkyPeepScreenFrame.pillarSpawnX(frame)>frame.right)
            assertTrue(SkyPeepScreenFrame.pillarDespawnX(frame)<frame.left)
        }
    }


    @Test fun hudKeepsOriginalFrameWhenSystemBarsAreHidden() {
        val f = SkyPeepScreenFrame.forPixels(1080, 2400)
        val safe = SkyPeepScreenFrame.hudSafeFrame(f, 1080, 2400,
            SkyPeepScreenFrame.PixelInsets(0, 0, 0, 0))
        assertEquals(f, safe)
    }

    @Test fun hudMovesAwayFromCutoutWithoutShiftingGameplayWorld() {
        val full = SkyPeepScreenFrame.forPixels(1080, 2400)
        val safe = SkyPeepScreenFrame.hudSafeFrame(full, 1080, 2400,
            SkyPeepScreenFrame.PixelInsets(32, 84, 12, 30))
        assertTrue(safe.left > full.left)
        assertTrue(safe.top < full.top)
        assertTrue(safe.right < full.right)
        assertTrue(safe.bottom > full.bottom)
        assertEquals(full.top, SkyPeepScreenFrame.forPixels(1080, 2400).top, .001f)
        assertEquals(941f * 84f / 1080f, full.top - safe.top, .02f)
    }

    @Test fun pauseTouchBoundsFollowTransientSystemBars() {
        val full = SkyPeepScreenFrame.forPixels(941, 1672)
        val safe = SkyPeepScreenFrame.hudSafeFrame(full, 941, 1672,
            SkyPeepScreenFrame.PixelInsets(0, 60, 30, 0))
        val centerX = SkyPeepHudLayout.pauseX(safe) + 60f
        assertTrue(SkyPeepHudLayout.pauseContains(safe, centerX, 110f))
        assertFalse(SkyPeepHudLayout.pauseContains(safe, centerX, 20f))
    }

    @Test(expected=IllegalArgumentException::class)
    fun invalidScreenWidthIsRejected() {
        SkyPeepScreenFrame.forPixels(0,2000)
    }
}
