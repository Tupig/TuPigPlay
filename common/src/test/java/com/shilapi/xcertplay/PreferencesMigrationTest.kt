package com.shilapi.xcertplay

import android.content.Context
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [29])
class PreferencesMigrationTest {
    private val context: Context get() = RuntimeEnvironment.getApplication()

    @Test
    fun copiesLegacyDiplayPrefsOnceIntoTupigcarplayFiles() {
        context.getSharedPreferences("diplay", 0).edit()
            .putBoolean("auto_connect", true)
            .putString("phone_address", "AA:BB")
            .commit()
        context.getSharedPreferences("diplay_car_hotspot", 0).edit()
            .putBoolean("auto_enable", true)
            .commit()
        context.getSharedPreferences("diplay_byd_outputs", 0).edit()
            .putBoolean("hud", true)
            .commit()

        PreferencesMigration.migrateIfNeeded(context)

        assertTrue(context.getSharedPreferences("tupigcarplay", 0).getBoolean("auto_connect", false))
        assertEquals("AA:BB", context.getSharedPreferences("tupigcarplay", 0).getString("phone_address", null))
        assertTrue(context.getSharedPreferences("tupigcarplay_car_hotspot", 0).getBoolean("auto_enable", false))
        assertTrue(context.getSharedPreferences("tupigcarplay_leapmotor_outputs", 0).getBoolean("hud", false))
        assertTrue(context.getSharedPreferences("tupigcarplay", 0).getBoolean("prefs_migrated_from_diplay", false))

        context.getSharedPreferences("diplay", 0).edit().putBoolean("auto_connect", false).commit()
        PreferencesMigration.migrateIfNeeded(context)
        assertTrue(context.getSharedPreferences("tupigcarplay", 0).getBoolean("auto_connect", false))
    }

    @Test
    fun doesNotOverwriteExistingTupigcarplayData() {
        context.getSharedPreferences("tupigcarplay", 0).edit().putBoolean("auto_connect", true).commit()
        context.getSharedPreferences("diplay", 0).edit().putBoolean("auto_connect", false).commit()
        PreferencesMigration.migrateIfNeeded(context)
        assertTrue(context.getSharedPreferences("tupigcarplay", 0).getBoolean("auto_connect", false))
    }
}
