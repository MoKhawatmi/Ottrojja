package com.ottrojja.screens.namesOfGodScreen

import android.app.Application
import android.widget.Toast
import androidx.compose.runtime.mutableStateListOf
import androidx.lifecycle.AndroidViewModel
import com.ottrojja.classes.ExpandableItem
import com.ottrojja.classes.JsonParser
import com.ottrojja.classes.NameOfGod

class NamesOfGodViewModel(application: Application) : AndroidViewModel(application) {
    val context = application.applicationContext;

    private val _namesOfGod = mutableStateListOf<ExpandableItem<NameOfGod>>()
    val namesOfGod: MutableList<ExpandableItem<NameOfGod>>
        get() = _namesOfGod


    init {
        try {
            JsonParser(context).parseJsonArrayFile<NameOfGod>("namesOfGod.json")
                ?.let {
                    it.forEach { item ->
                        _namesOfGod.add(ExpandableItem(data = item, expanded = false))
                    }
                }
        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(context, "حصل خطأ، يرجى المحاولة مرة اخرى", Toast.LENGTH_LONG).show()
        }
    }

    fun updateExpanded(item: ExpandableItem<NameOfGod>) {
        val indexOfItem = _namesOfGod.indexOfFirst { it.data.id == item.data.id };
        _namesOfGod.set(indexOfItem, item.copy(expanded = !item.expanded))
    }

}