package com.ottrojja.services.common

import android.os.Bundle
import androidx.media3.common.C
import androidx.media3.common.ForwardingPlayer
import androidx.media3.common.Player
import androidx.media3.session.MediaController
import androidx.media3.session.MediaSession

/**
 * Keeps the track duration away from the system (notification, lock screen, Bluetooth), so no
 * progress bar is drawn anywhere outside the app. The app still gets the real duration through
 * the session extras.
 *
 * Service side: build the session with [sessionPlayer] and add [publisher] to the real player.
 * App side: read it with [read].
 */
object HiddenDuration {
    private const val KEY_DURATION = "duration"

    /** The player to give the MediaSession; reports no duration. */
    fun sessionPlayer(player: Player): Player = object : ForwardingPlayer(player) {
        override fun getDuration() = C.TIME_UNSET
        override fun getContentDuration() = C.TIME_UNSET
    }

    /** Listener for the real player; sends its duration to controllers whenever it changes. */
    fun publisher(session: () -> MediaSession?): Player.Listener = object : Player.Listener {
        private var publishedMs = C.TIME_UNSET

        override fun onEvents(player: Player, events: Player.Events) {
            val duration = player.duration
            if (duration == publishedMs) return
            publishedMs = duration
            session()?.setSessionExtras(Bundle().apply { putLong(KEY_DURATION, duration) })
        }
    }

    /** Duration sent by the service, or C.TIME_UNSET when unknown. */
    fun read(controller: MediaController): Long =
        controller.sessionExtras.getLong(KEY_DURATION, C.TIME_UNSET)
}
