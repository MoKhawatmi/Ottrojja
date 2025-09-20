package com.ottrojja.screens.azkarScreen

import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.ottrojja.classes.Helpers.reportException
import com.ottrojja.classes.QuranRepository
import com.ottrojja.room.entities.Azkar
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class AzkarViewModel(private val repository: QuranRepository) : ViewModel() {
    private val _azkarData = mutableStateOf(listOf<Azkar>())
    var azkarData: List<Azkar>
        get() = _azkarData.value
        set(value: List<Azkar>) {
            _azkarData.value = value;
        }

    fun fetchAzakr(){
        viewModelScope.launch(Dispatchers.IO) {
            try {
                _azkarData.value = repository.getAllAzkar()
            }catch (e: Exception){
                e.printStackTrace()
                reportException(exception = e, file = "AzkarViewModel")
            }
        }
    }
}

class AzkarModelFactory(
    private val repository: QuranRepository,
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(AzkarViewModel::class.java)) {
            return AzkarViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}