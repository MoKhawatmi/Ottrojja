package com.ottrojja.screens.qiblaScreen

import android.app.Application
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.AndroidViewModel

class QiblaViewModel (application: Application) : AndroidViewModel(application){
    val context = application.applicationContext;

    private var _showPoistionDialog = mutableStateOf(false)
    var showPoistionDialog: Boolean
        get() = _showPoistionDialog.value
        set(value) {
            _showPoistionDialog.value = value
        }
}