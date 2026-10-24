package com.shilapi.xcertplay.hud

import com.shilapi.xcertplay.transport.Iap2IdentificationConfig
import com.shilapi.xcertplay.transport.VehicleGear
import com.shilapi.xcertplay.transport.withVehicleStatusFrom
import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import org.robolectric.annotation.Config
import org.robolectric.shadows.ShadowSystemClock
import java.time.Duration

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [29], manifest = Config.NONE)
class TuPigBatteryReadinessTest {
    private val context get() = RuntimeEnvironment.getApplication()
    private val reading = TuPigBatteryReading(25.0, 150, 25.1, false, TuPigBatteryProtocol.CANFD)
    private val identification = Iap2IdentificationConfig(
        name = "test", modelIdentifier = "test", manufacturer = "test", serialNumber = "test",
        firmwareVersion = "1", hardwareVersion = "1", carPlayUsbInterfaceNumber = 3,
        vehicleStatusEnabled = true,
    )

    @Test
    fun delayedAdbApprovalPrimesTheNextConnectionFromTheSettingsCheck() {
        val unavailable = TuPigAdbAccess.readStatus(context) { null }
        assertNull(unavailable.batteryPercent)
        assertNull(unavailable.speedKmh)
        assertNull(unavailable.gear)
        assertFalse(identification.withVehicleStatusFrom(TuPigBatteryStatus).vehicleStatusEnabled)

        // A successful settings check after approval must prime the provider, not just its label.
        val ready = TuPigAdbAccess.readStatus(context) { command ->
            if (command == "getprop ro.car.protocol") return@readStatus "CANFD"
            val bits = when (command.substringAfterLast(' ')) {
                "1246777400" -> "41c80000"
                "1246765118" -> "00000096"
                "882901008" -> "41c8cccd"
                "876609560" -> "0000000f"
                "-1807745016" -> "42100000"
                "555745336" -> "00000001"
                else -> "00000003"
            }
            "Result: Parcel(00000000 $bits   '........')"
        }
        assertEquals(25.0, ready.batteryPercent!!, 0.001)
        assertEquals(36.0, ready.speedKmh!!, 0.001)
        assertEquals(VehicleGear.PARK, ready.gear)
        assertTrue(identification.withVehicleStatusFrom(TuPigBatteryStatus).vehicleStatusEnabled)
        assertEquals(150, TuPigBatteryStatus.snapshot()!!.rangeKm)
        reconnectAfterIdleRefreshesWithoutWaitingForThePeriodicTick()
    }

    private fun reconnectAfterIdleRefreshesWithoutWaitingForThePeriodicTick() {
        TuPigBatteryStatus.readBattery = { reading }
        ShadowSystemClock.advanceBy(Duration.ofMinutes(4))
        TuPigBatteryStatus.start(context)
        awaitReading()
        ShadowSystemClock.advanceBy(Duration.ofMinutes(4))
        assertNull(TuPigBatteryStatus.snapshot())
        assertFalse(identification.withVehicleStatusFrom(TuPigBatteryStatus).vehicleStatusEnabled)

        TuPigBatteryStatus.start(context)
        awaitReading()
        assertTrue(identification.withVehicleStatusFrom(TuPigBatteryStatus).vehicleStatusEnabled)
    }

    @Test
    fun staleReadingBecomesUsableAgainOnlyAfterFreshDataIsPublished() {
        var clock = 0L
        val cache = TuPigBatteryCache { clock }
        cache.accept(reading)
        clock += 181_000
        assertNull(cache.snapshot(20))
        cache.accept(TuPigBatteryReading(10.0, 60, 10.0, false, TuPigBatteryProtocol.CANFD))
        val fresh = cache.snapshot(20)!!
        assertEquals(10.0, fresh.batteryPercent, 0.001)
        assertEquals(100_400L, fresh.maxChargeWh)
    }

    @Test
    fun protocolChangesCannotReuseThePreviousCapacity() {
        val cache = TuPigBatteryCache { 0 }
        cache.accept(reading)
        assertEquals(100_400L, cache.snapshot(20)!!.maxChargeWh)
        cache.accept(TuPigBatteryReading(51.0, 36, null, true, TuPigBatteryProtocol.CAN))
        val can = cache.snapshot(20)!!
        assertEquals(51.0, can.batteryPercent, 0.001)
        assertNull(can.currentChargeWh)
        assertNull(can.maxChargeWh)
        cache.accept(TuPigBatteryReading(10.0, 60, 5.0, false, TuPigBatteryProtocol.CANFD))
        assertEquals(50_000L, cache.snapshot(20)!!.maxChargeWh)
    }

    @Test
    fun failedSampleClearsTheReadingAndCapacity() {
        val cache = TuPigBatteryCache { 0 }
        cache.accept(reading)
        cache.accept(null)
        assertNull(cache.snapshot(20))
        cache.accept(TuPigBatteryReading(10.0, 60, 5.0, false, TuPigBatteryProtocol.CANFD))
        assertEquals(50_000L, cache.snapshot(20)!!.maxChargeWh)
    }

    @Test
    fun settingsCheckPublishesCanWithoutEnergyAndClearsUnknownProtocol() {
        val ready = TuPigAdbAccess.readStatus(context) { command ->
            when (command) {
                "getprop ro.car.protocol" -> "CAN\n"
                "service call autoservice 7 i32 1014 i32 1033543720" -> "Result: Parcel(00000000 424c0000   '........')"
                "service call autoservice 5 i32 1014 i32 1033203771" -> "Result: Parcel(00000000 00000024   '........')"
                "service call autoservice 5 i32 1009 i32 876611608" -> "Result: Parcel(00000000 00000001   '........')"
                else -> null
            }
        }
        assertEquals(TuPigAdbAccess.State.READY, ready.state)
        assertEquals(51.0, ready.batteryPercent!!, 0.001)
        assertEquals(36, ready.rangeKm)
        assertTrue(identification.withVehicleStatusFrom(TuPigBatteryStatus).vehicleStatusEnabled)
        assertNull(TuPigBatteryStatus.snapshot()!!.maxChargeWh)

        val unsupported = TuPigAdbAccess.readStatus(context) { "UNKNOWN" }
        assertEquals(TuPigAdbAccess.State.READY, unsupported.state)
        assertNull(unsupported.batteryPercent)
        assertNull(TuPigBatteryStatus.snapshot())
        assertFalse(identification.withVehicleStatusFrom(TuPigBatteryStatus).vehicleStatusEnabled)
    }

    private fun awaitReading() {
        val deadline = System.nanoTime() + 2_000_000_000L
        while (TuPigBatteryStatus.snapshot() == null && System.nanoTime() < deadline) Thread.sleep(5)
        assertNotNull("refresh should complete before the 30-second periodic tick", TuPigBatteryStatus.snapshot())
    }
}
