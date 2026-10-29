package com.brendigo.skypeep

import android.content.ActivityNotFoundException
import android.content.Intent
import android.widget.Toast
import android.content.pm.ApplicationInfo
import android.os.Bundle
import android.os.Build
import android.view.View
import android.view.WindowInsets
import android.view.WindowInsetsController
import android.window.OnBackInvokedCallback
import android.window.OnBackInvokedDispatcher
import com.badlogic.gdx.backends.android.AndroidApplication
import com.badlogic.gdx.backends.android.AndroidApplicationConfiguration

class MainActivity : AndroidApplication() {
    private var game: SkyPeepGame? = null
    private var backCallback: OnBackInvokedCallback? = null

    // LibGDX's legacy immersive-mode implementation uses deprecated
    // systemUiVisibility flags. We disable it in the configuration and
    // restore fullscreen with WindowInsetsController on Android 11+.
    @Suppress("DEPRECATION")
    private fun applyImmersiveFullscreen() {
        val decor = window.decorView
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            decor.windowInsetsController?.apply {
                systemBarsBehavior =
                    WindowInsetsController.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
                hide(WindowInsets.Type.systemBars())
            }
        } else {
            // Android 7-10 compatibility only; never run legacy flags on API 30+.
            decor.systemUiVisibility = View.SYSTEM_UI_FLAG_LAYOUT_STABLE or
                View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN or
                View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION or
                View.SYSTEM_UI_FLAG_FULLSCREEN or
                View.SYSTEM_UI_FLAG_HIDE_NAVIGATION or
                View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
        }
    }

    private fun installSafeAreaListener() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.P) return
        window.decorView.setOnApplyWindowInsetsListener { _, insets ->
            val cutout = insets.displayCutout
            // Android framework Insets and its four fields are API 29+.
            // Materialize values inside the API 30+ branch so Android Lint
            // can prove that older devices never access those fields.
            val bars = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                val systemBars = insets.getInsets(WindowInsets.Type.systemBars())
                SkyPeepScreenFrame.PixelInsets(
                    systemBars.left, systemBars.top,
                    systemBars.right, systemBars.bottom
                )
            } else {
                SkyPeepScreenFrame.PixelInsets(0, 0, 0, 0)
            }
            game?.updateSafeInsets(SkyPeepScreenFrame.PixelInsets(
                maxOf(cutout?.safeInsetLeft ?: 0, bars.left),
                maxOf(cutout?.safeInsetTop ?: 0, bars.top),
                maxOf(cutout?.safeInsetRight ?: 0, bars.right),
                maxOf(cutout?.safeInsetBottom ?: 0, bars.bottom)
            ))
            // No inset consumption: system gestures and the GL surface retain
            // normal Android dispatch; only game controls use the safe frame.
            insets
        }
        window.decorView.requestApplyInsets()
    }

    override fun onWindowFocusChanged(hasFocus: Boolean) {
        super.onWindowFocusChanged(hasFocus)
        if (hasFocus) applyImmersiveFullscreen()
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val config = AndroidApplicationConfiguration().apply {
            useImmersiveMode = false
            // SkyPeep is a one-tap arcade game, not a motion-controlled app.
            // LibGDX enables accelerometer/compass by default on Android:
            // unregistering those unused sensors reduces idle battery use.
            useAccelerometer = false
            useCompass = false
            useGyroscope = false
        }
        // QA worlds are addressable ONLY in debuggable APKs. Production
        // releases cannot bypass level progression through an Intent.
        val debuggable = (applicationInfo.flags and ApplicationInfo.FLAG_DEBUGGABLE) != 0
        val qaWorld = if (debuggable) intent.getIntExtra("sky_peep_qa_world", 0) else 0
        val qaSkin = if (debuggable) intent.getIntExtra("sky_peep_qa_skin", -1) else -1
        val qaVictory = if (debuggable) intent.getBooleanExtra("sky_peep_qa_victory", false) else false
        val qaPillars = if (debuggable) intent.getBooleanExtra("sky_peep_qa_pillars", false) else false
        val qaScore = if (debuggable) intent.getIntExtra("sky_peep_qa_score", -1) else -1
        val qaMapUnlocked = if (debuggable) intent.getIntExtra("sky_peep_qa_map_unlocked", -1) else -1
        val qaLoss = if (debuggable) intent.getBooleanExtra("sky_peep_qa_loss", false) else false
        // Diagnostic-only animated-world screenshot fixture.
        val qaBackgroundMotion = if (debuggable)
            intent.getBooleanExtra("sky_peep_qa_background_motion", false) else false
        val qaDifficulty = if (debuggable)
            intent.getIntExtra("sky_peep_qa_difficulty", -1) else -1
        val instance = SkyPeepGame(shareText = { result ->
            runOnUiThread {
                // Android may destroy the activity while the system chooser opens.
                if (isFinishing || isDestroyed) return@runOnUiThread
                val shareIntent = Intent(Intent.ACTION_SEND).apply {
                    type = "text/plain"
                    putExtra(Intent.EXTRA_TEXT, result)
                }
                try {
                    startActivity(Intent.createChooser(shareIntent, "Share SkyPeep score"))
                } catch (_: ActivityNotFoundException) {
                    Toast.makeText(this, "No sharing app available", Toast.LENGTH_SHORT).show()
                } catch (_: SecurityException) {
                    // Restrictive managed devices may block outgoing shares.
                    Toast.makeText(this, "Sharing is restricted on this device", Toast.LENGTH_SHORT).show()
                } catch (_: IllegalArgumentException) {
                    Toast.makeText(this, "Unable to share score", Toast.LENGTH_SHORT).show()
                }
            }
        }, qaWorld = qaWorld, qaSkin = qaSkin,
            qaVictory = qaVictory, qaPillars = qaPillars,
            qaScore = qaScore, qaMapUnlocked = qaMapUnlocked,
            qaLoss = qaLoss, qaBackgroundMotion = qaBackgroundMotion,
            qaDifficulty = qaDifficulty)
        game = instance
        initialize(instance, config)
        installSafeAreaListener()
        applyImmersiveFullscreen()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            // Android 13-16 predictive-back gesture: commit navigates once,
            // using the same state transition as the physical Back key.
            val callback = OnBackInvokedCallback { game?.requestSystemBack() }
            onBackInvokedDispatcher.registerOnBackInvokedCallback(
                OnBackInvokedDispatcher.PRIORITY_DEFAULT, callback)
            backCallback = callback
        }
    }

    override fun onDestroy() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            backCallback?.let { onBackInvokedDispatcher.unregisterOnBackInvokedCallback(it) }
            backCallback = null
        }
        game = null
        super.onDestroy()
    }
}
