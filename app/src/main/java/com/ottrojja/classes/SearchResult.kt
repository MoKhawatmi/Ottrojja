package com.ottrojja.classes

data class SearchResult(
    val pageNum: String,
    val surahNum: Int,
    val verseNum: Int,
    val verseText: String,
    val surahName: String
)
