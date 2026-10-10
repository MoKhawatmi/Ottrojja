package com.ottrojja.screens.quranScreen

import androidx.annotation.Keep
import com.ottrojja.room.entities.PageContent

@Keep
data class SectionVerse(val pageContent: PageContent, var expanded: Boolean)
