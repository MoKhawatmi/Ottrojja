package com.ottrojja.services.azkar

import android.content.Context
import android.os.Bundle
import androidx.media3.common.C
import androidx.media3.common.Player
import androidx.media3.session.MediaController
import androidx.media3.session.SessionCommand
import com.ottrojja.classes.Helpers.terminateAllServices
import com.ottrojja.services.common.HiddenDuration
import com.ottrojja.services.common.MediaSessionConnection
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update

/**
 * App-scoped bridge between ZikrViewModel and AzkarPlaybackService.
 * Same shape as QuranPlaybackManager. Call from the main thread.
 */
class AzkarPlaybackManager private constructor(context: Context) {

    private val appContext = context.applicationContext

    private val _state = MutableStateFlow(AzkarPlayerState())
    val state: StateFlow<AzkarPlayerState> = _state.asStateFlow()

    private val connection = MediaSessionConnection(
        context = appContext,
        serviceClass = AzkarPlaybackService::class.java,
        owner = "AzkarPlaybackManager",
        onSync = ::sync,
        onReset = { _state.value = AzkarPlayerState() },
    )
    private val controller get() = connection.controller

    val positionMs: Flow<Long> = connection.positionMs(_state.map { it.isPlaying })

    fun connect() = connection.connect()

    /** Called when the zikr screen goes away; keeps the connection while something is loaded. */
    fun releaseIfIdle() = connection.releaseIfIdle()

    // region commands

    /** Resumes if this zikr is paused, otherwise starts it from the beginning. */
    fun play(address: String, title: String) {
        val s = _state.value
        if (controller != null && s.isPaused && s.currentAddress == address) {
            controller?.play()
            return
        }

        // stop the Quran players first
        terminateAllServices(appContext, AzkarPlaybackService::class.java)

        connection.withController {
            it.sendCustomCommand(
                SessionCommand(AzkarPlaybackProtocol.CMD_PLAY, Bundle.EMPTY),
                Bundle().apply {
                    putString(AzkarPlaybackProtocol.KEY_ADDRESS, address)
                    putString(AzkarPlaybackProtocol.KEY_TITLE, title)
                }
            )
        }
    }

    fun pause() {
        controller?.pause()
    }

    /** For other players to stop azkar playback directly. */
    fun stop() {
        controller?.run {
            stop()
            clearMediaItems()
        }
    }

    fun seekTo(positionMs: Long) {
        controller?.seekTo(positionMs)
    }

    fun increaseSpeed() = changeSpeed(+0.25f)
    fun decreaseSpeed() = changeSpeed(-0.25f)

    private fun changeSpeed(delta: Float) {
        val c = controller ?: return
        c.setPlaybackSpeed((c.playbackParameters.speed + delta).coerceIn(0.25f, 2f))
    }

    // endregion

    private fun sync(p: MediaController) {
        val hasMedia = p.mediaItemCount > 0 && p.playbackState != Player.STATE_IDLE
        // the session hides the duration from the system, the real one comes in the extras
        val duration = HiddenDuration.read(p)
        _state.update {
            it.copy(
                hasMedia = hasMedia,
                isPlaying = p.isPlaying || (p.playWhenReady && p.playbackState == Player.STATE_BUFFERING),
                isPaused = hasMedia && !p.playWhenReady && p.playbackState != Player.STATE_ENDED,
                durationMs = duration.takeIf { d -> d != C.TIME_UNSET && d > 0 } ?: 0L,
                speed = p.playbackParameters.speed,
                currentAddress = if (hasMedia) p.currentMediaItem?.mediaId else null,
            )
        }
    }

    companion object {
        @Volatile
        private var instance: AzkarPlaybackManager? = null

        fun getInstance(context: Context): AzkarPlaybackManager =
            instance ?: synchronized(this) {
                instance ?: AzkarPlaybackManager(context).also { instance = it }
            }
    }
}
