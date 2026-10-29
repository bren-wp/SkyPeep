package com.brendigo.skypeep

import org.junit.Assert.*
import org.junit.Test

/** Regression coverage for low-FPS frames and TEMPEST contact sampling. */
class SkyPeepPhysicsStepTest {
    @Test fun usualSixtyHertzFramesUseShortSubsteps() {
        val dt = 1f / 60f
        val steps = SkyPeepPhysicsStep.substepCount(dt)
        assertEquals(2, steps)
        assertTrue(dt / steps <= SkyPeepPhysicsStep.MAX_STEP_SECONDS + 0.000001f)
    }

    @Test fun longAndroidFrameCannotSkipFortyMillisecondsOfContactChecks() {
        for (delta in listOf(.025f, .04f, .05f)) {
            val bounded = SkyPeepPhysicsStep.boundedDelta(delta)
            val count = SkyPeepPhysicsStep.substepCount(delta)
            assertTrue(count in 3..6)
            assertTrue(bounded / count <= SkyPeepPhysicsStep.MAX_STEP_SECONDS + 0.000001f)
            assertEquals(bounded, (bounded / count) * count, .000001f)
        }
    }

    @Test fun stalledOrInvalidRenderDeltaDoesNotRunUnboundedPhysics() {
        assertEquals(.05f, SkyPeepPhysicsStep.boundedDelta(2f), 0f)
        assertEquals(6, SkyPeepPhysicsStep.substepCount(2f))
        for (delta in listOf(0f, -1f, Float.NaN,
                Float.POSITIVE_INFINITY, Float.NEGATIVE_INFINITY)) {
            assertEquals(0f, SkyPeepPhysicsStep.boundedDelta(delta), 0f)
            assertEquals(0, SkyPeepPhysicsStep.substepCount(delta))
        }
    }

    @Test fun closePassChecksRemainShorterThanSmallestBodyRadius() {
        val stormSpeed = SkyRouteRules.speed(6, SkyDifficulty.TEMPEST)
        val maxStepTravel = stormSpeed * SkyPeepPhysicsStep.MAX_STEP_SECONDS
        assertTrue(maxStepTravel < SkyPeepContact.BODY_RADIUS_X / 10f)
        val terminalFall = 900f * SkyDifficulty.TEMPEST.gravityFactor
        assertTrue(terminalFall * SkyPeepPhysicsStep.MAX_STEP_SECONDS <
            SkyPeepContact.BODY_RADIUS_Y / 4f)
    }
    /** Integrates the *same* gravity, wind, terminal speed and 120Hz cap
     * used by SkyPeepGame. Does not change flight tuning. */
    private fun sampleOneSecond(
        world: Int, difficulty: SkyDifficulty, refreshRate: Int
    ): Pair<Float,Float> {
        var height=840f
        var speed=690f
        var physicsTime=0f
        repeat(refreshRate) {
            val frame=SkyPeepPhysicsStep.boundedDelta(1f/refreshRate)
            val count=SkyPeepPhysicsStep.substepCount(frame)
            val step=frame/count
            repeat(count) {
                val wind=SkyPeepWindClock.windAtStepMidpoint(
                    world,difficulty,physicsTime,step)
                physicsTime=SkyPeepWindClock.advance(physicsTime,step)
                speed=kotlin.math.max(-900f*difficulty.gravityFactor,
                    speed+(wind-SkyRouteRules.gravity(difficulty))*step)
                height+=speed*step
            }
        }
        return Pair(height,speed)
    }

    @Test fun allWorldFlightDynamicsStayStableFromThirtyTo144Hertz() {
        for(world in 1..6) for(difficulty in SkyDifficulty.entries) {
            val reference=sampleOneSecond(world,difficulty,120)
            for(rate in listOf(30,60,90,144)) {
                val result=sampleOneSecond(world,difficulty,rate)
                assertTrue("World $world $difficulty $rate Hz birdY drift",
                    kotlin.math.abs(result.first-reference.first)<9f)
                assertTrue("World $world $difficulty $rate Hz speed drift",
                    kotlin.math.abs(result.second-reference.second)<5f)
            }
        }
    }

    @Test fun eachSubstepIsBoundedAcrossCommonFrameRatesAndStalls() {
        for(rate in listOf(20,24,30,60,90,120,144,240)) {
            val seconds=SkyPeepPhysicsStep.boundedDelta(1f/rate)
            val count=SkyPeepPhysicsStep.substepCount(seconds)
            assertTrue(count in 1..6)
            assertTrue(seconds/count<=SkyPeepPhysicsStep.MAX_STEP_SECONDS+.000001f)
        }
        // Android returning from pause may report a huge frame delta;
        // a single stalled render must not advance one second of physics.
        assertEquals(.05f,SkyPeepPhysicsStep.boundedDelta(11f),.000001f)
        assertEquals(6,SkyPeepPhysicsStep.substepCount(11f))
    }

}
