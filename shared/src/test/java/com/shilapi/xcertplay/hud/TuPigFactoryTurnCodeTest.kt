package com.shilapi.xcertplay.hud

import org.junit.Assert.*
import org.junit.Test

class TuPigFactoryTurnCodeTest {
    @Test fun factoryUsesDifferentCodesFromBroadcasts() {
        assertEquals(1, TuPigFactoryTurnCode.map(2, 0))
        assertEquals(2, TuPigFactoryTurnCode.map(3, 0))
        assertEquals(5, TuPigFactoryTurnCode.map(5, 0))
        assertEquals(11, TuPigFactoryTurnCode.map(9, 0))
        assertEquals(48, TuPigFactoryTurnCode.map(15, 0))
    }
    @Test fun roundaboutDirectionAndBounds() {
        assertEquals(25, TuPigFactoryTurnCode.map(11, 1))
        assertEquals(34, TuPigFactoryTurnCode.map(12, 10))
        assertEquals(35, TuPigFactoryTurnCode.map(17, 1))
        assertEquals(44, TuPigFactoryTurnCode.map(18, 10))
        assertEquals(13, TuPigFactoryTurnCode.map(11, 11))
        assertEquals(14, TuPigFactoryTurnCode.map(17, 0))
    }
    @Test fun invalidInstructionsAreNotMadeIntoStraightArrows() {
        assertNull(TuPigFactoryTurnCode.map(-1, 0))
        assertNull(TuPigFactoryTurnCode.map(0, 0))
        assertNull(TuPigFactoryTurnCode.map(29, 0))
    }
}
