package com.shilapi.xcertplay

import android.content.Context
import com.shilapi.xcertplay.hud.TuPigAdbAccess
import com.shilapi.xcertplay.hud.TuPigVehicleCapabilityProbe
import com.shilapi.xcertplay.hud.TuPigVehicleProbeOutcome

/** Test seam for the blocking ADB work launched by the host settings page. */
internal interface TuPigVehicleSettingsBackend {
    fun check(context: Context, mayAsk: Boolean): TuPigAdbAccess.Status
    fun checkState(context: Context, mayAsk: Boolean): TuPigAdbAccess.State
    fun probe(context: Context, persist: Boolean): TuPigVehicleProbeOutcome
    fun execute(name: String, block: () -> Unit)
}

internal object AndroidTuPigVehicleSettingsBackend : TuPigVehicleSettingsBackend {
    override fun check(context: Context, mayAsk: Boolean) = TuPigAdbAccess.check(context, mayAsk)

    override fun checkState(context: Context, mayAsk: Boolean) = TuPigAdbAccess.checkState(context, mayAsk)

    override fun probe(context: Context, persist: Boolean) =
        TuPigVehicleCapabilityProbe.probe(context, persist)

    override fun execute(name: String, block: () -> Unit) {
        Thread(block, name).start()
    }
}

/** Replaced only by Robolectric tests; production always uses [AndroidTuPigVehicleSettingsBackend]. */
internal object TuPigVehicleSettingsBackendProvider {
    @Volatile var current: TuPigVehicleSettingsBackend = AndroidTuPigVehicleSettingsBackend

    fun reset() {
        current = AndroidTuPigVehicleSettingsBackend
    }
}
