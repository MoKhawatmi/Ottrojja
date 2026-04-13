package com.ottrojja.composables.overlayPermissionHandler

import android.app.Application
import android.content.Context
import android.content.SharedPreferences
import android.provider.Settings
import androidx.lifecycle.AndroidViewModel
import androidx.core.content.edit

class OverlaypermissionViewModel(application: Application):
    AndroidViewModel(application) {
    val context = application.applicationContext;

    val sharedPreferences: SharedPreferences =
        application.getSharedPreferences("ottrojja", Context.MODE_PRIVATE)


    fun hasAskedBefore() = sharedPreferences.getBoolean("asked_overlay", false)

    fun setAsked() {
        sharedPreferences.edit { putBoolean("asked_overlay", true) }
    }

    fun hasPermission(): Boolean {
        return Settings.canDrawOverlays(context)
    }

}