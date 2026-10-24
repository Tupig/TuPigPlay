package com.shilapi.xcertplay.hud

import android.content.Context
import android.util.Log
import android.os.SystemClock
import com.shilapi.xcertplay.transport.VehicleStatusProvider
import com.shilapi.xcertplay.transport.VehicleStatusSnapshot
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit
import kotlin.math.roundToInt
import kotlin.math.roundToLong

internal enum class TuPigBatteryProtocol(val percentId: Int, val rangeId: Int, val chargingStateId: Int) {
    CAN(1033543720, 1033203771, 876611608),
    CANFD(1246777400, 1246765118, 876609560),
}

/** The traction battery as TuPig's statistic, power and charging devices report it. */
internal data class TuPigBatteryReading(
    val percent: Double,
    val rangeKm: Int,
    val remainingKwh: Double?,
    val charging: Boolean,
    val protocol: TuPigBatteryProtocol? = null,
)

/**
 * Reads the traction battery through the adb shell (autoservice binder), where apps would need a TuPig
 * signature. The head unit's ro.car.protocol (or sys.car.protocol) selects the SDK addresses, not its
 * Android version.
 */
internal object TuPigBattery {
    private const val REMAINING = "service call autoservice 7 i32 1005 i32 882901008" // float kWh
    private const val BMS_CHARGING = 1

    /** Unknown protocols give no reading; CAN has no verified remaining-energy address. */
    fun read(shell: (String) -> String?): TuPigBatteryReading? {
        // Most firmware sets ro.car.protocol; the Tang's DiLink 5 (dynasty di5, 2025) sets only sys.car.protocol.
        val property = shell("getprop ro.car.protocol")?.trim()?.takeIf { it.isNotEmpty() }
            ?: shell("getprop sys.car.protocol")?.trim()
        val protocol = when (property) {
            "CAN" -> TuPigBatteryProtocol.CAN
            "CANFD" -> TuPigBatteryProtocol.CANFD
            else -> return null
        }
        val percent = TuPigParcel.value(shell("service call autoservice 7 i32 1014 i32 ${protocol.percentId}"))
            ?.let(::float)?.takeIf { it in 0.0..100.0 } ?: return null
        val range = TuPigParcel.value(shell("service call autoservice 5 i32 1014 i32 ${protocol.rangeId}"))
            ?.takeIf { it in 0..3000 } ?: return null
        val remaining = if (protocol == TuPigBatteryProtocol.CANFD) {
            TuPigParcel.value(shell(REMAINING))?.let(::float)?.takeIf { it in 0.0..300.0 } ?: return null
        } else null
        val bms = TuPigParcel.value(shell("service call autoservice 5 i32 1009 i32 ${protocol.chargingStateId}"))
        return TuPigBatteryReading(percent, range, remaining, bms == BMS_CHARGING, protocol)
    }

    fun read(context: Context, shell: (String) -> String?): TuPigBatteryReading? {
        if (!TuPigOutputSettings.legacyVehicleProbe(context)) return read(shell)
        val cached = TuPigVehicleFieldStore.load(context)
            ?: return null
        val bmsAddress = cached.result(TuPigVehicleField.BMS_STATE).address
        val remainingAddress = cached.result(TuPigVehicleField.REMAINING_KWH).address
        return readDetected(
            cached.result(TuPigVehicleField.SOC).address?.command() ?: return null,
            cached.result(TuPigVehicleField.RANGE).address?.command() ?: return null,
            remainingAddress?.command(),
            bmsAddress?.command(),
            shell = shell,
        )
    }

    private fun readDetected(
        percentCommand: String,
        rangeCommand: String,
        remainingCommand: String?,
        bmsCommand: String?,
        shell: (String) -> String?,
    ): TuPigBatteryReading? {
        val percent = TuPigParcel.value(shell(percentCommand))?.let(::float)?.takeIf { it in 0.0..100.0 } ?: return null
        val range = TuPigParcel.value(shell(rangeCommand))?.takeIf { it in 0..3000 } ?: return null
        val measured = if (remainingCommand == null) null else {
            TuPigParcel.value(shell(remainingCommand))?.let(::float)?.takeIf { it in 0.0..300.0 }
                ?: return null
        }
        val bms = bmsCommand?.let { TuPigParcel.value(shell(it)) }
        return TuPigBatteryReading(percent, range, measured, bms == BMS_CHARGING)
    }

    /**
     * What the iPhone gets. Full charge and full range are scaled up from the current values; below
     * 20 % the rounding of the percentage would make that scale jumpy, so [fullKwh] from an earlier,
     * higher reading is used when there is one.
     */
    fun snapshot(reading: TuPigBatteryReading, lowPercent: Int, fullKwh: Double?): VehicleStatusSnapshot {
        val fraction = reading.percent / 100
        val full = fullKwh ?: reading.remainingKwh?.let { if (fraction > 0) it / fraction else it }
        return VehicleStatusSnapshot(
            rangeKm = reading.rangeKm,
            rangeWarning = reading.percent <= lowPercent,
            batteryPercent = reading.percent,
            currentChargeWh = reading.remainingKwh?.let { (it * 1000).roundToLong() },
            maxChargeWh = full?.let { (it * 1000).roundToLong() },
            maxRangeKm = if (fraction > 0) (reading.rangeKm / fraction).roundToInt() else reading.rangeKm,
            charging = reading.charging,
        )
    }

    /** A full-charge estimate from [reading], when the percentage is high enough to trust it. */
    fun fullKwh(reading: TuPigBatteryReading): Double? =
        if (reading.percent >= 20) reading.remainingKwh?.div(reading.percent / 100) else null

    private fun float(bits: Int): Double = java.lang.Float.intBitsToFloat(bits).toDouble()
}

/**
 * Optional, needs ADB over network: tells the iPhone the car's charge and range, so Apple Maps can
 * warn about a low charge and suggest chargers. Reads the battery every 30 s while the iPhone asks
 * for vehicle status; the iAP2 loop only takes the last reading, so it never waits for adb.
 */
internal object TuPigBatteryStatus : VehicleStatusProvider {
    private const val TAG = "TuPigCarPlay-TuPig-Battery"
    private const val READ_MILLIS = 30_000L
    private const val IDLE_MILLIS = 2 * 60_000L

    private val shell = TuPigAdbShell(TAG)
    @Volatile private var context: Context? = null
    private var started = false
    private val cache = TuPigBatteryCache(::now)
    private val readLock = Any()
    // Never hold this short cache-publication lock while reading fields or waiting on ADB.
    private val publicationLock = Any()
    private var probePublicationGeneration = 0L
    @Volatile internal var readBattery: (Context) -> TuPigBatteryReading? = { app ->
        TuPigBattery.read(app) { shell.run(app, it) }
    }
    private val executor = Executors.newSingleThreadScheduledExecutor { runnable ->
        Thread(runnable, "tupigcarplay-battery").apply { isDaemon = true }
    }
    @Volatile private var askedMillis = 0L

    @Synchronized
    fun start(appContext: Context) {
        context = appContext.applicationContext
        askedMillis = now()
        if (started) {
            // A reconnect after idle must not wait for the next 30-second tick.
            executor.execute(::poll)
        } else {
            started = true
            executor.scheduleWithFixedDelay(::poll, 0, READ_MILLIS, TimeUnit.MILLISECONDS)
        }
    }

    override fun snapshot(): VehicleStatusSnapshot? {
        askedMillis = now()
        val app = context ?: return null
        return cache.snapshot(TuPigOutputSettings.lowChargePercent(app))
    }

    private fun poll() {
        val app = context ?: return
        // Nobody asked for a while: the session ended. Keep adb closed until the next one.
        if (now() - askedMillis > IDLE_MILLIS) return shell.close()
        read(app) { readBattery(app) }
    }

    /** Serialize both ADB readers without blocking snapshot() or the UI on shell I/O. */
    fun read(appContext: Context, reader: () -> TuPigBatteryReading?): TuPigBatteryReading? = synchronized(readLock) {
        val generation = synchronized(publicationLock) { probePublicationGeneration }
        val reading = reader()
        synchronized(publicationLock) {
            // An accepted probe can publish while this shell read is in flight. Its newer reading
            // must not be replaced by a poll that began with the previous field snapshot.
            if (generation == probePublicationGeneration) acceptReading(appContext, reading)
        }
        reading
    }

    /** Already-read data from an accepted field-store snapshot; never waits for shell I/O. */
    fun publishProbeReading(appContext: Context, reading: TuPigBatteryReading) = synchronized(publicationLock) {
        probePublicationGeneration += 1
        acceptReading(appContext, reading)
    }

    /** Caller holds [publicationLock]; neither the field store nor ADB is consulted here. */
    private fun acceptReading(appContext: Context, reading: TuPigBatteryReading?) {
        context = appContext.applicationContext
        if (cache.accept(reading) && reading != null) {
            Log.i(TAG, "battery ${reading.percent} % range ${reading.rangeKm} km ${reading.remainingKwh} kWh charging=${reading.charging} protocol=${reading.protocol}")
        }
    }

    private fun now() = SystemClock.elapsedRealtime()
}

/** Atomically publishes the reading, timestamp and capacity estimate across both ADB readers. */
internal class TuPigBatteryCache(private val now: () -> Long) {
    private var latest: TuPigBatteryReading? = null
    private var latestMillis = 0L
    private var fullKwh: Double? = null

    @Synchronized
    fun accept(reading: TuPigBatteryReading?): Boolean {
        if (reading == null) {
            latest = null
            fullKwh = null
            return false
        }
        if (latest?.protocol != reading.protocol) fullKwh = null
        val changed = latest?.let {
            it.protocol != reading.protocol || it.percent.roundToInt() != reading.percent.roundToInt() || it.charging != reading.charging
        } != false
        TuPigBattery.fullKwh(reading)?.let { fullKwh = it }
        latest = reading
        latestMillis = now()
        return changed
    }

    @Synchronized
    fun snapshot(lowPercent: Int): VehicleStatusSnapshot? {
        val reading = latest ?: return null
        if (now() - latestMillis > 3 * 60_000L) return null
        return TuPigBattery.snapshot(reading, lowPercent, fullKwh)
    }
}
