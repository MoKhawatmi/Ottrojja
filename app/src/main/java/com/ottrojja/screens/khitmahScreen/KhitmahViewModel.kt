package com.ottrojja.screens.khitmahScreen

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
import com.ottrojja.room.entities.Khitmah
import com.ottrojja.room.entities.KhitmahMark
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class KhitmahViewModel(private val repository: QuranRepository, application: Application) :
    AndroidViewModel(application) {
    val context = application.applicationContext;

    private val _khitmah = mutableStateOf<Khitmah?>(null)
    var khitmah: Khitmah?
        get() = _khitmah.value
        set(value) {
            _khitmah.value = value;
        }

    private val _khitmahMarks = mutableStateListOf<ExpandableItem<KhitmahMark>>() // mutableStateOf(emptyList<ExpandableItem<KhitmahMark>>())
    val khitmahMarks: MutableList<ExpandableItem<KhitmahMark>>
        get() = _khitmahMarks


    fun fetchKhitmah(id: Int) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                _khitmahMarks.clear()
                val khitmahWithMarks = repository.getKhitmah(id)
                _khitmah.value = khitmahWithMarks.khitmah;
                khitmahWithMarks.marks.forEach { mark ->
                    _khitmahMarks.add(ExpandableItem(data = mark, expanded = false))
                }
                println(khitmahWithMarks.marks)
            } catch (e: Exception) {
                e.printStackTrace()
                withContext(Dispatchers.Main) {
                    Toast.makeText(context, "حصل خطأ يرجى المحاولة لاحقا", Toast.LENGTH_LONG).show()
                }
            }
        }
    }

    fun removeKhitmahMark(id: Int) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                repository.deleteKhitmahMarkById(id)
                val indexOfItem = _khitmahMarks.indexOfFirst { khitmahMark -> khitmahMark.data.id == id };
                _khitmahMarks.removeAt(index = indexOfItem)
                withContext(Dispatchers.Main) {
                    Toast.makeText(context, " تم الحذف بنجاح", Toast.LENGTH_LONG).show()
                }
                //need a better approach for this
                if (_khitmahMarks.isNotEmpty()) {
                    if (_khitmah.value?.latestPage != _khitmahMarks.last().data.pageNum) {
                        _khitmah.value?.let {
                            repository.updateKhitmah(
                                it.copy(latestPage = _khitmahMarks.last().data.pageNum)
                            )
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

    fun toggleMarkExpanded(id: Int) {
        val indexOfItem = _khitmahMarks.indexOfFirst { khitmahMark -> khitmahMark.data.id == id };
        val foundItem = _khitmahMarks.get(indexOfItem)
        _khitmahMarks.set(indexOfItem, foundItem.copy(expanded = !foundItem.expanded));
    }

    fun deleteKhitmah() {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                repository.deleteKhitmah(_khitmah.value!!)
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

    fun toggleKhitmahStatus() {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                repository.updateKhitmah(
                    _khitmah.value!!.copy(isComplete = !_khitmah.value!!.isComplete)
                )
                withContext(Dispatchers.Main) {
                    Toast.makeText(context, "تم التحديث بنجاح", Toast.LENGTH_LONG).show()
                }
                fetchKhitmah(_khitmah.value!!.id)
            } catch (e: Exception) {
                e.printStackTrace()
                withContext(Dispatchers.Main) {
                    Toast.makeText(context, "حصل خطأ يرجى المحاولة لاحقا", Toast.LENGTH_LONG).show()
                }
            }
        }
    }


}

class KhitmahViewModelFactory(
    private val repository: QuranRepository,
    private val application: Application
) : ViewModelProvider.AndroidViewModelFactory(application) {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(KhitmahViewModel::class.java)) {
            return KhitmahViewModel(repository, application) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
