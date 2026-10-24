package com.shilapi.xcertplay

import android.content.Intent
import android.content.pm.PackageInfo
import android.os.Looper
import android.view.View
import android.view.ViewGroup
import android.widget.ScrollView
import android.widget.LinearLayout
import android.widget.Switch
import android.widget.TextView
import com.shilapi.xcertplay.host.R
import com.shilapi.xcertplay.adb.LocalAdb
import com.shilapi.xcertplay.hud.TuPigAdbAccess
import com.shilapi.xcertplay.hud.TuPigFieldProbeResult
import com.shilapi.xcertplay.hud.TuPigFieldSource
import com.shilapi.xcertplay.hud.TuPigOutputSettings
import com.shilapi.xcertplay.hud.TuPigReadAddress
import com.shilapi.xcertplay.hud.TuPigVehicleCapabilities
import com.shilapi.xcertplay.hud.TuPigVehicleField
import com.shilapi.xcertplay.hud.TuPigVehicleFieldStore
import com.shilapi.xcertplay.hud.TuPigVehicleProbeOutcome
import com.shilapi.xcertplay.transport.VehicleGear
import com.shilapi.xcertplay.orchestration.WirelessHotspotMode
import com.shilapi.xcertplay.transport.VehicleStatusProvider
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.Robolectric
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import org.robolectric.Shadows.shadowOf
import org.robolectric.util.ReflectionHelpers
import org.robolectric.android.controller.ActivityController
import org.robolectric.annotation.Config
import org.robolectric.annotation.LooperMode

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [32])
@LooperMode(LooperMode.Mode.PAUSED)
class TuPigVehicleDataSettingsTest {
    private val context get() = RuntimeEnvironment.getApplication()
    private var controller: ActivityController<TuPigCarPlayActivity>? = null
    private val activity get() = requireNotNull(controller).get()
    private lateinit var backend: FakeVehicleSettingsBackend
    private var reconnects = 0

    @Before fun setUp() {
        backend = FakeVehicleSettingsBackend()
        TuPigVehicleSettingsBackendProvider.current = backend
        shadowOf(context.packageManager).removePackage("com.byd.amapservice")
        context.getSharedPreferences("tupigcarplay_leapmotor_outputs", 0).edit().clear().commit()
        context.getSharedPreferences("tupigcarplay_leapmotor_vehicle_fields", 0).edit().clear().commit()
        TuPigVehicleFieldStore.clearMemoryForTests()
    }

    @After fun tearDown() {
        CarPlayBackgroundSession.clear()
        controller?.pause()?.stop()?.destroy()
        TuPigVehicleSettingsBackendProvider.reset()
        TuPigVehicleFieldStore.clearMemoryForTests()
        shadowOf(context.packageManager).removePackage("com.byd.amapservice")
    }

    @Test fun defaultModeShowsTheDiLink5SettingsBehindTheAdvancedButton() {
        openSettings()

        assertFalse(TuPigOutputSettings.navigationAvailable(activity))
        assertTrue(texts().any { it.text == activity.getString(R.string.advanced_vehicle_data) })
        assertFalse(switches().any { it.contentDescription == activity.getString(R.string.car_battery_for_the_iphone) })
        assertFalse(switches().any { it.contentDescription == activity.getString(R.string.wheel_speed_for_tunnels) })
        assertFalse(switches().any { it.contentDescription == activity.getString(R.string.cluster_song) })
        assertFalse(switches().any { it.contentDescription == activity.getString(R.string.navigation_on_hud_and_instrument_cluster) })

        texts().single { it.text == activity.getString(R.string.advanced_vehicle_data) }.performClick()

        assertTrue(texts().any { it.text == activity.getString(R.string.advanced_vehicle_data_description) })
        assertTrue(texts().any { it.text.toString().contains(activity.getString(R.string.vehicle_data_mode_default)) })
        assertTrue(texts().any { it.text == activity.getString(R.string.check_adb_access) })
        assertTrue(switches().any { it.contentDescription == activity.getString(R.string.car_battery_for_the_iphone) })
        assertTrue(switches().any { it.contentDescription == activity.getString(R.string.wheel_speed_for_tunnels) })
        assertTrue(switches().any { it.contentDescription == activity.getString(R.string.video_while_parked) })
    }

    @Test fun aTappedAdbCheckShowsItsResult() {
        // #724: on a head unit without network ADB only the status line changed, so the button seemed dead.
        openSettings()
        texts().single { it.text == activity.getString(R.string.advanced_vehicle_data) }.performClick()
        android.provider.Settings.Global.putInt(activity.contentResolver, android.provider.Settings.Global.ADB_ENABLED, 0)
        backend.checkResult = TuPigAdbAccess.Status(TuPigAdbAccess.State.ADB_OFF)
        texts().single { it.text == activity.getString(R.string.check_adb_access) }.performClick()
        backend.run("tupigcarplay-adb-state")
        shadowOf(android.os.Looper.getMainLooper()).idle()
        assertEquals(activity.getString(R.string.adb_off), org.robolectric.shadows.ShadowToast.getTextOfLatestToast())
    }

    @Test fun anAdbCheckWithUsbDebuggingOnSaysTheNetworkDoesNotAnswer() {
        // #1002: on DiLink UI7.0 the developer-options switch stays on after a restart while nothing listens on 5555.
        openSettings()
        texts().single { it.text == activity.getString(R.string.advanced_vehicle_data) }.performClick()
        android.provider.Settings.Global.putInt(activity.contentResolver, android.provider.Settings.Global.ADB_ENABLED, 1)
        backend.checkResult = TuPigAdbAccess.Status(TuPigAdbAccess.State.ADB_OFF)
        texts().single { it.text == activity.getString(R.string.check_adb_access) }.performClick()
        backend.run("tupigcarplay-adb-state")
        shadowOf(android.os.Looper.getMainLooper()).idle()
        assertEquals(activity.getString(R.string.adb_network_not_answering), org.robolectric.shadows.ShadowToast.getTextOfLatestToast())
    }

    @Test fun successfulLiveProbeRevealsOnlySupportedSettings() {
        TuPigOutputSettings.setLegacyVehicleProbe(context, true)
        openSettings()
        ReflectionHelpers.setField(activity, "tuPigVehicleAdvancedExpanded", true)
        ReflectionHelpers.setField(activity, "adbAccessState", TuPigAdbAccess.State.READY)
        ReflectionHelpers.setField(activity, "vehicleProbeOutcome",
            TuPigVehicleProbeOutcome(TuPigAdbAccess.State.READY, supportedCapabilities()))
        ReflectionHelpers.callInstanceMethod<Unit>(activity, "render")

        assertTrue(switches().any { it.contentDescription == activity.getString(R.string.car_battery_for_the_iphone) })
        assertTrue(switches().any { it.contentDescription == activity.getString(R.string.wheel_speed_for_tunnels) })
        assertTrue(switches().any { it.contentDescription == activity.getString(R.string.video_while_parked) })
    }

    @Test fun savedProbeAndEnabledSwitchSurviveAFreshActivityWithoutReprobing() {
        TuPigVehicleFieldStore.save(context, supportedCapabilities(TuPigVehicleFieldStore.firmwareKey()))
        TuPigOutputSettings.setWheelSpeedToIphone(context, true)
        backend.checkResult = readableStatus()
        openSettings()
        texts().single { it.text == activity.getString(R.string.advanced_vehicle_data) }.performClick()

        assertTrue(vehicleSwitch(R.string.wheel_speed_for_tunnels).isChecked)
        requireNotNull(controller).pause().stop().destroy()
        controller = null
        TuPigVehicleFieldStore.clearMemoryForTests()
        // Work the first activity left queued would be ignored; count only the fresh one's.
        backend.tasks.clear()
        openSettings()
        shadowOf(Looper.getMainLooper()).idle()
        backend.runAll("tupigcarplay-tupig13-auto-validate")
        texts().single { it.text == activity.getString(R.string.advanced_vehicle_data) }.performClick()

        assertTrue(texts().any { it.text == activity.getString(R.string.hide_advanced_vehicle_data) })
        assertTrue(vehicleSwitch(R.string.wheel_speed_for_tunnels).isChecked)
        // The fresh activity validated the saved fields by reading them, not by probing again.
        assertEquals(1, backend.checkCalls)
        assertEquals(0, backend.probeCalls)
    }

    @Test fun theHotspotCardCannotExposeFieldsRejectedByTheLegacyProbe() {
        val saved = supportedCapabilities(TuPigVehicleFieldStore.firmwareKey(), without = setOf(TuPigVehicleField.GEAR))
        TuPigVehicleFieldStore.save(context, saved)
        TuPigOutputSettings.setLegacyVehicleProbe(context, true)
        AirPlayPersistence.saveWirelessHotspotMode(context, WirelessHotspotMode.MANUAL)
        openSettings()
        texts().single { it.text == activity.getString(R.string.advanced_vehicle_data) }.performClick()
        val hotspot = LinearLayout(activity)
        ReflectionHelpers.callInstanceMethod<Unit>(activity, "renderTuPigAdbControls",
            ReflectionHelpers.ClassParameter.from(LinearLayout::class.java, hotspot),
            ReflectionHelpers.ClassParameter.from(LocalAdb.Access::class.java, LocalAdb.Access.READY))
        val page = descendants(activity.window.decorView).filterIsInstance<ScrollView>().first()
            .getChildAt(0) as LinearLayout
        page.addView(hotspot)

        assertEquals(1, switches().count { it.contentDescription == activity.getString(R.string.car_battery_for_the_iphone) })
        assertEquals(0, switches().count { it.contentDescription == activity.getString(R.string.wheel_speed_for_tunnels) })
        assertEquals(0, switches().count { it.contentDescription == activity.getString(R.string.video_while_parked) })
        assertTrue(TuPigOutputSettings.legacyVehicleProbe(context))
        assertEquals(saved, TuPigVehicleFieldStore.load(context))
    }

    @Test fun automaticValidationWaitsForHotspotAuthorizationThenResumes() {
        val saved = supportedCapabilities(TuPigVehicleFieldStore.firmwareKey())
        TuPigVehicleFieldStore.save(context, saved)
        TuPigOutputSettings.setLegacyVehicleProbe(context, true)
        backend.checkResult = readableStatus()
        controller = Robolectric.buildActivity(
            TuPigCarPlayActivity::class.java,
            Intent(context, TuPigCarPlayActivity::class.java).putExtra("page", "settings"),
        )
        // setup() can drain the validation posted by onCreate; establish authorization first.
        ReflectionHelpers.setField(activity, "adbSwitchChangePending", true)
        requireNotNull(controller).setup()
        shadowOf(Looper.getMainLooper()).idle()
        assertTrue(backend.tasks.none { it.first == "tupigcarplay-tupig13-auto-validate" })
        assertEquals(0, backend.checkCalls)
        assertEquals(0, backend.probeCalls)
        assertTrue(ReflectionHelpers.getField<Boolean>(activity, "automaticVehicleValidationPending"))

        ReflectionHelpers.setField(activity, "adbSwitchChangePending", false)
        ReflectionHelpers.callInstanceMethod<Unit>(activity, "runPendingAutomaticVehicleValidation")
        shadowOf(Looper.getMainLooper()).idle()
        assertEquals(1, backend.tasks.count { it.first == "tupigcarplay-tupig13-auto-validate" })
        backend.runAll("tupigcarplay-tupig13-auto-validate")
        assertEquals(1, backend.checkCalls)
        assertEquals(0, backend.probeCalls)
        assertEquals(saved, TuPigVehicleFieldStore.load(context))
    }

    @Test fun temporaryAdbFailureDoesNotHideSavedFunctionsOrClearSwitches() {
        TuPigVehicleFieldStore.save(context, supportedCapabilities(TuPigVehicleFieldStore.firmwareKey()))
        TuPigOutputSettings.setVideoWhileParked(context, true)
        backend.checkResult = TuPigAdbAccess.Status(TuPigAdbAccess.State.ADB_OFF)
        openSettings()
        shadowOf(Looper.getMainLooper()).idle()
        backend.runAll("tupigcarplay-tupig13-auto-validate")
        texts().single { it.text == activity.getString(R.string.advanced_vehicle_data) }.performClick()

        assertTrue(vehicleSwitch(R.string.video_while_parked).isChecked)
        assertTrue(texts().any { it.text == activity.getString(R.string.probe_vehicle_data_again) })
    }

    @Test fun clusterSongSwitchStaysInTheTuPigNavigationSectionOnly() {
        shadowOf(context.packageManager).installPackage(PackageInfo().apply { packageName = "com.byd.amapservice" })
        openSettings()
        openCategory(R.string.settings_navigation)

        assertTrue(texts().any { it.text == activity.getString(R.string.tupig_navigation) })
        assertEquals(1, switches().count { it.contentDescription == activity.getString(R.string.cluster_song) })
        openCategory(R.string.settings_advanced)
        texts().single { it.text == activity.getString(R.string.advanced_vehicle_data) }.performClick()
        assertEquals(0, switches().count { it.contentDescription == activity.getString(R.string.cluster_song) })
    }

    @Test fun withoutTuPigNavigationTheClusterSongSwitchIsUnderAdvancedVehicleData() {
        openSettings()
        assertFalse(texts().any { it.text == activity.getString(R.string.tupig_navigation) })
        assertFalse(switches().any { it.contentDescription == activity.getString(R.string.cluster_song) })

        texts().single { it.text == activity.getString(R.string.advanced_vehicle_data) }.performClick()
        vehicleSwitch(R.string.cluster_song).performClick()

        assertTrue(TuPigOutputSettings.clusterSong(context))
    }

    @Test fun gateSwitchShowsOnlyWhenClusterSongIsOn() {
        shadowOf(context.packageManager).installPackage(PackageInfo().apply { packageName = "com.byd.amapservice" })
        openSettings()
        openCategory(R.string.settings_navigation)

        assertEquals(1, switches().count { it.contentDescription == activity.getString(R.string.cluster_song) })
        assertEquals(0, switches().count { it.contentDescription == activity.getString(R.string.cluster_song_gate) })

        vehicleSwitch(R.string.cluster_song).performClick()
        shadowOf(Looper.getMainLooper()).idle()

        assertEquals(1, switches().count { it.contentDescription == activity.getString(R.string.cluster_song_gate) })
        assertFalse(TuPigOutputSettings.clusterSongGate(context)) // default false
    }

    @Test fun scheduledValidationCannotLeaveAUserProbeStuck() {
        TuPigVehicleFieldStore.save(context, supportedCapabilities(TuPigVehicleFieldStore.firmwareKey()))
        backend.checkStateResult = TuPigAdbAccess.State.READY
        backend.probeResult = TuPigVehicleProbeOutcome(
            TuPigAdbAccess.State.READY,
            supportedCapabilities(TuPigVehicleFieldStore.firmwareKey()),
        )
        backend.checkResult = readableStatus()
        openSettings()

        invokeProbe()
        ReflectionHelpers.callInstanceMethod<Unit>(activity, "scheduleAutomaticVehicleValidation")
        shadowOf(Looper.getMainLooper()).idle()
        backend.run("tupigcarplay-tupig13-probe")
        backend.runAll("tupigcarplay-tupig13-auto-validate")

        assertFalse(ReflectionHelpers.getField(activity, "vehicleProbeAuthorizationInProgress"))
        assertFalse(ReflectionHelpers.getField(activity, "vehicleProbeInProgress"))
        assertFalse(ReflectionHelpers.getField(activity, "automaticVehicleValidationInProgress"))
    }

    @Test fun scheduledValidationCannotLeaveAnAdbCheckStuck() {
        TuPigVehicleFieldStore.save(context, supportedCapabilities(TuPigVehicleFieldStore.firmwareKey()))
        backend.checkStateResult = TuPigAdbAccess.State.READY
        backend.checkResult = readableStatus()
        openSettings()

        ReflectionHelpers.callInstanceMethod<Unit>(activity, "checkAdbState",
            ReflectionHelpers.ClassParameter.from(Boolean::class.javaPrimitiveType!!, true))
        shadowOf(Looper.getMainLooper()).idle()
        backend.run("tupigcarplay-adb-state")
        backend.runAll("tupigcarplay-tupig13-auto-validate")

        assertFalse(ReflectionHelpers.getField(activity, "adbCheckInProgress"))
        assertFalse(ReflectionHelpers.getField(activity, "automaticVehicleValidationInProgress"))
    }

    @Test fun aNewIntentDoesNotCancelAUserProbe() {
        TuPigVehicleFieldStore.save(context, supportedCapabilities(TuPigVehicleFieldStore.firmwareKey()))
        backend.checkStateResult = TuPigAdbAccess.State.READY
        backend.probeResult = TuPigVehicleProbeOutcome(
            TuPigAdbAccess.State.READY,
            supportedCapabilities(TuPigVehicleFieldStore.firmwareKey()),
        )
        backend.checkResult = readableStatus()
        openSettings()

        invokeProbe()
        ReflectionHelpers.callInstanceMethod<Unit>(activity, "onNewIntent",
            ReflectionHelpers.ClassParameter.from(
                Intent::class.java,
                Intent(context, TuPigCarPlayActivity::class.java).putExtra("page", "settings"),
            ))
        shadowOf(Looper.getMainLooper()).idle()
        backend.run("tupigcarplay-tupig13-probe")
        backend.runAll("tupigcarplay-tupig13-auto-validate")

        assertFalse(ReflectionHelpers.getField(activity, "vehicleProbeAuthorizationInProgress"))
        assertFalse(ReflectionHelpers.getField(activity, "vehicleProbeInProgress"))
    }

    @Test fun lostFieldsNeedExplicitReplacement() {
        val currentKey = TuPigVehicleFieldStore.firmwareKey()
        TuPigVehicleFieldStore.save(context, supportedCapabilities(currentKey))
        backend.checkStateResult = TuPigAdbAccess.State.READY
        backend.probeResult = TuPigVehicleProbeOutcome(
            TuPigAdbAccess.State.READY,
            supportedCapabilities(currentKey, without = setOf(TuPigVehicleField.GEAR)),
        )
        openSettings()
        texts().single { it.text == activity.getString(R.string.advanced_vehicle_data) }.performClick()

        invokeProbe()
        backend.run("tupigcarplay-tupig13-probe")

        assertTrue(TuPigVehicleFieldStore.load(context)!!.gearSupported)
        assertTrue(texts().any { it.text.toString().contains(activity.getString(R.string.vehicle_field_gear)) })
        texts().single { it.text == activity.getString(R.string.replace_saved_vehicle_data_anyway) }.performClick()
        assertFalse(TuPigVehicleFieldStore.load(context)!!.gearSupported)
    }

    @Test fun aDestroyedUserProbeCannotSaveOrPublishItsLateCandidate() {
        val saved = preparePublishingProbe()
        backend.onProbe = {
            requireNotNull(controller).pause().stop().destroy()
            controller = null
        }

        invokeProbe()
        backend.run("tupigcarplay-tupig13-probe")

        assertEquals(1, backend.probeCalls)
        assertEquals(saved, TuPigVehicleFieldStore.load(context))
        assertEquals(25.0, publishedBatteryPercent(), 0.0)
    }

    @Test fun aModeChangeBeforeTheUserProbeReturnsKeepsThePreviousSnapshot() {
        val saved = preparePublishingProbe()
        backend.onProbe = { TuPigOutputSettings.setLegacyVehicleProbe(context, false) }

        invokeProbe()
        backend.run("tupigcarplay-tupig13-probe")

        assertEquals(1, backend.probeCalls)
        assertFalse(TuPigOutputSettings.legacyVehicleProbe(context))
        assertEquals(saved, TuPigVehicleFieldStore.load(context))
        assertEquals(25.0, publishedBatteryPercent(), 0.0)
        assertFalse(ReflectionHelpers.getField<Boolean>(activity, "vehicleProbeInProgress"))
    }

    @Test fun aPausedUserProbeStillSavesAndPublishesItsAcceptedCandidate() {
        preparePublishingProbe()
        backend.onProbe = { requireNotNull(controller).pause().stop() }

        invokeProbe()
        backend.run("tupigcarplay-tupig13-probe")

        assertEquals(backend.probeResult.capabilities, TuPigVehicleFieldStore.load(context))
        assertEquals(90.0, publishedBatteryPercent(), 0.0)
        requireNotNull(controller).destroy()
        controller = null
    }

    @Test fun selectingDefaultAgainCannotBeOverriddenByALateLegacySelection() {
        backend.checkStateResult = TuPigAdbAccess.State.READY
        backend.probeResult = TuPigVehicleProbeOutcome(
            TuPigAdbAccess.State.READY,
            supportedCapabilities(TuPigVehicleFieldStore.firmwareKey()),
        )
        openSettings()
        backend.onProbe = { selectLegacyMode(false) }

        selectLegacyMode(true)
        backend.run("tupigcarplay-tupig13-probe")

        assertFalse(TuPigOutputSettings.legacyVehicleProbe(context))
        assertEquals(null, TuPigVehicleFieldStore.load(context))
        assertFalse(ReflectionHelpers.getField<Boolean>(activity, "vehicleProbeInProgress"))
    }

    private fun preparePublishingProbe(): TuPigVehicleCapabilities {
        val capabilities = supportedCapabilities(TuPigVehicleFieldStore.firmwareKey())
        val saved = capabilities.copy(fields = capabilities.fields + (TuPigVehicleField.SOC to
            capabilities.result(TuPigVehicleField.SOC).copy(value = 25.0)))
        TuPigOutputSettings.setLegacyVehicleProbe(context, true)
        TuPigVehicleFieldStore.save(context, saved)
        backend.checkStateResult = TuPigAdbAccess.State.READY
        backend.probeResult = TuPigVehicleProbeOutcome(
            TuPigAdbAccess.State.READY,
            saved.copy(fields = saved.fields + (TuPigVehicleField.SOC to
                saved.result(TuPigVehicleField.SOC).copy(value = 90.0))),
        )
        openSettings()
        return saved
    }

    private fun publishedBatteryPercent(): Double {
        // Read the provider without starting its real ADB polling executor.
        val provider = Class.forName("com.shilapi.xcertplay.hud.TuPigBatteryStatus")
            .getField("INSTANCE").get(null) as VehicleStatusProvider
        return requireNotNull(provider.snapshot()).batteryPercent
    }

    @Test fun aPassingValidationKeepsTheOfferToReplaceSavedData() {
        val currentKey = TuPigVehicleFieldStore.firmwareKey()
        TuPigVehicleFieldStore.save(context, supportedCapabilities(currentKey))
        backend.checkStateResult = TuPigAdbAccess.State.READY
        backend.probeResult = TuPigVehicleProbeOutcome(
            TuPigAdbAccess.State.READY,
            supportedCapabilities(currentKey, without = setOf(TuPigVehicleField.GEAR)),
        )
        backend.checkResult = readableStatus()
        openSettings()
        texts().single { it.text == activity.getString(R.string.advanced_vehicle_data) }.performClick()
        invokeProbe()
        backend.run("tupigcarplay-tupig13-probe")

        // With no switch on, the check after a return from CarPlay passes without reading the lost gear.
        returnFromCarPlay()
        backend.runAll("tupigcarplay-tupig13-auto-validate")

        assertTrue(texts().any { it.text == activity.getString(R.string.replace_saved_vehicle_data_anyway) })
    }

    @Test fun replaceAnywayKeepsTheAdbStateAndASnapshotSavedSince() {
        val currentKey = TuPigVehicleFieldStore.firmwareKey()
        TuPigVehicleFieldStore.save(context, supportedCapabilities(currentKey))
        backend.checkStateResult = TuPigAdbAccess.State.READY
        backend.probeResult = TuPigVehicleProbeOutcome(
            TuPigAdbAccess.State.READY,
            supportedCapabilities(currentKey, without = setOf(TuPigVehicleField.GEAR)),
        )
        openSettings()
        texts().single { it.text == activity.getString(R.string.advanced_vehicle_data) }.performClick()
        invokeProbe()
        backend.run("tupigcarplay-tupig13-probe")

        // Meanwhile ADB was turned off, and another probe saved its result.
        backend.checkResult = TuPigAdbAccess.Status(TuPigAdbAccess.State.ADB_OFF)
        returnFromCarPlay()
        backend.runAll("tupigcarplay-tupig13-auto-validate")
        val savedSince = supportedCapabilities(currentKey).copy(detectedAtMillis = 42)
        TuPigVehicleFieldStore.save(context, savedSince)
        texts().single { it.text == activity.getString(R.string.replace_saved_vehicle_data_anyway) }.performClick()

        assertEquals(savedSince, TuPigVehicleFieldStore.load(context))
        assertTrue(texts().any { it.text == activity.getString(R.string.adb_off) })
        assertTrue(texts().any {
            it.text == activity.getString(R.string.vehicle_probe_failed,
                activity.getString(R.string.vehicle_probe_snapshot_changed))
        })
    }

    @Test fun aFailedReplacementIsShownInsteadOfCrashing() {
        val currentKey = TuPigVehicleFieldStore.firmwareKey()
        TuPigVehicleFieldStore.save(context, supportedCapabilities(currentKey))
        backend.checkStateResult = TuPigAdbAccess.State.READY
        // The store refuses to write another firmware's probe, standing in for a failed write.
        backend.probeResult = TuPigVehicleProbeOutcome(
            TuPigAdbAccess.State.READY,
            supportedCapabilities("another firmware", without = setOf(TuPigVehicleField.GEAR)),
        )
        openSettings()
        texts().single { it.text == activity.getString(R.string.advanced_vehicle_data) }.performClick()
        invokeProbe()
        backend.run("tupigcarplay-tupig13-probe")

        texts().single { it.text == activity.getString(R.string.replace_saved_vehicle_data_anyway) }.performClick()

        assertTrue(TuPigVehicleFieldStore.load(context)!!.gearSupported)
        assertTrue(texts().any {
            it.text == activity.getString(R.string.vehicle_probe_failed, "probe belongs to another firmware")
        })
    }

    @Test fun anAdbCheckAloneDoesNotStartAVehicleValidation() {
        TuPigVehicleFieldStore.save(context, supportedCapabilities(TuPigVehicleFieldStore.firmwareKey()))
        backend.checkStateResult = TuPigAdbAccess.State.READY
        backend.checkResult = readableStatus()
        openSettings()
        shadowOf(Looper.getMainLooper()).idle()
        backend.runAll("tupigcarplay-tupig13-auto-validate")

        // As when Dashboard song or Dashboard map is turned on.
        ReflectionHelpers.callInstanceMethod<Unit>(activity, "checkAdbState",
            ReflectionHelpers.ClassParameter.from(Boolean::class.javaPrimitiveType!!, true))
        backend.run("tupigcarplay-adb-state")

        assertEquals(1, backend.checkCalls)
        assertFalse(backend.tasks.any { it.first == "tupigcarplay-tupig13-auto-validate" })
    }

    @Test fun aSwitchTurnedOnDuringAValidationIsValidatedAfterIt() {
        TuPigVehicleFieldStore.save(context, supportedCapabilities(TuPigVehicleFieldStore.firmwareKey()))
        backend.checkResult = readableStatus()
        openSettings()
        shadowOf(Looper.getMainLooper()).idle()
        texts().single { it.text == activity.getString(R.string.advanced_vehicle_data) }.performClick()

        // The first validation has not finished reading when wheel speed is turned on.
        vehicleSwitch(R.string.wheel_speed_for_tunnels).performClick()
        shadowOf(Looper.getMainLooper()).idle()
        backend.runAll("tupigcarplay-tupig13-auto-validate")

        assertEquals(2, backend.checkCalls)
    }

    @Test fun aValidationCancelledBeforeItsSecondCheckReadsNothingMore() {
        TuPigVehicleFieldStore.save(context, supportedCapabilities(TuPigVehicleFieldStore.firmwareKey()))
        TuPigOutputSettings.setWheelSpeedToIphone(context, true)
        backend.checkResult = TuPigAdbAccess.Status(TuPigAdbAccess.State.READY)
        openSettings()
        shadowOf(Looper.getMainLooper()).idle()
        // A user operation cancels the validation once its first check finds the fields unreadable.
        backend.onCheck = {
            backend.onCheck = null
            ReflectionHelpers.callInstanceMethod<Any?>(activity, "cancelAutomaticVehicleValidationForUserOperation",
                ReflectionHelpers.ClassParameter.from(Boolean::class.javaPrimitiveType!!, false))
        }
        backend.run("tupigcarplay-tupig13-auto-validate")

        assertEquals(1, backend.checkCalls)
        assertEquals(0, backend.probeCalls)
    }

    @Test fun oneTimeAdbApprovalExplainsAlwaysAllow() {
        TuPigOutputSettings.setLegacyVehicleProbe(context, true)
        backend.checkStateResult = TuPigAdbAccess.State.READY
        backend.probeResult = TuPigVehicleProbeOutcome(TuPigAdbAccess.State.NOT_APPROVED)
        openSettings()
        texts().single { it.text == activity.getString(R.string.advanced_vehicle_data) }.performClick()

        invokeProbe()
        assertTrue(texts().any { it.text == activity.getString(R.string.adb_checking_may_ask) })
        backend.run("tupigcarplay-tupig13-probe")

        assertEquals(2, backend.probeCalls)
        assertTrue(texts().any { it.text.toString().contains(activity.getString(R.string.vehicle_probe_allowed_once)) })
    }

    @Test fun aProbeRefusedRightAfterApprovalIsRetriedOnce() {
        TuPigOutputSettings.setLegacyVehicleProbe(context, true)
        backend.checkStateResult = TuPigAdbAccess.State.READY
        // adbd saves an "Always allow" key just after approving, so the probe's first connection can be early.
        backend.queuedProbeResults += TuPigVehicleProbeOutcome(TuPigAdbAccess.State.NOT_APPROVED)
        backend.probeResult = TuPigVehicleProbeOutcome(
            TuPigAdbAccess.State.READY,
            supportedCapabilities(TuPigVehicleFieldStore.firmwareKey()),
        )
        openSettings()
        texts().single { it.text == activity.getString(R.string.advanced_vehicle_data) }.performClick()

        invokeProbe()
        backend.run("tupigcarplay-tupig13-probe")

        assertEquals(2, backend.probeCalls)
        assertTrue(TuPigVehicleFieldStore.load(context)!!.motionSupported)
        assertFalse(texts().any { it.text.toString().contains(activity.getString(R.string.vehicle_probe_allowed_once)) })
    }

    @Test fun failedLegacySelectionKeepsTheDefaultMode() {
        backend.checkStateResult = TuPigAdbAccess.State.ADB_OFF
        openSettings()
        ReflectionHelpers.setField(activity, "tuPigVehicleAdvancedExpanded", true)
        ReflectionHelpers.callInstanceMethod<Unit>(activity, "render")

        selectLegacyMode(true)
        backend.run("tupigcarplay-tupig13-probe")

        assertFalse(TuPigOutputSettings.legacyVehicleProbe(context))
        assertEquals(null, TuPigVehicleFieldStore.load(context))
        assertTrue(texts().any { it.text.toString().contains(activity.getString(R.string.vehicle_data_mode_default)) })
    }

    @Test fun successfulLegacySelectionPersistsAndSwitchingBackKeepsTheProbe() {
        backend.checkStateResult = TuPigAdbAccess.State.READY
        backend.probeResult = TuPigVehicleProbeOutcome(
            TuPigAdbAccess.State.READY,
            supportedCapabilities(TuPigVehicleFieldStore.firmwareKey()),
        )
        openSettings()
        ReflectionHelpers.setField(activity, "tuPigVehicleAdvancedExpanded", true)
        ReflectionHelpers.callInstanceMethod<Unit>(activity, "render")

        selectLegacyMode(true)
        backend.run("tupigcarplay-tupig13-probe")

        assertTrue(TuPigOutputSettings.legacyVehicleProbe(context))
        assertTrue(TuPigVehicleFieldStore.load(context)!!.motionSupported)

        selectLegacyMode(false)

        assertFalse(TuPigOutputSettings.legacyVehicleProbe(context))
        assertTrue(TuPigVehicleFieldStore.load(context)!!.motionSupported)
    }

    @Test fun defaultModeShowsUnreadableDataAndReconnectsOnlyOnceItIsReadable() {
        backend.checkResult = TuPigAdbAccess.Status(TuPigAdbAccess.State.READY)
        openSettings()
        connectCarPlay()
        texts().single { it.text == activity.getString(R.string.advanced_vehicle_data) }.performClick()

        vehicleSwitch(R.string.car_battery_for_the_iphone).performClick()
        backend.run("tupigcarplay-adb-state")

        assertTrue(TuPigOutputSettings.batteryToIphone(context))
        assertTrue(texts().any { it.text == activity.getString(R.string.adb_battery_unreadable) })
        assertEquals(0, reconnects)

        // Approval or a fix on the car later: the next check applies the waiting switch once.
        backend.checkResult = readableStatus()
        texts().single { it.text == activity.getString(R.string.check_adb_access) }.performClick()
        backend.run("tupigcarplay-adb-state")

        assertTrue(texts().any { it.text == activity.getString(R.string.adb_battery_reading, 75, 450) })
        assertEquals(1, reconnects)

        texts().single { it.text == activity.getString(R.string.check_adb_access) }.performClick()
        backend.run("tupigcarplay-adb-state")
        assertEquals(1, reconnects)
    }

    @Test fun switchingToLegacyReconnectsOnlyWhenAVehicleDataSwitchIsOn() {
        backend.checkStateResult = TuPigAdbAccess.State.READY
        backend.probeResult = TuPigVehicleProbeOutcome(
            TuPigAdbAccess.State.READY,
            supportedCapabilities(TuPigVehicleFieldStore.firmwareKey()),
        )
        openSettings()
        connectCarPlay()

        selectLegacyMode(true)
        backend.run("tupigcarplay-tupig13-probe")
        assertTrue(TuPigOutputSettings.legacyVehicleProbe(context))
        assertEquals(0, reconnects)

        selectLegacyMode(false)
        TuPigOutputSettings.setWheelSpeedToIphone(context, true)
        selectLegacyMode(true)
        assertEquals(1, reconnects)
    }

    @Test fun modeChoiceIsDisabledWhileAnAdbCheckRuns() {
        backend.checkResult = readableStatus()
        openSettings()
        texts().single { it.text == activity.getString(R.string.advanced_vehicle_data) }.performClick()

        texts().single { it.text == activity.getString(R.string.check_adb_access) }.performClick()
        assertFalse(modeChoice().isEnabled)

        backend.run("tupigcarplay-adb-state")
        assertTrue(modeChoice().isEnabled)
    }

    @Test fun passingAutomaticValidationClearsAnOldProbeError() {
        val currentKey = TuPigVehicleFieldStore.firmwareKey()
        TuPigVehicleFieldStore.save(context, supportedCapabilities(currentKey))
        TuPigOutputSettings.setVideoWhileParked(context, true)
        backend.checkResult = readableStatus()
        openSettings()
        ReflectionHelpers.setField(
            activity,
            "vehicleProbeOutcome",
            TuPigVehicleProbeOutcome(TuPigAdbAccess.State.READY, error = "old error"),
        )
        texts().single { it.text == activity.getString(R.string.advanced_vehicle_data) }.performClick()

        shadowOf(Looper.getMainLooper()).idle()
        backend.run("tupigcarplay-tupig13-auto-validate")

        assertEquals(null, ReflectionHelpers.getField<TuPigVehicleProbeOutcome?>(activity, "vehicleProbeOutcome"))
        assertFalse(texts().any { it.text.toString().contains("old error") })
    }

    @Test fun anOlderProbeOutcomeCannotHideANewerSavedSnapshot() {
        val currentKey = TuPigVehicleFieldStore.firmwareKey()
        val saved = supportedCapabilities(currentKey).copy(detectedAtMillis = 200)
        val older = supportedCapabilities(
            currentKey,
            without = setOf(TuPigVehicleField.GEAR),
        ).copy(detectedAtMillis = 100)
        TuPigVehicleFieldStore.save(context, saved)
        openSettings()
        ReflectionHelpers.setField(
            activity,
            "vehicleProbeOutcome",
            TuPigVehicleProbeOutcome(TuPigAdbAccess.State.READY, older),
        )
        texts().single { it.text == activity.getString(R.string.advanced_vehicle_data) }.performClick()

        assertTrue(switches().any { it.contentDescription == activity.getString(R.string.video_while_parked) })
    }

    @Test fun theSavedSnapshotIsShownWhateverTheClockSays() {
        val currentKey = TuPigVehicleFieldStore.firmwareKey()
        TuPigVehicleFieldStore.save(context, supportedCapabilities(currentKey).copy(detectedAtMillis = 100))
        openSettings()
        // An older result stamped later, as after the clock was set back.
        ReflectionHelpers.setField(
            activity,
            "vehicleProbeOutcome",
            TuPigVehicleProbeOutcome(
                TuPigAdbAccess.State.READY,
                supportedCapabilities(currentKey, without = setOf(TuPigVehicleField.GEAR)).copy(detectedAtMillis = 200),
            ),
        )
        texts().single { it.text == activity.getString(R.string.advanced_vehicle_data) }.performClick()

        assertTrue(switches().any { it.contentDescription == activity.getString(R.string.video_while_parked) })
    }

    @Test fun samePageRenderKeepsScrollAndPageChangeStartsAtTop() {
        openSettings()
        val first = layoutRoot()
        first.scrollTo(0, 400)
        val savedY = first.scrollY
        assertTrue(savedY > 0)

        ReflectionHelpers.callInstanceMethod<Unit>(activity, "render")
        val second = layoutRoot()
        assertEquals(savedY, second.scrollY)

        ReflectionHelpers.setField(activity, "settingsCategory", SettingsCategory.ABOUT)
        ReflectionHelpers.callInstanceMethod<Unit>(activity, "render")
        val third = layoutRoot()
        assertEquals(0, third.scrollY)
    }

    @Test fun rendersBeforeTheNextLayoutKeepTheScroll() {
        openSettings()
        val first = layoutRoot()
        first.scrollTo(0, 400)
        val savedY = first.scrollY
        assertTrue(savedY > 0)

        // A result arriving behind CarPlay: a stopped window dispatches pre-draw but skips layout.
        ReflectionHelpers.callInstanceMethod<Unit>(activity, "render")
        activity.window.decorView.viewTreeObserver.dispatchOnPreDraw()
        // Back from CarPlay: onNewIntent and onResume both render before the next frame.
        ReflectionHelpers.callInstanceMethod<Unit>(activity, "render")
        ReflectionHelpers.callInstanceMethod<Unit>(activity, "render")

        assertEquals(savedY, layoutRoot().scrollY)
    }

    private fun openSettings() {
        controller = Robolectric.buildActivity(
            TuPigCarPlayActivity::class.java,
            Intent(context, TuPigCarPlayActivity::class.java).putExtra("page", "settings"),
        ).setup()
        openCategory(R.string.settings_advanced)
    }

    private fun openCategory(title: Int) {
        val description = activity.getString(R.string.settings_open_category, activity.getString(title))
        var target = descendants(activity.window.decorView).firstOrNull { it.contentDescription == description }
        if (target == null) {
            activity.onBackPressedDispatcher.onBackPressed()
            target = descendants(activity.window.decorView).first { it.contentDescription == description }
        }
        target.performClick()
    }

    private fun switches(): Sequence<Switch> = descendants(activity.window.decorView).filterIsInstance<Switch>()

    private fun texts(): Sequence<TextView> = descendants(activity.window.decorView).filterIsInstance<TextView>()

    private fun vehicleSwitch(title: Int): Switch = switches()
        .single { it.contentDescription == activity.getString(title) }

    private fun invokeProbe() {
        ReflectionHelpers.callInstanceMethod<Unit>(activity, "probeVehicleData",
            ReflectionHelpers.ClassParameter.from(Boolean::class.javaPrimitiveType!!, true))
    }

    /** Three-finger gesture or Back to TuPigCarPlay: the settings page comes back through a new intent. */
    private fun returnFromCarPlay() {
        ReflectionHelpers.callInstanceMethod<Unit>(activity, "onNewIntent",
            ReflectionHelpers.ClassParameter.from(
                Intent::class.java,
                Intent(context, TuPigCarPlayActivity::class.java).putExtra("page", "settings"),
            ))
        shadowOf(Looper.getMainLooper()).idle()
    }

    private fun selectLegacyMode(enabled: Boolean) {
        ReflectionHelpers.callInstanceMethod<Unit>(activity, "selectVehicleDataMode",
            ReflectionHelpers.ClassParameter.from(Boolean::class.javaPrimitiveType!!, enabled))
    }

    private fun modeChoice(): TextView = texts()
        .single { it.text.startsWith(activity.getString(R.string.vehicle_data_mode) + " · ") }

    /** As in LocationReportingSettingsTest: a connected host without transports; counts reconnects. */
    private fun connectCarPlay() {
        AirPlayPersistence.saveWirelessEnabled(context, false)
        ReflectionHelpers.setField(activity, "setupError", null)
        val stop: ((() -> Unit) -> Unit) = { completion ->
            reconnects++
            completion()
        }
        ReflectionHelpers.setField(CarPlayBackgroundSession, "stopAction", stop)
    }

    private fun readableStatus() = TuPigAdbAccess.Status(
        state = TuPigAdbAccess.State.READY,
        batteryPercent = 75.0,
        rangeKm = 450,
        speedKmh = 0.0,
        gear = VehicleGear.PARK,
    )

    private fun layoutRoot(): ScrollView {
        val decor = activity.window.decorView
        decor.measure(
            View.MeasureSpec.makeMeasureSpec(1080, View.MeasureSpec.EXACTLY),
            View.MeasureSpec.makeMeasureSpec(600, View.MeasureSpec.EXACTLY),
        )
        decor.layout(0, 0, 1080, 600)
        return descendants(decor).filterIsInstance<ScrollView>().first()
    }

    private fun supportedCapabilities(
        firmwareKey: String = "test",
        without: Set<TuPigVehicleField> = emptySet(),
    ) = TuPigVehicleCapabilities(
        fields = TuPigVehicleField.entries.associateWith { field ->
            val value = when (field) {
                TuPigVehicleField.SPEED -> 0.0
                TuPigVehicleField.GEAR -> 1.0
                TuPigVehicleField.SOC -> 75.0
                TuPigVehicleField.RANGE -> 450.0
                TuPigVehicleField.REMAINING_KWH -> 60.0
                TuPigVehicleField.BMS_STATE -> 0.0
            }
            if (field in without) TuPigFieldProbeResult(field, supported = false, address = null)
            else TuPigFieldProbeResult(
                    field,
                    supported = true,
                    address = TuPigReadAddress(1000, field.ordinal + 1,
                        if (field in setOf(TuPigVehicleField.SPEED, TuPigVehicleField.SOC, TuPigVehicleField.REMAINING_KWH)) 7 else 5,
                        TuPigFieldSource.FIRMWARE),
                    value = value,
                )
        },
        catalogAvailable = true,
        firmwareKey = firmwareKey,
    )

    private class FakeVehicleSettingsBackend : TuPigVehicleSettingsBackend {
        var checkResult = TuPigAdbAccess.Status(TuPigAdbAccess.State.ADB_OFF)
        var checkStateResult = TuPigAdbAccess.State.ADB_OFF
        var probeResult = TuPigVehicleProbeOutcome(TuPigAdbAccess.State.ADB_OFF)
        /** Returned by the next probes before [probeResult]. */
        val queuedProbeResults = ArrayDeque<TuPigVehicleProbeOutcome>()
        var onCheck: (() -> Unit)? = null
        var onProbe: (() -> Unit)? = null
        var checkCalls = 0
        var probeCalls = 0
        val tasks = mutableListOf<Pair<String, () -> Unit>>()

        override fun check(context: android.content.Context, mayAsk: Boolean): TuPigAdbAccess.Status {
            checkCalls++
            onCheck?.invoke()
            return checkResult
        }

        override fun checkState(context: android.content.Context, mayAsk: Boolean) = checkStateResult

        override fun probe(context: android.content.Context, persist: Boolean): TuPigVehicleProbeOutcome {
            probeCalls++
            onProbe?.invoke()
            return queuedProbeResults.removeFirstOrNull() ?: probeResult
        }

        override fun execute(name: String, block: () -> Unit) {
            tasks += name to block
        }

        fun run(name: String) {
            val index = tasks.indexOfFirst { it.first == name }
            check(index >= 0) { "No queued task named $name; queued=${tasks.map { it.first }}" }
            tasks.removeAt(index).second.invoke()
            shadowOf(Looper.getMainLooper()).idle()
        }

        fun runAll(name: String) {
            while (tasks.any { it.first == name }) run(name)
        }
    }

    private fun descendants(view: View): Sequence<View> = sequence {
        yield(view)
        if (view is ViewGroup) for (index in 0 until view.childCount) yieldAll(descendants(view.getChildAt(index)))
    }
}
