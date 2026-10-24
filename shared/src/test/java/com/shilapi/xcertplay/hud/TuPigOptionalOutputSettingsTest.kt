package com.shilapi.xcertplay.hud

import android.content.Context
import android.content.ContextWrapper
import android.content.pm.ApplicationInfo
import android.content.pm.PackageInfo
import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import org.robolectric.Shadows.shadowOf
import org.robolectric.annotation.Config
import org.robolectric.shadows.ShadowBuild

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [29], manifest = Config.NONE)
class TuPigOptionalOutputSettingsTest {
    @Test fun optionalOutputsDefaultOffAndKeepExplicitPreviousSelections() {
        val app = RuntimeEnvironment.getApplication()
        val prefs = app.getSharedPreferences("tupigcarplay_leapmotor_outputs", Context.MODE_PRIVATE)
        prefs.edit().clear().commit()
        assertFalse(TuPigOutputSettings.hudSong(app))
        assertEquals(TuPigOemClusterHold.OFF, TuPigOutputSettings.oemClusterHold(app))
        prefs.edit().putBoolean("oem_cluster_freeze", true).commit()
        assertEquals(TuPigOemClusterHold.PACKAGE, TuPigOutputSettings.oemClusterHold(app))
        TuPigOutputSettings.setOemClusterHold(app, TuPigOemClusterHold.COMPONENT)
        assertEquals(TuPigOemClusterHold.COMPONENT, TuPigOutputSettings.oemClusterHold(app))
    }

    @Test fun installedStockReceiverDoesNotEnableUnverifiedDilink4Hud() {
        val app = RuntimeEnvironment.getApplication()
        val knownApp = object : ContextWrapper(app) {
            override fun getPackageName(): String = "com.android.gallery3d"
        }
        ShadowBuild.setFingerprint("TuPig/DiLink4:10/unverified")
        val info = PackageInfo().apply {
            packageName = "com.byd.clusterdebug"
            applicationInfo = ApplicationInfo().apply {
                packageName = "com.byd.clusterdebug"
                flags = ApplicationInfo.FLAG_SYSTEM
            }
        }
        shadowOf(app.packageManager).installPackage(info)
        assertFalse(TuPigStandaloneHudOutput.available(knownApp))
        assertTrue(TuPigStandaloneHudOutput.diagnostics(knownApp).contains("standaloneHudAvailable=false"))
    }

}
