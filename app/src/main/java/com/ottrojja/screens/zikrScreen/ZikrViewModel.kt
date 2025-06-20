package com.ottrojja.screens.zikrScreen

import android.app.Application
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.IBinder
import android.widget.Toast
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.ottrojja.services.AudioServiceInterface
import com.ottrojja.classes.Helpers
import com.ottrojja.classes.Helpers.isMyServiceRunning
import com.ottrojja.classes.Helpers.terminateAllServices
import com.ottrojja.services.MediaPlayerService
import com.ottrojja.classes.QuranRepository
import com.ottrojja.room.entities.Azkar
import com.ottrojja.services.PagePlayerService
import com.ottrojja.services.QuranPlayerService
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.File
import java.io.FileOutputStream
import java.io.IOException

class ZikrViewModel(
    private val repository: QuranRepository,
    application: Application,
    zikrTitle: String
) :
    AndroidViewModel(application) {
    val context = application.applicationContext;


    var _zikr = MutableStateFlow<Azkar>(Azkar("", "", "", "", ""))

    init {
        viewModelScope.launch(Dispatchers.IO) {
            _zikr.value = repository.getAzkarByTitle(zikrTitle)
        }
    }

    private var _selectedTab = mutableStateOf(ZikrTab.الذكر)
    var selectedTab: ZikrTab
        get() = _selectedTab.value
        set(value) {
            _selectedTab.value = value
        }

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

    private var _playbackSpeed by mutableStateOf(1.0f)
    var playbackSpeed: Float
        get() = this._playbackSpeed
        set(value) {
            this._playbackSpeed = value
        }


    fun playClicked() {
        // stop other services
        terminateAllServices(context, MediaPlayerService::class.java)


        if (isMyServiceRunning(MediaPlayerService::class.java, context)) {
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

                viewModelScope.launch {
                    audioService?.getPlayingState(_zikr.value.firebaseAddress)?.collect { state ->
                        println("is same zikr playing: $state")
                        _isPlaying.value = state;
                        if(state){
                            //TODO placed here this statement will also be called unnecessarily, find a better solution
                            audioService?.setCurrentPlayingTitle(_zikr.value.azkarTitle);
                        }
                    }
                }

                viewModelScope.launch {
                    audioService?.getIsZikrPlaying()?.collect { state ->
                        println("is zikr playing: $state")
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

                viewModelScope.launch {
                    audioService?.getPlaybackSpeed()!!.collect { state ->
                        _playbackSpeed = state;
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

    private var _isDownloading = mutableStateOf(false)
    var isDownloading: Boolean
        get() = _isDownloading.value
        set(value) {
            _isDownloading.value = value
        }

    fun checkIfZikrDownloaded(): Boolean {
        println("checking ${_zikr.value.firebaseAddress.split("/").last()}")
        val localFile =
            File(context.getExternalFilesDir(null), _zikr.value.firebaseAddress.split("/").last())
        return localFile.exists()
    }

    fun downloadZikr() {
        if (!Helpers.checkNetworkConnectivity(context)) {
            Toast
                .makeText(
                    context,
                    "لا يوجد اتصال بالانترنت",
                    Toast.LENGTH_LONG
                )
                .show()
            return;
        }

        _isDownloading.value = true;

        val localFile = File(
            context.getExternalFilesDir(null),
            _zikr.value.firebaseAddress.split("/").last()
        )
        val tempFile = File.createTempFile("temp_", ".mp3", context.getExternalFilesDir(null))

        val client = OkHttpClient()
        val request = Request.Builder()
            .url(_zikr.value.firebaseAddress)
            .build()


        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                try {
                    client.newCall(request).execute().use { response ->
                        if (!response.isSuccessful) throw IOException("Unexpected code $response")

                        response.body?.let { responseBody ->
                            FileOutputStream(tempFile).use { outputStream ->
                                responseBody.byteStream().use { inputStream ->
                                    inputStream.copyTo(outputStream)
                                }
                            }
                        }

                        tempFile.copyTo(localFile, overwrite = true)
                        withContext(Dispatchers.Main) {
                            Toast.makeText(
                                context,
                                "تم التحميل بنجاح!",
                                Toast.LENGTH_LONG
                            ).show()
                        }

                    }
                } catch (e: Exception) {
                    println("error in download")
                    e.printStackTrace()
                    withContext(Dispatchers.Main) {
                        Toast.makeText(context, "حدث خطأ اثناء التحميل", Toast.LENGTH_LONG).show()
                    }
                    localFile.delete()
                } finally {
                    if (tempFile.exists()) tempFile.delete()
                    _isDownloading.value = false;
                }
            }
        }
    }

    init {
        val sr = isMyServiceRunning(MediaPlayerService::class.java, context);
        println("service running $sr")
        if (sr) {
            bindToService()
        }
    }

    enum class ZikrTab {
        الذكر,
        الفيديو
    }
}

class ZikrViewModelFactory(
    private val repository: QuranRepository,
    private val application: Application,
    private val zikrTitle: String,
) : ViewModelProvider.AndroidViewModelFactory(application) {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ZikrViewModel::class.java)) {
            return ZikrViewModel(repository, application, zikrTitle) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}