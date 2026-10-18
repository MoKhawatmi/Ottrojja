package com.ottrojja.services.quran

import android.content.Intent
import android.net.Uri
import android.os.Bundle
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
import com.ottrojja.classes.QuranListeningMode
import com.ottrojja.composables.ToastManager.classes.AppToastManager
import com.ottrojja.composables.ToastManager.classes.AppToastType
import com.ottrojja.services.common.HiddenDuration
import com.ottrojja.services.common.buildSpeechPlayer
import com.ottrojja.services.common.logoNotificationProvider
import com.ottrojja.services.common.notificationControllerResult
import com.ottrojja.services.common.setLaunchActivity
import com.ottrojja.services.quran.QuranPlaybackProtocol.ACTION_TERMINATE
import com.ottrojja.services.quran.QuranPlaybackProtocol.CMD_PLAY
import com.ottrojja.services.quran.QuranPlaybackProtocol.CMD_UPDATE_OPTIONS
import com.ottrojja.services.quran.QuranPlaybackProtocol.KEY_CHAPTER_NAMES
import com.ottrojja.services.quran.QuranPlaybackProtocol.KEY_CONTINUOUS
import com.ottrojja.services.quran.QuranPlaybackProtocol.KEY_FILES
import com.ottrojja.services.quran.QuranPlaybackProtocol.KEY_MODE
import com.ottrojja.services.quran.QuranPlaybackProtocol.KEY_RANGE_REPS
import com.ottrojja.services.quran.QuranPlaybackProtocol.KEY_SURAH_ID
import com.ottrojja.services.quran.QuranPlaybackProtocol.KEY_SURAH_REPS
import com.ottrojja.services.quran.QuranPlaybackProtocol.KEY_TITLE
import com.ottrojja.services.quran.QuranPlaybackProtocol.KEY_VERSE_REPS

/**
 * Replaces QuranPlayerService. Media3 handles the foreground notification, lock screen and
 * headset controls, audio focus and binding. This class only builds playlists and applies
 * the repetition rules.
 *
 * Repetition:
 * - verse / surah repeats: the current item gets REPEAT_MODE_ONE until it has looped enough
 *   times (gapless, no seek). Basmalah is never repeated.
 * - range repeats: when the playlist ends, seek back to the first item.
 * - continuous surahs: the next surah is appended when the current one starts.
 */
class QuranPlaybackService : MediaSessionService() {

    private var mediaSession: MediaSession? = null
    private lateinit var player: ExoPlayer

    private var mode: QuranListeningMode? = null
    private var settings = RepeatSettings()
    private var chapterNames: List<String> = emptyList()
    private var rangeTitle = ""
    private var itemRepeats = 0
    private var rangeRepeats = 0

    private data class RepeatSettings(
        val surah: Int = 0,
        val verse: Int = 0,
        val range: Int = 0,
        val continuous: Boolean = true,
    ) {
        companion object {
            fun from(args: Bundle) = RepeatSettings(
                surah = args.getInt(KEY_SURAH_REPS),
                verse = args.getInt(KEY_VERSE_REPS),
                range = args.getInt(KEY_RANGE_REPS),
                continuous = args.getBoolean(KEY_CONTINUOUS, true),
            )
        }
    }

    override fun onCreate() {
        super.onCreate()

        setMediaNotificationProvider(logoNotificationProvider(this))

        player = buildSpeechPlayer(this)
        player.addListener(playerListener)
        player.addListener(HiddenDuration.publisher { mediaSession })

        // no duration for the system, so the notification shows no progress bar
        mediaSession = MediaSession.Builder(this, HiddenDuration.sessionPlayer(player))
            .setId("quran") // must differ from the Azkar session id (same process)
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
            mode = null
            stopSelf()
            return START_NOT_STICKY
        }
        return super.onStartCommand(intent, flags, startId)
    }

    override fun onTaskRemoved(rootIntent: Intent?) {
        // App swiped away: keep playing if playing, otherwise shut down.
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

    // region requests from the app

    private val sessionCallback = object : MediaSession.Callback {
        override fun onConnect(
            session: MediaSession,
            controller: MediaSession.ControllerInfo
        ): MediaSession.ConnectionResult {
            if (session.isMediaNotificationController(controller)) {
                return notificationControllerResult(session)
            }
            // Only our own app gets the custom commands; Bluetooth, Android Auto, system UI get the defaults.
            if (controller.packageName != packageName) {
                return MediaSession.ConnectionResult.AcceptedResultBuilder(session).build()
            }
            val commands = MediaSession.ConnectionResult.DEFAULT_SESSION_COMMANDS.buildUpon()
                .add(SessionCommand(CMD_PLAY, Bundle.EMPTY))
                .add(SessionCommand(CMD_UPDATE_OPTIONS, Bundle.EMPTY))
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
            when (customCommand.customAction) {
                CMD_PLAY -> startRequest(args)
                CMD_UPDATE_OPTIONS -> updateOptions(args)
            }
            return Futures.immediateFuture(SessionResult(SessionResult.RESULT_SUCCESS))
        }
    }

    private fun startRequest(args: Bundle) {
        val newMode = args.getString(KEY_MODE)
            ?.let { runCatching { QuranListeningMode.valueOf(it) }.getOrNull() }
            ?: return

        settings = RepeatSettings.from(args)
        rangeTitle = args.getString(KEY_TITLE).orEmpty()
        chapterNames = args.getStringArrayList(KEY_CHAPTER_NAMES) ?: emptyList()

        val items = when (newMode) {
            QuranListeningMode.سورة_كاملة -> listOf(surahItem(args.getInt(KEY_SURAH_ID)))
            else -> args.getStringArrayList(KEY_FILES).orEmpty().map(::verseItem)
        }
        if (items.isEmpty()) return

        if (items.any { !it.isLocal() } && !Helpers.checkNetworkConnectivity(this)) {
            AppToastManager.show(message = "لا يوجد اتصال بالانترنت", type = AppToastType.ERROR)
            return
        }

        mode = newMode // must be set before setMediaItems, the transition callback reads it
        itemRepeats = 0
        rangeRepeats = 0
        player.setMediaItems(items)
        player.prepare()
        player.play()
    }

    private fun updateOptions(args: Bundle) {
        settings = RepeatSettings.from(args)
        if (mode == QuranListeningMode.سورة_كاملة) syncNextSurahQueue()
        applyItemRepeatMode()
    }

    // endregion

    // region repetition

    private val playerListener = object : Player.Listener {
        override fun onMediaItemTransition(mediaItem: MediaItem?, reason: Int) {
            if (reason == Player.MEDIA_ITEM_TRANSITION_REASON_REPEAT) itemRepeats++ else itemRepeats = 0
            if (mode == QuranListeningMode.سورة_كاملة) syncNextSurahQueue()
            applyItemRepeatMode()
        }

        override fun onPlaybackStateChanged(playbackState: Int) {
            if (playbackState == Player.STATE_ENDED
                && mode == QuranListeningMode.مقطع_ايات
                && rangeRepeats < settings.range
            ) {
                rangeRepeats++
                itemRepeats = 0
                player.seekTo(0, 0L)
                applyItemRepeatMode()
                player.play()
            }
        }

        override fun onPlayerError(error: PlaybackException) {
            error.printStackTrace()
            reportException(exception = error, file = "QuranPlaybackService")
            AppToastManager.show(message = "حصل خطأ يرجى المحاولة لاحقا", type = AppToastType.ERROR)
        }
    }

    private fun applyItemRepeatMode() {
        val target = when (mode) {
            QuranListeningMode.سورة_كاملة -> settings.surah
            QuranListeningMode.مقطع_ايات ->
                if (QuranAudio.isBasmalah(player.currentMediaItem?.mediaId)) 0 else settings.verse

            else -> 0
        }
        player.repeatMode = if (itemRepeats < target) Player.REPEAT_MODE_ONE else Player.REPEAT_MODE_OFF
    }

    /** Keeps exactly one upcoming surah queued when continuous playback is on, none when off. */
    private fun syncNextSurahQueue() {
        val currentIndex = player.currentMediaItemIndex
        if (currentIndex > 0) {
            player.removeMediaItems(0, currentIndex) // drop finished surahs
        }
        val upcoming = player.mediaItemCount - 1 - player.currentMediaItemIndex

        if (!settings.continuous) {
            if (upcoming > 0) player.removeMediaItems(player.currentMediaItemIndex + 1, player.mediaItemCount)
            return
        }
        if (upcoming > 0) return

        val currentId = QuranAudio.surahIdFromMediaId(player.currentMediaItem?.mediaId) ?: return
        if (currentId >= 114) return

        val next = surahItem(currentId + 1)
        if (!next.isLocal() && !Helpers.checkNetworkConnectivity(this)) return
        player.addMediaItem(next)
    }

    // endregion

    // region media items

    private fun surahItem(surahId: Int) = buildItem(
        mediaId = QuranAudio.surahMediaId(surahId),
        uri = QuranAudio.resolveUri(this, QuranAudio.chapterFile(surahId), QuranAudio.chapterUrl(surahId)),
        title = "سورة ${chapterNames.getOrNull(surahId - 1) ?: surahId}",
    )

    private fun verseItem(fileName: String) = buildItem(
        mediaId = QuranAudio.verseMediaId(fileName),
        uri = QuranAudio.resolveUri(this, fileName, QuranAudio.verseUrl(fileName)),
        title = rangeTitle,
    )

    private fun buildItem(mediaId: String, uri: Uri, title: String) = MediaItem.Builder()
        .setMediaId(mediaId)
        .setUri(uri)
        .setMediaMetadata(
            MediaMetadata.Builder()
                .setTitle(title)
                .setArtist("تطبيق اترجة")
                .build()
        )
        .build()

    private fun MediaItem.isLocal() = localConfiguration?.uri?.scheme == "file"

    // endregion
}
