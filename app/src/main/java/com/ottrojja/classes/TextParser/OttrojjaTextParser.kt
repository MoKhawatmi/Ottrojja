package com.ottrojja.classes.TextParser

object OttrojjaTextParser {

    fun parse(content: String): List<TextBlock> {

        val regex = Regex(
            """\[(PARAGRAPH|QUOTE|POETRY|HEADING|STRONG)]([\s\S]*?)\[/\1]"""
        )

        val matches = regex.findAll(content).toList()

        if (matches.isEmpty()) {
            return listOf(
                TextBlock.Paragraph(content.trim())
            )
        }

        return matches.map { match ->
            val type = match.groupValues[1]
            val text = match.groupValues[2].trim()

            when (type) {
                "PARAGRAPH" -> TextBlock.Paragraph(text)
                "QUOTE" -> TextBlock.Quote(text)
                "POETRY" -> TextBlock.Poetry(text)
                "HEADING" -> TextBlock.Heading(text)
                else -> TextBlock.Paragraph(text)
            }
        }
    }
}