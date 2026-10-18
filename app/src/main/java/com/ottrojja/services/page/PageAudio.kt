package com.ottrojja.services.page

import android.content.Context
import android.net.Uri
import com.ottrojja.room.entities.PageContent
import java.io.File

/** File names, CDN urls and media ids of the page player. */
object PageAudio {
    private const val CDN = "https://ottrojja.fra1.cdn.digitaloceanspaces.com/verses"

    /** The page player uses Al-Fatiha's first verse as the basmalah (same as before). */
    const val BASMALAH_FILE = "1-1-1.mp3"

    private const val VERSE_PREFIX = "verse"
    private const val BASMALAH_PREFIX = "basmalah"

    fun verseFile(verse: PageContent) = "${verse.pageNum}-${verse.surahNum}-${verse.verseNum}.mp3"
    fun url(fileName: String) = "$CDN/$fileName"

    fun localFile(context: Context, fileName: String) = File(context.getExternalFilesDir(null), fileName)
    fun localUri(context: Context, fileName: String): Uri = Uri.fromFile(localFile(context, fileName))

    /** Where an item sits in the mushaf. [verse] is null for a basmalah item. */
    data class Position(val page: Int, val surah: Int, val verse: Int?)

    fun verseMediaId(page: Int, surah: Int, verse: Int) = "$VERSE_PREFIX:$page:$surah:$verse"
    fun basmalahMediaId(page: Int, surah: Int) = "$BASMALAH_PREFIX:$page:$surah"
    fun isBasmalah(mediaId: String?) = mediaId?.startsWith("$BASMALAH_PREFIX:") == true

    fun parse(mediaId: String?): Position? {
        val parts = mediaId?.split(":") ?: return null
        val page = parts.getOrNull(1)?.toIntOrNull() ?: return null
        val surah = parts.getOrNull(2)?.toIntOrNull() ?: return null
        return when (parts[0]) {
            VERSE_PREFIX -> Position(page, surah, parts.getOrNull(3)?.toIntOrNull() ?: return null)
            BASMALAH_PREFIX -> Position(page, surah, null)
            else -> null
        }
    }
}
