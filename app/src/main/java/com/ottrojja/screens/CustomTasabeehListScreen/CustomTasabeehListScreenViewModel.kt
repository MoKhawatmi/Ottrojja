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

class CustomTasabeehListScreenViewModel(private val repository: QuranRepository, application: Application) : AndroidViewModel(application) {
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

    fun fetchCustomTasabeehList(id: Int) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                _customTasabeeh.clear()
                val fullTasabeehList = repository.getTasabeehList(id)
                _customTasabeehList.value = fullTasabeehList.tasabeehList;
                fullTasabeehList.customTasabeeh.forEach { tasbeeh ->
                    _customTasabeeh.add(tasbeeh)
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
