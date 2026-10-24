package com.shilapi.xcertplay.hud

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class TuPigParkedStateTest {
    @Test
    fun readsTheGearboxReply() {
        assertEquals(true, TuPigParkedState.parked("Result: Parcel(00000000 00000001   '........')"))
        assertEquals(false, TuPigParkedState.parked("Result: Parcel(00000000 00000004   '........')"))
        assertEquals(false, TuPigParkedState.parked("Result: Parcel(00000000 00000006   '........')"))
        assertEquals(false, TuPigParkedState.parked("Result: Parcel(00000000 00000002   '........')"))
    }

    @Test
    fun unknownGearIsNotParked() {
        assertNull(TuPigParkedState.parked(null))
        assertNull(TuPigParkedState.parked("Result: Parcel(00000000 00000000   '........')"))
        assertNull(TuPigParkedState.parked("Result: Parcel(ffffffff 00000001   '........')"))
    }
}
