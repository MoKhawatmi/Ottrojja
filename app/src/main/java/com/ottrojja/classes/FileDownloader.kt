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