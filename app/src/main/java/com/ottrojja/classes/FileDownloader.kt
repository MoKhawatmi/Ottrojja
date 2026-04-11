package com.ottrojja.classes

import android.content.Context
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkInfo
import androidx.work.WorkManager
import androidx.work.workDataOf
import com.ottrojja.classes.DownloadState.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import java.io.File

sealed class DownloadState {
    data object Running : DownloadState()
    data class Success(val file: File) : DownloadState()
    data class Failure(val error: Exception) : DownloadState()
}

object FileDownloader {
    fun download(
        context: Context,
        url: String,
        relativePath: String,
        storageBase: StorageBase = StorageBase.EXTERNAL_FILES_DIR
    ): Flow<DownloadState> {

        val request = OneTimeWorkRequestBuilder<DownloadWorker>()
            .setInputData(
                workDataOf(
                    FileDownloadWorker.KEY_URL to url,
                    FileDownloadWorker.KEY_RELATIVE_PATH to relativePath,
                    FileDownloadWorker.KEY_STORAGE_BASE to storageBase.name
                )
            )
            .build()

        val workManager = WorkManager.getInstance(context)

        workManager.enqueue(request)

        return workManager
            .getWorkInfoByIdFlow(request.id)
            .map { info ->

                val state: DownloadState = when (info?.state) {

                    WorkInfo.State.ENQUEUED,
                    WorkInfo.State.RUNNING -> {
                        DownloadState.Running
                    }

                    WorkInfo.State.SUCCEEDED -> {
                        val path = info.outputData.getString("path")

                        if (path == null) {
                            Failure(Exception("Missing output path"))
                        } else {
                            Success(File(path))
                        }
                    }

                    WorkInfo.State.FAILED -> {
                        Failure(Exception("Download failed"))
                    }

                    WorkInfo.State.CANCELLED -> {
                        Failure(Exception("Cancelled"))
                    }

                    WorkInfo.State.BLOCKED -> TODO()
                    null -> TODO()
                }

                state
            }

        /*suspend fun download(
            context: Context,
            request: Request,
            destinationFile: File
        ): DownloadResult {

            Log.d("File Downloader","Downloading...")
            Log.d("File Downloader","extension ${destinationFile.extension}")

            return withContext(Dispatchers.IO) {
                val tempFile = File.createTempFile("temp_", destinationFile.extension,
                    context.getExternalFilesDir(null)
                )

                try {
                    ottrojjaClient.newCall(request).execute().use { response ->
                        if (!response.isSuccessful) throw IOException("Unexpected code $response")
                        if (response.body == null) throw IOException("Empty response body")

                        response.body.let { responseBody ->
                            FileOutputStream(tempFile).use { outputStream ->
                                responseBody.byteStream().use { inputStream ->
                                    inputStream.copyTo(outputStream, bufferSize = 8 * 1024)
                                }
                            }
                        }

                        if (!tempFile.exists() || tempFile.length() == 0L) {
                            throw IOException("Temp file was not created or is empty")
                        }
                        tempFile.copyTo(destinationFile, overwrite = true)
                    }
                    Log.d("File Downloader","download success")
                    DownloadResult.Success(destinationFile)
                } catch (e: Exception) {
                    Log.d("File Downloader","error in download")
                    destinationFile.delete()
                    DownloadResult.Failure(e)
                } finally {
                    Log.d("File Downloader","end downloading process")
                    if (tempFile.exists()) {
                        tempFile.delete()
                    }
                }
            }
        }*/
    }
}

suspend fun FileDownloader.downloadOnce(
    context: Context,
    url: String,
    relativePath: String,
    storageBase: StorageBase
): DownloadState {

    val request = OneTimeWorkRequestBuilder<DownloadWorker>()
        .setInputData(
            workDataOf(
                FileDownloadWorker.KEY_URL to url,
                FileDownloadWorker.KEY_RELATIVE_PATH to relativePath,
                FileDownloadWorker.KEY_STORAGE_BASE to storageBase.name
            )
        )
        .build()

    val wm = WorkManager.getInstance(context)

    wm.enqueue(request)

    return wm.getWorkInfoByIdFlow(request.id)
        .first { it?.state?.isFinished?:false }
        .let { info ->

            when (info?.state) {

                WorkInfo.State.SUCCEEDED -> {
                    val path = info.outputData.getString("path")
                        ?: return DownloadState.Failure(Exception("Missing path"))

                    DownloadState.Success(File(path))
                }

                WorkInfo.State.FAILED ->
                    DownloadState.Failure(Exception("Download failed"))

                WorkInfo.State.CANCELLED ->
                    DownloadState.Failure(Exception("Cancelled"))

                else ->
                    DownloadState.Failure(Exception("Unexpected state"))
            }
        }
}