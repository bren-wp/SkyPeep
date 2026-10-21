package com.brendigo.skypeep

import org.junit.Assert.*
import org.junit.Test

class SkyPeepBackgroundMotionTest {
    @Test fun reusableGameplayCropMatchesPureReferenceAcrossManyFrames() {
        val buffer=SkyPeepBackgroundMotion.MutableCrop()
        for (width in listOf(1, 8, 128, 706, 864, 1024, 2048)) {
            for (step in 0..260) {
                val elapsed=step*.081f
                val playing=step%17!=0
                SkyPeepBackgroundMotion.cropInto(width,elapsed,playing,buffer)
                assertEquals(SkyPeepBackgroundMotion.crop(width,elapsed,playing),
                    SkyPeepBackgroundMotion.Crop(buffer.sourceX,buffer.sourceWidth))
            }
        }
    }

    @Test fun reusableCropResetsWhenReturningToIdleBackground() {
        val buffer=SkyPeepBackgroundMotion.MutableCrop()
        SkyPeepBackgroundMotion.cropInto(864,12f,true,buffer)
        assertTrue(buffer.sourceWidth<864)
        SkyPeepBackgroundMotion.cropInto(864,12f,false,buffer)
        assertEquals(0,buffer.sourceX)
        assertEquals(864,buffer.sourceWidth)
    }

    @Test fun cropsNeverLeaveTheSourceTexture() {
        for (width in listOf(1, 8, 128, 706, 864, 1024, 2048)) {
            for (step in 0..400) {
                val crop = SkyPeepBackgroundMotion.crop(width, step * .35f, true)
                assertTrue(crop.sourceX >= 0)
                assertTrue(crop.sourceWidth > 0)
                assertTrue(crop.sourceX + crop.sourceWidth <= width)
            }
        }
    }

    @Test fun idleScenePreservesOriginalReferencePixels() {
        val first = SkyPeepBackgroundMotion.crop(706, 0f, false)
        assertEquals(SkyPeepBackgroundMotion.Crop(0, 706), first)
        assertEquals(first, SkyPeepBackgroundMotion.crop(706, 900f, false))
        assertEquals(first, SkyPeepBackgroundMotion.crop(706, 0f, true))
    }

    @Test fun firstFlapTransitionsSmoothlyToMovingCrop() {
        val initial = SkyPeepBackgroundMotion.crop(864, 0f, true)
        val middle = SkyPeepBackgroundMotion.crop(864, .7f, true)
        val final = SkyPeepBackgroundMotion.crop(864, 1.4f, true)
        assertEquals(864, initial.sourceWidth)
        assertTrue(middle.sourceWidth < initial.sourceWidth)
        assertTrue(middle.sourceWidth > final.sourceWidth)
    }

    @Test fun motionIsBoundedAndNonzeroDuringFlight() {
        val width = 864
        val first = SkyPeepBackgroundMotion.crop(width, 2f, true)
        val later = SkyPeepBackgroundMotion.crop(width, 13f, true)
        assertEquals(first.sourceWidth, later.sourceWidth)
        assertNotEquals(first.sourceX, later.sourceX)
        assertTrue(first.sourceWidth >= (width * .96).toInt())
    }

    @Test fun nonFiniteElapsedTimesCannotCrashBackgroundCrop() {
        val width = 706
        for (invalid in listOf(Float.NaN,Float.POSITIVE_INFINITY,
                Float.NEGATIVE_INFINITY)) {
            assertEquals(SkyPeepBackgroundMotion.Crop(0,width),
                SkyPeepBackgroundMotion.crop(width,invalid,true))
        }
        val huge = SkyPeepBackgroundMotion.crop(width,Float.MAX_VALUE,true)
        assertTrue(huge.sourceX >= 0)
        assertTrue(huge.sourceWidth in 1..width)
        assertTrue(huge.sourceX+huge.sourceWidth <= width)
    }

    @Test(expected = IllegalArgumentException::class)
    fun rejectsInvalidTextures() {
        SkyPeepBackgroundMotion.crop(0, 1f, true)
    }
}
