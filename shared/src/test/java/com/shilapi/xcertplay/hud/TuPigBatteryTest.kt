package com.shilapi.xcertplay.hud

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class TuPigBatteryTest {
    // Replies from the car on 2026-09-28: 25 %, 150 km, 25.1 kWh left, not charging (BMS state 15).
    private val car = mapOf(
        "1246777400" to "Result: Parcel(00000000 41c80000   '.......A')",
        "1246765118" to "Result: Parcel(00000000 00000096   '........')",
        "882901008" to "Result: Parcel(00000000 41c8cccd   '.......A')",
        "876609560" to "Result: Parcel(00000000 0000000f   '........')",
    )

    private fun shell(replies: Map<String, String>, protocol: String = "CANFD"): (String) -> String? = { command ->
        if (command == "getprop ro.car.protocol") protocol
        else replies.entries.firstOrNull { command.endsWith(" ${it.key}") }?.value
    }

    @Test
    fun readsTheCarsReplies() {
        val reading = TuPigBattery.read(shell(car))!!

        assertEquals(25.0, reading.percent, 0.001)
        assertEquals(150, reading.rangeKm)
        assertEquals(25.1, reading.remainingKwh!!, 0.001)
        assertEquals(TuPigBatteryProtocol.CANFD, reading.protocol)
        assertFalse(reading.charging)
    }

    @Test
    fun noAdbAccessGivesNoReading() {
        // TuPigAdbShell answers null while adbd is off, refuses TuPigCarPlay's key or waits for approval.
        assertNull(TuPigBattery.read { null })
    }

    @Test
    fun aMissingOrRejectedValueGivesNoReading() {
        assertNull(TuPigBattery.read(shell(car - "1246765118")))
        assertNull(TuPigBattery.read(shell(car + ("1246777400" to "Result: Parcel(ffffd8e5    '....')"))))
    }

    @Test
    fun theIphoneGetsWattHoursAndAWarningAtTheThreshold() {
        val reading = TuPigBatteryReading(percent = 25.0, rangeKm = 150, remainingKwh = 25.1, charging = false, protocol = TuPigBatteryProtocol.CANFD)

        val above = TuPigBattery.snapshot(reading, lowPercent = 20, fullKwh = null)
        assertFalse(above.rangeWarning)
        assertEquals(25_100L, above.currentChargeWh)
        assertEquals(100_400L, above.maxChargeWh)
        assertEquals(600, above.maxRangeKm)

        assertTrue(TuPigBattery.snapshot(reading, lowPercent = 25, fullKwh = null).rangeWarning)
    }

    @Test
    fun aLowChargeKeepsTheEarlierFullChargeEstimate() {
        val high = TuPigBatteryReading(percent = 80.0, rangeKm = 480, remainingKwh = 80.0, charging = false, protocol = TuPigBatteryProtocol.CANFD)
        val low = TuPigBatteryReading(percent = 5.0, rangeKm = 30, remainingKwh = 5.6, charging = false, protocol = TuPigBatteryProtocol.CANFD)

        assertNull(TuPigBattery.fullKwh(low))
        assertEquals(100_000L, TuPigBattery.snapshot(low, lowPercent = 20, fullKwh = TuPigBattery.fullKwh(high)).maxChargeWh)
    }

    @Test
    fun readsVerifiedCanCommandsWithoutProbingUnverifiedEnergy() {
        val replies = mapOf(
            "getprop ro.car.protocol" to "CAN\r\n",
            "service call autoservice 7 i32 1014 i32 1033543720" to "Result: Parcel(00000000 424c0000   '........')",
            "service call autoservice 5 i32 1014 i32 1033203771" to "Result: Parcel(00000000 00000024   '........')",
            "service call autoservice 5 i32 1009 i32 876611608" to "Result: Parcel(00000000 00000001   '........')",
        )
        val commands = mutableListOf<String>()
        val reading = TuPigBattery.read { command -> commands.add(command); replies[command] }!!

        assertEquals(replies.keys.toList(), commands)
        assertEquals(TuPigBatteryProtocol.CAN, reading.protocol)
        assertEquals(51.0, reading.percent, 0.001)
        assertEquals(36, reading.rangeKm)
        assertTrue(reading.charging)
        assertNull(reading.remainingKwh)
        assertNull(TuPigBattery.fullKwh(reading))
        val snapshot = TuPigBattery.snapshot(reading, 20, null)
        assertNull(snapshot.currentChargeWh)
        assertNull(snapshot.maxChargeWh)
        assertEquals(51.0, snapshot.batteryPercent, 0.001)
        assertEquals(36, snapshot.rangeKm)
    }

    @Test
    fun detectsProtocolBeforeEveryCanfdSample() {
        val commands = mutableListOf<String>()
        val reply = shell(car, " CANFD\n")
        repeat(2) { assertTrue(TuPigBattery.read { commands.add(it); reply(it) } != null) }
        val expected = listOf(
            "getprop ro.car.protocol",
            "service call autoservice 7 i32 1014 i32 1246777400",
            "service call autoservice 5 i32 1014 i32 1246765118",
            "service call autoservice 7 i32 1005 i32 882901008",
            "service call autoservice 5 i32 1009 i32 876609560",
        )
        assertEquals(expected + expected, commands)
    }

    @Test
    fun readsSysCarProtocolWhenRoIsEmpty() {
        // A 2024 Tang on DiLink 5 (dynasty di5, 2025 firmware) sets only sys.car.protocol.
        val reading = TuPigBattery.read { command ->
            when (command) {
                "getprop ro.car.protocol" -> "\n"
                "getprop sys.car.protocol" -> "CANFD\n"
                else -> car.entries.firstOrNull { command.endsWith(" ${it.key}") }?.value
            }
        }!!
        assertEquals(TuPigBatteryProtocol.CANFD, reading.protocol)
        assertEquals(150, reading.rangeKm)
    }

    @Test
    fun missingOrUnknownProtocolNeverFallsBackToCanfd() {
        for (protocol in listOf(null, "", "\r\n", "SOMEIP", "error: closed")) {
            val commands = mutableListOf<String>()
            assertNull(TuPigBattery.read { command -> commands.add(command); protocol })
            // A blank ro.car.protocol also asks sys.car.protocol; an unknown value is not second-guessed.
            val asked = if (protocol.isNullOrBlank()) listOf("getprop ro.car.protocol", "getprop sys.car.protocol")
                else listOf("getprop ro.car.protocol")
            assertEquals(asked, commands)
        }
    }

    @Test
    fun invalidCanValuesGiveNoReading() {
        val replies = mapOf(
            "1033543720" to "Result: Parcel(00000000 424c0000   '........')",
            "1033203771" to "Result: Parcel(00000000 00000024   '........')",
            "876611608" to "Result: Parcel(00000000 00000001   '........')",
        )
        assertNull(TuPigBattery.read(shell(replies - "1033203771", "CAN")))
        for (invalid in listOf("7fc00000", "7f800000", "42ca0000", "bf800000")) {
            assertNull(TuPigBattery.read(shell(replies + ("1033543720" to "Result: Parcel(00000000 $invalid   '........')"), "CAN")))
        }
    }
}
