package com.ottrojja.services.page

import android.content.Intent
import android.os.Bundle
import androidx.media3.common.C
import androidx.media3.common.MediaItem
import androidx.media3.common.MediaMetadata
import androidx.media3.common.PlaybackException
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.session.MediaSession
import androidx.media3.session.MediaSessionService
import androidx.media3.session.SessionCommand
import androidx.media3.session.SessionResult
import com.google.common.util.concurrent.Futures
import com.google.common.util.concurrent.ListenableFuture
import com.ottrojja.R
import com.ottrojja.classes.Helpers
import com.ottrojja.classes.Helpers.reportException
import com.ottrojja.composables.ToastManager.classes.AppToastManager
import com.ottrojja.composables.ToastManager.classes.AppToastType
import com.ottrojja.room.entities.PageContent
import com.ottrojja.room.entities.PageContentItemType
import com.ottrojja.services.common.HiddenDuration
import com.ottrojja.services.common.buildSpeechPlayer
import com.ottrojja.services.common.logoNotificationProvider
import com.ottrojja.services.common.notificationControllerResult
import com.ottrojja.services.common.setLaunchActivity
import com.ottrojja.services.page.PagePlaybackProtocol.ACTION_CANCEL_DOWNLOAD
import com.ottrojja.services.page.PagePlaybackProtocol.ACTION_TERMINATE
import com.ottrojja.services.page.PagePlaybackProtocol.CMD_PLAY
import com.ottrojja.services.page.PagePlaybackProtocol.CMD_STOP
import com.ottrojja.services.page.PagePlaybackProtocol.CMD_UPDATE_OPTIONS
import com.ottrojja.services.page.PagePlaybackProtocol.EXTRA_DOWNLOADING
import com.ottrojja.services.page.PagePlaybackProtocol.EXTRA_DOWNLOAD_DONE
import com.ottrojja.services.page.PagePlaybackProtocol.EXTRA_DOWNLOAD_TOTAL
import com.ottrojja.services.page.PagePlaybackProtocol.KEY_CONTINUOUS
import com.ottrojja.services.page.PagePlaybackProtocol.KEY_REPEAT_RANGE
import com.ottrojja.services.page.PagePlaybackProtocol.KEY_REPETITIONS
import com.ottrojja.services.page.PagePlaybackProtocol.LARGE_DOWNLOAD_THRESHOLD
import com.ottrojja.services.page.PagePlaybackProtocol.SESSION_ID
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * Replaces PagePlayerService. Built like QuranPlaybackService (the listening player):
 * Media3 handles the notification, lock screen / headset controls, audio focus and binding;
 * the app talks to it through PagePlaybackManager with custom session commands.
 *
 * Unlike the old service, this one owns the whole flow, so it keeps working with the app in the
 * background:
 * - builds the playlist from the database (basmalah before verse 1, except surahs 1 and 9)
 * - downloads missing verse files itself (progress notification for large downloads)
 * - continuous mode: prefetches + downloads the next page while the current one plays and appends it
 *   to the queue ("rolling queue"), so playback moves from page to page without the UI
 * - publishes the playing segment and download progress as session extras; the playing page comes
 *   from the current media item id. The Quran screen follows that page when it is (re)opened.
 *
 * Repetition (same rules as before):
 * - per verse: the current item gets REPEAT_MODE_ONE until it looped enough times (never the basmalah)
 * - whole range: when the segment ends it seeks back to the first item
 */
@androidx.annotation.OptIn(UnstableApi::class)
class PagePlaybackService : MediaSessionService() {

    private var mediaSession: MediaSession? = null
    private lateinit var player: ExoPlayer
    private lateinit var downloader: VerseAudioDownloader
    private lateinit var downloadNotifier: PageDownloadNotifier

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)
    private val repository by lazy { PagePlaybackDependencies.repository(this) }

    private var chapterNames: Map<Int, String> = emptyMap()
    private var settings = RepeatSettings()

    /** Segment whose items start at player index 0. */
    private var current: Segment? = null

    /** Prepared (downloaded) next segment, appended to the player when [nextAppended]. */
    private var next: Segment? = null
    private var nextAppended = false

    private var startJob: Job? = null
    private var nextJob: Job? = null

    private var itemRepeats = 0
    private var rangeRepeats = 0

    private var downloading = false
    private var downloadDone = 0
    private var downloadTotal = 0

    private class Segment(val bounds: PageSegment, val items: List<MediaItem>)

    private data class RepeatSettings(
        val repetitions: Int = 0,
        val repeatRange: Boolean = false,
        val continuous: Boolean = false,
    ) {
        companion object {
            fun from(args: Bundle) = RepeatSettings(
                repetitions = args.getInt(KEY_REPETITIONS),
                repeatRange = args.getBoolean(KEY_REPEAT_RANGE),
                continuous = args.getBoolean(KEY_CONTINUOUS),
            )
        }
    }

    // region lifecycle

    override fun onCreate() {
        super.onCreate()
        downloader = VerseAudioDownloader(this)
        downloadNotifier = PageDownloadNotifier(this)

        setMediaNotificationProvider(logoNotificationProvider(this))

        player = buildSpeechPlayer(this, C.WAKE_MODE_LOCAL) // all page audio is played from local files
        player.addListener(playerListener)

        // no duration for the system, so the notification shows no progress bar
        // (the session extras carry the segment / download state, the app does not need the duration)
        mediaSession = MediaSession.Builder(this, HiddenDuration.sessionPlayer(player))
            .setId(SESSION_ID) // must differ from the "quran" and "azkar" sessions (same process)
            .setCallback(sessionCallback)
            .setLaunchActivity(this)
            .build()
    }

    override fun onGetSession(controllerInfo: MediaSession.ControllerInfo): MediaSession? = mediaSession

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            // Plain-intent stop (Helpers.terminateAllServices uses the playback manager instead).
            ACTION_TERMINATE -> {
                stopEverything()
                stopSelf()
                return START_NOT_STICKY
            }

            // Cancel button of the download notification.
            ACTION_CANCEL_DOWNLOAD -> {
                startJob?.cancel()
                nextJob?.cancel()
                downloadNotifier.cancel()
                return START_NOT_STICKY
            }
        }
        return super.onStartCommand(intent, flags, startId)
    }

    override fun onTaskRemoved(rootIntent: Intent?) {
        if (!player.playWhenReady || player.mediaItemCount == 0 || player.playbackState == Player.STATE_ENDED) {
            stopEverything()
            stopSelf()
        }
    }

    override fun onDestroy() {
        scope.cancel()
        downloadNotifier.cancel()
        mediaSession?.run {
            player.release()
            release()
        }
        mediaSession = null
        super.onDestroy()
    }

    // endregion

    // region commands from the app

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
                .add(SessionCommand(CMD_STOP, Bundle.EMPTY))
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
                CMD_STOP -> stopEverything()
            }
            return Futures.immediateFuture(SessionResult(SessionResult.RESULT_SUCCESS))
        }
    }

    private fun startRequest(args: Bundle) {
        val request = PagePlaybackRequest.fromBundle(args) ?: return
        settings = RepeatSettings.from(args)

        startJob?.cancel()
        nextJob?.cancel()
        finish() // whatever was playing stops right away

        startJob = scope.launch {
            try {
                val rows = withContext(Dispatchers.IO) {
                    repository.getPagesContentRange(
                        request.startSurah, request.startVerse, request.endSurah, request.endVerse
                    )
                }
                val segment = buildSegment(rows, allowNotification = true) ?: return@launch
                startSegment(segment)
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                e.printStackTrace()
                reportException(exception = e, file = "PagePlaybackService", details = "request: $request")
                AppToastManager.show(message = "حصل خطأ يرجى المحاولة لاحقا", type = AppToastType.ERROR)
            }
        }
    }

    private fun updateOptions(args: Bundle) {
        settings = RepeatSettings.from(args)
        val cur = current

        val rangeRepeatsPending = settings.repeatRange && rangeRepeats < settings.repetitions
        if (cur != null && nextAppended && (!settings.continuous || rangeRepeatsPending)) {
            // take the queued next page off the player again
            player.removeMediaItems(cur.items.size, player.mediaItemCount)
            nextAppended = false
        }
        if (!settings.continuous) {
            nextJob?.cancel()
            next = null
        } else {
            prepareNextIfNeeded()
        }
        applyItemRepeatMode()
        maybeAppendNext()
    }

    /** Stop button, notification close, another player starting. */
    private fun stopEverything() {
        startJob?.cancel()
        nextJob?.cancel()
        downloadNotifier.cancel()
        finish()
    }

    // endregion

    // region segments (rolling queue)

    private fun startSegment(segment: Segment) {
        current = segment
        next = null
        nextAppended = false
        itemRepeats = 0
        rangeRepeats = 0
        nextJob?.cancel()

        player.setMediaItems(segment.items)
        player.prepare()
        player.play()

        publishExtras()
        prepareNextIfNeeded()
    }

    /** Prefetches (and downloads) what comes after the current segment, while it plays. */
    private fun prepareNextIfNeeded() {
        val cur = current ?: return
        if (!settings.continuous || next != null || nextJob?.isActive == true) return

        nextJob = scope.launch {
            try {
                val rows = withContext(Dispatchers.IO) { versesAfter(cur.bounds) }
                val segment = if (rows.isEmpty()) null else buildSegment(rows, allowNotification = false)
                if (current !== cur) return@launch // superseded meanwhile

                if (segment == null) {
                    // end of the mushaf, or the download failed: stop at the end of this segment
                    if (player.playbackState == Player.STATE_ENDED) finish()
                    return@launch
                }
                next = segment
                maybeAppendNext()
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                e.printStackTrace()
                reportException(exception = e, file = "PagePlaybackService", details = "prefetch after ${cur.bounds}")
            }
        }
    }

    /**
     * Continuous mode continues with the rest of the end page when the selection stopped mid-page
     * (the old service skipped those verses), otherwise with the whole next page.
     */
    private suspend fun versesAfter(bounds: PageSegment): List<PageContent> {
        val endPageVerses = repository.fetchPageVerses(bounds.endPage.toString()).onlyVerses()
        val endIndex = endPageVerses.indexOfFirst { it.surahNum == bounds.endSurah && it.verseNum == bounds.endVerse }
        val rest = if (endIndex >= 0) endPageVerses.drop(endIndex + 1) else emptyList()
        if (rest.isNotEmpty()) return rest
        if (bounds.endPage >= 604) return emptyList()
        return repository.fetchPageVerses((bounds.endPage + 1).toString())
    }

    /**
     * Appends the prepared next segment once the last item of the current one plays and no range
     * repetition is left, so the player never reaches ENDED between pages (keeps the foreground
     * service alive in the background and makes the page change gapless).
     */
    private fun maybeAppendNext() {
        val cur = current ?: return
        val nxt = next ?: return
        if (nextAppended || !settings.continuous) return
        if (settings.repeatRange && rangeRepeats < settings.repetitions) return

        if (player.playbackState == Player.STATE_ENDED) {
            // the next page was not ready in time: start it now
            startSegment(nxt)
            return
        }
        if (player.currentMediaItemIndex != cur.items.size - 1) return

        player.addMediaItems(nxt.items)
        nextAppended = true
    }

    /** Stops and clears the player; the playback notification goes away. */
    private fun finish() {
        player.stop()
        player.clearMediaItems()
        current = null
        next = null
        nextAppended = false
        itemRepeats = 0
        rangeRepeats = 0
        publishExtras()
    }

    // endregion

    // region player events

    private val playerListener = object : Player.Listener {
        override fun onMediaItemTransition(mediaItem: MediaItem?, reason: Int) {
            if (reason == Player.MEDIA_ITEM_TRANSITION_REASON_REPEAT) itemRepeats++ else itemRepeats = 0

            val cur = current
            if (cur != null && nextAppended && player.currentMediaItemIndex >= cur.items.size) {
                // moved into the next page: it becomes the current segment
                player.removeMediaItems(0, cur.items.size)
                current = next
                next = null
                nextAppended = false
                rangeRepeats = 0
                publishExtras()
                prepareNextIfNeeded()
            }

            applyItemRepeatMode()
            maybeAppendNext()
        }

        override fun onPlaybackStateChanged(playbackState: Int) {
            if (playbackState != Player.STATE_ENDED) return
            current ?: return

            // repeat the whole range
            if (settings.repeatRange && rangeRepeats < settings.repetitions) {
                rangeRepeats++
                itemRepeats = 0
                player.seekTo(0, 0L)
                applyItemRepeatMode()
                player.play()
                maybeAppendNext()
                return
            }

            val nxt = next
            when {
                settings.continuous && nxt != null -> startSegment(nxt)
                settings.continuous && nextJob?.isActive == true -> Unit // starts in maybeAppendNext() once ready
                else -> finish()
            }
        }

        override fun onPlayerError(error: PlaybackException) {
            error.printStackTrace()
            reportException(exception = error, file = "PagePlaybackService")
            AppToastManager.show(message = "حصل خطأ يرجى المحاولة لاحقا", type = AppToastType.ERROR)
        }
    }

    private fun applyItemRepeatMode() {
        val isBasmalah = PageAudio.isBasmalah(player.currentMediaItem?.mediaId)
        val target = if (!settings.repeatRange && !isBasmalah) settings.repetitions else 0
        player.repeatMode = if (itemRepeats < target) Player.REPEAT_MODE_ONE else Player.REPEAT_MODE_OFF
    }

    // endregion

    // region building items and downloading

    /** Null when there is nothing to play or the download failed / was cancelled. */
    private suspend fun buildSegment(rows: List<PageContent>, allowNotification: Boolean): Segment? {
        val verses = rows.onlyVerses()
        if (verses.isEmpty()) return null
        ensureChapterNames()

        val files = mutableListOf<String>()
        val items = mutableListOf<MediaItem>()
        verses.forEach { verse ->
            val page = verse.pageNum.toInt()
            val verseNum = verse.verseNum!!
            val surahName = chapterNames[verse.surahNum].orEmpty()

            // basmalah at the start of each surah except Al-Fatiha (its verse 1 is the basmalah) and At-Tawbah
            if (verseNum == 1 && verse.surahNum != 1 && verse.surahNum != 9) {
                files += PageAudio.BASMALAH_FILE
                items += buildItem(
                    mediaId = PageAudio.basmalahMediaId(page, verse.surahNum),
                    fileName = PageAudio.BASMALAH_FILE,
                    title = "البسملة - سورة $surahName",
                    subtitle = "الصفحة $page",
                )
            }

            val file = PageAudio.verseFile(verse)
            files += file
            items += buildItem(
                mediaId = PageAudio.verseMediaId(page, verse.surahNum, verseNum),
                fileName = file,
                title = "سورة $surahName - الآية $verseNum",
                subtitle = "الصفحة $page",
            )
        }

        if (!ensureDownloaded(files.distinct(), allowNotification)) return null

        val first = verses.first()
        val last = verses.last()
        return Segment(
            bounds = PageSegment(
                startPage = first.pageNum.toInt(),
                startSurah = first.surahNum,
                startVerse = first.verseNum!!,
                endPage = last.pageNum.toInt(),
                endSurah = last.surahNum,
                endVerse = last.verseNum!!,
            ),
            items = items,
        )
    }

    private fun buildItem(mediaId: String, fileName: String, title: String, subtitle: String) =
        MediaItem.Builder()
            .setMediaId(mediaId)
            .setUri(PageAudio.localUri(this, fileName))
            .setMediaMetadata(
                MediaMetadata.Builder()
                    .setTitle(title)
                    .setArtist(subtitle)
                    .setAlbumTitle("تطبيق اترجة")
                    .build()
            )
            .build()

    private suspend fun ensureChapterNames() {
        if (chapterNames.isNotEmpty()) return
        chapterNames = withContext(Dispatchers.IO) {
            repository.getAllChapters().associate { it.surahId to it.chapterName }
        }
    }

    /** True when every file is on disk (downloading the missing ones first). */
    private suspend fun ensureDownloaded(files: List<String>, allowNotification: Boolean): Boolean {
        val missing = withContext(Dispatchers.IO) {
            files.filterNot { PageAudio.localFile(this@PagePlaybackService, it).exists() }
        }
        if (missing.isEmpty()) return true

        if (!Helpers.checkNetworkConnectivity(this)) {
            AppToastManager.show(message = "لا يوجد اتصال بالانترنت", type = AppToastType.ERROR)
            return false
        }

        val notify = allowNotification && missing.size >= LARGE_DOWNLOAD_THRESHOLD
        setDownloading(true, 0, missing.size, notify)
        try {
            return when (downloader.download(missing) { done -> setDownloading(true, done, missing.size, notify) }) {
                VerseAudioDownloader.Result.Success -> {
                    downloadNotifier.cancel()
                    true
                }

                VerseAudioDownloader.Result.NoSpace -> {
                    val message = resources.getString(R.string.enospc)
                    AppToastManager.show(message = message, type = AppToastType.ERROR)
                    if (notify) downloadNotifier.showFailed(message) else downloadNotifier.cancel()
                    false
                }

                VerseAudioDownloader.Result.Failed -> {
                    val message = "حدث خطأ اثناء التحميل"
                    AppToastManager.show(message = message, type = AppToastType.ERROR)
                    if (notify) downloadNotifier.showFailed(message) else downloadNotifier.cancel()
                    false
                }
            }
        } catch (e: CancellationException) {
            downloadNotifier.cancel()
            throw e
        } finally {
            setDownloading(false, 0, 0, false)
        }
    }

    private fun setDownloading(active: Boolean, done: Int, total: Int, notify: Boolean) {
        downloading = active
        downloadDone = done
        downloadTotal = total
        if (active && notify) downloadNotifier.showProgress(done, total)
        publishExtras()
    }

    // endregion

    /** Tells the app what is being played and downloaded (read by PagePlaybackManager). */
    private fun publishExtras() {
        val extras = Bundle()
        current?.bounds?.writeTo(extras)
        extras.putBoolean(EXTRA_DOWNLOADING, downloading)
        extras.putInt(EXTRA_DOWNLOAD_DONE, downloadDone)
        extras.putInt(EXTRA_DOWNLOAD_TOTAL, downloadTotal)
        mediaSession?.setSessionExtras(extras)
    }

    private fun List<PageContent>.onlyVerses() =
        filter { it.type == PageContentItemType.verse && it.verseNum != null }
}
