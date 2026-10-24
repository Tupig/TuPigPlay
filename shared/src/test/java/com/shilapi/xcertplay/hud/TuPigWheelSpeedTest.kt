package com.shilapi.xcertplay.hud

import com.shilapi.xcertplay.transport.VehicleGear
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class TuPigWheelSpeedTest {
    @Test
    fun readsSpeedInMetersPerSecond() {
        // The car's reply while parked on 2026-09-29, then 36.0 km/h.
        assertEquals(0.0, TuPigWheelSpeed.metersPerSecond("Result: Parcel(00000000 00000000   '........')")!!, 1e-9)
        assertEquals(10.0, TuPigWheelSpeed.metersPerSecond("Result: Parcel(00000000 42100000   '.......B')")!!, 1e-6)
    }

    @Test
    fun rejectsMissingOrImpossibleSpeed() {
        assertNull(TuPigWheelSpeed.metersPerSecond(null))
        assertNull(TuPigWheelSpeed.metersPerSecond("Result: Parcel(ffffffff 00000000   '........')"))
        assertNull(TuPigWheelSpeed.metersPerSecond("Result: Parcel(00000000 bf800000   '........')")) // -1.0
        assertNull(TuPigWheelSpeed.metersPerSecond("Result: Parcel(00000000 7fc00000   '........')")) // NaN
    }

    @Test
    fun mapsGears() {
        assertEquals(VehicleGear.PARK, TuPigWheelSpeed.gear("Result: Parcel(00000000 00000001   '........')"))
        assertEquals(VehicleGear.REVERSE, TuPigWheelSpeed.gear("Result: Parcel(00000000 00000002   '........')"))
        assertEquals(VehicleGear.NEUTRAL, TuPigWheelSpeed.gear("Result: Parcel(00000000 00000003   '........')"))
        assertEquals(VehicleGear.DRIVE, TuPigWheelSpeed.gear("Result: Parcel(00000000 00000004   '........')"))
        assertEquals(VehicleGear.DRIVE, TuPigWheelSpeed.gear("Result: Parcel(00000000 00000005   '........')"))
        assertEquals(VehicleGear.DRIVE, TuPigWheelSpeed.gear("Result: Parcel(00000000 00000006   '........')"))
        assertNull(TuPigWheelSpeed.gear("Result: Parcel(00000000 00000000   '........')"))
    }

    // #807: a DiLink 4.0 read nothing in default mode for a whole drive; the report has to say which read failed.
    @Test
    fun describesWhichReadGaveNothing() {
        val reads = WheelSpeedReads()
        reads.speed("Result: Parcel(ffffffff 00000000   '........')", null)
        reads.speed(null, null)
        reads.gear("Result: Parcel(00000000 00000000   '........')", null)
        assertEquals("adb=READY speed=0/2 gear=none speedReply=\"Result: Parcel(ffffffff 00000000 '........')\" " +
            "gearReply=\"Result: Parcel(00000000 00000000 '........')\"", reads.describe("READY", null))
    }

    @Test
    fun aReadableGearAndSpeedNeedNoReplies() {
        val reads = WheelSpeedReads()
        reads.speed("Result: Parcel(00000000 42100000   '.......B')", 10.0)
        reads.gear("Result: Parcel(00000000 00000004   '........')", VehicleGear.DRIVE)
        assertEquals("adb=READY speed=1/1 gear=D", reads.describe("READY", VehicleGear.DRIVE))
    }

    @Test
    fun aReplyIsShortAndOnOneLine() {
        val reads = WheelSpeedReads()
        reads.speed("service: Service autoservice does not exist\n" + "x".repeat(200), null)
        val reply = reads.describe("READY", VehicleGear.PARK).substringAfter("speedReply=")
        assertEquals("\"service: Service autoservice does not exist " + "x".repeat(36) + "\"", reply)
    }
}
