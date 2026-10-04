package com.ottrojja.screens.jwam3Screen

import android.app.Application
import android.widget.Toast
import androidx.compose.runtime.mutableStateListOf
import androidx.lifecycle.AndroidViewModel
import com.ottrojja.classes.ExpandableItem
import com.ottrojja.classes.Helpers.reportException
import com.ottrojja.classes.Jam3Supplication
import com.ottrojja.classes.JsonParser
import com.ottrojja.composables.ToastManager.classes.AppToastManager
import com.ottrojja.composables.ToastManager.classes.AppToastType

class Jwam3ViewModel(application: Application) : AndroidViewModel(application) {
    val context = application.applicationContext;

    private val _supplications = mutableStateListOf<ExpandableItem<Jam3Supplication>>()
    val supplications: MutableList<ExpandableItem<Jam3Supplication>>
        get() = _supplications


    init {
        try {
            JsonParser(context).parseJsonArrayFile<Jam3Supplication>("supplications.json")
                ?.let {
                    _supplications.addAll(
                        it.map { item -> ExpandableItem(data = item, expanded = false) })
                }
        } catch (e: Exception) {
            e.printStackTrace()
            reportException(exception = e, file = "Jwam3ViewModel")
            AppToastManager.show(
                message = "حصل خطأ يرجى المحاولة لاحقا",
                type = AppToastType.ERROR
            )
        }
    }

    fun updateExpanded(item: ExpandableItem<Jam3Supplication>) {
        val indexOfItem = _supplications.indexOfFirst { it.data.supplication == item.data.supplication };
        _supplications.set(indexOfItem, item.copy(expanded = !item.expanded))
    }


}