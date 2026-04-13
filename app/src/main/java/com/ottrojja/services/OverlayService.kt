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
                handler.postDelayed(this, 60_000*30) // test: 1 min
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

        // 👉 Slide in animation
        view.post {
            val params = view.layoutParams as WindowManager.LayoutParams

            val startX = -view.width
            val endX = 20 // your margin

            params.x = startX
            windowManager.updateViewLayout(view, params)

            val animator = ValueAnimator.ofInt(startX, endX)
            animator.duration = 250

            animator.addUpdateListener {
                val value = it.animatedValue as Int
                params.x = value
                windowManager.updateViewLayout(view, params)
            }

            animator.start()
        }
        // 👉 Click to dismiss
        view.setOnClickListener {
            removeOverlay()
        }

        // 👉 Auto dismiss after 10 sec
        handler.postDelayed({
            removeOverlay()
        }, 10_000)
    }

    private fun removeOverlay() {
        val view = overlayView ?: return

        val params = view.layoutParams as WindowManager.LayoutParams

        val startX = params.x
        val endX = -view.width // move completely off screen

        val animator = ValueAnimator.ofInt(startX, endX)

        animator.duration = 350

        animator.addUpdateListener {
            val value = it.animatedValue as Int
            params.x = value
            windowManager.updateViewLayout(view, params)
        }

        animator.doOnEnd {
            try {
                windowManager.removeView(view)
            } catch (_: Exception) {}
            overlayView = null
        }

        animator.start()
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
            )

            getSystemService(NotificationManager::class.java)
                .createNotificationChannel(channel)
        }

        return NotificationCompat.Builder(this, channelId)
            .setContentTitle("Azkar running")
            .setContentText("Floating reminders active")
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .build()
    }
}