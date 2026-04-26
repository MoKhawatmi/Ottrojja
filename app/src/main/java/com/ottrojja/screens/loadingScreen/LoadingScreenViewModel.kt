package com.ottrojja.screens.loadingScreen

import com.ottrojja.classes.JsonParser
import android.app.Application
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.provider.Settings
import android.util.Log
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import com.ottrojja.classes.DataStore.DataStoreRepository
import com.ottrojja.classes.DownloadState
import com.ottrojja.classes.FileDownloader
import com.ottrojja.room.entities.CauseOfRevelation
import com.ottrojja.classes.Helpers
import com.ottrojja.classes.Helpers.isServiceRunning
import com.ottrojja.classes.Helpers.reportException
import com.ottrojja.classes.Helpers.validateReminders
import com.ottrojja.classes.NetworkClient.ottrojjaClient
import com.ottrojja.room.entities.QuranPage
import com.ottrojja.classes.QuranRepository
import com.ottrojja.classes.StorageBase
import com.ottrojja.classes.downloadOnce
import com.ottrojja.room.entities.Azkar
import com.ottrojja.screens.mainScreen.ChapterData
import com.ottrojja.screens.mainScreen.PartData
import com.ottrojja.room.entities.E3rabData
import com.ottrojja.room.entities.PageContent
import com.ottrojja.room.entities.Quarter
import com.ottrojja.room.entities.TafseerData
import com.ottrojja.room.entities.VerseMeanings
import com.ottrojja.screens.loadingScreen.classes.SyncLocalData
import com.ottrojja.screens.loadingScreen.classes.SyncQuranFile
import com.ottrojja.screens.reminderScreen.classes.RescheduleRemindersWorker
import com.ottrojja.services.OverlayService
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.File

class LoadingScreenViewModel(
    private val repository: QuranRepository,
    application: Application
) : AndroidViewModel(application) {

    private val context = application.applicationContext
    private val prefs: SharedPreferences =
        application.getSharedPreferences("ottrojja", Context.MODE_PRIVATE)

    private val _loaded = mutableStateOf(false)
    val loaded: Boolean get() = _loaded.value

    private val localDataSync = SyncLocalData(context, repository, prefs)
    private val quranFileSync = SyncQuranFile(context, repository, prefs)

    init {
        viewModelScope.launch(Dispatchers.IO) {
            runInitialization()
            validateReminders(context)
        }
    }

    private suspend fun runInitialization() {
        try {
            deleteTempFiles()
            localDataSync.sync()      // all assets except pagesContent
            quranFileSync.sync()      // quran pages inserted into DB here
            localDataSync.syncPagesContent()  // safe now — FK dependency satisfied
        } catch (e: Exception) {
            e.printStackTrace()
            reportException(exception = e, file = "LoadingScreenViewModel")
        } finally {
            withContext(Dispatchers.Main) { _loaded.value = true }
        }
    }

    private fun deleteTempFiles() {
        context.getExternalFilesDir(null)
            ?.listFiles { file -> file.name.startsWith("temp_") }
            ?.forEach { it.delete().also { _ -> Log.d("Init", "Deleted ${it.name}") } }
    }
}

class LoadingScreenViewModelFactory(
    private val repository: QuranRepository,
    private val application: Application
) : ViewModelProvider.AndroidViewModelFactory(application) {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(LoadingScreenViewModel::class.java)) {
            return LoadingScreenViewModel(repository, application) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}

