package com.ottrojja.classes.TextParser

sealed interface TextBlock {

    data class Paragraph(
        val text: String
    ) : TextBlock

    data class Quote(
        val text: String
    ) : TextBlock

    data class Poetry(
        val text: String
    ) : TextBlock

    data class Heading(
        val text: String
    ) : TextBlock
}