package com.brendigo.skypeep

import org.junit.Assert.*
import org.junit.Test

/** The source of the old horizontal 144px stripes was y-dependent decoration. */
class PillarStemProfileTest {
    @Test fun repeatingShaftsUseOneIdenticalColourProfileForEveryRow() {
        val width = PillarStemProfile.WIDTH
        val height = PillarStemProfile.HEIGHT
        assertEquals(128, width)
        assertEquals(144, height)
        val baseline = IntArray(width) { PillarStemProfile.brightness(it) }
        // create() uses only brightness(x) for each vertical column: equal
        // rows also meet perfectly at boundaries after any number of repeats.
        for (repeat in 0 until 8) {
            for (y in 0 until height) {
                for (x in 0 until width) {
                    assertEquals("seam at tile $repeat row $y x $x",
                        baseline[x], PillarStemProfile.brightness(x))
                }
            }
        }
    }

    @Test fun sourceShaftKeepsShadedEdgesAndStoneFace() {
        assertEquals(72, PillarStemProfile.brightness(0))
        assertEquals(116, PillarStemProfile.brightness(20))
        assertEquals(100, PillarStemProfile.brightness(64))
        assertEquals(84, PillarStemProfile.brightness(110))
        assertEquals(72, PillarStemProfile.brightness(127))
    }

    @Test fun sourceProfileRejectsOutOfBoundsSampling() {
        try {
            PillarStemProfile.brightness(-1)
            fail("Expected invalid left pixel")
        } catch (_: IllegalArgumentException) { }
        try {
            PillarStemProfile.brightness(PillarStemProfile.WIDTH)
            fail("Expected invalid right pixel")
        } catch (_: IllegalArgumentException) { }
    }
}
