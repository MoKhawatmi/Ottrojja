package com.ottrojja.services.quran

import android.os.Bundle
import com.ottrojja.classes.Helpers.repetitionOptionsMap

/** Repetition options as the UI shows them (keys of repetitionOptionsMap). */
data class ListeningOptions(
    val surahRepetitions: String = "0",
    val verseRepetitions: String = "0",
    val rangeRepetitions: String = "0",
    val continuousChapterPlaying: Boolean = true,
) {
    fun toBundle() = Bundle().apply {
        putInt(QuranPlaybackProtocol.KEY_SURAH_REPS, repetitionOptionsMap[surahRepetitions] ?: 0)
        putInt(QuranPlaybackProtocol.KEY_VERSE_REPS, repetitionOptionsMap[verseRepetitions] ?: 0)
        putInt(QuranPlaybackProtocol.KEY_RANGE_REPS, repetitionOptionsMap[rangeRepetitions] ?: 0)
        putBoolean(QuranPlaybackProtocol.KEY_CONTINUOUS, continuousChapterPlaying)
    }
}
