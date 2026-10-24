package com.shilapi.xcertplay.hud

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class TuPigClusterNaviModeTest {
    @Test
    fun readsTheModeFromServiceCallOutput() {
        assertEquals(TuPigClusterNaviMode.TURN_ON_BY_NAVI, TuPigClusterNaviMode.parseRead("Result: Parcel(00000000 00000002   '........')"))
        assertEquals(TuPigClusterNaviMode.FULL, TuPigClusterNaviMode.parseRead("Result: Parcel(00000000 00000004   '........')"))
        // An exception code in the first word, an unknown value, or no parcel at all.
        assertNull(TuPigClusterNaviMode.parseRead("Result: Parcel(ffffffff 00000002   '........')"))
        assertNull(TuPigClusterNaviMode.parseRead("Result: Parcel(00000000 00000009   '........')"))
        assertNull(TuPigClusterNaviMode.parseRead(null))
    }

    @Test
    fun onlySmallAndFullShowTheMap() {
        assertEquals(
            listOf(TuPigClusterNaviMode.SMALL, TuPigClusterNaviMode.FULL),
            TuPigClusterNaviMode.entries.filter { it.showsMap },
        )
    }

    @Test
    fun readsTheVerifiedId() {
        assertEquals("service call autoservice 5 i32 1007 i32 1086337074", TuPigClusterNaviMode.READ_COMMAND)
    }
}
