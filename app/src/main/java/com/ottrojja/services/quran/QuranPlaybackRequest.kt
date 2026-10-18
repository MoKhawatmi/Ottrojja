package com.ottrojja.services.quran

import android.os.Bundle
import com.ottrojja.classes.QuranListeningMode
import com.ottrojja.room.entities.ChapterData

/** What the user asked to play. Lives in the app-scoped manager, so the UI can restore from it. */
sealed interface QuranPlaybackRequest {
    val mode: QuranListeningMode
    val title: String

    data class FullSurah(
        val surah: ChapterData,
        /** Names ordered by surah id, so the service can title the next surah in continuous mode. */
        val chapterNames: List<String>,
    ) : QuranPlaybackRequest {
        override val mode get() = QuranListeningMode.سورة_كاملة
        override val title get() = "سورة ${surah.chapterName}"
    }

    data class VerseRange(
        val startSurah: ChapterData,
        val startVerse: Int,
        val endSurah: ChapterData,
        val endVerse: Int,
        /** File names in play order, basmalah already inserted. */
        val audioFiles: List<String>,
    ) : QuranPlaybackRequest {
        override val mode get() = QuranListeningMode.مقطع_ايات
        override val title get() =
            "${startSurah.chapterName}: $startVerse - ${endSurah.chapterName}: $endVerse"
    }

    fun toBundle(options: ListeningOptions): Bundle = options.toBundle().apply {
        putString(QuranPlaybackProtocol.KEY_MODE, mode.name)
        putString(QuranPlaybackProtocol.KEY_TITLE, title)
        when (val request = this@QuranPlaybackRequest) {
            is FullSurah -> {
                putInt(QuranPlaybackProtocol.KEY_SURAH_ID, request.surah.surahId)
                putStringArrayList(QuranPlaybackProtocol.KEY_CHAPTER_NAMES, ArrayList(request.chapterNames))
            }

            is VerseRange -> putStringArrayList(QuranPlaybackProtocol.KEY_FILES, ArrayList(request.audioFiles))
        }
    }
}
