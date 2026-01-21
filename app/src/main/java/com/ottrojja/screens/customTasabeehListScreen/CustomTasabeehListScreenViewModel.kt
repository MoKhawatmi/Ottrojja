package com.ottrojja.screens.customTasabeehListScreen

import android.Manifest
import android.app.Application
import android.content.ContentValues
import android.content.Context
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import android.widget.Toast
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.google.common.reflect.TypeToken
import com.google.gson.Gson
import com.ottrojja.classes.Helpers.reportException
import com.ottrojja.classes.JsonParser
import com.ottrojja.classes.ModalFormMode
import com.ottrojja.classes.QuranRepository
import com.ottrojja.classes.Tasabeeh
import com.ottrojja.room.entities.CustomTasbeeh
import com.ottrojja.room.entities.TasabeehList
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.io.IOException
import kotlin.coroutines.cancellation.CancellationException

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


    private val _tasbeehInWork = mutableStateOf(
        CustomTasbeeh(text = "", count = 0, listId = 0, position = 0)
    )
    var tasbeehInWork: CustomTasbeeh
        get() = _tasbeehInWork.value
        set(value) {
            _tasbeehInWork.value = value;
        }

    fun fetchCustomTasabeehList(id: Int) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                repository.getTasabeehList(id).collect { state ->
                    withContext(Dispatchers.Main) {
                        _customTasabeeh.clear()
                        if (state != null) {
                            _customTasabeehList.value = state.tasabeehList;
                            // i'd like to use this line to thank the roomdb dev team for not adding order by functionality to @relation queries, which led us to this wonderful moment
                            _customTasabeeh.addAll(state.customTasabeeh.sortedBy { it.position })
                        }
                    }
                }
            } catch (e: CancellationException) {
                //nothing
            } catch (e: Exception) {
                e.printStackTrace()
                reportException(exception = e, file = "CustomTasabeehListScreenViewModel")
                withContext(Dispatchers.Main) {
                    Toast.makeText(context, "حصل خطأ يرجى المحاولة لاحقا", Toast.LENGTH_LONG).show()
                }
            }
        }
    }

    fun upsertCustomTasbeeh() {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                if (customTasbeehModalMode == ModalFormMode.ADD) {
                    val nextPos = repository.getMaxPosition(_customTasabeehList.value!!.id) + 1
                    repository.insertCustomTasbeeh(
                        tasbeehInWork.copy(listId = _customTasabeehList.value!!.id,
                            position = nextPos
                        )
                    )
                } else {
                    repository.insertCustomTasbeeh(
                        tasbeehInWork.copy(listId = _customTasabeehList.value!!.id)
                    )
                }
                withContext(Dispatchers.Main) {
                    if (customTasbeehModalMode == ModalFormMode.ADD) {
                        Toast.makeText(context, "تمت الإضافة بنجاح", Toast.LENGTH_LONG).show()
                    } else {
                        itemCounts.put(_tasbeehInWork.value.id, _tasbeehInWork.value.count)
                        Toast.makeText(context, "تم التعديل بنجاح", Toast.LENGTH_LONG).show()
                    }
                }
                closeCustomTasbeehModal()
            } catch (e: Exception) {
                e.printStackTrace()
                reportException(exception = e, file = "CustomTasabeehListScreenViewModel")
                withContext(Dispatchers.Main) {
                    Toast.makeText(context, "حصل خطأ يرجى المحاولة لاحقا", Toast.LENGTH_LONG).show()
                }
            }
        }
    }

    fun deleteCustomTasabeehList() {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                _customTasabeehList.value?.let {
                    repository.deleteTasabeehList(it)
                    withContext(Dispatchers.Main) {
                        Toast.makeText(context, "تم الحذف بنجاح", Toast.LENGTH_LONG).show()
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
                reportException(exception = e, file = "CustomTasabeehListScreenViewModel")
                withContext(Dispatchers.Main) {
                    Toast.makeText(context, "حصل خطأ يرجى المحاولة لاحقا", Toast.LENGTH_LONG).show()
                }
            }
        }
    }

    fun deleteCustomTasbeeh(item: CustomTasbeeh) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                repository.deleteCustomTasabeeh(item)
                withContext(Dispatchers.Main) {
                    Toast.makeText(context, "تم الحذف بنجاح", Toast.LENGTH_LONG).show()
                }
            } catch (e: Exception) {
                e.printStackTrace()
                reportException(exception = e, file = "CustomTasabeehListScreenViewModel")
                withContext(Dispatchers.Main) {
                    Toast.makeText(context, "حصل خطأ يرجى المحاولة لاحقا", Toast.LENGTH_LONG).show()
                }
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
            Toast.makeText(context, "حصل خطأ، يرجى المحاولة مرة اخرى", Toast.LENGTH_LONG).show()
        }
    }

    // to keep count of each tasbeeh counter by their id, useful for updating and checking
    var itemCounts = mutableStateMapOf<Int, Int>().apply {
        customTasabeeh.forEach { put(it.id, it.count) }
    }

    fun closeCustomTasbeehModal() {
        _addTasbeehDialog.value = false;
        // reset form
        _tasbeehInWork.value = CustomTasbeeh(text = "", count = 0, listId = 0, position = 0);
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
                Toast.makeText(context, "حصل خطأ، يرجى المحاولة مرة اخرى", Toast.LENGTH_LONG).show()
            }
        }
    }

    fun exportListAsJson() {
        val gson = com.google.gson.Gson()
        val jsonString = gson.toJson(_customTasabeeh)

        println(jsonString)
        saveJsonToDownloads(context, _exportFileTitle.value, jsonString)
    }

    fun saveJsonToDownloads(context: Context, fileName: String, jsonString: String) {
        try {
            val resolver = context.contentResolver

            val contentValues = ContentValues().apply {
                put(MediaStore.Downloads.DISPLAY_NAME, fileName)
                put(MediaStore.Downloads.MIME_TYPE, "application/json")
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    // Android 10+
                    put(
                        MediaStore.MediaColumns.RELATIVE_PATH,
                        Environment.DIRECTORY_DOWNLOADS
                    )
                } else {
                    // Android 7–9 (REQUIRED)
                    val downloadsDir =
                        Environment.getExternalStoragePublicDirectory(
                            Environment.DIRECTORY_DOWNLOADS
                        )

                    if (!downloadsDir.exists()) {
                        downloadsDir.mkdirs()
                    }

                    put(
                        MediaStore.MediaColumns.DATA,
                        File(downloadsDir, fileName).absolutePath
                    )
                }

            }


            val contentUri: Uri =
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    MediaStore.Downloads.EXTERNAL_CONTENT_URI
                } else {
                    MediaStore.Files.getContentUri("external")
                }

            val uri = resolver.insert(contentUri, contentValues)


            uri?.let {
                resolver.openOutputStream(it)?.use { outputStream ->
                    outputStream.write(jsonString.toByteArray())
                    outputStream.flush()
                }
            }

            Toast.makeText(context, "تم التصدير بنجاح", Toast.LENGTH_SHORT).show()
            exportListDialog = false;

        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(context, "حصل خطأ يرجى المحاولة لاحقا", Toast.LENGTH_SHORT).show()
            reportException(exception = e, file = "CustomTasabeehListScreenViewModel")
        }
    }

    /****************Handle Import*************************/
    fun readJsonFromUri(
        context: Context,
        uri: Uri
    ): String {
        val mime = context.contentResolver.getType(uri)
        if (mime != "application/json" && mime != "text/plain" && mime!="application/octet-stream") {
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

                val listType = object : TypeToken<List<CustomTasbeeh>>() {}.type
                val importedTasabeeh = gson.fromJson<List<CustomTasbeeh>>(jsonString, listType)


                repository.insertMultCustomTasbeeh(importedTasabeeh.map { item -> CustomTasbeeh(listId = _customTasabeehList.value!!.id, text = item.text, count = item.count, position = 0) })
                withContext(Dispatchers.Main){
                    updateTasabeehListPositions()
                }
            } catch (e: Exception) {
                e.printStackTrace()
                reportException(exception = e, file = "CustomTasabeehListScreenViewModel")
                Toast.makeText(context, "حصل خطأ يرجى المحاولة لاحقا", Toast.LENGTH_SHORT).show()
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
