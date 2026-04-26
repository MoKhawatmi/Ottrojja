package com.ottrojja.screens.loadingScreen.classes

import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import com.ottrojja.classes.DownloadState
import com.ottrojja.classes.FileDownloader
import com.ottrojja.classes.Helpers.checkNetworkConnectivity
import com.ottrojja.classes.Helpers.reportException
import com.ottrojja.classes.JsonParser
import com.ottrojja.classes.NetworkClient.ottrojjaClient
import com.ottrojja.classes.QuranRepository
import com.ottrojja.classes.StorageBase
import com.ottrojja.classes.downloadOnce
import com.ottrojja.room.entities.QuranPage
import okhttp3.Request
import java.io.File

class SyncQuranFile(
    private val context: Context,
    private val repository: QuranRepository,
    private val prefs: SharedPreferences
) {
    private val jsonParser = JsonParser(context)
    private val quranFile get() = File(context.filesDir, "quran.json")

    private val QURAN_FILE_URL =
        "https://ottrojja.fra1.cdn.digitaloceanspaces.com/quran.json"
    private val storedFileTimestamp
        get() = prefs.getLong("quranFileCreateTime", 0L)

    /**
     * Main entry point. The caller (ViewModel) owns the try/catch wrapper.
     * This function always completes — it never silently swallows all errors.
     */
    suspend fun sync() {
        if (!checkNetworkConnectivity(context)) {
            loadFromFilesDir()
            return
        }
        val remoteTimestamp = fetchRemoteTimestamp() ?: run {
            loadFromFilesDir()
            return
        }
        if (remoteTimestamp > storedFileTimestamp) {
            downloadAndLoad(remoteTimestamp)
        } else {
            loadFromFilesDir()
        }
    }

    // ── private steps ─────────────────────────────────────────────────────────

    /** Returns null on any network/parse failure. */
    private fun fetchRemoteTimestamp(): Long? {
        val request = Request.Builder().url(QURAN_FILE_URL).head().build()
        return try {
            val response = ottrojjaClient.newCall(request).execute()
            if (response.isSuccessful) {
                response.header("x-amz-meta-uploaded-at")?.toLongOrNull()
            } else {
                Log.w("QuranSync", "HEAD failed: ${response.code}")
                null
            }
        } catch (e: Exception) {
            reportException(exception = e, file = "QuranFileSyncUseCase")
            null
        }
    }

    private suspend fun downloadAndLoad(remoteTimestamp: Long) {
        when (val result = FileDownloader.downloadOnce(
            context = context,
            url = QURAN_FILE_URL,
            relativePath = quranFile.name,
            storageBase = StorageBase.FILES_DIR
        )) {
            is DownloadState.Success -> {
                prefs.edit().putLong("quranFileCreateTime", remoteTimestamp).apply()
                loadFromFile(result.file, isUpdate = true)
            }
            is DownloadState.Failure -> {
                reportException(exception = result.error, file = "QuranFileSyncUseCase",
                    details = "Download failure")
                loadFromFilesDir() // graceful fallback
            }
            DownloadState.Running -> { /* no-op: handled by downloadOnce internally */ }
        }
    }

    /**
     * Load from files dir if the file exists, otherwise fall back to assets.
     */
    private suspend fun loadFromFilesDir() {
        if (quranFile.exists()) {
            loadFromFile(quranFile, isUpdate = false)
        } else {
            loadFromAssets()
        }
    }

    private suspend fun loadFromFile(file: File, isUpdate: Boolean) {
        try {
            val pages = jsonParser.parseJsonArrayFileFromFilesDir(file.name)
            if (pages.isNullOrEmpty()) throw Exception("Empty or null quran file: ${file.name}")
            insertPages(pages, isUpdate)
        } catch (e: Exception) {
            Log.e("QuranSync", "Failed to load ${file.name}: ${e.message}")
            reportException(exception = e, file = "QuranFileSyncUseCase")
            file.delete() // corrupt file — remove it
            loadFromAssets()
        }
    }

    private suspend fun loadFromAssets() {
        try {
            val pages = jsonParser.parseJsonArrayFile<QuranPage>("quran.json")
            if (pages.isNullOrEmpty()) throw Exception("Asset quran.json is empty")
            insertPages(pages, isUpdate = false)
        } catch (e: Exception) {
            Log.e("QuranSync", "Asset fallback failed: ${e.message}")
            reportException(exception = e, file = "QuranFileSyncUseCase")
            // Nothing left to fall back to — let the exception propagate to ViewModel
            throw e
        }
    }

    private suspend fun insertPages(pages: List<QuranPage>, isUpdate: Boolean) {
        if (!isUpdate && repository.getPagesCount() == 604) return // already seeded
        repository.insertAllPages(pages)
    }
}