package com.ottrojja.screens.CustomTasabeehListScreen

import android.app.Application
import android.widget.Toast
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.ottrojja.classes.ExpandableItem
import com.ottrojja.classes.QuranRepository
import com.ottrojja.room.entities.CustomTasbeeh
import com.ottrojja.room.entities.Khitmah
import com.ottrojja.room.entities.KhitmahMark
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


    fun fetchCustomTasabeehList(id: Int) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                repository.getTasabeehList(id).collect{
                    state->
                    _customTasabeeh.clear()
                    _customTasabeehList.value = state.tasabeehList;
                    state.customTasabeeh.forEach { tasbeeh ->
                        _customTasabeeh.add(tasbeeh)
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

    fun addCustomTasbeeh(text: String, count: Int) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                repository.insertCustomTasbeeh(CustomTasbeeh(text = text,
                    count = count,
                    listId = _customTasabeehList.value!!.id
                )
                )
                _addTasbeehDialog.value = false;
                withContext(Dispatchers.Main) {
                    Toast.makeText(context, "تمت الإضافة بنجاح", Toast.LENGTH_LONG).show()
                }
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
