package com.ottrojja.services.page

import android.content.Context
import com.ottrojja.classes.DownloadState
import com.ottrojja.classes.FileDownloader
import com.ottrojja.classes.Helpers.reportException
import com.ottrojja.classes.StorageBase
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.sync.Semaphore
import kotlinx.coroutines.sync.withPermit
import java.util.concurrent.atomic.AtomicInteger

/**
 * Downloads missing verse files for the page player, a few at a time.
 * Stops everything on the first failure; cancelling the calling coroutine cancels the downloads.
 * A file that did not finish downloading is deleted, so a half-written file is never treated as
 * downloaded later.
 */
class VerseAudioDownloader(private val context: Context) {

    sealed interface Result {
        object Success : Result
        object NoSpace : Result
        object Failed : Result
    }

    private class DownloadFailed(val error: Throwable) : Exception(error)

    suspend fun download(files: List<String>, onProgress: (done: Int) -> Unit): Result {
        val done = AtomicInteger(0)
        val permits = Semaphore(PARALLEL_DOWNLOADS)
        return try {
            coroutineScope {
                files.map { file ->
                    async {
                        permits.withPermit { downloadOne(file) }
                        onProgress(done.incrementAndGet())
                    }
                }.awaitAll()
            }
            Result.Success
        } catch (e: DownloadFailed) {
            if (e.error.message?.contains("ENOSPC") == true) Result.NoSpace else Result.Failed
        }
    }

    private suspend fun downloadOne(fileName: String) {
        val localFile = PageAudio.localFile(context, fileName)
        var completed = false
        try {
            val state = FileDownloader.download(
                context = context,
                url = PageAudio.url(fileName),
                relativePath = fileName,
                storageBase = StorageBase.EXTERNAL_FILES_DIR
            ).first { it !is DownloadState.Running }

            if (state is DownloadState.Failure) {
                state.error.printStackTrace()
                reportException(
                    exception = state.error,
                    file = "VerseAudioDownloader",
                    details = "Cancellation or Failure to download verse file; link: /verses/$fileName"
                )
                throw DownloadFailed(state.error)
            }
            completed = true
        } finally {
            if (!completed) localFile.delete()
        }
    }

    private companion object {
        const val PARALLEL_DOWNLOADS = 3
    }
}
