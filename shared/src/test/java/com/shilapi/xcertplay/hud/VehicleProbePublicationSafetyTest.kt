package com.shilapi.xcertplay.hud

import android.content.Context
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [29], manifest = Config.NONE)
class VehicleProbePublicationSafetyTest {
    private val context get() = RuntimeEnvironment.getApplication()

    @Before fun reset() {
        context.getSharedPreferences("tupigcarplay_leapmotor_vehicle_fields", Context.MODE_PRIVATE).edit().clear().commit()
        context.getSharedPreferences("tupigcarplay_leapmotor_outputs", Context.MODE_PRIVATE).edit().clear().commit()
        TuPigVehicleFieldStore.clearMemoryForTests()
        TuPigBatteryStatus.read(context) { null }
    }

    @After fun cleanup() = reset()

    @Test fun candidateHeldForLosingGearMustNotReplaceThePublishedBattery() {
        val saved = capabilities(25.0, hasGear = true)
        TuPigVehicleFieldStore.save(context, saved)
        TuPigBatteryStatus.read(context) { TuPigBatteryReading(25.0, 150, null, false) }
        val candidate = capabilities(90.0, hasGear = false)

        TuPigVehicleCapabilityProbe.finishProbe(context, TuPigAdbAccess.State.READY,
            TuPigVehicleCapabilityProbe.ProbeBatch(candidate, 6, 6, 0), persist = false)
        val result = TuPigVehicleFieldStore.replaceAutomatically(context, saved, candidate)

        assertFalse(result.saved)
        assertEquals(saved, TuPigVehicleFieldStore.load(context))
        assertEquals(25.0, TuPigBatteryStatus.snapshot()!!.batteryPercent, 0.0)
    }

    private fun capabilities(percent: Double, hasGear: Boolean): TuPigVehicleCapabilities = TuPigVehicleCapabilities(
        fields = TuPigVehicleField.entries.associateWith { field ->
            val supported = field != TuPigVehicleField.GEAR || hasGear
            TuPigFieldProbeResult(field, supported,
                if (supported) TuPigVehicleFieldStore.defaultAddress(field) else null,
                when (field) {
                    TuPigVehicleField.SOC -> percent
                    TuPigVehicleField.RANGE -> 150.0
                    TuPigVehicleField.REMAINING_KWH -> 20.0
                    else -> 1.0
                })
        }, catalogAvailable = true, firmwareKey = TuPigVehicleFieldStore.firmwareKey())
}
