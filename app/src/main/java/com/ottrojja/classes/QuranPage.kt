package com.ottrojja.classes

data class QuranPage(val pageNum:String, val ytLink: String, val benefits:Array<String>, val appliance:Array<String>, val guidance:Array<String> ,val pageContent:Array<PageContent>) {

}

data class PageContent(
    val type: String,
    val surahName: String,
    val surahNum: String,
    val surahTotal: String,
    val surahType: String,
    val verseNum: String,
    val verseText: String,
    val verseTextPlain: String,
) {

}

