package com.shilapi.xcertplay.hud

import android.content.Context
import android.util.Log
import com.shilapi.xcertplay.iap2.wire.Iap2Frame
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit

internal object TuPigStandaloneNavigationBridge {
    private val lock = Any()
    private val route = TuPigHudRouteState()
    private var context: Context? = null
    private var output: TuPigStandaloneHudOutput? = null
    private var started = false

    fun initialize(appContext: Context) = synchronized(lock) {
        context = appContext.applicationContext
        if (output == null) output = TuPigStandaloneHudOutput.create(appContext)
        if (output != null && !started) {
            started = true
            Executors.newSingleThreadScheduledExecutor { task ->
                Thread(task, "tupigcarplay-standalone-navi").apply { isDaemon = true }
            }.scheduleWithFixedDelay(::tick, 0, 250, TimeUnit.MILLISECONDS)
        }
    }

    fun onFrame(frame: Iap2Frame) = synchronized(lock) {
        route.accept(frame.messageId, frame.payload)
        Unit
    }

    fun clear() = synchronized(lock) {
        route.clear() // A keepalive must never resurrect ended guidance.
        output?.clear()
        Unit
    }

    private fun tick() = synchronized(lock) {
        if (TuPigStandaloneHudOutput.syntheticHold) return@synchronized
        try {
            val frame = if (context?.let(TuPigOutputSettings::enabled) == true)
                route.currentApple()?.let(TuPigClusterFrame::from) else null
            val line = context?.takeIf(TuPigOutputSettings::hudSong)
                ?.let { TuPigClusterSong.current() }?.takeIf { it.playing }?.line
            // Directions own the shared row. Text-only mode clears previous maneuver records first.
            when {
                frame != null -> output?.update(frame.icon, frame.roundaboutExit, frame.distanceMeters, frame.road)
                line != null -> output?.showText(line)
                else -> output?.clear()
            }
        } catch (error: Exception) {
            Log.w("TuPigCarPlay-Standalone", "HUD update/cleanup will retry", error)
        }
    }
}
