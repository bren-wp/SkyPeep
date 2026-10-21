package com.brendigo.skypeep

import org.junit.Assert.*
import org.junit.Test

class SkyPeepWorldBoundsTest {
    private val birdX = 172f
    private val birdHeight = SkyPeepBirdLayout.HEIGHT

    @Test fun floorRecoveryRecalculatesContactFromCorrectedPosition() {
        for ((width,height) in listOf(360 to 640, 720 to 1280, 1080 to 2400, 1600 to 2000)) {
            val frame=SkyPeepScreenFrame.forPixels(width,height)
            val beforeY=frame.bottom-100f
            val oldBody=SkyPeepContact.body(birdX,beforeY,SkyPeepBirdLayout.WIDTH,birdHeight)
            assertTrue(SkyPeepContact.hitsWorldEdge(oldBody,frame.bottom,frame.top))
            val fixed=SkyPeepWorldBounds.recover(beforeY,frame.bottom,frame.top,birdHeight)
            val actual=SkyPeepContact.body(birdX,fixed.birdY,SkyPeepBirdLayout.WIDTH,birdHeight)
            assertFalse("floor still outside at $width x $height",
                SkyPeepContact.hitsWorldEdge(actual,frame.bottom,frame.top))
            assertTrue("floor should bounce upward",fixed.speedY>0f)
            assertTrue(actual.cy>oldBody.cy)
            // The stale body overlaps the illustrated bottom stone. The
            // bounced body is centered in the now-clear passage. This is why
            // the physics loop must recompute the contact ellipse before
            // deciding whether a gate collision/coin pickup occurred.
            val gapBottom=actual.bottom-2f
            val gapTop=actual.top+2f
            val pillarX=actual.cx-110f
            assertTrue(SkyPeepContact.hitsGate(
                oldBody,pillarX,275f,gapBottom,gapTop))
            assertFalse(SkyPeepContact.hitsGate(
                actual,pillarX,275f,gapBottom,gapTop))
        }
    }

    @Test fun ceilingRecoveryAlwaysMovesInsideAndDownwards() {
        for (mode in SkyDifficulty.entries)
            for (world in 1..6) {
                val frame=SkyPeepScreenFrame.forPixels(1080,2400)
                val before=frame.top+world*40f
                val fixed=SkyPeepWorldBounds.recover(before,frame.bottom,frame.top,birdHeight)
                val body=SkyPeepContact.body(birdX,fixed.birdY,SkyPeepBirdLayout.WIDTH,birdHeight)
                assertFalse("$mode world=$world",
                    SkyPeepContact.hitsWorldEdge(body,frame.bottom,frame.top))
                assertTrue(fixed.speedY<0f)
            }
    }

    @Test fun invalidAndUnreasonablyNarrowWorldCannotPushInvalidPhysics() {
        try {
            SkyPeepWorldBounds.recover(Float.NaN,0f,1672f,birdHeight)
            fail("non-finite coordinate was accepted")
        } catch (_: IllegalArgumentException) { }
        try {
            SkyPeepWorldBounds.recover(0f,0f,10f,birdHeight)
            fail("impossible visible sky accepted")
        } catch (_: IllegalArgumentException) { }
    }

    @Test fun recoveryIsConsistentAtAllLevelsAndOnExpandedScreens() {
        val frame=SkyPeepScreenFrame.forPixels(1600,2000)
        for (difficulty in SkyDifficulty.entries) for (level in 1..6) {
            val restored=SkyPeepWorldBounds.recover(
                frame.bottom-100f,frame.bottom,frame.top,birdHeight)
            assertTrue(restored.birdY.isFinite())
            assertEquals(235f,restored.speedY,0f)
            assertTrue(SkyRouteRules.gap(level,difficulty)>0f)
        }
    }
}
