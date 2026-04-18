package com.ottrojja.services

import android.animation.ValueAnimator
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Intent
import android.graphics.PixelFormat
import android.os.Build
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import android.provider.Settings
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.WindowManager
import android.view.animation.AccelerateInterpolator
import android.view.animation.DecelerateInterpolator
import android.widget.TextView
import androidx.core.animation.doOnEnd
import androidx.core.app.NotificationCompat
import com.ottrojja.R
import com.ottrojja.classes.DynamicAzkarHelper

class OverlayService : Service() {

    private lateinit var windowManager: WindowManager
    private var overlayView: View? = null
    private val handler = Handler(Looper.getMainLooper())

    override fun onCreate() {
        super.onCreate()

        startForeground(1, createNotification())

        windowManager = getSystemService(WINDOW_SERVICE) as WindowManager

        startLoop()
    }


    private fun startLoop() {
        handler.post(object : Runnable {
            override fun run() {
                showOverlayIfNeeded()
                handler.postDelayed(this, 60_000 * 30) // 30_000 test: 1 min
            }
        })
    }

    private fun showOverlayIfNeeded() {
        if (overlayView != null) return
        if (!Settings.canDrawOverlays(this)) return

        showOverlay()
    }

    private fun showOverlay() {

        val inflater = LayoutInflater.from(this)
        val view = inflater.inflate(R.layout.overlay_layout, null)
        val textView = view.findViewById<TextView>(R.id.txtReminder)


        textView.text = DynamicAzkarHelper.getNextZekr(this)

        val params = WindowManager.LayoutParams(
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
            PixelFormat.TRANSLUCENT
        ).apply {
            gravity = Gravity.TOP or Gravity.END
            x = 20
            y = 100
        }

        overlayView = view
        windowManager.addView(view, params)

        view.alpha = 0f

        view.animate().cancel()
        view.animate()
            .alpha(1f)
            .setDuration(250)
            .setInterpolator(DecelerateInterpolator())
            .start()

        view.setOnClickListener {
            removeOverlay()
        }

        dismissRunnable = Runnable {
            removeOverlay()
        }
        handler.postDelayed(dismissRunnable!!, 5_000)
    }

    private var dismissRunnable: Runnable? = null
    private var isRemoving = false

    private fun removeOverlay() {
        val view = overlayView ?: return
        if (isRemoving) return
        isRemoving = true

        dismissRunnable?.let { handler.removeCallbacks(it) }

        view.animate().cancel()

        view.animate()
            .alpha(0f)
            .setDuration(250)
            .setInterpolator(AccelerateInterpolator())
            .withEndAction {
                try {
                    windowManager.removeViewImmediate(view)
                } catch (_: Exception) {
                }

                overlayView = null
                isRemoving = false
            }
            .start()
    }

    override fun onDestroy() {
        super.onDestroy()

        super.onDestroy()
        handler.removeCallbacksAndMessages(null)
        overlayView?.let { windowManager.removeView(it) }
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        super.onStartCommand(intent, flags, startId)
        return START_STICKY
    }

    private fun createNotification(): Notification {
        val channelId = "overlay_service"

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                "Overlay Service",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                setShowBadge(false)
                lockscreenVisibility = Notification.VISIBILITY_SECRET
            }

            getSystemService(NotificationManager::class.java)
                .createNotificationChannel(channel)
        }

        return NotificationCompat.Builder(this, channelId)
            .setContentTitle("خدمة اذكار الشاشة")
            .setContentText("")
            .setSmallIcon(R.drawable.logo)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .build()
    }
}