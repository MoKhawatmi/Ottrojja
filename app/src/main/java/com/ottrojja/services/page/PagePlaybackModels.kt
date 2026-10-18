package com.ottrojja.services.page

import android.os.Bundle
import com.ottrojja.classes.Helpers.repetitionOptionsMap

/** Custom session commands, bundle keys and ids shared by PagePlaybackManager and PagePlaybackService. */
object PagePlaybackProtocol {
    const val SESSION_ID = "page"

    const val CMD_PLAY = "com.ottrojja.page.PLAY"
    const val CMD_UPDATE_OPTIONS = "com.ottrojja.page.UPDATE_OPTIONS"
    const val CMD_STOP = "com.ottrojja.page.STOP"

    /** Same action string the old services used; stops the player when sent as a plain service intent. */
    const val ACTION_TERMINATE = "TERMINATE"
    const val ACTION_CANCEL_DOWNLOAD = "com.ottrojja.page.CANCEL_DOWNLOAD"

    // request
    const val KEY_START_SURAH = "start_surah"
    const val KEY_START_VERSE = "start_verse"
    const val KEY_END_SURAH = "end_surah"
    const val KEY_END_VERSE = "end_verse"

    // options
    const val KEY_REPETITIONS = "repetitions"
    const val KEY_REPEAT_RANGE = "repeat_range"
    const val KEY_CONTINUOUS = "continuous"

    // session extras (service -> app)
    const val EXTRA_SEG_START_PAGE = "seg_start_page"
    const val EXTRA_SEG_START_SURAH = "seg_start_surah"
    const val EXTRA_SEG_START_VERSE = "seg_start_verse"
    const val EXTRA_SEG_END_PAGE = "seg_end_page"
    const val EXTRA_SEG_END_SURAH = "seg_end_surah"
    const val EXTRA_SEG_END_VERSE = "seg_end_verse"
    const val EXTRA_DOWNLOADING = "downloading"
    const val EXTRA_DOWNLOAD_DONE = "download_done"
    const val EXTRA_DOWNLOAD_TOTAL = "download_total"

    /** Downloads of at least this many missing verse files get a progress notification. */
    const val LARGE_DOWNLOAD_THRESHOLD = 20
    const val DOWNLOAD_NOTIFICATION_ID = 301
    const val DOWNLOAD_CHANNEL_ID = "VERSES_DOWNLOAD_CHANNEL"
}

/** What the user selected to play: a verse range. */
data class PagePlaybackRequest(
    val startSurah: Int,
    val startVerse: Int,
    val endSurah: Int,
    val endVerse: Int,
) {
    fun matches(segment: PageSegment?) = segment != null
            && segment.startSurah == startSurah && segment.startVerse == startVerse
            && segment.endSurah == endSurah && segment.endVerse == endVerse

    fun toBundle(options: PageListeningOptions): Bundle = options.toBundle().apply {
        putInt(PagePlaybackProtocol.KEY_START_SURAH, startSurah)
        putInt(PagePlaybackProtocol.KEY_START_VERSE, startVerse)
        putInt(PagePlaybackProtocol.KEY_END_SURAH, endSurah)
        putInt(PagePlaybackProtocol.KEY_END_VERSE, endVerse)
    }

    companion object {
        fun fromBundle(args: Bundle): PagePlaybackRequest? {
            val startSurah = args.getInt(PagePlaybackProtocol.KEY_START_SURAH, -1)
            val startVerse = args.getInt(PagePlaybackProtocol.KEY_START_VERSE, -1)
            val endSurah = args.getInt(PagePlaybackProtocol.KEY_END_SURAH, -1)
            val endVerse = args.getInt(PagePlaybackProtocol.KEY_END_VERSE, -1)
            if (startSurah < 1 || startVerse < 1 || endSurah < 1 || endVerse < 1) return null
            return PagePlaybackRequest(startSurah, startVerse, endSurah, endVerse)
        }
    }
}

/**
 * Listening options as the UI shows them.
 * [repetition] is a key of repetitionOptionsMap; it applies per verse, or to the whole range
 * when [repeatRange] is true (the old RepetitionTab.الاية / RepetitionTab.المقطع).
 */
data class PageListeningOptions(
    val repetition: String = "0",
    val repeatRange: Boolean = false,
    val continuous: Boolean = false,
) {
    fun toBundle() = Bundle().apply {
        putInt(PagePlaybackProtocol.KEY_REPETITIONS, repetitionOptionsMap[repetition] ?: 0)
        putBoolean(PagePlaybackProtocol.KEY_REPEAT_RANGE, repeatRange)
        putBoolean(PagePlaybackProtocol.KEY_CONTINUOUS, continuous)
    }
}

/**
 * The part of the mushaf the service is playing right now. Starts as the user's selection and moves
 * forward page by page in continuous mode.
 */
data class PageSegment(
    val startPage: Int,
    val startSurah: Int,
    val startVerse: Int,
    val endPage: Int,
    val endSurah: Int,
    val endVerse: Int,
) {
    fun writeTo(bundle: Bundle) = bundle.apply {
        putInt(PagePlaybackProtocol.EXTRA_SEG_START_PAGE, startPage)
        putInt(PagePlaybackProtocol.EXTRA_SEG_START_SURAH, startSurah)
        putInt(PagePlaybackProtocol.EXTRA_SEG_START_VERSE, startVerse)
        putInt(PagePlaybackProtocol.EXTRA_SEG_END_PAGE, endPage)
        putInt(PagePlaybackProtocol.EXTRA_SEG_END_SURAH, endSurah)
        putInt(PagePlaybackProtocol.EXTRA_SEG_END_VERSE, endVerse)
    }

    companion object {
        fun fromBundle(extras: Bundle?): PageSegment? {
            extras ?: return null
            val startPage = extras.getInt(PagePlaybackProtocol.EXTRA_SEG_START_PAGE, -1)
            if (startPage < 1) return null
            return PageSegment(
                startPage = startPage,
                startSurah = extras.getInt(PagePlaybackProtocol.EXTRA_SEG_START_SURAH),
                startVerse = extras.getInt(PagePlaybackProtocol.EXTRA_SEG_START_VERSE),
                endPage = extras.getInt(PagePlaybackProtocol.EXTRA_SEG_END_PAGE),
                endSurah = extras.getInt(PagePlaybackProtocol.EXTRA_SEG_END_SURAH),
                endVerse = extras.getInt(PagePlaybackProtocol.EXTRA_SEG_END_VERSE),
            )
        }
    }
}

data class PagePlayerState(
    val hasMedia: Boolean = false,
    val isPlaying: Boolean = false,
    val isPaused: Boolean = false,
    val speed: Float = 1f,
    /** Page of the verse being played; drives auto pagination. */
    val playingPage: Int? = null,
    val currentSurah: Int? = null,
    /** Null while the basmalah plays. */
    val currentVerse: Int? = null,
    val segment: PageSegment? = null,
    val isDownloading: Boolean = false,
    val downloadDone: Int = 0,
    val downloadTotal: Int = 0,
    val options: PageListeningOptions = PageListeningOptions(),
    /**
     * Whether the Quran screen should follow the playing page. Turned off when the user swipes
     * away manually, back on when they return to the playing page or press play.
     * App-scoped, so it survives leaving and re-opening the screen.
     */
    val following: Boolean = true,
)
