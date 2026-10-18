package com.ottrojja.services.quran

import com.ottrojja.classes.QuranListeningMode

data class QuranPlayerState(
    val hasMedia: Boolean = false,
    val isPlaying: Boolean = false,
    val isPaused: Boolean = false,
    val durationMs: Long = 0L,
    val speed: Float = 1f,
    val title: String = "",
    val currentSurahId: Int? = null,
    val request: QuranPlaybackRequest? = null,
    val options: ListeningOptions = ListeningOptions(),
) {
    /** Mode of what is loaded in the player, or null when nothing is. */
    val activeMode: QuranListeningMode? get() = request?.mode?.takeIf { hasMedia }
}
