package com.ottrojja.services.quran

import android.content.Context
import android.net.Uri
import androidx.core.net.toUri
import com.ottrojja.room.entities.PageContent
import java.io.File

/**
 * Single place for audio file names, CDN URLs and media ids.
 * Local file names are the same as the last segment of the remote URL.
 */
object QuranAudio {
    private const val CDN = "https://ottrojja.fra1.cdn.digitaloceanspaces.com"
    private const val SURAH_PREFIX = "surah:"
    private const val VERSE_PREFIX = "verse:"

    const val BASMALAH_FILE = "basmalah.mp3"

    fun chapterFile(surahId: Int) = "$surahId.mp3"
    fun chapterUrl(surahId: Int) = "$CDN/chapters/${chapterFile(surahId)}"

    fun verseFile(verse: PageContent) = "${verse.pageNum}-${verse.surahNum}-${verse.verseNum}.mp3"
    fun verseUrl(fileName: String) = "$CDN/verses/$fileName"

    fun localFile(context: Context, fileName: String) =
        File(context.getExternalFilesDir(null), fileName)

    /** Downloaded copy if it exists, CDN otherwise. */
    fun resolveUri(context: Context, fileName: String, remoteUrl: String): Uri {
        val file = localFile(context, fileName)
        return if (file.exists()) Uri.fromFile(file) else remoteUrl.toUri()
    }

    fun surahMediaId(surahId: Int) = "$SURAH_PREFIX$surahId"
    fun surahIdFromMediaId(mediaId: String?): Int? =
        mediaId?.takeIf { it.startsWith(SURAH_PREFIX) }?.removePrefix(SURAH_PREFIX)?.toIntOrNull()

    fun verseMediaId(fileName: String) = "$VERSE_PREFIX$fileName"
    fun isBasmalah(mediaId: String?) = mediaId == verseMediaId(BASMALAH_FILE)
}
