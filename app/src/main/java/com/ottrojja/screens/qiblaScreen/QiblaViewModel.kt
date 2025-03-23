package com.ottrojja.screens.qiblaScreen

import android.app.Application
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.ottrojja.classes.CompassSensorListener
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

class QiblaViewModel (application: Application) : AndroidViewModel(application){
    val context = application.applicationContext;

    private val sensorListener = CompassSensorListener(application)
    private val _azimuth = mutableStateOf(0f)
    val azimuth: State<Float> = _azimuth

    private val _needsCalibration = mutableStateOf(false)
    val needsCalibration: State<Boolean> = _needsCalibration

    init {
        sensorListener.register()
        // You might start a coroutine or use a timer to poll sensorListener values periodically
        viewModelScope.launch {
            while (isActive) {
                _azimuth.value = sensorListener.azimuth
                _needsCalibration.value = sensorListener.calibrationNeeded
                delay(100) // Polling interval
            }
        }
    }

    override fun onCleared() {
        sensorListener.unregister()
        super.onCleared()
    }
}