package com.ottrojja.classes

import com.ottrojja.screens.mainScreen.ChapterData

data class QuranPlayingParameters(
    val startingSurah: ChapterData? = null,
    val startingVerse: Int? = null,
    val endSurah: ChapterData? = null,
    val endVerse: Int? = null,
    val playListItems: List<String>? = null,
    val listeningMode: QuranListeningMode,
    val selectedSurah: ChapterData? = null,
    val continuousChapterPlaying: Boolean = true,
    val surahRepetitions: String = "0",
    val verseRepetitions: String = "0",
    val verseRangeRepetitions: String = "0",
)

enum class QuranListeningMode {
    مقطع_ايات, سورة_كاملة
}
