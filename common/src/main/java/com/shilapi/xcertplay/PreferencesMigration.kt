package com.shilapi.xcertplay

import android.content.Context
import android.content.SharedPreferences

/**
 * Copies legacy DiPlay SharedPreferences into TuPigCarPlay-named files once, so an upgrade keeps
 * chosen iPhone, connection mode, wireless address family and car-hotspot automation (#103).
 */
internal object PreferencesMigration {
    private const val FLAG_PREFS = "tupigcarplay"
    private const val FLAG_KEY = "prefs_migrated_from_diplay"

    /** Old file name → new file name. Longest / most specific first is not required; keys are exact. */
    private val FILES = listOf(
        "diplay" to "tupigcarplay",
        "diplay_car_hotspot" to "tupigcarplay_car_hotspot",
        "diplay_wireless" to "tupigcarplay_wireless",
        "diplay_wifi_scan_pause" to "tupigcarplay_wifi_scan_pause",
        "diplay_bt_suspend" to "tupigcarplay_bt_suspend",
        "diplay_byd_outputs" to "tupigcarplay_leapmotor_outputs",
        "diplay_byd_vehicle_fields" to "tupigcarplay_leapmotor_vehicle_fields",
        "diplay_phone_wake" to "tupigcarplay_phone_wake",
        "diplay_phone_wake_diagnostics" to "tupigcarplay_phone_wake_diagnostics",
        "diplay_startup_diagnostics" to "tupigcarplay_startup_diagnostics",
        "diplay_display_diagnostics" to "tupigcarplay_display_diagnostics",
        "diplay_update_availability" to "tupigcarplay_update_availability",
        "diplay_navigation_wheel" to "tupigcarplay_navigation_wheel",
        "diplay_wheel_map_zoom" to "tupigcarplay_wheel_map_zoom",
        "diplay_carplay_rotation" to "tupigcarplay_carplay_rotation",
        "diplay_carplay_dock" to "tupigcarplay_carplay_dock",
        "diplay_split_screen" to "tupigcarplay_split_screen",
        "diplay_center_map" to "tupigcarplay_center_map",
        "diplay_cluster_layout" to "tupigcarplay_cluster_layout",
        "diplay_launcher_bubble" to "tupigcarplay_launcher_bubble",
        "diplay_last_problem" to "tupigcarplay_last_problem",
        "diplay_side_panel" to "tupigcarplay_side_panel",
        "diplay_oem_cluster" to "tupigcarplay_oem_cluster",
        "diplay_dilink3_cluster" to "tupigcarplay_dilink3_cluster",
    )

    @Synchronized
    fun migrateIfNeeded(context: Context) {
        val flag = context.getSharedPreferences(FLAG_PREFS, Context.MODE_PRIVATE)
        if (flag.getBoolean(FLAG_KEY, false)) return
        for ((oldName, newName) in FILES) {
            copyIfNewEmpty(context, oldName, newName)
        }
        flag.edit().putBoolean(FLAG_KEY, true).apply()
    }

    private fun copyIfNewEmpty(context: Context, oldName: String, newName: String) {
        if (oldName == newName) return
        val old = context.getSharedPreferences(oldName, Context.MODE_PRIVATE)
        val new = context.getSharedPreferences(newName, Context.MODE_PRIVATE)
        val oldAll = old.all
        if (oldAll.isEmpty()) return
        // Prefer existing new data (a fresh TuPigCarPlay install that already wrote settings).
        if (new.all.any { it.key != FLAG_KEY }) return
        val editor = new.edit()
        for ((key, value) in oldAll) {
            if (key == FLAG_KEY) continue
            put(editor, key, value)
        }
        editor.apply()
    }

    private fun put(editor: SharedPreferences.Editor, key: String, value: Any?) {
        when (value) {
            null -> editor.remove(key)
            is Boolean -> editor.putBoolean(key, value)
            is Int -> editor.putInt(key, value)
            is Long -> editor.putLong(key, value)
            is Float -> editor.putFloat(key, value)
            is String -> editor.putString(key, value)
            is Set<*> -> @Suppress("UNCHECKED_CAST")
            editor.putStringSet(key, value as Set<String>)
        }
    }
}
