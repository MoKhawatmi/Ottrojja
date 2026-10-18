package com.ottrojja.services.quran

/** Custom session commands and bundle keys shared by the manager and the service. */
object QuranPlaybackProtocol {
    const val CMD_PLAY = "com.ottrojja.quran.PLAY"
    const val CMD_UPDATE_OPTIONS = "com.ottrojja.quran.UPDATE_OPTIONS"

    /** Same action string the old services used; stops the player when sent as a plain service intent. */
    const val ACTION_TERMINATE = "TERMINATE"

    const val KEY_MODE = "mode"
    const val KEY_TITLE = "title"
    const val KEY_SURAH_ID = "surah_id"
    const val KEY_CHAPTER_NAMES = "chapter_names"
    const val KEY_FILES = "files"
    const val KEY_SURAH_REPS = "surah_reps"
    const val KEY_VERSE_REPS = "verse_reps"
    const val KEY_RANGE_REPS = "range_reps"
    const val KEY_CONTINUOUS = "continuous"
}
