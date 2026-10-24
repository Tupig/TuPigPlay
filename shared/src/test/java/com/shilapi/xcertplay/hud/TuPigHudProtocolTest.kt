package com.shilapi.xcertplay.hud

import org.junit.Assert.*
import org.junit.Test

class TuPigHudProtocolTest {
    @Test fun onlyStartAcceptsAlreadyStarted() {
        assertTrue(TuPigHudProtocol.serviceStarted(0))
        assertTrue(TuPigHudProtocol.serviceStarted(13))
        assertTrue(TuPigHudProtocol.eventAccepted(0))
        assertFalse(TuPigHudProtocol.eventAccepted(13))
    }
    @Test fun positiveGatewayErrorsAreNotSuccess() {
        for (code in listOf(-1, 1, 2, 3, 4, 5, 6, 8, 9, 10, 11, 12, 15)) {
            assertFalse("start=$code", TuPigHudProtocol.serviceStarted(code))
            assertFalse("event=$code", TuPigHudProtocol.eventAccepted(code))
        }
    }

    @Test fun aRefusedGatewayBindBacksOffToAMinute() {
        assertEquals(0L, TuPigHudProtocol.bindRetryMillis(0))
        assertEquals(300L, TuPigHudProtocol.bindRetryMillis(1))
        assertEquals(600L, TuPigHudProtocol.bindRetryMillis(2))
        assertEquals(38_400L, TuPigHudProtocol.bindRetryMillis(8))
        assertEquals(60_000L, TuPigHudProtocol.bindRetryMillis(9))
        assertEquals(60_000L, TuPigHudProtocol.bindRetryMillis(1_000))
    }
}
