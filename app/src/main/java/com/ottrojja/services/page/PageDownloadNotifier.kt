package com.ottrojja.services.page

import android.Manifest
import android.annotation.SuppressLint
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import com.ottrojja.R
import com.ottrojja.services.page.PagePlaybackProtocol.ACTION_CANCEL_DOWNLOAD
import com.ottrojja.services.page.PagePlaybackProtocol.DOWNLOAD_CHANNEL_ID
import com.ottrojja.services.page.PagePlaybackProtocol.DOWNLOAD_NOTIFICATION_ID

/**
 * Progress notification for large verse downloads (see LARGE_DOWNLOAD_THRESHOLD), with a cancel
 * action. Separate from the playback notification, which Media3 manages.
 */
class PageDownloadNotifier(private val context: Context) {

    private val manager = NotificationManagerCompat.from(context)

    fun showProgress(done: Int, total: Int) {
        notify(
            builder()
                .setContentTitle("جاري تحميل آيات التلاوة")
                .setContentText("$done من $total")
                .setProgress(total, done, false)
                .setOngoing(true)
                .addAction(0, "إلغاء", cancelIntent())
                .build()
        )
    }

    fun showFailed(message: String) {
        notify(
            builder()
                .setContentTitle("تعذر تحميل آيات التلاوة")
                .setContentText(message)
                .setAutoCancel(true)
                .build()
        )
    }

    fun cancel() = manager.cancel(DOWNLOAD_NOTIFICATION_ID)

    private fun builder() = NotificationCompat.Builder(context, DOWNLOAD_CHANNEL_ID)
        .setSmallIcon(R.drawable.logo)
        .setOnlyAlertOnce(true)
        .setSilent(true)
        .setShowWhen(false)
        .setContentIntent(openAppIntent())

    @SuppressLint("MissingPermission") // checked in canNotify()
    private fun notify(notification: android.app.Notification) {
        if (!canNotify()) return
        ensureChannel()
        manager.notify(DOWNLOAD_NOTIFICATION_ID, notification)
    }

    private fun canNotify(): Boolean {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
            ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED
        ) return false
        return manager.areNotificationsEnabled()
    }

    private fun ensureChannel() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return
        val system = context.getSystemService(NotificationManager::class.java) ?: return
        if (system.getNotificationChannel(DOWNLOAD_CHANNEL_ID) != null) return
        system.createNotificationChannel(
            NotificationChannel(DOWNLOAD_CHANNEL_ID, "تحميل الآيات", NotificationManager.IMPORTANCE_LOW)
        )
    }

    private fun cancelIntent(): PendingIntent = PendingIntent.getService(
        context,
        DOWNLOAD_NOTIFICATION_ID,
        Intent(context, PagePlaybackService::class.java).setAction(ACTION_CANCEL_DOWNLOAD),
        PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
    )

    private fun openAppIntent(): PendingIntent? =
        context.packageManager.getLaunchIntentForPackage(context.packageName)?.let {
            PendingIntent.getActivity(
                context, 0, it,
                PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
            )
        }
}
