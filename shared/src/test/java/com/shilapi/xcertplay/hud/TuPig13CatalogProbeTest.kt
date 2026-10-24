package com.shilapi.xcertplay.hud

import android.content.Context
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import org.robolectric.annotation.Config

class TuPig13CatalogProbeTest {
    @Test fun parsesController13FirmwareSymbolsAndDevices() {
        val catalog = TuPigFirmwareCatalog.parse(
            """
            ${TuPig13CatalogProbeMain.HEADER}
            BYDAutoConstants.BYDAUTO_DEVICE_SPEED=1013
            BYDAutoConstants.BYDAUTO_DEVICE_GEARBOX=1011
            BYDAutoConstants.BYDAUTO_DEVICE_STATISTIC=1014
            BYDAutoConstants.BYDAUTO_DEVICE_POWER=1005
            BYDAutoConstants.BYDAUTO_DEVICE_CHARGING=1009
            Speed.SPEED_AUTO_SPEED=-1176502256
            Gearbox.GEARBOX_AUTO_MODE_TYPE=555745336
            Statistic.STATISTIC_ELEC_PERCENTAGE=1033543720
            Statistic.STATISTIC_ELEC_DRIVING_RANGE=1033543766
            Power.POWER_BATTERY_REMAIN_ELECTRICITY=1320165408
            Charging.CHARGING_BATTERRY_DEVICE_STATE=1231032336
            """.trimIndent()
        )!!

        assertEquals(-1176502256, catalog.fid(TuPigVehicleField.SPEED))
        assertEquals(1033543720, catalog.fid(TuPigVehicleField.SOC))
        assertEquals(1231032336, catalog.fid(TuPigVehicleField.BMS_STATE))
        assertEquals(1013, catalog.device(TuPigVehicleField.SPEED))
        assertEquals(1009, catalog.device(TuPigVehicleField.BMS_STATE))
    }

    @Test fun rejectsOutputWithoutProbeHeader() {
        assertEquals(null, TuPigFirmwareCatalog.parse("Speed.SPEED_AUTO_SPEED=-1176502256"))
    }

    @Test fun firmwareResolvedController13FieldsAreLiveValidated() {
        val catalogOutput = """
            ${TuPig13CatalogProbeMain.HEADER}
            BYDAutoConstants.BYDAUTO_DEVICE_SPEED=1013
            BYDAutoConstants.BYDAUTO_DEVICE_GEARBOX=1011
            BYDAutoConstants.BYDAUTO_DEVICE_STATISTIC=1014
            BYDAutoConstants.BYDAUTO_DEVICE_POWER=1005
            BYDAutoConstants.BYDAUTO_DEVICE_CHARGING=1009
            Speed.SPEED_AUTO_SPEED=-1176502256
            Gearbox.GEARBOX_AUTO_MODE_TYPE=555745336
            Statistic.STATISTIC_ELEC_PERCENTAGE=1033543720
            Statistic.STATISTIC_ELEC_DRIVING_RANGE=1033543766
            Power.POWER_BATTERY_REMAIN_ELECTRICITY=1320165408
            Charging.CHARGING_BATTERRY_DEVICE_STATE=1231032336
            """.trimIndent()
        val replies = mapOf(
            -1176502256 to "42100000", // 36 km/h
            555745336 to "00000001", // P
            1033543720 to "41c80000", // 25 %
            1033543766 to "00000096", // 150 km
            1320165408 to "41c8cccd", // 25.1 kWh
            1231032336 to "00000001", // charging
        )

        val batch = TuPigVehicleCapabilityProbe.probeFields(catalogOutput, 29, { command ->
            replies[command.substringAfterLast(' ').toInt()]?.let {
                "Result: Parcel(00000000 $it   '........')"
            }
        }, firmwareKey = "test")

        assertEquals(6, batch.transportReplies)
        assertEquals(6, batch.repliedFields)
        assertTrue(batch.complete)
        assertTrue(batch.capabilities.motionSupported)
        assertTrue(batch.capabilities.batterySupported)
        assertTrue(batch.capabilities.chargingSupported)
        assertEquals(36.0, batch.capabilities.result(TuPigVehicleField.SPEED).value!!, 0.001)
        assertEquals(TuPigFieldSource.FIRMWARE, batch.capabilities.result(TuPigVehicleField.SOC).address!!.source)
    }

    @Test fun aPartialTransportResponseIsNotACompleteProbe() {
        val batch = TuPigVehicleCapabilityProbe.probeFields("catalog unavailable", 29, { command ->
            if (command.contains(" i32 1013 ")) "Result: Parcel(00000000 00000000 '........')" else null
        }, firmwareKey = "test")

        assertFalse(batch.complete)
        assertTrue(batch.repliedFields < TuPigVehicleField.entries.size)
    }

    @Test fun aNullCatalogOutputMakesAnOtherwiseCompleteFallbackProbeIncomplete() {
        var reads = 0
        val batch = TuPigVehicleCapabilityProbe.probeFields(
            null,
            29,
            { reads++; "Result: Parcel(00000000 00000001 '........')" },
            firmwareKey = "test",
        )

        assertFalse(batch.complete)
        assertTrue(batch.failedReads > 0)
        // Already incomplete, so no field is read.
        assertEquals(0, reads)
    }

    @Test fun aNullPreferredReadCannotBeHiddenByADecodableFallback() {
        val catalogOutput = """
            ${TuPig13CatalogProbeMain.HEADER}
            BYDAutoConstants.BYDAUTO_DEVICE_SPEED=1013
            Speed.SPEED_AUTO_SPEED=-42
        """.trimIndent()
        var reads = 0
        val batch = TuPigVehicleCapabilityProbe.probeFields(
            catalogOutput,
            29,
            { command ->
                reads++
                if (command.endsWith(" i32 -42")) null
                else "Result: Parcel(00000000 00000001 '........')"
            },
            firmwareKey = "test",
        )

        assertFalse(batch.complete)
        assertEquals(1, batch.failedReads)
        // The probe stops at the failed read instead of waiting on the fallback and later fields.
        assertEquals(1, reads)
    }

    @Test fun nonNullCatalogOutputWithoutTheHeaderAllowsFallbacks() {
        val batch = TuPigVehicleCapabilityProbe.probeFields(
            "catalog unavailable",
            29,
            { "Result: Parcel(00000000 00000001 '........')" },
            firmwareKey = "test",
        )

        assertTrue(batch.complete)
        assertFalse(batch.capabilities.catalogAvailable)
    }
}

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [29], manifest = Config.NONE)
class TuPigVehicleFieldStoreTest {
    private val context get() = RuntimeEnvironment.getApplication()

    @Before fun setUp() {
        context.getSharedPreferences("tupigcarplay_leapmotor_vehicle_fields", Context.MODE_PRIVATE).edit().clear().commit()
        context.getSharedPreferences("tupigcarplay_leapmotor_outputs", Context.MODE_PRIVATE).edit().clear().commit()
        TuPigVehicleFieldStore.clearMemoryForTests()
    }

    @After fun tearDown() {
        setUp()
    }

    @Test fun legacyFeaturesStayInactiveUntilAProbeIsCached() {
        TuPigOutputSettings.setLegacyVehicleProbe(context, true)
        TuPigOutputSettings.setBatteryToIphone(context, true)
        TuPigOutputSettings.setWheelSpeedToIphone(context, true)
        TuPigOutputSettings.setVideoWhileParked(context, true)

        assertFalse(TuPigOutputSettings.batteryToIphoneActive(context))
        assertFalse(TuPigOutputSettings.wheelSpeedToIphoneActive(context))
        assertFalse(TuPigOutputSettings.videoWhileParkedActive(context))

        TuPigVehicleFieldStore.save(context, supportedCapabilities())
        // Model a killed/restarted app process: the persisted probe, not the singleton, must win.
        TuPigVehicleFieldStore.clearMemoryForTests()

        assertTrue(TuPigOutputSettings.batteryToIphoneActive(context))
        assertTrue(TuPigOutputSettings.wheelSpeedToIphoneActive(context))
        assertTrue(TuPigOutputSettings.videoWhileParkedActive(context))
        assertEquals(-1176502256, TuPigVehicleFieldStore.address(context, TuPigVehicleField.SPEED).fid)
        assertEquals(TuPigFieldSource.FIRMWARE, TuPigVehicleFieldStore.address(context, TuPigVehicleField.SPEED).source)
    }

    @Test fun socAndRangeRemainUsableWhenController13HasNoRemainingKwhField() {
        TuPigVehicleFieldStore.save(context, supportedCapabilities(without = setOf(TuPigVehicleField.REMAINING_KWH)))
        val replies = mapOf(
            1033543720 to "41c80000", // 25 %
            1033543766 to "00000096", // 150 km
            1231032336 to "00000001", // charging
        )
        val reading = TuPigBattery.read(context) { command ->
            replies[command.substringAfterLast(' ').toInt()]?.let {
                "Result: Parcel(00000000 $it   '........')"
            }
        }!!

        assertNull(reading.remainingKwh)
        assertTrue(reading.charging)
        TuPigOutputSettings.setBatteryToIphone(context, true)
        assertTrue(TuPigOutputSettings.batteryToIphoneActive(context))
    }

    @Test fun aConfirmedEnergyFieldMustRemainReadable() {
        TuPigVehicleFieldStore.save(context, supportedCapabilities())
        val replies = mapOf(
            1033543720 to "41c80000",
            1033543766 to "00000096",
            1231032336 to "00000001",
        )

        assertNull(TuPigBattery.read(context) { command ->
            replies[command.substringAfterLast(' ').toInt()]?.let {
                "Result: Parcel(00000000 $it   '........')"
            }
        })
    }

    @Test fun freshInstallUsesTheDefaultDiLink5Commands() {
        assertFalse(TuPigOutputSettings.legacyVehicleProbe(context))
        assertEquals(TuPigWheelSpeed.SPEED, TuPigWheelSpeed.speedCommand(context))
        assertEquals(TuPigWheelSpeed.GEAR, TuPigWheelSpeed.gearCommand(context))

        TuPigOutputSettings.setBatteryToIphone(context, true)
        TuPigOutputSettings.setWheelSpeedToIphone(context, true)
        TuPigOutputSettings.setVideoWhileParked(context, true)
        assertTrue(TuPigOutputSettings.batteryToIphoneActive(context))
        assertTrue(TuPigOutputSettings.wheelSpeedToIphoneActive(context))
        assertTrue(TuPigOutputSettings.videoWhileParkedActive(context))
    }

    @Test fun savedProbeMigratesToLegacyModeButCanSwitchBackWithoutDeletingIt() {
        TuPigVehicleFieldStore.save(context, supportedCapabilities())

        assertTrue(TuPigOutputSettings.legacyVehicleProbe(context))
        assertEquals(-1176502256, TuPigWheelSpeed.speedCommand(context).substringAfterLast(' ').toInt())

        TuPigOutputSettings.setLegacyVehicleProbe(context, false)

        assertFalse(TuPigOutputSettings.legacyVehicleProbe(context))
        assertEquals(TuPigWheelSpeed.SPEED, TuPigWheelSpeed.speedCommand(context))
        assertTrue(TuPigVehicleFieldStore.load(context)!!.motionSupported)
    }

    @Test fun aSavedProbeSurvivesFirmwareMetadataChanges() {
        TuPigVehicleFieldStore.save(context, supportedCapabilities())
        context.getSharedPreferences("tupigcarplay_leapmotor_vehicle_fields", Context.MODE_PRIVATE)
            .edit().putString("firmware", "previous-controller-13-build").commit()
        TuPigVehicleFieldStore.clearMemoryForTests()

        val loaded = TuPigVehicleFieldStore.load(context)!!
        assertEquals("previous-controller-13-build", loaded.firmwareKey)
        assertTrue(loaded.motionSupported)
        assertTrue(loaded.batterySupported)
    }

    @Test fun hiddenCapabilitiesDoNotEraseTheirSavedSwitches() {
        TuPigOutputSettings.setWheelSpeedToIphone(context, true)
        TuPigVehicleFieldStore.save(
            context,
            supportedCapabilities(without = setOf(TuPigVehicleField.SPEED, TuPigVehicleField.GEAR)),
        )

        assertTrue(TuPigOutputSettings.wheelSpeedToIphone(context))
        assertFalse(TuPigOutputSettings.wheelSpeedToIphoneActive(context))

        TuPigVehicleFieldStore.save(context, supportedCapabilities())
        assertTrue(TuPigOutputSettings.wheelSpeedToIphone(context))
        assertTrue(TuPigOutputSettings.wheelSpeedToIphoneActive(context))
    }

    @Test fun automaticReplacementCannotDropPreviouslyConfirmedFields() {
        TuPigVehicleFieldStore.save(context, supportedCapabilities())
        val expected = TuPigVehicleFieldStore.load(context)
        val candidate = supportedCapabilities(without = setOf(TuPigVehicleField.GEAR))

        val result = TuPigVehicleFieldStore.replaceAutomatically(context, expected, candidate)

        assertEquals(setOf(TuPigVehicleField.GEAR), result.lostFields)
        assertFalse(result.saved)
        assertTrue(TuPigVehicleFieldStore.load(context)!!.gearSupported)
    }

    @Test fun anExplicitReplacementMayDropFieldsButNotOverwriteANewerSnapshot() {
        TuPigVehicleFieldStore.save(context, supportedCapabilities())
        val expected = TuPigVehicleFieldStore.load(context)!!
        val candidate = supportedCapabilities(without = setOf(TuPigVehicleField.GEAR))
        val newer = supportedCapabilities().copy(detectedAtMillis = expected.detectedAtMillis + 1)
        TuPigVehicleFieldStore.save(context, newer)

        assertFalse(TuPigVehicleFieldStore.replaceAnyway(context, expected, candidate))
        assertEquals(newer, TuPigVehicleFieldStore.load(context))

        assertTrue(TuPigVehicleFieldStore.replaceAnyway(context, newer, candidate))
        assertFalse(TuPigVehicleFieldStore.load(context)!!.gearSupported)
    }

    @Test fun lateAutomaticReplacementCannotOverwriteANewerSnapshot() {
        TuPigVehicleFieldStore.save(context, supportedCapabilities())
        val expected = TuPigVehicleFieldStore.load(context)!!
        val newer = supportedCapabilities().copy(detectedAtMillis = expected.detectedAtMillis + 1)
        TuPigVehicleFieldStore.save(context, newer)

        val result = TuPigVehicleFieldStore.replaceAutomatically(
            context,
            expected,
            supportedCapabilities().copy(detectedAtMillis = expected.detectedAtMillis + 2),
        )

        assertTrue(result.snapshotChanged)
        assertFalse(result.saved)
        assertEquals(newer.detectedAtMillis, TuPigVehicleFieldStore.load(context)!!.detectedAtMillis)
    }

    @Test fun incompleteNullCatalogProbeCannotReplaceTheSavedSnapshot() {
        val saved = supportedCapabilities()
        TuPigVehicleFieldStore.save(context, saved)
        val batch = TuPigVehicleCapabilityProbe.probeFields(
            null,
            29,
            { "Result: Parcel(00000000 00000001 '........')" },
        )

        val outcome = TuPigVehicleCapabilityProbe.finishProbe(
            context,
            TuPigAdbAccess.State.READY,
            batch,
            persist = true,
        )

        assertNull(outcome.capabilities)
        assertEquals(saved.detectedAtMillis, TuPigVehicleFieldStore.load(context)!!.detectedAtMillis)
    }

    @Test fun incompletePreferredReadCannotReplaceTheSavedSnapshot() {
        val saved = supportedCapabilities()
        TuPigVehicleFieldStore.save(context, saved)
        val catalogOutput = """
            ${TuPig13CatalogProbeMain.HEADER}
            BYDAutoConstants.BYDAUTO_DEVICE_SPEED=1013
            Speed.SPEED_AUTO_SPEED=-42
        """.trimIndent()
        val batch = TuPigVehicleCapabilityProbe.probeFields(
            catalogOutput,
            29,
            { command ->
                if (command.endsWith(" i32 -42")) null
                else "Result: Parcel(00000000 00000001 '........')"
            },
        )

        val outcome = TuPigVehicleCapabilityProbe.finishProbe(
            context,
            TuPigAdbAccess.State.READY,
            batch,
            persist = true,
        )

        assertNull(outcome.capabilities)
        assertEquals(saved.detectedAtMillis, TuPigVehicleFieldStore.load(context)!!.detectedAtMillis)
    }

    private fun supportedCapabilities(without: Set<TuPigVehicleField> = emptySet()): TuPigVehicleCapabilities {
        val fids = mapOf(
            TuPigVehicleField.SPEED to -1176502256,
            TuPigVehicleField.GEAR to 555745336,
            TuPigVehicleField.SOC to 1033543720,
            TuPigVehicleField.RANGE to 1033543766,
            TuPigVehicleField.REMAINING_KWH to 1320165408,
            TuPigVehicleField.BMS_STATE to 1231032336,
        )
        return TuPigVehicleCapabilities(
            fields = TuPigVehicleField.entries.associateWith { field ->
                if (field in without) TuPigFieldProbeResult(field, supported = false, address = null)
                else TuPigFieldProbeResult(
                        field,
                        supported = true,
                        address = TuPigReadAddress(field.defaultDevice, fids.getValue(field), field.transaction, TuPigFieldSource.FIRMWARE),
                    )
            },
            catalogAvailable = true,
            firmwareKey = TuPigVehicleFieldStore.firmwareKey(),
        )
    }
}
