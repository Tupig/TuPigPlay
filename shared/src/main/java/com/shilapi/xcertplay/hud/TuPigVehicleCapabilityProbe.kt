package com.shilapi.xcertplay.hud

import android.content.Context
import android.os.Build
import com.shilapi.xcertplay.adb.AdbKeys
import com.shilapi.xcertplay.adb.LocalAdb

data class TuPigVehicleProbeOutcome(
    val access: TuPigAdbAccess.State,
    val capabilities: TuPigVehicleCapabilities? = null,
    val error: String? = null,
)

/** User-initiated, read-only detection for controller-13 / DiLink 3 head units. */
object TuPigVehicleCapabilityProbe {
    // One probe at a time: a cancelled automatic re-probe still reading must not push a new one into timeouts.
    @Synchronized
    fun probe(context: Context, persist: Boolean = true): TuPigVehicleProbeOutcome {
        val app = context.applicationContext
        LocalAdb(AdbKeys.load(app)).use { adb ->
            val access = TuPigAdbAccess.state(adb.connect(mayAsk = false))
            if (access != TuPigAdbAccess.State.READY) return TuPigVehicleProbeOutcome(access)

            val catalogOutput = adb.shell(catalogCommand(app), 15_000)
            val batch = probeFields(catalogOutput, Build.VERSION.SDK_INT, adb::shell)
            return finishProbe(app, access, batch, persist)
        }
    }

    internal fun finishProbe(
        context: Context,
        access: TuPigAdbAccess.State,
        batch: ProbeBatch,
        persist: Boolean,
    ): TuPigVehicleProbeOutcome {
        if (!batch.complete) {
            return TuPigVehicleProbeOutcome(
                access,
                error = "autoservice probe incomplete: repliedFields=${batch.repliedFields}/" +
                    "${TuPigVehicleField.entries.size} failedReads=${batch.failedReads}",
            )
        }
        val capabilities = batch.capabilities
        if (persist) TuPigVehicleFieldStore.save(context, capabilities)
        return TuPigVehicleProbeOutcome(access, capabilities)
    }

    private fun catalogCommand(context: Context): String {
        val apk = shellQuote(context.packageCodePath)
        return "CLASSPATH=$apk app_process /system/bin ${TuPig13CatalogProbeMain::class.java.name}"
    }

    private fun shellQuote(value: String): String = "'" + value.replace("'", "'\"'\"'") + "'"

    internal data class ProbeBatch(
        val capabilities: TuPigVehicleCapabilities,
        val transportReplies: Int,
        val repliedFields: Int,
        val failedReads: Int,
    ) {
        val complete: Boolean get() =
            failedReads == 0 && repliedFields == TuPigVehicleField.entries.size
    }

    internal fun probeFields(
        catalogOutput: String?,
        sdkInt: Int,
        shell: (String) -> String?,
        firmwareKey: String = TuPigVehicleFieldStore.firmwareKey(),
    ): ProbeBatch {
        val catalog = catalogOutput?.let(TuPigFirmwareCatalog::parse)
        var transportReplies = 0
        var repliedFields = 0
        var failedReads = if (catalogOutput == null) 1 else 0
        val results = LinkedHashMap<TuPigVehicleField, TuPigFieldProbeResult>()
        for (field in TuPigVehicleField.entries) {
            // A failed read already makes the probe incomplete: stop rather than wait out more timeouts.
            if (failedReads > 0) break
            var accepted: TuPigFieldProbeResult? = null
            var lastFailure = "no candidate"
            var receivedReply = false
            for (candidate in candidates(field, catalog, sdkInt)) {
                val output = shell(candidate.command())
                if (output != null) {
                    transportReplies++
                    receivedReply = true
                } else {
                    failedReads++
                }
                val decoded = decode(field, output)
                if (decoded != null) {
                    accepted = TuPigFieldProbeResult(field, true, candidate, decoded)
                    break
                }
                lastFailure = if (output == null) "ADB read failed" else "unsupported reply"
                if (output == null) break
            }
            if (receivedReply) repliedFields++
            results[field] = accepted ?: TuPigFieldProbeResult(field, false, null, failure = lastFailure)
        }
        return ProbeBatch(
            capabilities = TuPigVehicleCapabilities(
                fields = results,
                catalogAvailable = catalog != null,
                firmwareKey = firmwareKey,
            ),
            transportReplies = transportReplies,
            repliedFields = repliedFields,
            failedReads = failedReads,
        )
    }

    private fun candidates(field: TuPigVehicleField, catalog: TuPigFirmwareCatalog?, sdkInt: Int): List<TuPigReadAddress> {
        val values = ArrayList<TuPigReadAddress>()
        catalog?.fid(field)?.let { fid ->
            values += TuPigReadAddress(catalog.device(field), fid, field.transaction, TuPigFieldSource.FIRMWARE)
        }
        if (sdkInt <= Build.VERSION_CODES.Q) values += known13(field)
        values += TuPigVehicleFieldStore.defaultAddress(field)
        return values.distinctBy { Triple(it.transaction, it.device, it.fid) }
    }

    /** Field reports observed on controller-13 / DiLink 3; every value is still live-probed. */
    private fun known13(field: TuPigVehicleField): List<TuPigReadAddress> = when (field) {
        TuPigVehicleField.SPEED -> listOf(TuPigReadAddress(1013, -1176502256, 7, TuPigFieldSource.KNOWN_13))
        TuPigVehicleField.GEAR -> listOf(TuPigReadAddress(1011, 555745336, 5, TuPigFieldSource.KNOWN_13))
        TuPigVehicleField.SOC -> listOf(TuPigReadAddress(1014, 1033543720, 7, TuPigFieldSource.KNOWN_13))
        TuPigVehicleField.BMS_STATE -> listOf(
            TuPigReadAddress(1009, 1231032336, 5, TuPigFieldSource.KNOWN_13),
            TuPigReadAddress(1009, 876611608, 5, TuPigFieldSource.KNOWN_13),
        )
        else -> emptyList()
    }

    private fun decode(field: TuPigVehicleField, output: String?): Double? {
        val word = TuPigParcel.value(output) ?: return null
        return when (field) {
            TuPigVehicleField.SPEED -> java.lang.Float.intBitsToFloat(word).toDouble()
                .takeIf { it.isFinite() && it in 0.0..300.0 }
            TuPigVehicleField.GEAR -> word.toDouble().takeIf { word in 1..6 }
            TuPigVehicleField.SOC -> java.lang.Float.intBitsToFloat(word).toDouble()
                .takeIf { it.isFinite() && it in 0.0..100.0 }
            TuPigVehicleField.RANGE -> word.toDouble().takeIf { word in 0..3000 }
            TuPigVehicleField.REMAINING_KWH -> java.lang.Float.intBitsToFloat(word).toDouble()
                .takeIf { it.isFinite() && it in 0.0..300.0 }
            TuPigVehicleField.BMS_STATE -> word.toDouble().takeIf { word in 0..255 }
        }
    }

    internal fun publishBatteryReading(context: Context, capabilities: TuPigVehicleCapabilities) {
        if (!TuPigOutputSettings.legacyVehicleProbe(context)) return
        if (!capabilities.batterySupported) return
        val percent = capabilities.result(TuPigVehicleField.SOC).value ?: return
        val range = capabilities.result(TuPigVehicleField.RANGE).value?.toInt() ?: return
        val measured = capabilities.result(TuPigVehicleField.REMAINING_KWH).value
        val charging = capabilities.result(TuPigVehicleField.BMS_STATE).value?.toInt() == 1
        TuPigBatteryStatus.publishProbeReading(context, TuPigBatteryReading(percent, range, measured, charging))
    }
}
