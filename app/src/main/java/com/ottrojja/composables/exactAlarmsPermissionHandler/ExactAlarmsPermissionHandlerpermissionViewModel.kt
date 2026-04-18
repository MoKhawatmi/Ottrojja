package com.ottrojja.composables.exactAlarmsPermissionHandler

import android.app.AlarmManager
import android.app.Application
import android.content.Context
import android.content.SharedPreferences
import android.os.Build
import android.provider.Settings
import androidx.lifecycle.AndroidViewModel
import androidx.core.content.edit

class ExactAlarmsPermissionHandlerpermissionViewModel(application: Application):
    AndroidViewModel(application) {
    val context = application.applicationContext;

    val sharedPreferences: SharedPreferences =
        application.getSharedPreferences("ottrojja", Context.MODE_PRIVATE)


    fun hasAskedBefore() = sharedPreferences.getBoolean("asked_exact_alarms", false)

    fun setAsked() {
        sharedPreferences.edit { putBoolean("asked_exact_alarms", true) }
    }

    fun hasPermission(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val alarmManager = context.getSystemService(AlarmManager::class.java)
            alarmManager.canScheduleExactAlarms()
        } else {
            true
        }
    }

}