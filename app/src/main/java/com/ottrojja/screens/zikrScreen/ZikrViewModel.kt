package com.ottrojja.screens.zikrScreen

import android.app.Application
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.IBinder
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ottrojja.classes.AudioServiceInterface
import com.ottrojja.classes.Helpers
import com.ottrojja.classes.MediaPlayerService
import com.ottrojja.screens.azkarScreen.Azkar
import com.ottrojja.screens.azkarScreen.AzkarStore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class ZikrViewModel(application: Application) : AndroidViewModel(application) {
    val context = application.applicationContext;


    private var _zikr = mutableStateOf(Azkar("", "", "", "", ""))
    var zikr: Azkar
        get() = this._zikr.value
        set(value) {
            this._zikr.value = value
        }

    fun setZikr(zikrTitle: String) {
        this._zikr.value =
            AzkarStore.getAzkarData().find { azkar -> azkar.azkarTitle == zikrTitle }!!
    }

    private var _selectedTab = mutableStateOf("الذكر")
    var selectedTab: String
        get() = this._selectedTab.value
        set(value) {
            this._selectedTab.value = value
        }
    val zikrTabs = listOf("الذكر", "الفيديو")

    private var _showController = mutableStateOf(true)
    var showController: Boolean
        get() = this._showController.value
        set(value) {
            this._showController.value = value
        }

    private var _isPlaying = mutableStateOf(false)
    var isPlaying: Boolean
        get() = this._isPlaying.value
        set(value) {
            this._isPlaying.value = value
        }

    private var _isZikrPlaying = mutableStateOf(false) //for service binding issues
    var isZikrPlaying: Boolean
        get() = this._isZikrPlaying.value
        set(value) {
            this._isZikrPlaying.value = value
        }


    private var _isPaused = mutableStateOf(false)
    var isPaused: Boolean
        get() = _isPaused.value
        set(value) {
            _isPaused.value = value
        }


    private var _sliderPosition = mutableStateOf(0f)
    var sliderPosition: Float
        get() = this._sliderPosition.value
        set(value) {
            this._sliderPosition.value = value
        }

    private var _maxDuration = mutableStateOf(0f)
    var maxDuration: Float
        get() = this._maxDuration.value
        set(value) {
            this._maxDuration.value = value
        }


    fun playClicked() {
        if (Helpers.isMyServiceRunning(MediaPlayerService::class.java, context)) {
            playZikr()
        } else {
            clickedPlay = true;
            startAndBind();
        }
    }

    fun increasePlaybackSpeed() {
        audioService?.increaseSpeed();
    }

    fun decreasePlaybackSpeed() {
        audioService?.decreaseSpeed();
    }

    fun pauseZikr() {
        audioService?.pause();
    }

    fun startAndBind() {
        val serviceIntent = Intent(context, MediaPlayerService::class.java)
        serviceIntent.setAction("START")
        context.startService(serviceIntent)
        bindToService()
    }

    var clickedPlay = false;

    fun playZikr() {
        audioService?.playZiker(_zikr.value.firebaseAddress);
        // clean architicture and code? don't even try
    }

    private val serviceConnection = object : ServiceConnection {
        override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
            try {
                val binder = service as MediaPlayerService.YourBinder
                audioService = binder.getService()
                println("in binder get service")
                println(audioService)

                // Collect StateFlow updates
                viewModelScope.launch {
                    audioService?.getPlayingState(_zikr.value.firebaseAddress)?.collect { state ->
                        // Handle state updates here
                        _isPlaying.value = state;
                    }
                }

                viewModelScope.launch {
                    audioService?.getIsZikrPlaying()?.collect { state ->
                        // Handle state updates here
                        _isZikrPlaying.value = state;
                    }
                }

                viewModelScope.launch {
                    audioService?.getPaused()!!.collect { state ->
                        _isPaused.value = state;
                    }
                }

                viewModelScope.launch {
                    audioService?.getSliderMaxDuration()?.collect { state ->
                        _maxDuration.value = state;
                    }
                }

                viewModelScope.launch {
                    audioService?.getSliderPosition()?.collect { state ->
                        _sliderPosition.value = state;
                    }
                }

                viewModelScope.launch {
                    audioService?.getDestroyed()?.collect { state ->
                        if (state) {
                            unbindFromService()
                        }
                    }
                }

                viewModelScope.launch {
                    audioService?.resumeClicked()?.collect { state ->
                        audioService?.getPlayingState(_zikr.value.firebaseAddress)
                    }
                }


                if (clickedPlay) {
                    audioService?.playZiker(_zikr.value.firebaseAddress);
                }


            } catch (e: Exception) {
                println(e)
            }
        }

        override fun onServiceDisconnected(name: ComponentName?) {
            // Handle service disconnection
            unbindFromService();
        }
    }

    private var audioService: AudioServiceInterface? = null

    fun bindToService() {
        val intent = Intent(context, MediaPlayerService::class.java)
        context.bindService(intent, serviceConnection, Context.BIND_AUTO_CREATE)
    }

    fun unbindFromService() {
        if (audioService != null) {
            context.unbindService(serviceConnection)
        }
    }

    fun sliderChanged(value: Float) {
        println("vm changing position to $value")
        _sliderPosition.value = value;
        audioService?.setSliderPosition(value)
    }

    init {
        val sr = Helpers.isMyServiceRunning(MediaPlayerService::class.java, context);
        println("service running $sr")
        if (sr) {
            bindToService()
        }
    }

}