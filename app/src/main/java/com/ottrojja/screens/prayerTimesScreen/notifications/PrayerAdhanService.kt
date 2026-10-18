package com.ottrojja.screens.prayerTimesScreen.notifications

import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.pm.ServiceInfo
import android.media.AudioAttributes
import android.media.AudioManager
import android.media.MediaPlayer
import android.os.Build
import android.os.IBinder
import android.media.AudioFocusRequest
import androidx.core.app.ServiceCompat
import com.ottrojja.classes.Helpers.reportException
import com.ottrojja.screens.prayerTimesScreen.notifications.PrayerAlarmReceiver.Companion.putAlert
import com.ottrojja.screens.prayerTimesScreen.notifications.PrayerAlarmReceiver.Companion.toAlert

/**
 * Plays the adhan as a short-lived foreground service (type mediaPlayback), with a lock
 * screen visible notification whose "stop" action, body tap or swipe ends it (swipe: Android
 * 14+ only, older versions don't let foreground service notifications be dismissed). When the
 * adhan ends, the notification is replaced by a regular, silent one.
 *
 * Started from [PrayerAlarmReceiver]; an exact alarm makes the background start allowed.
 */
class PrayerAdhanService : Service() {

    private var player: MediaPlayer? = null
    private var alert: PrayerAlert? = null
    private var focusRequest: Any? = null // AudioFocusRequest on API 26+
    private val audioManager by lazy { getSystemService(Context.AUDIO_SERVICE) as AudioManager }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_PLAY -> intent.toAlert()?.let(::start) ?: stopSelf()
            ACTION_STOP -> finish()
            else -> stopSelf()
        }
        return START_NOT_STICKY
    }

    private fun start(newAlert: PrayerAlert) {
        // A new adhan replaces one still playing (only possible with very close prayer times).
        releasePlayer()
        alert?.let { if (it.type != newAlert.type) PrayerNotifications.showPrayerNotification(this, it, silent = true) }
        alert = newAlert

        PrayerNotifications.ensureChannels(this)
        val notification = PrayerNotifications.buildAdhanNotification(
            this, newAlert, stopPendingIntent(), tapPendingIntent()
        )
        try {
            ServiceCompat.startForeground(
                this,
                PrayerNotifications.notificationId(newAlert.type),
                notification,
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) ServiceInfo.FOREGROUND_SERVICE_TYPE_MEDIA_PLAYBACK else 0,
            )
        } catch (e: Exception) {
            // E.g. ForegroundServiceStartNotAllowedException: fall back to a plain notification.
            reportException(exception = e, file = "PrayerAdhanService")
            PrayerNotifications.showPrayerNotification(this, newAlert)
            alert = null
            stopSelf()
            return
        }

        player = try {
            MediaPlayer.create(
                this, PrayerNotifications.adhanSound(newAlert.type), adhanAttributes,
                audioManager.generateAudioSessionId(),
            )
        } catch (e: Exception) {
            reportException(exception = e, file = "PrayerAdhanService")
            null
        }

        val mediaPlayer = player ?: return finish()
        requestAudioFocus()
        mediaPlayer.setOnCompletionListener { finish() }
        mediaPlayer.setOnErrorListener { _, _, _ -> finish(); true }
        mediaPlayer.start()
    }

    /** Ends playback and leaves a regular, dismissible notification behind. */
    private fun finish() {
        releasePlayer()
        abandonAudioFocus()
        ServiceCompat.stopForeground(this, ServiceCompat.STOP_FOREGROUND_REMOVE)
        alert?.let { PrayerNotifications.showPrayerNotification(this, it, silent = true) }
        alert = null
        stopSelf()
    }

    private fun releasePlayer() {
        player?.run {
            setOnCompletionListener(null)
            runCatching { if (isPlaying) stop() }
            release()
        }
        player = null
    }

    private val adhanAttributes: AudioAttributes = AudioAttributes.Builder()
        .setUsage(AudioAttributes.USAGE_ALARM)
        .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
        .build()

    /** Other apps pause for the adhan; nothing to do when they take focus back. */
    private val focusListener = AudioManager.OnAudioFocusChangeListener { }

    private fun requestAudioFocus() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val request = AudioFocusRequest.Builder(AudioManager.AUDIOFOCUS_GAIN_TRANSIENT)
                .setAudioAttributes(adhanAttributes)
                .setOnAudioFocusChangeListener(focusListener)
                .build()
            focusRequest = request
            audioManager.requestAudioFocus(request)
        } else {
            @Suppress("DEPRECATION")
            audioManager.requestAudioFocus(focusListener, AudioManager.STREAM_ALARM, AudioManager.AUDIOFOCUS_GAIN_TRANSIENT)
        }
    }

    private fun abandonAudioFocus() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            (focusRequest as? AudioFocusRequest)?.let(audioManager::abandonAudioFocusRequest)
        } else {
            @Suppress("DEPRECATION")
            audioManager.abandonAudioFocus(focusListener)
        }
        focusRequest = null
    }

    /** Stop action and swipe: a service intent, so it works on the lock screen without unlocking. */
    private fun stopPendingIntent(): PendingIntent = PendingIntent.getService(
        this, 0,
        stopIntent(this),
        PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
    )

    /** Body tap: stops too, see PrayerAdhanStopActivity. */
    private fun tapPendingIntent(): PendingIntent = PendingIntent.getActivity(
        this, 0,
        Intent(this, PrayerAdhanStopActivity::class.java).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK),
        PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
    )

    override fun onDestroy() {
        releasePlayer()
        abandonAudioFocus()
        super.onDestroy()
    }

    companion object {
        private const val ACTION_PLAY = "com.ottrojja.action.PRAYER_ADHAN_PLAY"
        private const val ACTION_STOP = "com.ottrojja.action.PRAYER_ADHAN_STOP"

        fun playIntent(context: Context, alert: PrayerAlert): Intent =
            Intent(context, PrayerAdhanService::class.java).setAction(ACTION_PLAY).putAlert(alert)

        fun stopIntent(context: Context): Intent =
            Intent(context, PrayerAdhanService::class.java).setAction(ACTION_STOP)
    }
}
