package com.shilapi.xcertplay.hud

import org.junit.Assert.*
import org.junit.Test

class TuPigAmbientLightPolicyTest {
    private val token = "0123456789abcdef0123456789abcdef"

    @Test fun protocolAcceptsOnlyWhitelistedCommandsAndBounds() {
        assertEquals("minimum-stop", TuPigAmbientLightWorkerProtocol.parse("$token minimum-stop", token)?.op)
        assertNull(TuPigAmbientLightWorkerProtocol.parse("$token minimum-stop 1", token))
        assertNull(TuPigAmbientLightWorkerProtocol.parse("$token off", token))
        assertEquals("begin", TuPigAmbientLightWorkerProtocol.parse("$token begin", token)?.op)
        assertEquals(listOf(1, 16, 6), TuPigAmbientLightWorkerProtocol.parse("$token apply 1 16 6", token)?.args)
        assertNull(TuPigAmbientLightWorkerProtocol.parse("$token shell id", token))
        assertNull(TuPigAmbientLightWorkerProtocol.parse("$token apply 0 16 6", token))
        assertNull(TuPigAmbientLightWorkerProtocol.parse("$token apply 1 32 6", token))
        assertNull(TuPigAmbientLightWorkerProtocol.parse("$token apply 1 16 0", token))
        assertNull(TuPigAmbientLightWorkerProtocol.parse("${token.dropLast(1)}x read", token))
        assertNull(protocolLineTooLong(token))
    }

    @Test fun theWorkersReplyIsItsLastLineAfterTheFirmwaresHotfixNote() {
        // As a 2024 Tang (DiLink 5) prints it before the worker's first reply.
        val hotfix = "HotFixRoot: /data/app/com.android.shell.aG90Zml -> patchMetaFile:/data/app/com.android.shell.aG90Zml/current.meta"
        assertEquals("ready", TuPigAmbientLightWorkerProtocol.reply("$hotfix\nready"))
        assertEquals("state=20,20,3,3,3", TuPigAmbientLightWorkerProtocol.reply("state=20,20,3,3,3"))
        assertEquals("ready", TuPigAmbientLightWorkerProtocol.reply("ready\r\n"))
        assertNull(TuPigAmbientLightWorkerProtocol.reply(null))
        assertNull(TuPigAmbientLightWorkerProtocol.reply(""))
    }

    @Test fun onlyLightingFeaturesAreWhitelisted() {
        assertEquals(9, TuPigAmbientLightPolicy.getNames.size)
        assertEquals(3, TuPigAmbientLightPolicy.setNames.size)
        assertFalse(TuPigAmbientLightPolicy.setNames.any { "SOURCE" in it })
        assertEquals(15_000L, TuPigAmbientLightPolicy.LEASE_MILLIS)
        assertEquals(50L, TuPigAmbientLightPolicy.READBACK_POLL_MILLIS)
    }

    @Test fun validatesSnapshotAndRequestedRawValues() {
        assertTrue(TuPigAmbientLightPolicy.validSnapshot(TuPigAmbientLightPolicy.Snapshot(16, 16, 6, 6, 3)))
        assertFalse(TuPigAmbientLightPolicy.validSnapshot(TuPigAmbientLightPolicy.Snapshot(0, 16, 6, 6, 3)))
        assertFalse(TuPigAmbientLightPolicy.validSnapshot(TuPigAmbientLightPolicy.Snapshot(16, 16, 0, 6, 3)))
        assertFalse(TuPigAmbientLightPolicy.validSnapshot(TuPigAmbientLightPolicy.Snapshot(16, 16, 6, 6, 4)))
        assertTrue(TuPigAmbientLightPolicy.validApply(2, 31, 1))
        assertFalse(TuPigAmbientLightPolicy.validApply(3, 32, 6))
    }

    @Test fun equalZoneSnapshotCanUseKnownAllBatchAndThenAreaOnly() {
        val s = TuPigAmbientLightPolicy.Snapshot(16, 16, 6, 6, 2)
        assertEquals(listOf(
            TuPigAmbientLightPolicy.RestoreStep(values = TuPigAmbientLightPolicy.Values(16, 6, 3)),
            TuPigAmbientLightPolicy.RestoreStep(areaOnly = 2),
        ), TuPigAmbientLightPolicy.restoreSteps(s))
    }

    @Test fun differentZoneSnapshotRestoresFrontThenBackThenAreaOnly() {
        val s = TuPigAmbientLightPolicy.Snapshot(7, 23, 2, 5, 3)
        assertEquals(listOf(
            TuPigAmbientLightPolicy.RestoreStep(values = TuPigAmbientLightPolicy.Values(7, 2, 1)),
            TuPigAmbientLightPolicy.RestoreStep(values = TuPigAmbientLightPolicy.Values(23, 5, 2)),
            TuPigAmbientLightPolicy.RestoreStep(areaOnly = 3),
        ), TuPigAmbientLightPolicy.restoreSteps(s))
    }

    @Test fun minimumKeepsCurrentColorsAndAreaInsteadOfRestoringSavedBrightness() {
        val current = TuPigAmbientLightPolicy.Snapshot(7, 23, 2, 5, 2)
        val target = TuPigAmbientLightPolicy.Snapshot(7, 23, 1, 1, 2)
        assertEquals(target, TuPigAmbientLightPolicy.minimumTarget(current))
        assertEquals(listOf(
            TuPigAmbientLightPolicy.RestoreStep(values = TuPigAmbientLightPolicy.Values(7, 1, 1)),
            TuPigAmbientLightPolicy.RestoreStep(values = TuPigAmbientLightPolicy.Values(23, 1, 2)),
            TuPigAmbientLightPolicy.RestoreStep(areaOnly = 2),
        ), TuPigAmbientLightPolicy.minimumSteps(current))
        assertEquals(TuPigAmbientLightPolicy.Snapshot(7, 23, 2, 5, 2), current)
    }

    @Test fun equalCurrentColorsStillUseTwoMinimumZoneWritesAndReturnToTheArea() {
        val current = TuPigAmbientLightPolicy.Snapshot(16, 16, 6, 6, 3)
        assertEquals(listOf(
            TuPigAmbientLightPolicy.RestoreStep(values = TuPigAmbientLightPolicy.Values(16, 1, 1)),
            TuPigAmbientLightPolicy.RestoreStep(values = TuPigAmbientLightPolicy.Values(16, 1, 2)),
            TuPigAmbientLightPolicy.RestoreStep(areaOnly = 3),
        ), TuPigAmbientLightPolicy.minimumSteps(current))
        val target = TuPigAmbientLightPolicy.minimumTarget(current)
        assertEquals(target, TuPigAmbientLightPolicy.minimumTarget(target))
        assertNotEquals(TuPigAmbientLightPolicy.restoreSteps(current), TuPigAmbientLightPolicy.minimumSteps(current))
    }

    @Test(expected = IllegalArgumentException::class)
    fun minimumCannotBypassSnapshotValidationWithUnsupportedZeroColor() {
        TuPigAmbientLightPolicy.minimumSteps(TuPigAmbientLightPolicy.Snapshot(0, 16, 6, 6, 3))
    }

    @Test fun seededBeginDoesNotRecaptureCurrentAppColorsAndOldBeginRemainsCompatible() {
        val seed = TuPigAmbientLightPolicy.Snapshot(7, 23, 2, 5, 2)
        val request = requireNotNull(TuPigAmbientLightWorkerProtocol.parse("$token begin 7 23 2 5 2", token))
        assertEquals(seed, TuPigAmbientLightWorkerProtocol.snapshotForBegin(request) { error("Must not read app-controlled current colors") })
        val old = requireNotNull(TuPigAmbientLightWorkerProtocol.parse("$token begin", token))
        assertEquals(seed, TuPigAmbientLightWorkerProtocol.snapshotForBegin(old) { seed })
        assertNull(TuPigAmbientLightWorkerProtocol.parse("$token begin 7 23 0 5 2", token))
        assertNull(TuPigAmbientLightWorkerProtocol.parse("$token begin 7 23 2 5 4", token))
        assertNull(TuPigAmbientLightWorkerProtocol.parse("$token begin 7 23 2 5", token))
        assertNull(TuPigAmbientLightWorkerProtocol.parse("${token.dropLast(1)}x begin 7 23 2 5 2", token))
    }

    private fun protocolLineTooLong(token: String) = TuPigAmbientLightWorkerProtocol.parse("$token ${"x".repeat(600)}", token)
}
