package com.ottrojja.screens.tasbeehScreen

import com.ottrojja.classes.JsonParser
import android.app.Application
import android.content.Context
import android.content.SharedPreferences
import android.widget.Toast
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.ottrojja.classes.ExpandableItem
import com.ottrojja.classes.QuranRepository
import com.ottrojja.classes.Tasabeeh
import com.ottrojja.room.entities.TasabeehList
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class TasbeehScreenViewModel(private val repository: QuranRepository, application: Application) :
    AndroidViewModel(application) {
    val context = application.applicationContext;
    val sharedPreferences: SharedPreferences =
        application.getSharedPreferences("ottrojja", Context.MODE_PRIVATE)

    private val _tasbeehCount = mutableStateOf(0)
    var tasbeehCount: Int
        get() = _tasbeehCount.value
        set(value) {
            _tasbeehCount.value = value
        }


    private val _selectedTab = mutableStateOf(TasbeehTab.المسبحة)
    var selectedTab: TasbeehTab
        get() = _selectedTab.value
        set(value) {
            _selectedTab.value = value
        }


    private val _tasabeeh = mutableStateListOf<ExpandableItem<Tasabeeh>>()
    val tasabeeh: MutableList<ExpandableItem<Tasabeeh>>
        get() = _tasabeeh


    init {
        _tasbeehCount.value = sharedPreferences.getInt("tasbeehCount", 0)
        try {
            JsonParser(context).parseJsonArrayFile<Tasabeeh>("tasabeeh.json")
                ?.let {
                    it.forEach { item ->
                        _tasabeeh.add(ExpandableItem(data = item, expanded = false))
                    }
                }
        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(context, "حصل خطأ، يرجى المحاولة مرة اخرى", Toast.LENGTH_LONG).show()
        }
    }

    fun increaseTasbeeh() {
        _tasbeehCount.value++;
        val editor: SharedPreferences.Editor = sharedPreferences.edit()
        editor.putInt("tasbeehCount", _tasbeehCount.value)
        editor.apply()
    }

    fun resetTasbeeh() {
        _tasbeehCount.value = 0
        val editor: SharedPreferences.Editor = sharedPreferences.edit()
        editor.putInt("tasbeehCount", _tasbeehCount.value)
        editor.apply()
    }

    fun updateExpanded(item: ExpandableItem<Tasabeeh>) {
        val indexOfItem = _tasabeeh.indexOfFirst { it.data.ziker == item.data.ziker };
        _tasabeeh.set(indexOfItem, item.copy(expanded = !item.expanded))
    }

    private val _tasabeehLists = mutableStateOf(emptyList<TasabeehList>())
    var tasabeehLists: List<TasabeehList>
        get() = _tasabeehLists.value
        set(value) {
            _tasabeehLists.value = value;
        }


    private val _showAddListDialog = mutableStateOf(false)
    var showAddListDialog: Boolean
        get() = _showAddListDialog.value
        set(value) {
            _showAddListDialog.value = value;
        }

    fun fetchTasabeehLists() {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                repository.getTasabeehLists().collect { state ->
                    _tasabeehLists.value = state;
                }
            } catch (e: Exception) {
                e.printStackTrace()
                withContext(Dispatchers.Main){
                    Toast.makeText(context, "حصل خطأ يرجى المحاولة لاحقا", Toast.LENGTH_LONG).show()
                }
            }
        }
    }

    fun createTasabeehList(title: String) {
        try {
            viewModelScope.launch(Dispatchers.IO) {
                repository.insertTasabeehList(TasabeehList(title = title));
                withContext(Dispatchers.Main) {
                    Toast.makeText(context, "تمت الإضافة بنجاح", Toast.LENGTH_LONG).show()
                }
                _showAddListDialog.value = false;
            }
        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(context, "حصل خطأ يرجى المحاولة لاحقا", Toast.LENGTH_LONG).show()
        }
    }

}

enum class TasbeehTab {
    المسبحة, الاذكار, القوائم
}

class TasbeehViewModelFactory(
    private val repository: QuranRepository,
    private val application: Application
) : ViewModelProvider.AndroidViewModelFactory(application) {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(TasbeehScreenViewModel::class.java)) {
            return TasbeehScreenViewModel(repository, application) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
