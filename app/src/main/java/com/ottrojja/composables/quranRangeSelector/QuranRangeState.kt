package com.ottrojja.composables.quranRangeSelector

import com.ottrojja.room.entities.ChapterData
import com.ottrojja.room.entities.PageContent

data class QuranRangeState(
    val startSurah: ChapterData? = null,
    val startVerse: PageContent? = null,
    val endSurah: ChapterData? = null,
    val endVerse: PageContent? = null
)
