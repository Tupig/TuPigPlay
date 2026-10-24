package com.shilapi.xcertplay.hud

import org.junit.Assert.*
import org.junit.Test

class TuPigClusterSpeedReaderTest {
    @Test fun convertsSpeedAndRejectsMissingOrInvalidReadings() {
        assertEquals(0, TuPigClusterSpeedReader.kmh(0.0))
        assertEquals(72, TuPigClusterSpeedReader.kmh(20.0))
        assertNull(TuPigClusterSpeedReader.kmh(null))
        assertNull(TuPigClusterSpeedReader.kmh(Double.NaN))
        assertNull(TuPigClusterSpeedReader.kmh(Double.POSITIVE_INFINITY))
        assertNull(TuPigClusterSpeedReader.kmh(-1.0))
        assertNull(TuPigClusterSpeedReader.kmh(100.0))
    }
}
