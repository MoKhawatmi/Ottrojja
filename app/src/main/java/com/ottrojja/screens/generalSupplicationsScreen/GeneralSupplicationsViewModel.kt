package com.ottrojja.screens.generalSupplicationsScreen

import android.app.Application
import android.widget.Toast
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import com.ottrojja.classes.GeneralSupplications
import com.ottrojja.classes.JsonParser
import com.ottrojja.screens.mainScreen.ChapterData

class GeneralSupplicationsViewModel(application: Application) : AndroidViewModel(application) {
    val context = application.applicationContext;


    private val _supplications = mutableStateListOf<GeneralSupplications>()
    val supplications: MutableList<GeneralSupplications>
        get() = _supplications

    private val _selectedSupplications = mutableStateOf<GeneralSupplications?>(null)
    var selectedSupplications: GeneralSupplications?
        get() = _selectedSupplications.value
        set(value) {
            _selectedSupplications.value = value
        }

    private var _searchFilter = mutableStateOf("")
    var searchFilter: String
        get() = _searchFilter.value
        set(value) {
            _searchFilter.value = value
        }

    fun clearSelectedSupplications() {
        _selectedSupplications.value = null;
    }

    fun getFilteredSupplications(): MutableList<GeneralSupplications> {
        if (_supplications.size > 0) {
            return _supplications.filter { it.category.contains(_searchFilter.value)
                    || it.array.any{it.text.contains(_searchFilter.value)} }.toMutableList()
        }
        return emptyList<GeneralSupplications>().toMutableList();
    }

    init {
        try {
            JsonParser(context).parseJsonArrayFile<GeneralSupplications>(
                "generalSupplications.json"
            )?.let { _supplications.addAll(it) }
        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(context, "حصل خطأ، يرجى المحاولة مرة اخرى", Toast.LENGTH_LONG).show()
        }
    }

}