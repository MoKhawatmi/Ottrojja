package com.ottrojja.screens.TeacherScreen

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.ottrojja.classes.QuranRepository

class TeacherScreenViewModel(private val repository: QuranRepository, application: Application) :
    AndroidViewModel(application) {


}

class TeacherScreenViewModelFactory(
    private val repository: QuranRepository,
    private val application: Application
) : ViewModelProvider.AndroidViewModelFactory(application) {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(TeacherScreenViewModel::class.java)) {
            return TeacherScreenViewModel(repository, application) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}