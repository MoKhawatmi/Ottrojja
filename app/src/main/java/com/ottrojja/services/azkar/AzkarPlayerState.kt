package com.ottrojja.services.azkar

data class AzkarPlayerState(
    val hasMedia: Boolean = false,
    val isPlaying: Boolean = false,
    val isPaused: Boolean = false,
    val durationMs: Long = 0L,
    val speed: Float = 1f,
    /** zikrAddress of the loaded zikr (used as the media id), null when nothing is loaded. */
    val currentAddress: String? = null,
)
