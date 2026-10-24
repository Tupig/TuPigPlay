package com.shilapi.xcertplay.hud

import org.junit.Assert.assertFalse
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import org.robolectric.annotation.Config

/** A generic Android tablet must never retry a TuPig-only service that is absent. */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [28], manifest = Config.NONE)
class TuPigHudBridgeTest {
    @Test fun noTuPigGatewayDoesNotStartPeriodicRetry() {
        val bridge = TuPigHudBridge
        bridge.initialize(RuntimeEnvironment.getApplication())

        val retryStarted = bridge.javaClass.getDeclaredField("senderStarted")
            .apply { isAccessible = true }
            .getBoolean(bridge)
        assertFalse("No TuPig gateway: retry loop must not start", retryStarted)
    }
}
