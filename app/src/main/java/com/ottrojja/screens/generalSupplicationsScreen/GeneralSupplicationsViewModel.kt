package com.ottrojja.screens.generalSupplicationsScreen

import android.app.Application
import android.widget.Toast
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
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

    fun clearSelectedSupplications() {
        _selectedSupplications.value = null;
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