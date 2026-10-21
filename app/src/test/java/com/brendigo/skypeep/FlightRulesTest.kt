package com.brendigo.skypeep
import org.junit.Assert.*
import org.junit.Test
class FlightRulesTest {
    @Test fun worldGapsRemainPhysicallyBoundedWithoutChangingFlightMechanics() {
        assertEquals(585f,FlightRules.gap(1),.001f)
        assertEquals(395f,FlightRules.gap(6),.001f)
        assertEquals(585f,FlightRules.gap(0),.001f)
        assertEquals(395f,FlightRules.gap(100),.001f)
    }
}
