package com.shilapi.xcertplay.hud

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class TuPigBootStartRepairTest {
    @Test fun whitelistRequiresTheRequestedPackageEntry() {
        val pkg = "com.shilapi.xcertplay"
        assertTrue(TuPigBootStartRepair.isWhitelisted("user,$pkg,10234\n", pkg))
        assertTrue(TuPigBootStartRepair.isWhitelisted("system,$pkg,10234", pkg))
        assertFalse(TuPigBootStartRepair.isWhitelisted("user,$pkg.debug,10234", pkg))
        assertFalse(TuPigBootStartRepair.isWhitelisted("Permission denied for $pkg", pkg))
        assertFalse(TuPigBootStartRepair.isWhitelisted(null, pkg))
    }
}
