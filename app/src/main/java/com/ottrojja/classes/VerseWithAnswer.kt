package com.ottrojja.classes

import com.ottrojja.room.entities.PageContent

data class VerseWithAnswer(
    val verse: PageContent,
    var answerCorrect: Boolean = false
)
