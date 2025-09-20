package com.ottrojja.screens.khitmahListScreen

import android.app.Application
import android.widget.Toast
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.ottrojja.classes.Helpers.reportException
import com.ottrojja.classes.QuranRepository
import com.ottrojja.room.entities.Khitmah
import com.ottrojja.room.entities.KhitmahMark
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class KhitmahListViewModel(private val repository: QuranRepository, application: Application) :
    AndroidViewModel(application) {
    val context = application.applicationContext;

    private val _khitmahList = mutableStateOf(emptyList<Khitmah>())
    var khitmahList: List<Khitmah>
        get() = _khitmahList.value
        set(value) {
            _khitmahList.value = value;
        }

    private val _showAddKhitmahDialog = mutableStateOf(false)
    var showAddKhitmahDialog: Boolean
        get() = _showAddKhitmahDialog.value
        set(value) {
            _showAddKhitmahDialog.value = value;
        }


    fun fetchKhitmahList() {
        try {
            viewModelScope.launch(Dispatchers.IO) {
                repository.getAllKhitmah().collect { state ->
                    withContext(Dispatchers.Main){
                        _khitmahList.value = state;
                    }
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
            reportException(exception = e, file = "KhitmahListViewModel")
            Toast.makeText(context, "حصل خطأ يرجى المحاولة لاحقا", Toast.LENGTH_LONG).show()
        }
    }

    fun createKhitmah(title: String) {
        try {
            viewModelScope.launch(Dispatchers.IO) {
                val createdKhitmahId = repository.insertKhitmah(Khitmah(title = title))
                repository.insertKhitmahMark(
                    KhitmahMark(khitmahId = createdKhitmahId.toInt(), pageNum = "1")
                )
                withContext(Dispatchers.Main) {
                    Toast.makeText(context, "تمت الإضافة بنجاح", Toast.LENGTH_LONG).show()
                }
                _showAddKhitmahDialog.value = false;
            }
        } catch (e: Exception) {
            e.printStackTrace()
            reportException(exception = e, file = "KhitmahListViewModel")
            Toast.makeText(context, "حصل خطأ يرجى المحاولة لاحقا", Toast.LENGTH_LONG).show()
        }
    }
}

class KhitmahListViewModelFactory(
    private val repository: QuranRepository,
    private val application: Application
) : ViewModelProvider.AndroidViewModelFactory(application) {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(KhitmahListViewModel::class.java)) {
            return KhitmahListViewModel(repository, application) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
