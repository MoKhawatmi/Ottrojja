package com.ottrojja.services.common

import android.app.PendingIntent
import android.content.Context
import androidx.annotation.OptIn
import androidx.media3.common.AudioAttributes
import androidx.media3.common.C
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.session.DefaultMediaNotificationProvider
import androidx.media3.session.MediaNotification
import androidx.media3.session.MediaSession
import com.ottrojja.R
import com.ottrojja.classes.Helpers.getMediaSrcFactory

/**
 * ExoPlayer set up for recitation, shared by the playback services.
 * [wakeMode]: C.WAKE_MODE_NETWORK when streaming, C.WAKE_MODE_LOCAL for local files only.
 */
fun buildSpeechPlayer(context: Context, wakeMode: Int = C.WAKE_MODE_NETWORK): ExoPlayer = ExoPlayer.Builder(context)
    .setMediaSourceFactory(getMediaSrcFactory(context))
    .setAudioAttributes(
        AudioAttributes.Builder()
            .setUsage(C.USAGE_MEDIA)
            .setContentType(C.AUDIO_CONTENT_TYPE_SPEECH)
            .build(),
        /* handleAudioFocus = */ true
    )
    .setHandleAudioBecomingNoisy(true) // pause when headphones are unplugged
    .setWakeMode(wakeMode)              // keep playing with the screen off
    .build()

/** Standard Media3 media notification with the app logo as its small icon. */
@OptIn(UnstableApi::class)
fun logoNotificationProvider(context: Context): MediaNotification.Provider =
    DefaultMediaNotificationProvider.Builder(context).build().apply {
        setSmallIcon(R.drawable.logo)
    }

/** The notification (and lock screen / system media controls) get the defaults minus seeking. */
fun notificationControllerResult(session: MediaSession): MediaSession.ConnectionResult {
    val playerCommands = MediaSession.ConnectionResult.DEFAULT_PLAYER_COMMANDS.buildUpon()
        .remove(Player.COMMAND_SEEK_IN_CURRENT_MEDIA_ITEM)
        .build()
    return MediaSession.ConnectionResult.AcceptedResultBuilder(session)
        .setAvailablePlayerCommands(playerCommands)
        .build()
}

/** Tapping the notification opens the app. */
fun MediaSession.Builder.setLaunchActivity(context: Context): MediaSession.Builder {
    context.packageManager.getLaunchIntentForPackage(context.packageName)?.let { launchIntent ->
        setSessionActivity(
            PendingIntent.getActivity(
                context, 0, launchIntent,
                PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
            )
        )
    }
    return this
}
