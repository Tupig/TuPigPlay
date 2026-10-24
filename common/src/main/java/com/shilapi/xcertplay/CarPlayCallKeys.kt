package com.shilapi.xcertplay

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Handler
import android.os.Looper
import android.util.Log
import com.shilapi.xcertplay.hud.TuPigNavigationOutputs
import com.shilapi.xcertplay.hud.TuPigOutputSettings
import com.shilapi.xcertplay.hud.CarPlayCallKeyPolicy
import com.shilapi.xcertplay.orchestration.CarPlayController

/**
 * The steering wheel's call keys during a CarPlay call (see [CarPlayCallKeyPolicy]): the call key answers,
 * the hang-up and multifunction keys end or decline the call. The call key comes as a key event, to the
 * wheel key service or to the CarPlay screen; the hang-up keys only as TuPig's hang-up broadcast.
 */
internal object CarPlayCallKeys {
    private const val TAG = "TuPigCarPlay-CallKeys"
    // TuPig's window manager opens its phone app on the call key's release; CarPlay comes back after it.
    private const val RETURN_DELAY_MILLIS = 700L

    private val handler = Handler(Looper.getMainLooper())
    @Volatile private var installed = false

    /** Listens for TuPig's hang-up broadcast for the life of the process; idempotent. */
    fun install(context: Context) {
        if (installed) return
        synchronized(this) {
            if (installed) return
            val app = context.applicationContext
            val receiver = object : BroadcastReceiver() {
                override fun onReceive(context: Context, intent: Intent) {
                    if (!TuPigOutputSettings.carPlayCallControls(context)) return
                    val keyCode = intent.getIntExtra(CarPlayCallKeyPolicy.EXTRA_KEYCODE, -1)
                    val controller = currentController()
                    val action = CarPlayCallKeyPolicy.onHangUpBroadcast(
                        keyCode, TuPigNavigationOutputs.carPlayCall(), controller.hasSession(),
                    )
                    if (action == CarPlayCallKeyPolicy.Action.END) {
                        val sent = controller?.endCall() == true
                        Log.i(TAG, "hang-up key $keyCode: end CarPlay call sent=$sent")
                    }
                }
            }
            // Sent by the system (TuPig's window manager), so it has to be exported.
            runCatching {
                val filter = IntentFilter(CarPlayCallKeyPolicy.ACTION_TUPIG_HANG_UP)
                if (android.os.Build.VERSION.SDK_INT >= 33) {
                    app.registerReceiver(receiver, filter, android.Manifest.permission.DUMP, null,
                        Context.RECEIVER_EXPORTED)
                } else {
                    app.registerReceiver(receiver, filter, android.Manifest.permission.DUMP, null)
                }
            }.onFailure { Log.w(TAG, "hang-up broadcast unavailable", it) }
            installed = true
        }
    }

    /** Returns true when the key belongs to a CarPlay call and must not reach the car. */
    fun onKey(context: Context, keyCode: Int, down: Boolean, controller: CarPlayController? = currentController()): Boolean {
        if (!TuPigOutputSettings.carPlayCallControls(context)) return false
        val action = CarPlayCallKeyPolicy.onKey(keyCode, down, TuPigNavigationOutputs.carPlayCall(), controller.hasSession())
        when (action) {
            CarPlayCallKeyPolicy.Action.PASS -> return false
            CarPlayCallKeyPolicy.Action.CONSUME -> Unit
            CarPlayCallKeyPolicy.Action.ANSWER -> {
                val sent = controller?.answerCall() == true
                Log.i(TAG, "call key $keyCode: answer CarPlay call sent=$sent")
            }
            CarPlayCallKeyPolicy.Action.END -> {
                val sent = controller?.endCall() == true
                Log.i(TAG, "key $keyCode: end CarPlay call sent=$sent")
            }
        }
        if (!down && keyCode == CarPlayCallKeyPolicy.KEYCODE_TUPIG_DIAL_ANSWER) returnToCarPlay(context.applicationContext)
        return true
    }

    private fun returnToCarPlay(app: Context) {
        handler.postDelayed({
            if (!TuPigOutputSettings.carPlayCallControls(app) || !currentController().hasSession()) return@postDelayed
            runCatching {
                app.startActivity(
                    CarPlayScreen.intent(app)
                        .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_REORDER_TO_FRONT),
                )
            }.onFailure { Log.w(TAG, "could not bring CarPlay back after the call key", it) }
        }, RETURN_DELAY_MILLIS)
    }

    private fun currentController(): CarPlayController? = CarPlayBackgroundSession.snapshot()?.controller

    private fun CarPlayController?.hasSession(): Boolean = this?.activeAirPlaySessionToken() != null
}
