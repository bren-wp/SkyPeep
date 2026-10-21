package com.brendigo.skypeep

import org.junit.Assert.*
import org.junit.Test

class SkyPeepPillarVisualTest {
    @Test fun illustratedPillarShaftIsContainedInsideCollidableStone() {
        val capWidth=275f
        val inset=SkyPeepContact.CAP_SIDE_INSET
        assertTrue(PillarGeometry.STEM_INSET >= inset)
        assertTrue(PillarGeometry.STEM_INSET+PillarGeometry.STEM_WIDTH <= capWidth-inset)
    }

    @Test fun capShaftOverlapAvoidsTinyVisualHairlineGaps() {
        assertTrue(PillarGeometry.topTailStart(800f,300f) < 800f+300f)
        assertTrue(PillarGeometry.bottomTailHeight(800f,300f) > 800f-300f)
    }
}
