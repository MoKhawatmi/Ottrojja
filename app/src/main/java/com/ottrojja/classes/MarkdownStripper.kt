package com.ottrojja.classes

// Converts a markdown string to clean plain text (used when copying to the clipboard).

private val FENCE = Regex("""^\s{0,3}(```|~~~)""")
private val HEADING = Regex("""^\s{0,3}#{1,6}\s+(.*?)(\s+#+)?\s*$""")
private val QUOTE = Regex("""^(\s{0,3}>\s?)+""")
private val ALERT = Regex("""^\[!(NOTE|TIP|IMPORTANT|WARNING|CAUTION)]$""", RegexOption.IGNORE_CASE)
private val HR = Regex("""^\s{0,3}([-*_])(\s*\1){2,}\s*$""")
private val TABLE_SEPARATOR = Regex("""^\s*\|?\s*:?-{3,}:?\s*(\|\s*:?-{3,}:?\s*)*\|?\s*$""")
private val BULLET = Regex("""^(\s*)[-*+]\s+(?:\[([ xX])]\s+)?""")

private val BR = Regex("""<br\s*/?>""", RegexOption.IGNORE_CASE)
private val IMAGE = Regex("""!\[([^\]]*)]\([^)]*\)""")
private val LINK = Regex("""\[([^\]]+)]\(\s*<?([^)\s>]+)>?(?:\s+"[^"]*")?\s*\)""")
private val AUTOLINK = Regex("""<(https?://[^>\s]+)>""")
private val INLINE_CODE = Regex("""`([^`]+)`""")
private val BOLD_ITALIC = Regex("""\*\*\*(?=\S)(.+?)(?<=\S)\*\*\*""")
private val BOLD_STAR = Regex("""\*\*(?=\S)(.+?)(?<=\S)\*\*""")
private val BOLD_UNDERSCORE = Regex("""(?<![\p{L}\p{N}_])__(?=\S)(.+?)(?<=\S)__(?![\p{L}\p{N}_])""")
private val ITALIC_STAR = Regex("""\*(?=\S)(.+?)(?<=\S)\*""")
private val ITALIC_UNDERSCORE = Regex("""(?<![\p{L}\p{N}_])_(?=\S)(.+?)(?<=\S)_(?![\p{L}\p{N}_])""")
private val STRIKE = Regex("""~~(?=\S)(.+?)(?<=\S)~~""")
private val UNESCAPE = Regex("""\\([\\`*_{}\[\]()#+\-.!>~|<])""")
private val EXTRA_BLANK_LINES = Regex("""\n{3,}""")

fun String.stripMarkdown(): String {
    var inFence = false
    val out = ArrayList<String>()

    for (raw in replace("\r\n", "\n").lines()) {
        // Code fences: drop the fence markers, keep the code as-is.
        if (FENCE.containsMatchIn(raw)) {
            inFence = !inFence
            continue
        }
        if (inFence) {
            out += raw
            continue
        }

        var line = QUOTE.replaceFirst(raw, "")

        if (ALERT.matches(line.trim())) continue
        if (HR.matches(line) || TABLE_SEPARATOR.matches(line)) continue

        HEADING.matchEntire(line)?.let { line = it.groupValues[1] }

        if (line.trimStart().startsWith("|")) {
            line = line.trim().trim('|').trim()
        }

        line = BULLET.replace(line) { m ->
            val indent = m.groupValues[1]
            when (m.groupValues[2]) {
                "" -> "$indent• "
                " " -> "$indent☐ "
                else -> "$indent☑ "
            }
        }

        out += stripInline(line).trimEnd()
    }

    return out.joinToString("\n")
        .replace(EXTRA_BLANK_LINES, "\n\n")
        .trim()
}

private fun stripInline(text: String): String {
    var s = text
    s = BR.replace(s, "\n")
    s = IMAGE.replace(s) { it.groupValues[1] }
    s = LINK.replace(s) { m ->
        val label = m.groupValues[1]
        val url = m.groupValues[2]
        if (label == url) url else "$label ($url)"
    }
    s = AUTOLINK.replace(s, "$1")
    s = INLINE_CODE.replace(s, "$1")
    s = BOLD_ITALIC.replace(s, "$1")
    s = BOLD_STAR.replace(s, "$1")
    s = BOLD_UNDERSCORE.replace(s, "$1")
    s = ITALIC_STAR.replace(s, "$1")
    s = ITALIC_UNDERSCORE.replace(s, "$1")
    s = STRIKE.replace(s, "$1")
    s = UNESCAPE.replace(s, "$1")
    return s
}

