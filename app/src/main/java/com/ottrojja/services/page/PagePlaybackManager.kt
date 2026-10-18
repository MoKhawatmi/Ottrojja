package com.ottrojja.services.page

import android.content.Context
import android.os.Bundle
import androidx.media3.common.Player
import androidx.media3.session.MediaController
import androidx.media3.session.SessionCommand
import com.ottrojja.classes.Helpers.terminateAllServices
import com.ottrojja.services.common.MediaSessionConnection
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

/**
 * App-scoped bridge between QuranViewModel and PagePlaybackService (same shape as
 * QuranPlaybackManager). Owns the MediaController and exposes one StateFlow.
 *
 * Connects lazily (on the first play), so just reading the Quran does not create the service.
 * Call from the main thread.
 */
class PagePlaybackManager private constructor(context: Context) {

    private val appContext = context.applicationContext

    private val _state = MutableStateFlow(PagePlayerState())
    val state: StateFlow<PagePlayerState> = _state.asStateFlow()

    // no public connect(): the first play() connects
    private val connection = MediaSessionConnection(
        context = appContext,
        serviceClass = PagePlaybackService::class.java,
        owner = "PagePlaybackManager",
        onSync = ::sync,
        // keeps the user's listening options; everything else goes back to defaults
        onReset = { _state.update { PagePlayerState(options = it.options) } },
        isBusy = { _state.value.isDownloading },
    )
    private val controller get() = connection.controller

    /** Called when the Quran screen goes away; keeps the connection while playing or downloading. */
    fun releaseIfIdle() = connection.releaseIfIdle()

    // region commands

    /**
     * Resumes if this selection is the paused segment, otherwise starts it (the service downloads
     * missing files first). Pressing play always turns page following back on.
     */
    fun play(request: PagePlaybackRequest, options: PageListeningOptions) {
        val s = _state.value
        val c = controller
        if (c != null && s.hasMedia && s.isPaused && c.playbackState != Player.STATE_ENDED && request.matches(s.segment)) {
            _state.update { it.copy(following = true) }
            updateOptions(options)
            c.play()
            return
        }

        // stop the other players (Quran listening, azkar) first
        terminateAllServices(appContext, PagePlaybackService::class.java)

        _state.update { it.copy(options = options, following = true) }
        connection.withController {
            it.sendCustomCommand(
                SessionCommand(PagePlaybackProtocol.CMD_PLAY, Bundle.EMPTY),
                request.toBundle(options)
            )
        }
    }

    /** Repetition / continuous changes apply to what is playing right away. */
    fun updateOptions(options: PageListeningOptions) {
        _state.update { it.copy(options = options) }
        controller?.sendCustomCommand(
            SessionCommand(PagePlaybackProtocol.CMD_UPDATE_OPTIONS, Bundle.EMPTY),
            options.toBundle()
        )
    }

    fun pause() {
        controller?.pause()
    }

    /** Stops playback and any running download. */
    fun stop() {
        controller?.sendCustomCommand(SessionCommand(PagePlaybackProtocol.CMD_STOP, Bundle.EMPTY), Bundle.EMPTY)
    }

    fun nextVerse() {
        controller?.seekToNextMediaItem()
    }

    fun previousVerse() {
        controller?.seekToPreviousMediaItem()
    }

    fun increaseSpeed() = changeSpeed(+0.25f)
    fun decreaseSpeed() = changeSpeed(-0.25f)

    private fun changeSpeed(delta: Float) {
        val c = controller ?: return
        c.setPlaybackSpeed((c.playbackParameters.speed + delta).coerceIn(0.25f, 2f))
    }

    /** See PagePlayerState.following. */
    fun setFollowing(value: Boolean) {
        _state.update { it.copy(following = value) }
    }

    // endregion

    private fun sync(c: MediaController) {
        syncPlayer(c)
        syncExtras(c.sessionExtras)
    }

    private fun syncPlayer(p: Player) {
        val hasMedia = p.mediaItemCount > 0 && p.playbackState != Player.STATE_IDLE
        val position = if (hasMedia) PageAudio.parse(p.currentMediaItem?.mediaId) else null
        _state.update {
            it.copy(
                hasMedia = hasMedia,
                isPlaying = p.isPlaying || (p.playWhenReady && p.playbackState == Player.STATE_BUFFERING),
                isPaused = hasMedia && !p.playWhenReady && p.playbackState != Player.STATE_ENDED,
                speed = p.playbackParameters.speed,
                playingPage = position?.page,
                currentSurah = position?.surah,
                currentVerse = position?.verse,
            )
        }
    }

    private fun syncExtras(extras: Bundle) {
        _state.update {
            it.copy(
                segment = PageSegment.fromBundle(extras),
                isDownloading = extras.getBoolean(PagePlaybackProtocol.EXTRA_DOWNLOADING),
                downloadDone = extras.getInt(PagePlaybackProtocol.EXTRA_DOWNLOAD_DONE),
                downloadTotal = extras.getInt(PagePlaybackProtocol.EXTRA_DOWNLOAD_TOTAL),
            )
        }
    }

    companion object {
        @Volatile
        private var instance: PagePlaybackManager? = null

        fun getInstance(context: Context): PagePlaybackManager =
            instance ?: synchronized(this) {
                instance ?: PagePlaybackManager(context).also { instance = it }
            }
    }
}
