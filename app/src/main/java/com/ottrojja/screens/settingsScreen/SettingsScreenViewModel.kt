package com.ottrojja.screens.settingsScreen

import android.app.Application
import android.content.Context
import android.content.SharedPreferences
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel

class SettingsScreenViewModel(application: Application) : AndroidViewModel(application) {

    val sharedPreferences: SharedPreferences =
        application.getSharedPreferences("ottrojja", Context.MODE_PRIVATE)

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

    private var _nightReadingMode by mutableStateOf(false)
    var nightReadingMode: Boolean
        get() = _nightReadingMode
        set(value) {
            _nightReadingMode = value
        }

    fun toggleNightReadingMode() {
        _nightReadingMode = !_nightReadingMode;
        val editor: SharedPreferences.Editor = sharedPreferences.edit()
        editor.putBoolean("nightReadingMode", _nightReadingMode)
        editor.apply()
    }

    fun getSettings() {
        _nightReadingMode = sharedPreferences.getBoolean("nightReadingMode", false)
    }


}
