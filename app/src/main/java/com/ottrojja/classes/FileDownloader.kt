package com.ottrojja.classes

import android.content.Context
import android.widget.Toast
import androidx.media3.common.util.Log
import com.ottrojja.R
import com.ottrojja.classes.Helpers.reportException
import com.ottrojja.classes.NetworkClient.ottrojjaClient
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.Request
import java.io.File
import java.io.FileOutputStream
import java.io.IOException

sealed class DownloadResult {
    data class Success(val file: File) : DownloadResult()
    data class Failure(val exception: Exception) : DownloadResult()
}

object FileDownloader {
    suspend fun download(
        context: Context,
        request: Request,
        destinationFile: File
    ): DownloadResult {

        Log.d("File Downloader","Downloading")

        return withContext(Dispatchers.IO) {
            val tempFile = File.createTempFile("temp_", ".mp3",
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
    }
}