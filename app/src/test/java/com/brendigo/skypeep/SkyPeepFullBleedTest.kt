package com.brendigo.skypeep

import org.junit.Assert.*
import org.junit.Test

class SkyPeepFullBleedTest {
    @Test fun edgeSamplingDoesNotProduceTinyFlatBands() {
        assertEquals(128, SkyPeepFullBleed.sample(1254))
        assertEquals(117, SkyPeepFullBleed.sample(941))
        assertEquals(4, SkyPeepFullBleed.sample(16))
        assertEquals(2, SkyPeepFullBleed.sample(2))
    }

    @Test fun menusSampleRealEdgeArtWithoutCopyingWalletsOrLogos() {
        for (dimension in listOf(941,1254,1672,2048,16)) {
            val pixels=SkyPeepFullBleed.safeUiStrip(dimension)
            assertTrue(pixels <= 12)
            assertTrue(pixels <= dimension)
            assertTrue(pixels > 0)
        }
        // Gameplay backgrounds have no baked wallet controls and may safely
        // use a broad naturally illustrated source region.
        assertTrue(SkyPeepFullBleed.sample(1254) >
            SkyPeepFullBleed.safeUiStrip(1254))
    }

    @Test fun illustratedWorldPanelsUseBroadSceneryNotTwelvePixelUiStrip() {
        for (screen in listOf("PLAY", "SHOP", "SETTINGS")) {
            assertTrue("world scenery: $screen",
                SkyPeepFullBleed.usesScenery(screen))
            val top = SkyPeepFullBleed.horizontalBand(
                941, 1672, SkyPeepFullBleed.usesScenery(screen), bottom=false)
            assertEquals(128, top.height)
        }
        for (screen in listOf("SPLASH", "MENU", "MAP", "SKINS", "PAUSE")) {
            assertFalse("photo UI: $screen", SkyPeepFullBleed.usesScenery(screen))
            val top = SkyPeepFullBleed.horizontalBand(
                941, 1672, SkyPeepFullBleed.usesScenery(screen), bottom=false)
            assertEquals(12, top.height)
        }
        assertFalse(SkyPeepFullBleed.usesScenery("RESULT", false))
        assertTrue(SkyPeepFullBleed.usesScenery("RESULT", true))
        assertFalse(SkyPeepFullBleed.usesScenery("UNKNOWN"))
    }

    @Test fun sceneryOverscanWorksAcrossAllSupportedPhoneAndTabletRatios() {
        for ((w,h) in listOf(360 to 640,720 to 1280,1080 to 2400,
                               1200 to 1920,1600 to 2000,2048 to 1536)) {
            val frame = SkyPeepScreenFrame.forPixels(w,h)
            assertTrue(frame.extraX >= 0f && frame.extraY >= 0f)
            val sampled = SkyPeepFullBleed.horizontalBand(706,1254,
                SkyPeepFullBleed.usesScenery("SHOP"), bottom=true)
            assertEquals(128, sampled.height)
            assertEquals(1254, sampled.y + sampled.height)
        }
    }

    @Test fun tallScreenSourceBandsStayInsideOriginalTexture() {
        for ((width,height) in listOf(12 to 16,706 to 1254,
                                      941 to 1672,2048 to 1536)) {
            for (scenery in listOf(false,true)) {
                val top=SkyPeepFullBleed.horizontalBand(
                    width,height,scenery,bottom=false)
                val bottom=SkyPeepFullBleed.horizontalBand(
                    width,height,scenery,bottom=true)
                for (band in listOf(top,bottom)) {
                    // Never combine a centre-cropped x coordinate with an
                    // entire image width: that used to sample beyond the PNG.
                    assertEquals(0,band.x)
                    assertEquals(width,band.width)
                    assertTrue(band.height > 0 && band.height <= height)
                    assertTrue(band.y >= 0 && band.y+band.height <= height)
                    assertEquals(width,band.x+band.width)
                }
                assertEquals(0,top.y)
                assertEquals(height,bottom.y+bottom.height)
                assertEquals(top.height,bottom.height)
            }
        }
    }

    @Test fun overscanUsesSourceArtworkOnTallAndWideDisplays() {
        for ((w,h) in listOf(360 to 800, 1080 to 2400, 1200 to 1920,
                               1440 to 3200, 2048 to 1536, 2560 to 1600)) {
            val frame=SkyPeepScreenFrame.forPixels(w,h)
            assertTrue(frame.left <= 0f && frame.right >= 941f)
            assertTrue(frame.bottom <= 0f && frame.top >= 1672f)
            assertTrue(frame.extraX > 0f || frame.extraY > 0f)
        }
    }

    @Test(expected=IllegalArgumentException::class)
    fun invalidTextureDimensionCannotBeSampled() {
        SkyPeepFullBleed.sample(0)
    }
}
