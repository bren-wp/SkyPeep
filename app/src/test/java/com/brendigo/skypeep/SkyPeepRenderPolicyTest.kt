package com.brendigo.skypeep

import org.junit.Assert.*
import org.junit.Test

class SkyPeepRenderPolicyTest {
    @Test fun menusAndPausedScenesDoNotRequireContinuousGpuFrames() {
        for (name in arrayOf("SPLASH","MENU","MAP","SKINS","SHOP",
            "SETTINGS","PAUSE","RESULT")) {
            assertFalse(name,SkyPeepRenderPolicy.continuous(name,0f))
        }
    }

    @Test fun physicsAndAnimatedFlightStayLive() {
        assertTrue(SkyPeepRenderPolicy.continuous("PLAY",0f))
        assertTrue(SkyPeepRenderPolicy.continuous("PLAY",Float.NaN))
    }

    @Test fun temporaryMessageCanFadeWithoutFrozenUi() {
        assertTrue(SkyPeepRenderPolicy.continuous("MENU",2.3f))
        assertFalse(SkyPeepRenderPolicy.continuous("MENU",0f))
        assertFalse(SkyPeepRenderPolicy.continuous("MENU",Float.POSITIVE_INFINITY))
        assertFalse(SkyPeepRenderPolicy.continuous("PAUSE",-1f))
    }
}
