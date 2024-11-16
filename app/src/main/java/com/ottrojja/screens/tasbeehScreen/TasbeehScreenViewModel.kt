package com.ottrojja.screens.tasbeehScreen

import com.ottrojja.classes.JsonParser
import android.app.Application
import android.content.Context
import android.content.SharedPreferences
import android.widget.Toast
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.AndroidViewModel
import com.ottrojja.classes.Tasabeeh

class TasbeehScreenViewModel(application: Application) : AndroidViewModel(application) {
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

    private val _tasabeeh = mutableStateOf(mutableListOf<Tasabeeh>())
    var tasabeeh: MutableList<Tasabeeh>
        get() = _tasabeeh.value
        set(value) {
            _tasabeeh.value = value
        }


    init {
        _tasbeehCount.value = sharedPreferences.getInt("tasbeehCount", 0)
        try {
            JsonParser(context).parseJsonArrayFile<Tasabeeh>("tasabeeh.json")
                ?.let { _tasabeeh.value = it.toMutableList() }

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

    fun updateExpanded(item: Tasabeeh) {
        //println("update $item")
        val tempList = _tasabeeh.value.toMutableList();
        val indexOfItem =
            tempList.indexOfFirst { tasabeeh -> tasabeeh.ziker == item.ziker };
        //println("index $indexOfItem")
        val newItem: Tasabeeh = Tasabeeh(item.ziker, item.benefit, !item.expanded);
        tempList.set(indexOfItem, newItem);
        _tasabeeh.value = tempList;
    }
}


enum class TasbeehTab {
    المسبحة, الاذكار
}