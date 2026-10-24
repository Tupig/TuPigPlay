package com.shilapi.xcertplay.hud

import org.junit.After
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import org.robolectric.annotation.Config
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit
import kotlin.concurrent.thread

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [29], manifest = Config.NONE)
class TuPigClusterMapPauseTest {
    private val context get() = RuntimeEnvironment.getApplication()
    private val release = CountDownLatch(1)

    @After
    fun tearDown() {
        release.countDown()
        TuPigClusterMapPause.streamControl = null
        TuPigClusterMapPause.clusterMapShown = false
        TuPigClusterMapPause.onNaviMode = null
    }

    @Test
    fun disablingPauseResumesMapWhileLayoutFollowingRemainsEnabled() {
        TuPigClusterMapPause.readMode = { TuPigClusterNaviMode.OFF }
        TuPigClusterMapPause.onNaviMode = {}
        TuPigClusterMapPause.clusterMapShown = true
        var streaming = true
        TuPigClusterMapPause.streamControl = { streaming = it }
        TuPigClusterMapPause::class.java.getDeclaredField("context")
            .apply { isAccessible = true }.set(null, context)
        val tick = TuPigClusterMapPause::class.java.getDeclaredMethod("tick").apply { isAccessible = true }
        TuPigOutputSettings.setClusterStreamPause(context, true)
        tick.invoke(TuPigClusterMapPause)
        org.junit.Assert.assertFalse(streaming)
        TuPigOutputSettings.setClusterStreamPause(context, false)
        tick.invoke(TuPigClusterMapPause)
        assertTrue(streaming)
    }

    @Test
    fun initializationDoesNotWaitForAPendingAdbRead() {
        val reading = CountDownLatch(1)
        // An adbd that accepted the read and has not answered yet.
        TuPigClusterMapPause.readMode = {
            reading.countDown()
            release.await()
            null
        }
        TuPigOutputSettings.setClusterStreamPause(context, true)
        TuPigClusterMapPause.clusterMapShown = true
        TuPigClusterMapPause.streamControl = {}
        TuPigClusterMapPause.initialize(context)
        assertTrue("the ticker should start reading", reading.await(5, TimeUnit.SECONDS))

        val initialized = CountDownLatch(1)
        thread { TuPigClusterMapPause.initialize(context); initialized.countDown() }

        assertTrue("initialize() waited for the adb read", initialized.await(1, TimeUnit.SECONDS))
    }
}
