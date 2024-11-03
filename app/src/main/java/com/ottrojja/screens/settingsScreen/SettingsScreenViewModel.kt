package com.ottrojja.screens.settingsScreen

import android.app.Application
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel

class SettingsScreenViewModel(application: Application) : AndroidViewModel(application) {

    private var _ShowAboutDialog by mutableStateOf(false)
    var ShowAboutDialog: Boolean
        get() = _ShowAboutDialog
        set(value) {
            _ShowAboutDialog = value
        }

    private var _ShowContactDialog by mutableStateOf(false)
    var ShowContactDialog: Boolean
        get() = _ShowContactDialog
        set(value) {
            _ShowContactDialog = value
        }

}
