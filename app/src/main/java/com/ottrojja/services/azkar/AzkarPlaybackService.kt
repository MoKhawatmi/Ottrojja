package com.ottrojja.services.azkar

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.core.net.toUri
import androidx.media3.common.MediaItem
import androidx.media3.common.MediaMetadata
import androidx.media3.common.PlaybackException
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.session.MediaSession
import androidx.media3.session.MediaSessionService
import androidx.media3.session.SessionCommand
import androidx.media3.session.SessionResult
import com.google.common.util.concurrent.Futures
import com.google.common.util.concurrent.ListenableFuture
import com.ottrojja.classes.Helpers
import com.ottrojja.classes.Helpers.reportException
import com.ottrojja.composables.ToastManager.classes.AppToastManager
import com.ottrojja.composables.ToastManager.classes.AppToastType
import com.ottrojja.services.common.HiddenDuration
import com.ottrojja.services.common.buildSpeechPlayer
import com.ottrojja.services.common.logoNotificationProvider
import com.ottrojja.services.common.notificationControllerResult
import com.ottrojja.services.common.setLaunchActivity
import com.ottrojja.services.azkar.AzkarPlaybackProtocol.ACTION_TERMINATE
import com.ottrojja.services.azkar.AzkarPlaybackProtocol.CMD_PLAY
import com.ottrojja.services.azkar.AzkarPlaybackProtocol.KEY_ADDRESS
import com.ottrojja.services.azkar.AzkarPlaybackProtocol.KEY_TITLE
import com.ottrojja.services.azkar.AzkarPlaybackProtocol.SESSION_ID
import java.io.File

/**
 * Replaces AzkarPlayerService. Plays one zikr at a time.
 *
 * Notification: same as QuranPlaybackService, the standard Media3 media notification with the
 * app logo, no progress bar and no seeking.
 */
class AzkarPlaybackService : MediaSessionService() {

    private var mediaSession: MediaSession? = null
    private lateinit var player: ExoPlayer

    override fun onCreate() {
        super.onCreate()

        setMediaNotificationProvider(logoNotificationProvider(this))

        player = buildSpeechPlayer(this)
        player.addListener(playerListener)
        player.addListener(HiddenDuration.publisher { mediaSession })

        // no duration for the system, so the notification shows no progress bar
        mediaSession = MediaSession.Builder(this, HiddenDuration.sessionPlayer(player))
            .setId(SESSION_ID) // must differ from the Quran session id (same process)
            .setCallback(sessionCallback)
            .setLaunchActivity(this)
            .build()
    }

    override fun onGetSession(controllerInfo: MediaSession.ControllerInfo): MediaSession? = mediaSession

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        // Plain-intent stop (Helpers.terminateAllServices uses the playback manager instead).
        if (intent?.action == ACTION_TERMINATE) {
            player.stop()
            player.clearMediaItems()
            stopSelf()
            return START_NOT_STICKY
        }
        return super.onStartCommand(intent, flags, startId)
    }

    override fun onTaskRemoved(rootIntent: Intent?) {
        if (!player.playWhenReady || player.mediaItemCount == 0 || player.playbackState == Player.STATE_ENDED) {
            stopSelf()
        }
    }

    override fun onDestroy() {
        mediaSession?.run {
            player.release()
            release()
        }
        mediaSession = null
        super.onDestroy()
    }

    private val sessionCallback = object : MediaSession.Callback {
        override fun onConnect(
            session: MediaSession,
            controller: MediaSession.ControllerInfo
        ): MediaSession.ConnectionResult {
            if (session.isMediaNotificationController(controller)) {
                return notificationControllerResult(session)
            }
            // Only our own app gets the custom command; Bluetooth, Android Auto, system UI get the defaults.
            if (controller.packageName != packageName) {
                return MediaSession.ConnectionResult.AcceptedResultBuilder(session).build()
            }
            val commands = MediaSession.ConnectionResult.DEFAULT_SESSION_COMMANDS.buildUpon()
                .add(SessionCommand(CMD_PLAY, Bundle.EMPTY))
                .build()
            return MediaSession.ConnectionResult.AcceptedResultBuilder(session)
                .setAvailableSessionCommands(commands)
                .build()
        }

        override fun onCustomCommand(
            session: MediaSession,
            controller: MediaSession.ControllerInfo,
            customCommand: SessionCommand,
            args: Bundle
        ): ListenableFuture<SessionResult> {
            if (customCommand.customAction == CMD_PLAY) startZikr(args)
            return Futures.immediateFuture(SessionResult(SessionResult.RESULT_SUCCESS))
        }
    }

    private fun startZikr(args: Bundle) {
        val address = args.getString(KEY_ADDRESS)?.takeIf { it.isNotBlank() } ?: return
        val title = args.getString(KEY_TITLE).orEmpty()

        val localFile = File(getExternalFilesDir(null), address.substringAfterLast('/'))
        if (!localFile.exists() && !Helpers.checkNetworkConnectivity(this)) {
            AppToastManager.show(message = "لا يوجد اتصال بالانترنت", type = AppToastType.ERROR)
            return
        }
        val uri: Uri = if (localFile.exists()) Uri.fromFile(localFile) else address.toUri()

        val item = MediaItem.Builder()
            .setMediaId(address)
            .setUri(uri)
            .setMediaMetadata(
                MediaMetadata.Builder()
                    .setTitle(title)
                    .setArtist("تطبيق اترجة")
                    .build()
            )
            .build()

        player.setMediaItem(item)
        player.prepare()
        player.play()
    }

    private val playerListener = object : Player.Listener {
        override fun onPlaybackStateChanged(playbackState: Int) {
            when (playbackState) {
                // finished: clear so the notification goes away and the next play starts fresh
                Player.STATE_ENDED -> {
                    player.stop()
                    player.clearMediaItems()
                }

                // stopped from outside, e.g. the paused notification swiped away
                Player.STATE_IDLE -> if (player.playerError == null && player.mediaItemCount > 0) {
                    player.clearMediaItems()
                }

                else -> Unit
            }
        }

        override fun onPlayerError(error: PlaybackException) {
            error.printStackTrace()
            reportException(exception = error, file = "AzkarPlaybackService")
            AppToastManager.show(message = "حصل خطأ، يرجى المحاولة لاحقا", type = AppToastType.ERROR)
        }
    }
}
