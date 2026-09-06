package com.ottrojja.screens.Khitmah.classes

import com.ottrojja.interfaces.TabItem

enum class KhitmahTab(override val displayName: String) : TabItem {
    OVERVIEW(displayName = "نظرة عامة"),
    PROGRESS(displayName = "التقدم"),
    PART_PROGRESS(displayName = "الأجزاء"),
    CHAPTER_PROGRESS(displayName = "السور")
}