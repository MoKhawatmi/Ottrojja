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
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.ottrojja.classes.DataStore.DataStoreRepository
import com.ottrojja.classes.Helpers.reportException
import com.ottrojja.classes.Helpers.validateReminders
import com.ottrojja.composables.ToastManager.classes.AppToastManager
import com.ottrojja.composables.ToastManager.classes.AppToastType
import com.ottrojja.room.entities.CustomTasbeeh
import com.ottrojja.room.repositories.QuranRepository
import com.ottrojja.screens.loadingScreen.classes.SyncLocalData
import com.ottrojja.screens.loadingScreen.classes.SyncQuranFile
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class LoadingScreenViewModel(
    private val repository: QuranRepository,
    application: Application
) : AndroidViewModel(application) {

    private val context = application.applicationContext
    private val prefs: SharedPreferences =
        application.getSharedPreferences("ottrojja", Context.MODE_PRIVATE)

    enum class LoadState { Loading, Loaded, Failed }

    private val _state = mutableStateOf(LoadState.Loading)
    val state: LoadState get() = _state.value

    private val localDataSync = SyncLocalData(context, repository, prefs)
    private val quranFileSync = SyncQuranFile(context, repository, prefs)

    private var initJob: Job? = null

    init {
        start()
    }

    /** Re-runs initialization (used by the retry button). No-op while a run is active. */
    fun retry() {
        if (initJob?.isActive == true) return
        _state.value = LoadState.Loading
        start()
    }

    /** Lets the user enter the app after a failed sync (data may be incomplete). */
    fun continueAnyway() {
        _state.value = LoadState.Loaded
    }

    private fun start() {
        initJob = viewModelScope.launch(Dispatchers.IO) {
            val success = runInitialization()
            // Reminder validation is independent of data sync; it must never crash the scope.
            try {
                validateReminders(context)
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                reportException(exception = e, file = "LoadingScreenViewModel")
            }
            _state.value = if (success) LoadState.Loaded else LoadState.Failed
        }
    }

    suspend fun seedDefaultTasabeehIfNeeded(context: Context) {
        if (DataStoreRepository.tasabeehSeededFlow.first()) {
            return
        }

        val gson = Gson()

        val files = context.assets
            .list("default_tasabeeh")
            ?: return

        for (fileName in files) {
            if (!fileName.endsWith(".json")) continue

            val title = fileName.removeSuffix(".json")

            val json = context.assets
                .open("default_tasabeeh/$fileName")
                .bufferedReader()
                .use { it.readText() }

            val type = object : TypeToken<List<CustomTasbeeh>>() {}.type

            val items = gson.fromJson<List<CustomTasbeeh>>(json, type)

            repository.insertListWithTasabeeh(
                title = title,
                tasabeeh = items
            )
        }

        DataStoreRepository.tasabeehSeededTrue()
    }

    /** @return true if every step succeeded. Cancellation propagates instead of being swallowed. */
    private suspend fun runInitialization(): Boolean {
        return try {
            deleteTempFiles()
            localDataSync.sync()      // all assets except pagesContent
            quranFileSync.sync()      // quran pages inserted into DB here
            localDataSync.syncPagesContent()  // safe now — FK dependency satisfied
            seedDefaultTasabeehIfNeeded(context)
            localDataSync.failures.isEmpty()
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            e.printStackTrace()
            AppToastManager.show("حصل خطأ اثناء تحميل بعض البيانات", type = AppToastType.ERROR)
            reportException(exception = e, file = "LoadingScreenViewModel")
            false
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

