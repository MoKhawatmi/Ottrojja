package com.ottrojja.classes

data class TeacherAnswer(val answer: String, val status: AnswerStatus)

enum class AnswerStatus {
    UNCHECKED, RIGHT, WRONG
}