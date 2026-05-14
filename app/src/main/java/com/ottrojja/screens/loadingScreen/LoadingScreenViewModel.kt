package com.ottrojja.screens.loadingScreen

import android.app.Application
import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.ottrojja.classes.Helpers.reportException
import com.ottrojja.classes.Helpers.validateReminders
import com.ottrojja.classes.QuranRepository
import com.ottrojja.screens.loadingScreen.classes.SyncLocalData
import com.ottrojja.screens.loadingScreen.classes.SyncQuranFile
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

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

