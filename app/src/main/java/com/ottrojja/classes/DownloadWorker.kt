package com.ottrojja.classes

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import androidx.work.workDataOf
import com.ottrojja.classes.FileDownloadWorker.KEY_RELATIVE_PATH
import com.ottrojja.classes.FileDownloadWorker.KEY_STORAGE_BASE
import com.ottrojja.classes.FileDownloadWorker.KEY_URL
import com.ottrojja.classes.FileDownloadWorker.MAX_RETRIES
import com.ottrojja.classes.NetworkClient.ottrojjaClient
import okhttp3.Request
import okhttp3.internal.platform.PlatformRegistry.applicationContext
import java.io.File
import java.io.FileOutputStream


enum class StorageBase {
    FILES_DIR,
    EXTERNAL_FILES_DIR
}

//worker input contract
object FileDownloadWorker {

    const val KEY_URL = "key_url"
    const val KEY_RELATIVE_PATH = "key_relative_path"
    const val KEY_STORAGE_BASE = "key_storage_base"

    const val MAX_RETRIES = 3
}

private fun resolveFile(
    relativePath: String,
    storageBase: StorageBase
): File {

    val baseDir = when (storageBase) {

        StorageBase.FILES_DIR ->
            applicationContext?.filesDir

        StorageBase.EXTERNAL_FILES_DIR ->
            applicationContext?.getExternalFilesDir(null)
                ?: applicationContext?.filesDir
    }

    return File(baseDir, relativePath)
}

class DownloadWorker(
    context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params) {
    override suspend fun doWork(): Result {

        val url = inputData.getString(KEY_URL)
            ?: return Result.failure()

        val relativePath = inputData.getString(KEY_RELATIVE_PATH)
            ?: return Result.failure()

        val storageBase = inputData.getString(KEY_STORAGE_BASE)
            ?.let { StorageBase.valueOf(it) }
            ?: StorageBase.FILES_DIR

        val destinationFile = resolveFile(relativePath, storageBase)

        val tempFile = File.createTempFile(
            "temp_",
            ".tmp",
            applicationContext.cacheDir
        )

        return try {

            val request = Request.Builder()
                .url(url)
                .build()

            ottrojjaClient.newCall(request).execute().use { response ->

                if (!response.isSuccessful) return retryOrFail()

                val body = response.body ?: return retryOrFail()

                FileOutputStream(tempFile).use { output ->
                    body.byteStream().use { input ->
                        input.copyTo(output, 8 * 1024)
                    }
                }
            }

            if (!tempFile.exists() || tempFile.length() == 0L) {
                return retryOrFail()
            }

            // ensure parent dirs exist
            destinationFile.parentFile?.mkdirs()

            tempFile.copyTo(destinationFile, overwrite = true)

            Result.success(
                workDataOf(
                    "path" to destinationFile.absolutePath
                )
            )

        } catch (e: Exception) {
            retryOrFail()
        } finally {
            tempFile.delete()
        }
    }

    private fun retryOrFail(): Result {
        return if (runAttemptCount < MAX_RETRIES) {
            Result.retry()
        } else {
            Result.failure()
        }
    }
}

