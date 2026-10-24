package com.shilapi.xcertplay.hud

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class TuPigAmapAdapterTest {
    @Test
    fun findsTheDiLink3AdapterWhenTheTuPigOneIsAbsent() {
        // DiLink 3.0, Android 10 (TuPig Han EV, GCC): only com.example.amapservice is installed.
        val adapter = TuPigAmapAdapter.find { it == "com.example.amapservice" }

        assertEquals(TuPigAmapAdapter.DILINK3, adapter)
        assertTrue(adapter!!.needsSimpleNavigationMode)
    }

    @Test
    fun prefersTheTuPigAdapterAndLeavesItsClusterModeAlone() {
        val adapter = TuPigAmapAdapter.find { true }

        assertEquals(TuPigAmapAdapter.TuPig, adapter)
        assertFalse(adapter!!.needsSimpleNavigationMode)
    }

    @Test
    fun noAdapterMeansNoClusterOutput() {
        assertNull(TuPigAmapAdapter.find { false })
    }

    @Test
    fun clusterModeUsesClusterDebugCommands() {
        assertEquals("service call AutoContainer 2 i32 1000 i32 39 s16 \"\"", TuPigDiLink3ClusterMode.Mode.SIMPLE_NAVIGATION.command)
        assertEquals("service call AutoContainer 2 i32 1000 i32 16 s16 \"\"", TuPigDiLink3ClusterMode.Mode.PROJECTION.entryCommand)
        assertEquals("service call AutoContainer 2 i32 1000 i32 17 s16 \"\"", TuPigDiLink3ClusterMode.Mode.PROJECTION.command)
        // Full screen needs no entry command: 16 is the projection itself, not a way into 17.
        assertNull(TuPigDiLink3ClusterMode.Mode.FULL_PROJECTION.entryCommand)
        assertEquals("service call AutoContainer 2 i32 1000 i32 16 s16 \"\"", TuPigDiLink3ClusterMode.Mode.FULL_PROJECTION.command)
        assertEquals("service call AutoContainer 2 i32 1000 i32 18 s16 \"\"", TuPigDiLink3ClusterMode.Mode.STOCK.command)
    }

    @Test
    fun theChosenProjectionShapeIsRequestedWhileTheMapIsShown() {
        val mode = TuPigDiLink3ClusterMode
        assertEquals(TuPigDiLink3ClusterMode.Mode.FULL_PROJECTION, mode.desired(true, false, null, fullScreen = true))
        assertEquals(TuPigDiLink3ClusterMode.Mode.PROJECTION, mode.desired(true, false, null, fullScreen = false))
        // Guidance still falls back to the simple-navigation card, whatever shape is configured.
        assertEquals(TuPigDiLink3ClusterMode.Mode.SIMPLE_NAVIGATION, mode.desired(false, true, null, fullScreen = true))
        assertEquals(TuPigDiLink3ClusterMode.Mode.STOCK, mode.desired(false, false, TuPigDiLink3ClusterMode.Mode.FULL_PROJECTION, fullScreen = true))
        // Both projection modes count as a map, so recovery must not restore the stock view instead.
        assertTrue(TuPigDiLink3ClusterMode.Mode.PROJECTION.showsMap)
        assertTrue(TuPigDiLink3ClusterMode.Mode.FULL_PROJECTION.showsMap)
        assertFalse(TuPigDiLink3ClusterMode.Mode.SIMPLE_NAVIGATION.showsMap)
        assertFalse(TuPigDiLink3ClusterMode.Mode.STOCK.showsMap)
    }

    @Test
    fun mapWinsOverGuidanceAndStockIsRestoredOnlyAfterAChange() {
        val mode = TuPigDiLink3ClusterMode
        assertNull(mode.desired(mapShown = false, guidanceActive = false, requested = null))
        assertEquals(TuPigDiLink3ClusterMode.Mode.SIMPLE_NAVIGATION, mode.desired(false, true, null))
        assertEquals(TuPigDiLink3ClusterMode.Mode.PROJECTION, mode.desired(true, true, TuPigDiLink3ClusterMode.Mode.SIMPLE_NAVIGATION))
        assertEquals(TuPigDiLink3ClusterMode.Mode.SIMPLE_NAVIGATION, mode.desired(false, true, TuPigDiLink3ClusterMode.Mode.PROJECTION))
        assertEquals(TuPigDiLink3ClusterMode.Mode.STOCK, mode.desired(false, false, TuPigDiLink3ClusterMode.Mode.PROJECTION))
    }

    @Test
    fun diLink3GuidanceTextIsReadableOnTheCluster() {
        val text = TuPigDiLink3GuidanceText
        assertEquals("250 m", text.distance(250))
        assertEquals("5.4 km", text.distance(5400))
        assertEquals("12 km", text.distance(12_345))
        assertNull(text.distance(-1))
        assertEquals("10 min", text.duration(600))
        assertEquals("1 min", text.duration(1))
        assertEquals("1 h 5 min", text.duration(3900))
        assertNull(text.duration(-1))
        val utc = java.util.TimeZone.getTimeZone("UTC")
        assertEquals("预计15:55到达", text.arrival(15L * 3_600_000 + 45 * 60_000, 600, utc, use24Hour = true))
        assertEquals("预计3:55到达", text.arrival(15L * 3_600_000 + 45 * 60_000, 600, utc, use24Hour = false))
        assertNull(text.arrival(0, -1, utc, use24Hour = true))
    }

    @Test
    fun clusterShowsTheWholeArrivalTimeAfterTrimmingAmapsWords() {
        val utc = java.util.TimeZone.getTimeZone("UTC")
        val eta = TuPigDiLink3GuidanceText.arrival(11L * 3_600_000 + 42 * 60_000, 7 * 60, utc, use24Hour = true)!!
        // The cluster drops two characters at each end; a bare "11:49" became ":4" in #384.
        assertEquals("11:49", eta.substring(2, eta.length - 2))
    }

    @Test
    fun displayIsCreatedAsDashCastDoesAndLeftInTheStockView() {
        assertEquals(
            listOf(16, 35, 18).map { "service call AutoContainer 2 i32 1000 i32 $it s16 \"\"" },
            TuPigDiLink3ClusterMode.CREATE_DISPLAY,
        )
    }

    @Test
    fun clusterModeAcceptsOnlyAnExceptionFreeReply() {
        assertTrue(TuPigDiLink3ClusterMode.accepted("Result: Parcel(00000000 00000000   '........')"))
        assertFalse(TuPigDiLink3ClusterMode.accepted("Result: Parcel(ffffffec 00000000 '........')"))
        assertFalse(TuPigDiLink3ClusterMode.accepted("service: Service AutoContainer does not exist"))
        assertFalse(TuPigDiLink3ClusterMode.accepted(null))
    }
}
