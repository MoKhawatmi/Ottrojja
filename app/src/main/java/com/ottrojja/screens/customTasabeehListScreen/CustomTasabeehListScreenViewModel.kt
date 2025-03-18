package com.ottrojja.screens.customTasabeehListScreen

import android.app.Application
import android.widget.Toast
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.ottrojja.classes.JsonParser
import com.ottrojja.classes.ModalFormMode
import com.ottrojja.classes.QuranRepository
import com.ottrojja.classes.Tasabeeh
import com.ottrojja.room.entities.CustomTasbeeh
import com.ottrojja.room.entities.TasabeehList
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class CustomTasabeehListScreenViewModel(private val repository: QuranRepository,
                                        application: Application) : AndroidViewModel(application) {
    val context = application.applicationContext;

    private val _customTasabeehList = mutableStateOf<TasabeehList?>(null)
    var customTasabeehList: TasabeehList?
        get() = _customTasabeehList.value
        set(value) {
            _customTasabeehList.value = value;
        }

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

    private val _tasbeehInWork = mutableStateOf(CustomTasbeeh(text = "", count = 0, listId = 0))
    var tasbeehInWork: CustomTasbeeh
        get() = _tasbeehInWork.value
        set(value) {
            _tasbeehInWork.value = value;
        }

    fun fetchCustomTasabeehList(id: Int) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                repository.getTasabeehList(id).collect { state ->
                    _customTasabeeh.clear()
                    if (state != null) {
                        _customTasabeehList.value = state.tasabeehList;
                        state.customTasabeeh.forEach { tasbeeh ->
                            _customTasabeeh.add(tasbeeh)
                        }
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
                withContext(Dispatchers.Main) {
                    Toast.makeText(context, "حصل خطأ يرجى المحاولة لاحقا", Toast.LENGTH_LONG).show()
                }
            }
        }
    }

    fun upsertCustomTasbeeh() {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                repository.insertCustomTasbeeh(
                    tasbeehInWork.copy(listId = _customTasabeehList.value!!.id)
                )
                withContext(Dispatchers.Main) {
                    if (customTasbeehModalMode == ModalFormMode.ADD) {
                        Toast.makeText(context, "تمت الإضافة بنجاح", Toast.LENGTH_LONG).show()
                    } else {
                        Toast.makeText(context, "تم التعديل بنجاح", Toast.LENGTH_LONG).show()
                        itemCounts.put(_tasbeehInWork.value.id, _tasbeehInWork.value.count)
                    }
                }
                _addTasbeehDialog.value = false;
                // reset form
                _tasbeehInWork.value = CustomTasbeeh(text = "", count = 0, listId = 0);
            } catch (e: Exception) {
                e.printStackTrace()
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
            Toast.makeText(context, "حصل خطأ، يرجى المحاولة مرة اخرى", Toast.LENGTH_LONG).show()
        }
    }

    // to keep count of each tasbeeh counter by their id, useful for updating and checking
    var itemCounts = mutableStateMapOf<Int, Int>().apply {
        customTasabeeh.forEach { put(it.id, it.count) }
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
