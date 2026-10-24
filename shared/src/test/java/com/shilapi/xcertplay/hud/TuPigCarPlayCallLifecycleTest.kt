package com.shilapi.xcertplay.hud

import android.content.Context
import com.shilapi.xcertplay.iap2.message.Iap2Messages
import java.util.concurrent.CopyOnWriteArrayList
import java.util.concurrent.CountDownLatch
import java.util.concurrent.ExecutorService
import java.util.concurrent.TimeUnit
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import org.robolectric.annotation.Config
import org.robolectric.annotation.Implementation
import org.robolectric.annotation.Implements
import org.robolectric.util.ReflectionHelpers

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [29], manifest = Config.NONE, shadows = [TuPigCarPlayCallLifecycleTest.Shell::class])
class TuPigCarPlayCallLifecycleTest {
    private val app get() = RuntimeEnvironment.getApplication()
    private val writer get() = ReflectionHelpers.getField<ExecutorService>(TuPigCarPlayCall, "writer")
    private fun drain() { writer.submit {}.get(5, TimeUnit.SECONDS) }
    private fun ringing(id: String) = Iap2Messages.buildRaw(CarPlayCallState.CALL_STATE_UPDATE) {
        u8(2, 2); string(4, id)
    }

    @Before fun setup() {
        TuPigCarPlayCall.watcherReadyMillis = 500L
        drain()
        ReflectionHelpers.getField<CarPlayCallState>(TuPigCarPlayCall, "state").clear()
        ReflectionHelpers.setField(TuPigCarPlayCall, "shown", null)
        ReflectionHelpers.setField(TuPigCarPlayCall, "watcherToken", null)
        ReflectionHelpers.setField(TuPigCarPlayCall, "watcherRunning", false)
        ReflectionHelpers.setField(TuPigCarPlayCall, "cleanupPending", false)
        ReflectionHelpers.setField(TuPigCarPlayCall, "prepared", false)
        ReflectionHelpers.setField(TuPigCarPlayCall, "sessionActive", false)
        TuPigCarPlayCall.attach(app)
        TuPigOutputSettings.setCarPlayCalls(app, false)
        Shell.commands.clear()
        Shell.rejectPhase = null
        Shell.endFailures = 0
        Shell.omitCompletion = false
        Shell.rejectWatcher = false
        Shell.omitReadiness = false
        Shell.omitCancelCompletion = false
        Shell.emptyWatcherReply = false
    }

    @After fun cleanup() {
        Shell.endFailures = 0
        Shell.omitCompletion = false
        Shell.omitCancelCompletion = false
        TuPigCarPlayCall.end(); drain()
    }

    @Test fun defaultOffTracksCallWithoutAnyShellOrWatcherMutation() {
        TuPigCarPlayCall.onFrame(ringing("a")); drain()
        assertNotNull(TuPigCarPlayCall.current())
        assertTrue(Shell.commands.isEmpty())
    }

    @Test fun backToBackCallsUseDifferentTokensForWritesWatcherAndCleanup() {
        TuPigOutputSettings.setCarPlayCalls(app, true)
        TuPigCarPlayCall.onFrame(ringing("a")); drain()
        val first = tokenFrom(Shell.commands.first { it.contains(" ringing ") })
        assertTrue(Shell.commands.any { it.contains(" watch $first ${app.packageName} ") })
        TuPigCarPlayCall.end(); drain()
        assertTrue(Shell.commands.any { it.endsWith(" end - $first") })
        TuPigCarPlayCall.onFrame(ringing("b")); drain()
        val next = tokenFrom(Shell.commands.last { it.contains(" ringing ") })
        assertNotEquals(first, next)
        assertTrue(Shell.commands.any { it.contains(" watch $next ${app.packageName} ") })
        assertTrue(Shell.commands.none { it.contains("rm -f") })
    }

    @Test fun disablingBeforeQueuedUpdateExecutesPreventsLateCallWrites() {
        val entered = CountDownLatch(1)
        val release = CountDownLatch(1)
        writer.execute { entered.countDown(); release.await(5, TimeUnit.SECONDS) }
        assertTrue(entered.await(5, TimeUnit.SECONDS))
        try {
            TuPigOutputSettings.setCarPlayCalls(app, true)
            TuPigCarPlayCall.onFrame(ringing("a"))
            TuPigOutputSettings.setCarPlayCalls(app, false)
            TuPigCarPlayCall.settingChanged(false)
        } finally { release.countDown() }
        drain()
        assertTrue(Shell.commands.isEmpty())
    }

    @Test fun failedFirstShowRetainsCleanupOwnershipAndStartsTheWatcher() {
        TuPigOutputSettings.setCarPlayCalls(app, true)
        Shell.rejectPhase = "ringing"
        TuPigCarPlayCall.onFrame(ringing("a")); drain()
        val token = tokenFrom(Shell.commands.first { it.contains(" ringing ") })
        assertEquals(token, ReflectionHelpers.getField<String?>(TuPigCarPlayCall, "watcherToken"))
        assertNull(ReflectionHelpers.getField<Any?>(TuPigCarPlayCall, "shown"))
        assertTrue(Shell.commands.any { it.contains(" watch $token ${app.packageName} ") })
        TuPigCarPlayCall.end(); drain()
        assertTrue(Shell.commands.any { it.endsWith(" end - $token") })
    }

    @Test fun failedCleanupRemainsRetryableEvenWhenNoCallWasMarkedShown() {
        TuPigOutputSettings.setCarPlayCalls(app, true)
        Shell.rejectPhase = "ringing"
        TuPigCarPlayCall.onFrame(ringing("a")); drain()
        val token = tokenFrom(Shell.commands.first { it.contains(" ringing ") })
        Shell.endFailures = 1
        TuPigCarPlayCall.end(); drain()
        assertEquals(token, ReflectionHelpers.getField<String?>(TuPigCarPlayCall, "watcherToken"))
        TuPigOutputSettings.setCarPlayCalls(app, false)
        TuPigCarPlayCall.settingChanged(false); drain()
        assertEquals(2, Shell.commands.count { it.endsWith(" end - $token") })
        assertNull(ReflectionHelpers.getField<String?>(TuPigCarPlayCall, "watcherToken"))
    }

    @Test fun failedActiveUpdateMustCleanUpBeforeShowingAnotherCall() {
        TuPigOutputSettings.setCarPlayCalls(app, true)
        TuPigCarPlayCall.onFrame(ringing("a")); drain()
        val oldToken = tokenFrom(Shell.commands.first { it.contains(" ringing ") })
        Shell.rejectPhase = "active"
        TuPigCarPlayCall.onFrame(Iap2Messages.buildRaw(CarPlayCallState.CALL_STATE_UPDATE) {
            u8(2, 4); string(4, "a")
        }); drain()
        Shell.rejectPhase = null
        TuPigCarPlayCall.onFrame(ringing("b")); drain()
        val end = Shell.commands.indexOfFirst { it.endsWith(" end - $oldToken") }
        val next = Shell.commands.indexOfLast { it.contains(" ringing ") }
        assertTrue(end >= 0 && end < next)
        assertNotEquals(oldToken, tokenFrom(Shell.commands[next]))
    }

    @Test fun missingCompletionAckCannotMarkAnUnknownWriteAsShown() {
        TuPigOutputSettings.setCarPlayCalls(app, true)
        Shell.omitCompletion = true
        TuPigCarPlayCall.onFrame(ringing("a")); drain()
        assertNull(ReflectionHelpers.getField<Any?>(TuPigCarPlayCall, "shown"))
        val token = tokenFrom(Shell.commands.first { it.contains(" ringing ") })
        assertEquals(token, ReflectionHelpers.getField<String?>(TuPigCarPlayCall, "watcherToken"))
        Shell.omitCompletion = false
        TuPigCarPlayCall.end(); drain()
        assertTrue(Shell.commands.any { it.endsWith(" end - $token") })
    }

    @Test fun unknownShowAndCleanupRepliesBlockFurtherCallMutationUntilCleanupIsConfirmed() {
        TuPigOutputSettings.setCarPlayCalls(app, true)
        Shell.omitCompletion = true
        TuPigCarPlayCall.onFrame(ringing("a")); drain()
        val token = tokenFrom(Shell.commands.first { it.contains(" ringing ") })
        TuPigCarPlayCall.onFrame(Iap2Messages.buildRaw(CarPlayCallState.CALL_STATE_UPDATE) {
            u8(2, 4); string(4, "a")
        }); drain()
        assertTrue(Shell.commands.any { it.endsWith(" end - $token") })
        assertTrue(Shell.commands.none { it.contains(" active ") })
        assertNull(ReflectionHelpers.getField<Any?>(TuPigCarPlayCall, "shown"))
        Shell.omitCompletion = false
        TuPigCarPlayCall.settingChanged(true); drain()
        assertTrue(Shell.commands.any { it.contains(" active ") })
        assertNotNull(ReflectionHelpers.getField<Any?>(TuPigCarPlayCall, "shown"))
    }

    @Test fun refusedWatcherLaunchCannotBeMarkedRunningOrWriteCallFields() {
        TuPigOutputSettings.setCarPlayCalls(app, true)
        Shell.rejectWatcher = true
        TuPigCarPlayCall.onFrame(ringing("unprotected")); drain()
        assertFalse(ReflectionHelpers.getField<Boolean>(TuPigCarPlayCall, "watcherRunning"))
        assertTrue(Shell.commands.none { it.contains(" ringing ") })
    }

    @Test fun aMissingChildReadinessRecordCannotPermitVehicleMutation() {
        TuPigOutputSettings.setCarPlayCalls(app, true)
        Shell.omitReadiness = true
        TuPigCarPlayCall.onFrame(ringing("not-ready")); drain()
        assertTrue(Shell.commands.none { it.contains(" ringing ") })
    }

    @Test fun anEmptyBackgroundLaunchReplyStillNeedsTheChildReadinessRecord() {
        TuPigOutputSettings.setCarPlayCalls(app, true)
        Shell.emptyWatcherReply = true
        Shell.omitReadiness = true
        TuPigCarPlayCall.onFrame(ringing("empty-launch")); drain()
        assertFalse(ReflectionHelpers.getField<Boolean>(TuPigCarPlayCall, "watcherRunning"))
        assertTrue(Shell.commands.none { it.contains(" ringing ") })
        assertTrue(Shell.commands.any { it.contains(" cancel - ") })
    }

    @Test fun vehicleWritesFollowPreparationAndInitializedChildAcknowledgement() {
        TuPigOutputSettings.setCarPlayCalls(app, true)
        TuPigCarPlayCall.onFrame(ringing("ordered")); drain()
        val prepare = Shell.commands.indexOfFirst { it.contains(" prepare - ") }
        val launch = Shell.commands.indexOfFirst { it.startsWith("setsid nohup ") }
        val probe = Shell.commands.indexOfFirst { it.contains(" probe - ") }
        val show = Shell.commands.indexOfFirst { it.contains(" ringing ") }
        assertTrue(prepare >= 0 && prepare < launch && launch < probe && probe < show)
        val pid = android.os.Process.myPid().toString()
        assertTrue(Shell.commands[show].endsWith(" $pid"))
    }

    @Test fun failedWatcherHasOneRealWriterRetryEvenWhenTheCallFrameNeverChanges() {
        TuPigOutputSettings.setCarPlayCalls(app, true)
        Shell.rejectWatcher = true
        TuPigCarPlayCall.onFrame(ringing("unchanged")); drain()
        assertTrue(Shell.commands.none { it.contains(" ringing ") })
        Shell.rejectWatcher = false
        awaitRetry()
        assertEquals(1, Shell.commands.count { it.contains(" ringing ") })
        assertNotNull(ReflectionHelpers.getField<Any?>(TuPigCarPlayCall, "shown"))
    }

    @Test fun missingPristineCancelAcknowledgementRetainsItsTokenUntilConfirmedRetry() {
        TuPigOutputSettings.setCarPlayCalls(app, true)
        Shell.rejectWatcher = true
        Shell.omitCancelCompletion = true
        TuPigCarPlayCall.onFrame(ringing("cancel-pending")); drain()
        val original = ReflectionHelpers.getField<String?>(TuPigCarPlayCall, "watcherToken")!!
        assertTrue(ReflectionHelpers.getField<Boolean>(TuPigCarPlayCall, "prepared"))
        Shell.omitCancelCompletion = false
        Shell.rejectWatcher = false
        awaitRetry()
        val firstCancel = Shell.commands.indexOfFirst { it.endsWith(" cancel - $original") }
        val retryCancel = Shell.commands.indexOfLast { it.endsWith(" cancel - $original") }
        val newPrepare = Shell.commands.indexOfLast { it.contains(" prepare - ") }
        assertTrue(firstCancel >= 0 && retryCancel > firstCancel && newPrepare > retryCancel)
        assertTrue(Shell.commands.none { it.endsWith(" end - $original") })
        assertEquals(1, Shell.commands.count { it.contains(" ringing ") })
    }

    @Test fun disablingTheOptionCancelsTheUnchangedCallRetryWithoutHardwareWrites() {
        TuPigOutputSettings.setCarPlayCalls(app, true)
        Shell.rejectWatcher = true
        TuPigCarPlayCall.onFrame(ringing("disabled")); drain()
        TuPigOutputSettings.setCarPlayCalls(app, false)
        TuPigCarPlayCall.settingChanged(false); drain()
        Shell.rejectWatcher = false
        Thread.sleep(1200); drain()
        assertTrue(Shell.commands.none { it.contains(" ringing ") || it.contains(" end - ") })
    }

    @Test fun refusedEndGetsOnlyOneAutomaticRetryUntilAnExplicitNewCleanupEvent() {
        TuPigOutputSettings.setCarPlayCalls(app, true)
        TuPigCarPlayCall.onFrame(ringing("end-retry")); drain()
        val path = ReflectionHelpers.getField<String?>(TuPigCarPlayCall, "watcherToken")!!
        Shell.endFailures = 10
        TuPigCarPlayCall.end(); drain()
        awaitRetry()
        Thread.sleep(1200); drain()
        assertEquals(2, Shell.commands.count { it.endsWith(" end - $path") })
        Shell.endFailures = 0
        TuPigCarPlayCall.end(); drain()
        assertEquals(3, Shell.commands.count { it.endsWith(" end - $path") })
        assertNull(ReflectionHelpers.getField<String?>(TuPigCarPlayCall, "watcherToken"))
    }

    @Test fun sessionStartArmsTheWatcherSoTheFirstCallSkipsItsLaunch() {
        TuPigOutputSettings.setCarPlayCalls(app, true)
        TuPigCarPlayCall.sessionStarted(); drain()
        val armed = ReflectionHelpers.getField<String?>(TuPigCarPlayCall, "watcherToken")!!
        assertTrue(Shell.commands.any { it.contains(" watch $armed ${app.packageName} ") })
        assertTrue(Shell.commands.none { it.contains(" ringing ") || it.contains(" end - ") || it.contains(" cancel - ") })
        TuPigCarPlayCall.onFrame(ringing("armed")); drain()
        assertEquals(armed, tokenFrom(Shell.commands.single { it.contains(" ringing ") }))
        assertEquals(1, Shell.commands.count { it.startsWith("setsid nohup ") })
    }

    @Test fun endingASessionWithAnArmedWatcherOnlyCancelsIt() {
        TuPigOutputSettings.setCarPlayCalls(app, true)
        TuPigCarPlayCall.sessionStarted(); drain()
        val armed = ReflectionHelpers.getField<String?>(TuPigCarPlayCall, "watcherToken")!!
        TuPigCarPlayCall.end(); drain()
        assertTrue(Shell.commands.any { it.endsWith(" cancel - $armed") })
        assertTrue(Shell.commands.none { it.contains(" end - ") })
        assertNull(ReflectionHelpers.getField<String?>(TuPigCarPlayCall, "watcherToken"))
    }

    @Test fun turningTheOptionOffCancelsAnArmedWatcherAndOnArmsAgain() {
        TuPigOutputSettings.setCarPlayCalls(app, true)
        TuPigCarPlayCall.sessionStarted(); drain()
        val armed = ReflectionHelpers.getField<String?>(TuPigCarPlayCall, "watcherToken")!!
        TuPigOutputSettings.setCarPlayCalls(app, false)
        TuPigCarPlayCall.settingChanged(false); drain()
        assertTrue(Shell.commands.any { it.endsWith(" cancel - $armed") })
        assertNull(ReflectionHelpers.getField<String?>(TuPigCarPlayCall, "watcherToken"))
        TuPigOutputSettings.setCarPlayCalls(app, true)
        TuPigCarPlayCall.settingChanged(true); drain()
        assertNotEquals(armed, ReflectionHelpers.getField<String?>(TuPigCarPlayCall, "watcherToken"))
        assertTrue(Shell.commands.none { it.contains(" end - ") })
    }

    @Test fun anEndedCallRearmsWithAFreshToken() {
        TuPigOutputSettings.setCarPlayCalls(app, true)
        TuPigCarPlayCall.sessionStarted(); drain()
        TuPigCarPlayCall.onFrame(ringing("first")); drain()
        val first = tokenFrom(Shell.commands.single { it.contains(" ringing ") })
        TuPigCarPlayCall.onFrame(Iap2Messages.buildRaw(CarPlayCallState.CALL_STATE_UPDATE) {
            u8(2, 0); string(4, "first")
        }); drain()
        assertTrue(Shell.commands.any { it.endsWith(" end - $first") })
        val next = ReflectionHelpers.getField<String?>(TuPigCarPlayCall, "watcherToken")!!
        assertNotEquals(first, next)
        assertTrue(ReflectionHelpers.getField<Boolean>(TuPigCarPlayCall, "prepared"))
        assertTrue(Shell.commands.any { it.contains(" watch $next ${app.packageName} ") })
    }

    @Test fun noSessionMeansNoArmedWatcher() {
        TuPigOutputSettings.setCarPlayCalls(app, true)
        TuPigCarPlayCall.settingChanged(true); drain()
        assertTrue(Shell.commands.isEmpty())
    }

    private fun awaitRetry() {
        val future = ReflectionHelpers.getField<java.util.concurrent.ScheduledFuture<*>?>(TuPigCarPlayCall, "retry")
            ?: error("Expected a bounded retry")
        future.get(5, TimeUnit.SECONDS)
        drain()
    }

    private fun tokenFrom(command: String): String =
        Regex("/data/local/tmp/tupigcarplay-carplay-call-[A-Za-z0-9_.]+-[0-9a-f-]{36}")
            .find(command)!!.value

    @Implements(TuPigAdbShell::class, isInAndroidSdk = false)
    class Shell {
        @Implementation fun run(context: Context, command: String): String? {
            commands.add(command)
            if (command.startsWith("setsid nohup ") && rejectWatcher) return "nohup: watcher launch refused"
            if (command.startsWith("setsid nohup ") && emptyWatcherReply) return ""
            if (command.contains(" cancel - ") && omitCancelCompletion) return ""
            if (command.contains(" probe ")) return if (rejectWatcher || omitReadiness) "watch=ERR" else "watch=0"
            if (rejectPhase?.let { command.contains(" $it ") } == true) return "write=ERR call write refused"
            if (command.contains(" end - ") && endFailures > 0) {
                endFailures--
                return "write=ERR cleanup refused"
            }
            val phaseOrEnd = command.contains(" end - ") ||
                listOf("ringing", "dialing", "active").any { command.contains(" $it ") }
            return if (omitCompletion && phaseOrEnd) "state=0" else "state=0\nwrite=0"
        }
        companion object {
            val commands = CopyOnWriteArrayList<String>()
            @Volatile var rejectPhase: String? = null
            @Volatile var endFailures = 0
            @Volatile var omitCompletion = false
            @Volatile var rejectWatcher = false
            @Volatile var omitReadiness = false
            @Volatile var omitCancelCompletion = false
            @Volatile var emptyWatcherReply = false
        }
    }
}
