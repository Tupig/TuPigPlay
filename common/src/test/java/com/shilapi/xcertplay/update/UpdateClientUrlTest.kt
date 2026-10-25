package com.shilapi.xcertplay.update

import org.junit.Assert.assertTrue
import org.junit.Test

class UpdateClientUrlTest {
    @Test
    fun releasesUrlPointsAtThisRepository() {
        assertTrue(UpdateClient.RELEASES_URL.contains("Tupig/TuPigPlay"))
        assertTrue(UpdateClient.RELEASES_URL.contains("/releases"))
    }
}
