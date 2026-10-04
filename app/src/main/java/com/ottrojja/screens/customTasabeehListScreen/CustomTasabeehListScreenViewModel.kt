package com.ottrojja.screens.customTasabeehListScreen

import android.annotation.SuppressLint
import android.app.Application
import android.app.DownloadManager
import android.content.ContentValues
import android.content.Context
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import android.widget.Toast
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.google.common.reflect.TypeToken
import com.google.gson.Gson
import com.ottrojja.classes.Helpers.reportException
import com.ottrojja.classes.JsonParser
import com.ottrojja.classes.ModalFormMode
import com.ottrojja.room.repositories.QuranRepository
import com.ottrojja.classes.Tasabeeh
import com.ottrojja.composables.ToastManager.classes.AppToastManager
import com.ottrojja.composables.ToastManager.classes.AppToastType
import com.ottrojja.room.entities.CustomTasbeeh
import com.ottrojja.room.entities.TasabeehList
import com.ottrojja.screens.customTasabeehListScreen.classes.ImportedCustomTasbeeh
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.io.IOException
import kotlin.coroutines.cancellation.CancellationException
import kotlin.time.Duration.Companion.milliseconds

class CustomTasabeehListScreenViewModel(private val repository: QuranRepository,
                                        application: Application) : AndroidViewModel(application) {
    val context = application.applicationContext;

    // this refers to details (id and title etc) of the list that contains the tasabeeh
    private val _customTasabeehList = mutableStateOf<TasabeehList?>(null)
    var customTasabeehList: TasabeehList?
        get() = _customTasabeehList.value
        set(value) {
            _customTasabeehList.value = value;
        }

    // this refers to the actual tasabeeh contained by the list
    private val _customTasabeeh = mutableStateListOf<CustomTasbeeh>()
    val customTasabeeh: MutableList<CustomTasbeeh>
        get() = _customTasabeeh

    private val _addTasbeehDialog = mutableStateOf(false)
    var addTasbeehDialog: Boolean
        get() = _addTasbeehDialog.value
        set(value) {
            _addTasbeehDialog.value = value;
        }

    private val _showImportTasbeehDialog = mutableStateOf(false)
    var showImportTasbeehDialog: Boolean
        get() = _showImportTasbeehDialog.value
        set(value) {
            _showImportTasbeehDialog.value = value;
        }

    private val _exportListDialog = mutableStateOf(false)
    var exportListDialog: Boolean
        get() = _exportListDialog.value
        set(value) {
            _exportListDialog.value = value;
        }

    private val _exportFileTitle = mutableStateOf("")
    var exportFileTitle: String
        get() = _exportFileTitle.value
        set(value) {
            _exportFileTitle.value = value;
        }

    fun closeExportListDialog() {
        showImportTasbeehDialog = false;
        _exportFileTitle.value = "";
    }

    val baseCustomTasbeeh = CustomTasbeeh(text = "", count = 0, listId = 0, position = 0, currentCount = 0);
    private val _tasbeehInWork = mutableStateOf(baseCustomTasbeeh.copy())
    var tasbeehInWork: CustomTasbeeh
        get() = _tasbeehInWork.value
        set(value) {
            _tasbeehInWork.value = value;
        }

    fun fetchCustomTasabeehList(id: Int) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val state = repository.getTasabeehList(id)
                withContext(Dispatchers.Main) {
                    _customTasabeeh.clear()
                    if (state != null) {
                        _customTasabeehList.value = state.tasabeehList
                        // i'd like to use this line to thank the roomdb dev team for not adding order by functionality to @relation queries, which led us to this wonderful moment
                        _customTasabeeh.addAll(state.customTasabeeh.sortedBy { it.position })
                        state.customTasabeeh.forEach { itemCounts[it.id] = it.currentCount }
                        val validIds = state.customTasabeeh.map { it.id }.toSet()
                        itemCounts.keys.retainAll(validIds) // removed deleted or no longer existing ids from counts

                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
                reportException(exception = e, file = "CustomTasabeehListScreenViewModel")
                AppToastManager.show(
                    message = "حصل خطأ يرجى المحاولة لاحقا",
                    type = AppToastType.ERROR
                )
            }
        }
    }


    fun upsertCustomTasbeeh() {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val listId = _customTasabeehList.value!!.id
                if (customTasbeehModalMode == ModalFormMode.ADD) {
                    val nextPos = repository.getMaxPosition(listId) + 1
                    repository.insertCustomTasbeeh(
                        tasbeehInWork.copy(listId = listId,
                            position = nextPos
                        )
                    )
                } else {
                    repository.insertCustomTasbeeh(
                        tasbeehInWork.copy(listId = listId)
                    )
                }
                fetchCustomTasabeehList(listId)
                withContext(Dispatchers.Main) {
                    if (customTasbeehModalMode == ModalFormMode.ADD) {
                        AppToastManager.show(
                            message = "تمت الإضافة بنجاح",
                            type = AppToastType.SUCCESS
                        )
                    } else {
                        AppToastManager.show(
                            message = "تم التعديل بنجاح",
                            type = AppToastType.SUCCESS
                        )
                    }
                }
                closeCustomTasbeehModal()
            } catch (e: Exception) {
                e.printStackTrace()
                reportException(exception = e, file = "CustomTasabeehListScreenViewModel")
                AppToastManager.show(
                    message = "حصل خطأ يرجى المحاولة لاحقا",
                    type = AppToastType.ERROR
                )
            }
        }
    }

    fun deleteCustomTasabeehList() {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                _customTasabeehList.value?.let {
                    repository.deleteTasabeehList(it)
                    AppToastManager.show(
                        message = "تم الحذف بنجاح",
                        type = AppToastType.SUCCESS
                    )
                }
            } catch (e: Exception) {
                e.printStackTrace()
                reportException(exception = e, file = "CustomTasabeehListScreenViewModel")
                AppToastManager.show(
                    message = "حصل خطأ يرجى المحاولة لاحقا",
                    type = AppToastType.ERROR
                )
            }
        }
    }

    fun deleteCustomTasbeeh(item: CustomTasbeeh) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                repository.deleteCustomTasabeeh(item)
                fetchCustomTasabeehList(item.listId)
                AppToastManager.show(
                    message = "تم الحذف بنجاح",
                    type = AppToastType.SUCCESS
                )
            } catch (e: Exception) {
                e.printStackTrace()
                reportException(exception = e, file = "CustomTasabeehListScreenViewModel")
                AppToastManager.show(
                    message = "حصل خطأ يرجى المحاولة لاحقا",
                    type = AppToastType.ERROR
                )
            }
        }
    }

    private val countPersistJobs = mutableMapOf<Int, Job>()

    fun onTasbeehCountChanged(item: CustomTasbeeh, newCount: Int) {
        itemCounts[item.id] = newCount // instant UI update

        countPersistJobs[item.id]?.cancel()
        countPersistJobs[item.id] = viewModelScope.launch(Dispatchers.IO) {
            delay(400.milliseconds)
            try {
                repository.updateTasbeehCurrentCount(item.id, newCount)
            } catch (e: Exception) {
                e.printStackTrace()
                reportException(exception = e, file = "CustomTasabeehListScreenViewModel")
            }
        }
    }

    fun flushPendingCounts() {
        countPersistJobs.values.forEach { it.cancel() }
        countPersistJobs.clear()
        viewModelScope.launch(Dispatchers.IO) {
            try {
                itemCounts.forEach { (id, count) ->
                    repository.updateTasbeehCurrentCount(id, count)
                }
            } catch (e: Exception) {
                e.printStackTrace()
                reportException(exception = e, file = "CustomTasabeehListScreenViewModel")
            }
        }
    }


    private val _tasabeeh = mutableStateOf(listOf<Tasabeeh>())
    var tasabeeh: List<Tasabeeh>
        get() = _tasabeeh.value
        set(value) {
            _tasabeeh.value = value
        }

    private val _customTasbeehModalMode = mutableStateOf(ModalFormMode.ADD)
    var customTasbeehModalMode: ModalFormMode
        get() = _customTasbeehModalMode.value
        set(value) {
            _customTasbeehModalMode.value = value
        }

    init {
        try {
            JsonParser(context).parseJsonArrayFile<Tasabeeh>("tasabeeh.json")
                ?.let {
                    _tasabeeh.value = it
                }
        } catch (e: Exception) {
            e.printStackTrace()
            reportException(exception = e, file = "CustomTasabeehListScreenViewModel")
            AppToastManager.show(
                message = "حصل خطأ يرجى المحاولة لاحقا",
                type = AppToastType.ERROR
            )
        }
    }

    // to keep count of each tasbeeh counter by their id, useful for updating and checking
    var itemCounts = mutableStateMapOf<Int, Int>()

    fun closeCustomTasbeehModal() {
        _addTasbeehDialog.value = false;
        // reset form
        _tasbeehInWork.value = baseCustomTasbeeh.copy();
    }

    suspend fun updateUIListOnDrag(toIndex: Int, fromIndex: Int) {
        _customTasabeeh.apply {
            add(toIndex, removeAt(fromIndex))
        }
    }

    fun updateTasabeehListPositions() {
        _customTasabeeh.forEachIndexed { index, item ->
            if (item.position != index) {
                _customTasabeeh[index] = item.copy(position = index)
            }
        }
        viewModelScope.launch(Dispatchers.IO) {
            try {
                repository.massUpdateCustomTasabeeh(_customTasabeeh)
            } catch (e: Exception) {
                e.printStackTrace()
                reportException(exception = e, file = "CustomTasabeehListScreenViewModel")
                AppToastManager.show(
                    message = "حصل خطأ يرجى المحاولة لاحقا",
                    type = AppToastType.ERROR
                )
            }
        }
    }

    fun resetAllCounters() {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                countPersistJobs.values.forEach { it.cancel() }
                countPersistJobs.clear()

                _customTasabeeh.forEach {
                    repository.updateTasbeehCurrentCount(it.id, it.count)
                }
                _customTasabeehList.value?.let {
                    fetchCustomTasabeehList(it.id)
                }
            } catch (e: Exception) {
                e.printStackTrace()
                reportException(exception = e, file = "CustomTasabeehListScreenViewModel")
                AppToastManager.show(
                    message = "حصل خطأ يرجى المحاولة لاحقا",
                    type = AppToastType.ERROR
                )
            }
        }
    }
    fun exportListAsJson() {
        val gson = Gson()
        val jsonString = gson.toJson(_customTasabeeh)

        println(jsonString)

        val fileName = if (_exportFileTitle.value.endsWith(".json")) {
            _exportFileTitle.value
        } else {
            "${_exportFileTitle.value}.json"
        }

        saveJsonToDownloads(context, fileName, jsonString)
    }

    fun saveJsonToDownloads(context: Context, fileName: String, jsonString: String) {
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                saveJsonToDownloadsModern(context, fileName, jsonString)
            } else {
                saveJsonToDownloadsLegacy(context, fileName, jsonString)
            }

            AppToastManager.show(
                message = "تم التصدير بنجاح",
                type = AppToastType.SUCCESS
            )

            exportListDialog = false

        } catch (e: Exception) {
            e.printStackTrace()
            AppToastManager.show(
                message = "حصل خطأ يرجى المحاولة لاحقا",
                type = AppToastType.ERROR
            )
            reportException(exception = e, file = "CustomTasabeehListScreenViewModel")
        }
    }

    // Android 10+ (API 29+): MediaStore.Downloads is a live, indexed collection —
    // insert + write is enough, no manual scan needed.
    @SuppressLint("NewApi")
    private fun saveJsonToDownloadsModern(context: Context, fileName: String, jsonString: String) {
        val resolver = context.contentResolver

        val contentValues = ContentValues().apply {
            put(MediaStore.Downloads.DISPLAY_NAME, fileName)
            put(MediaStore.Downloads.MIME_TYPE, "application/json")
            put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_DOWNLOADS)
        }

        val uri = resolver.insert(MediaStore.Downloads.EXTERNAL_CONTENT_URI, contentValues)

        uri?.let {
            resolver.openOutputStream(it)?.use { outputStream ->
                outputStream.write(jsonString.toByteArray())
                outputStream.flush()
            }
        } ?: throw Exception("MediaStore insert returned null Uri")
    }

    // Android 7–9 (API 24–28): write the raw file directly, then register it with
    // DownloadManager — this is the provider the stock Downloads app actually reads from.
    // MediaStore.Files inserts / MediaScannerConnection do NOT populate that provider.
    private fun saveJsonToDownloadsLegacy(context: Context, fileName: String, jsonString: String) {
        val downloadsDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
        if (!downloadsDir.exists()) {
            downloadsDir.mkdirs()
        }

        val file = File(downloadsDir, fileName)
        file.writeText(jsonString)

        val downloadManager = context.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
        downloadManager.addCompletedDownload(
            fileName,
            "Ottrojja export",
            true,
            "application/json",
            file.absolutePath,
            file.length(),
            true
        )
    }

    /****************Handle Import*************************/
    fun readJsonFromUri(
        context: Context,
        uri: Uri
    ): String {
        val mime = context.contentResolver.getType(uri)
        if (mime != "application/json" && mime != "text/plain" && mime != "application/octet-stream") {
            throw IllegalArgumentException("Not a JSON file")
        }

        return context.contentResolver
            .openInputStream(uri)
            ?.bufferedReader()
            ?.use { it.readText() }
            ?: throw IOException("Unable to open input stream")
    }

    fun importJson(context: Context, uri: Uri) {
        val gson = Gson()
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val jsonString = readJsonFromUri(context, uri)
                val listType = object : TypeToken<List<ImportedCustomTasbeeh>>() {}.type
                val importedTasabeeh = gson.fromJson<List<ImportedCustomTasbeeh>>(jsonString, listType)
                val listId = _customTasabeehList.value!!.id

                repository.insertMultCustomTasbeeh(
                    importedTasabeeh.map { item ->
                        CustomTasbeeh(
                            listId = listId,
                            text = item.text,
                            count = item.count,
                            currentCount = item.currentCount ?: item.count,
                            position = 0
                        )
                    }
                )
                fetchCustomTasabeehList(listId)
            } catch (e: Exception) {
                e.printStackTrace()
                reportException(exception = e, file = "CustomTasabeehListScreenViewModel")
                AppToastManager.show(
                    message = "حصل خطأ يرجى المحاولة لاحقا",
                    type = AppToastType.ERROR
                )
            }
        }
    }
}

class CustomTasabeehListScreenViewModelFactory(
    private val repository: QuranRepository,
    private val application: Application
) : ViewModelProvider.AndroidViewModelFactory(application) {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(CustomTasabeehListScreenViewModel::class.java)) {
            return CustomTasabeehListScreenViewModel(repository, application) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
