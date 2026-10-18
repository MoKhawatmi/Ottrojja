package com.ottrojja.services.quran

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
 * App-scoped bridge between the UI and QuranPlaybackService. Owns the MediaController
 * and exposes one StateFlow.
 *
 * Must be called from the main thread (MediaController requirement); ViewModels already are.
 */
class QuranPlaybackManager private constructor(context: Context) {

    private val appContext = context.applicationContext

    private val _state = MutableStateFlow(QuranPlayerState())
    val state: StateFlow<QuranPlayerState> = _state.asStateFlow()

    private val connection = MediaSessionConnection(
        context = appContext,
        serviceClass = QuranPlaybackService::class.java,
        owner = "QuranPlaybackManager",
        onSync = ::sync,
        onReset = { _state.update { QuranPlayerState(options = it.options) } },
    )
    private val controller get() = connection.controller

    val positionMs: Flow<Long> = connection.positionMs(_state.map { it.isPlaying })

    fun connect() = connection.connect()

    /** Called when the listening screen goes away; keeps the connection while something is loaded. */
    fun releaseIfIdle() = connection.releaseIfIdle()

    // region commands

    /** Resumes if the same request is paused, otherwise starts it from the beginning. */
    fun play(request: QuranPlaybackRequest, options: ListeningOptions) {
        if (isResumable(request)) {
            updateOptions(options)
            controller?.play()
            return
        }

        // stop the page player (and any other audio service) first
        terminateAllServices(appContext, QuranPlaybackService::class.java)

        _state.update { it.copy(request = request, options = options) }
        connection.withController {
            it.sendCustomCommand(
                SessionCommand(QuranPlaybackProtocol.CMD_PLAY, Bundle.EMPTY),
                request.toBundle(options)
            )
        }
    }

    fun updateOptions(options: ListeningOptions) {
        _state.update { it.copy(options = options) }
        if (!_state.value.hasMedia) return
        controller?.sendCustomCommand(
            SessionCommand(QuranPlaybackProtocol.CMD_UPDATE_OPTIONS, Bundle.EMPTY),
            options.toBundle()
        )
    }

    fun pause() {
        controller?.pause()
    }

    /** For other players (e.g. the page player) to stop Quran playback directly. */
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

    private fun isResumable(request: QuranPlaybackRequest): Boolean {
        val c = controller ?: return false
        val s = _state.value
        if (!s.hasMedia || !s.isPaused || c.playbackState == Player.STATE_ENDED) return false
        val current = s.request ?: return false
        return when (request) {
            // in continuous mode the player may have moved on, compare against what is actually playing
            is QuranPlaybackRequest.FullSurah ->
                current is QuranPlaybackRequest.FullSurah && request.surah.surahId == s.currentSurahId

            is QuranPlaybackRequest.VerseRange -> current == request
        }
    }

    // endregion

    private fun sync(p: MediaController) {
        val hasMedia = p.mediaItemCount > 0 && p.playbackState != Player.STATE_IDLE
        // the session hides the duration from the notification, the real one comes in the extras
        val duration = HiddenDuration.read(p)
        _state.update {
            it.copy(
                hasMedia = hasMedia,
                isPlaying = p.isPlaying || (p.playWhenReady && p.playbackState == Player.STATE_BUFFERING),
                isPaused = hasMedia && !p.playWhenReady && p.playbackState != Player.STATE_ENDED,
                durationMs = duration.takeIf { d -> d != C.TIME_UNSET && d > 0 } ?: 0L,
                speed = p.playbackParameters.speed,
                title = if (hasMedia) p.mediaMetadata.title?.toString().orEmpty() else "",
                currentSurahId = QuranAudio.surahIdFromMediaId(p.currentMediaItem?.mediaId),
            )
        }
    }

    companion object {
        @Volatile
        private var instance: QuranPlaybackManager? = null

        fun getInstance(context: Context): QuranPlaybackManager =
            instance ?: synchronized(this) {
                instance ?: QuranPlaybackManager(context).also { instance = it }
            }
    }
}
