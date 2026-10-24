package com.shilapi.xcertplay.hud

import android.content.Context
import java.util.concurrent.ExecutorService
import java.util.concurrent.TimeUnit
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [28], manifest = Config.NONE)
class TuPigCarMusicRhythmTest {
    private val context get() = RuntimeEnvironment.getApplication()
    private val direct = FakeCall()
    private val helper = FakeCall()

    @Before fun useFakes() {
        TuPigCarMusicRhythm.setPlaying(context, false)
        settle()
        TuPigCarMusicRhythm.direct = direct
        TuPigCarMusicRhythm.helper = helper
    }

    @After fun restore() {
        TuPigCarMusicRhythm.setPlaying(context, false)
        settle()
        TuPigCarMusicRhythm.direct = TuPigCarMusicRhythmDirect
        TuPigCarMusicRhythm.helper = TuPigCarMusicRhythmHelper
    }

    @Test fun aDirectStartNeedsNoAdbAndIsStoppedDirectly() {
        play(true)
        assertEquals(1, direct.starts)
        assertEquals(0, helper.starts)
        play(false)
        assertEquals(1, direct.stops)
        assertEquals(0, helper.stops)
    }

    @Test fun aBlockedDirectCallFallsBackToTheHelperWhichAlsoStopsIt() {
        direct.works = false
        play(true)
        assertEquals(1, direct.starts)
        assertEquals(1, helper.starts)
        play(false)
        assertEquals(0, direct.stops)
        assertEquals(1, helper.stops)
    }

    @Test fun eachSessionIsStoppedTheWayItStarted() {
        direct.works = false
        play(true)
        play(false)
        // The direct call works again: the next session starts and stops directly, the helper is not touched.
        direct.works = true
        play(true)
        play(false)
        assertEquals(2, direct.starts)
        assertEquals(1, direct.stops)
        assertEquals(1, helper.starts)
        assertEquals(1, helper.stops)
    }

    @Test fun supportIsCheckedDirectlyBeforeOverAdb() {
        assertTrue(TuPigCarMusicRhythm.checkSupport(context).get(2, TimeUnit.SECONDS))
        assertEquals(0, helper.checks)

        direct.works = false
        assertTrue(TuPigCarMusicRhythm.checkSupport(context).get(2, TimeUnit.SECONDS))
        assertEquals(1, helper.checks)

        helper.works = false
        assertFalse(TuPigCarMusicRhythm.checkSupport(context).get(2, TimeUnit.SECONDS))
        assertEquals(0, direct.starts + helper.starts)
    }

    @Test fun theTangStopTakesTheTokenAndTheHanStopTakesOnlyThePackage() {
        val token = android.os.Binder()
        val tang = FakeTangAudio()
        TuPigCarMusicRhythmTool.find(tang).apply { startRhythm(token); stopRhythm(token) }
        assertEquals(listOf("start com.byd.mediacenter", "stop com.byd.mediacenter token"), tang.calls)

        val han = FakeHanAudio()
        TuPigCarMusicRhythmTool.find(han).apply { startRhythm(token); stopRhythm(token) }
        assertEquals(listOf("start com.byd.mediacenter", "stop com.byd.mediacenter"), han.calls)
    }

    @Test fun aServiceWithoutBothCallsIsNotSupported() {
        assertFalse(runCatching { TuPigCarMusicRhythmTool.find(Any()) }.isSuccess)
        assertFalse(runCatching { TuPigCarMusicRhythmTool.find(StartOnlyAudio()) }.isSuccess)
    }

    class FakeTangAudio {
        val calls = mutableListOf<String>()
        fun startAudioOutput(pkg: String, callback: android.os.IBinder?) { calls += "start $pkg" }
        fun stopAudioOutput(pkg: String, callback: android.os.IBinder?) { calls += "stop $pkg token" }
    }

    class FakeHanAudio {
        val calls = mutableListOf<String>()
        fun startAudioOutput(pkg: String, callback: android.os.IBinder?) { calls += "start $pkg" }
        fun stopAudioOutput(pkg: String) { calls += "stop $pkg" }
    }

    class StartOnlyAudio {
        fun startAudioOutput(pkg: String, callback: android.os.IBinder?) = Unit
    }

    @Test fun bandsReachTheHelperOnlyWhileTheRhythmRuns() {
        val bands = IntArray(16) { 1 + it % 15 }
        TuPigCarMusicRhythm.sendBands(context, bands)
        settle()
        assertEquals(0, helper.bandSends)
        play(true)
        TuPigCarMusicRhythm.sendBands(context, bands)
        settle()
        assertEquals(1, helper.bandSends)
        assertTrue(bands.contentEquals(helper.lastBands))
        play(false)
        TuPigCarMusicRhythm.sendBands(context, bands)
        settle()
        assertEquals(1, helper.bandSends)
    }

    @Test fun invalidBandsAreNeverSent() {
        play(true)
        TuPigCarMusicRhythm.sendBands(context, IntArray(16) { 0 })
        TuPigCarMusicRhythm.sendBands(context, IntArray(16) { 16 })
        TuPigCarMusicRhythm.sendBands(context, IntArray(15) { 5 })
        settle()
        assertEquals(0, helper.bandSends)
    }

    @Test fun bandsAreParsedAsSixteenValuesFromOneToFifteen() {
        assertTrue(intArrayOf(1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 1)
            .contentEquals(TuPigCarMusicRhythmBands.parse("1 2 3 4 5 6 7 8 9 10 11 12 13 14 15 1")))
        assertEquals(null, TuPigCarMusicRhythmBands.parse("1 2 3"))
        assertEquals(null, TuPigCarMusicRhythmBands.parse("0 2 3 4 5 6 7 8 9 10 11 12 13 14 15 1"))
        assertEquals(null, TuPigCarMusicRhythmBands.parse("16 2 3 4 5 6 7 8 9 10 11 12 13 14 15 1"))
        assertEquals(null, TuPigCarMusicRhythmBands.parse("a 2 3 4 5 6 7 8 9 10 11 12 13 14 15 1"))
    }

    @Test fun bandFeatureIdsFollowTuPigsSixteenConsecutiveIds() {
        val ids = TuPigCarMusicRhythmBands.featureIds()
        assertEquals(16, ids.size)
        assertEquals(774897672, ids.first())
        assertEquals(774897732, ids.last())
    }

    private fun play(value: Boolean) {
        TuPigCarMusicRhythm.setPlaying(context, value)
        settle()
    }

    private fun settle() {
        val executor = TuPigCarMusicRhythm::class.java.getDeclaredField("executor").apply { isAccessible = true }
            .get(TuPigCarMusicRhythm) as ExecutorService
        executor.submit {}.get(2, TimeUnit.SECONDS)
    }

    private class FakeCall : TuPigCarMusicRhythmCall {
        var works = true
        var checks = 0
        var starts = 0
        var stops = 0
        var bandSends = 0
        var lastBands: IntArray? = null
        override fun supported(context: Context) = works.also { checks++ }
        override fun start(context: Context) = works.also { starts++ }
        override fun stop(context: Context) { stops++ }
        override fun sendBands(context: Context, bands: IntArray): Boolean {
            bandSends++
            lastBands = bands
            return true
        }
    }
}
