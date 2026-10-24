package com.shilapi.xcertplay.hud

import android.content.Context
import java.util.concurrent.CountDownLatch
import java.util.concurrent.Executors
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

/** Real store acceptance and real battery publication, with shell-read duration controlled. */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [29], manifest = Config.NONE)
class VehicleProbeBatteryConcurrencyTest {
    private val context get() = RuntimeEnvironment.getApplication()

    @Before fun reset() {
        context.getSharedPreferences("tupigcarplay_leapmotor_vehicle_fields", Context.MODE_PRIVATE).edit().clear().commit()
        context.getSharedPreferences("tupigcarplay_leapmotor_outputs", Context.MODE_PRIVATE).edit().clear().commit()
        TuPigVehicleFieldStore.clearMemoryForTests()
        TuPigBatteryStatus.read(context) { null }
    }

    @After fun cleanup() = reset()

    @Test fun savingAnAcceptedProbeDoesNotWaitForAnOlderBatteryRead() =
        acceptedProbeWhileReadPaused { _, candidate ->
            TuPigVehicleFieldStore.save(context, candidate)
            true
        }

    @Test fun automaticReplacementDoesNotWaitForAnOlderBatteryRead() =
        acceptedProbeWhileReadPaused { saved, candidate ->
            TuPigVehicleFieldStore.replaceAutomatically(context, saved, candidate).saved
        }

    @Test fun explicitReplacementDoesNotWaitForAnOlderBatteryRead() =
        acceptedProbeWhileReadPaused { saved, candidate ->
            TuPigVehicleFieldStore.replaceAnyway(context, saved, candidate)
        }

    @Test fun anAcceptedProbeInDefaultModeDoesNotPublishItsBatteryReading() {
        TuPigOutputSettings.setLegacyVehicleProbe(context, false)
        val saved = capabilities(25.0)
        TuPigVehicleFieldStore.save(context, saved)
        TuPigBatteryStatus.read(context) { reading(25.0) }
        val candidate = capabilities(90.0)

        assertTrue(TuPigVehicleFieldStore.replaceAutomatically(context, saved, candidate).saved)

        assertEquals(candidate, TuPigVehicleFieldStore.load(context))
        assertBattery(25.0)
    }

    @Test fun staleAutomaticAndExplicitCandidatesDoNotPublishTheirBatteryReading() {
        TuPigOutputSettings.setLegacyVehicleProbe(context, true)
        val expected = capabilities(25.0)
        TuPigVehicleFieldStore.save(context, expected)
        val newer = capabilities(60.0)
        TuPigVehicleFieldStore.save(context, newer)
        val candidate = capabilities(90.0)

        val automatic = TuPigVehicleFieldStore.replaceAutomatically(context, expected, candidate)
        assertFalse(automatic.saved)
        assertTrue(automatic.snapshotChanged)
        assertFalse(TuPigVehicleFieldStore.replaceAnyway(context, expected, candidate))
        assertEquals(newer, TuPigVehicleFieldStore.load(context))
        assertBattery(60.0)
    }

    private fun acceptedProbeWhileReadPaused(
        replace: (TuPigVehicleCapabilities, TuPigVehicleCapabilities) -> Boolean,
    ) {
        TuPigOutputSettings.setLegacyVehicleProbe(context, true)
        val saved = capabilities(25.0)
        TuPigVehicleFieldStore.save(context, saved)
        val candidate = capabilities(90.0)
        val readEntered = CountDownLatch(1)
        val releaseRead = CountDownLatch(1)
        val workers = Executors.newFixedThreadPool(2) { task ->
            Thread(task, "probe-battery-regression").apply { isDaemon = true }
        }
        try {
            val oldRead = workers.submit<TuPigBatteryReading?> {
                TuPigBatteryStatus.read(context) {
                    // Polling holds readLock and loads the store before doing slow shell I/O.
                    check(TuPigVehicleFieldStore.load(context) == saved)
                    readEntered.countDown()
                    check(releaseRead.await(5, TimeUnit.SECONDS)) { "controlled battery read was not released" }
                    reading(25.0)
                }
            }
            assertTrue("Battery reader must enter before the probe", readEntered.await(2, TimeUnit.SECONDS))
            val accepted = workers.submit<Boolean> { replace(saved, candidate) }

            // Previously the store monitor waited for readLock here. Releasing in finally also
            // lets that broken implementation fail within a bound without stranding workers.
            assertTrue("Accepted publication must complete while shell I/O is still paused",
                accepted.get(1, TimeUnit.SECONDS))
            assertEquals(candidate, TuPigVehicleFieldStore.load(context))
            assertBattery(90.0)

            releaseRead.countDown()
            assertEquals(25.0, oldRead.get(1, TimeUnit.SECONDS)!!.percent, 0.0)
            assertBattery(90.0) // The older read cannot overwrite the accepted probe.

            TuPigBatteryStatus.read(context) { reading(70.0) }
            assertBattery(70.0) // A fresh reader can still update normally.
        } finally {
            releaseRead.countDown()
            workers.shutdown()
            if (!workers.awaitTermination(2, TimeUnit.SECONDS)) {
                workers.shutdownNow()
                assertTrue("Controlled workers must terminate", workers.awaitTermination(2, TimeUnit.SECONDS))
            }
        }
    }

    private fun reading(percent: Double) = TuPigBatteryReading(percent, 150, 20.0, false)

    private fun assertBattery(percent: Double) =
        assertEquals(percent, TuPigBatteryStatus.snapshot()!!.batteryPercent, 0.0)

    private fun capabilities(percent: Double) = TuPigVehicleCapabilities(
        fields = TuPigVehicleField.entries.associateWith { field ->
            TuPigFieldProbeResult(field, true, TuPigVehicleFieldStore.defaultAddress(field), when (field) {
                TuPigVehicleField.SOC -> percent
                TuPigVehicleField.RANGE -> 150.0
                TuPigVehicleField.REMAINING_KWH -> 20.0
                else -> 1.0
            })
        }, catalogAvailable = true, firmwareKey = TuPigVehicleFieldStore.firmwareKey())
}
