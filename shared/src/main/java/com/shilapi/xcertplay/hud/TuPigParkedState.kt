package com.shilapi.xcertplay.hud

import android.content.Context

/**
 * Whether the car is in P, read through the adb shell (autoservice binder) where apps would need a
 * TuPig signature. The id is TuPig SDK 1.0.5's gearbox device: 1 P, 2 R, 3 N, 4 D.
 */
internal object TuPigParkedState {
    const val GEAR = "service call autoservice 5 i32 1011 i32 555745336"
    private const val PARK = 1

    private val shell = TuPigAdbShell("TuPigCarPlay-TuPig-Parked")

    /** Null when the gear cannot be read (no ADB over network, or another car). Blocking. */
    fun parked(context: Context): Boolean? {
        return parked(shell.run(context, TuPigWheelSpeed.gearCommand(context)))
    }

    fun parked(output: String?): Boolean? = TuPigParcel.value(output)?.takeIf { it in 1..6 }?.let { it == PARK }
}
