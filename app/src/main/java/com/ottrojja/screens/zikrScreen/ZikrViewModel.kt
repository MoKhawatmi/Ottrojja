package com.ottrojja.screens.zikrScreen

import android.app.Application
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.Build
import android.os.IBinder
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.ottrojja.R
import com.ottrojja.classes.DownloadState
import com.ottrojja.classes.FileDownloader
import com.ottrojja.services.AudioServiceInterface
import com.ottrojja.classes.Helpers
import com.ottrojja.classes.Helpers.formatTime
import com.ottrojja.classes.Helpers.isServiceRunning
import com.ottrojja.classes.Helpers.reportException
import com.ottrojja.classes.Helpers.terminateAllServices
import com.ottrojja.services.AzkarPlayerService
import com.ottrojja.room.repositories.QuranRepository
import com.ottrojja.classes.StorageBase
import com.ottrojja.composables.ToastManager.classes.AppToastManager
import com.ottrojja.composables.ToastManager.classes.AppToastType
import com.ottrojja.interfaces.TabItem
import com.ottrojja.room.entities.Azkar
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import java.io.File

class ZikrViewModel(
    private val repository: QuranRepository,
    application: Application,
) : AndroidViewModel(application) {

    val context: Context = application.applicationContext

    // ---------------------------------------------------------------------
    // Zikr data
    // ---------------------------------------------------------------------

    private val _zikr = MutableStateFlow<Azkar?>(null)
    val zikr = _zikr.asStateFlow()

    /** Null-safe accessors, use these instead of _zikr.value.xxx */
    private val zikrAddress: String? get() = _zikr.value?.zikrAddress

    private fun localFileFor(address: String): File =
        File(context.getExternalFilesDir(null), address.substringAfterLast('/'))

    fun fetchZikr(id: Long) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val result = repository.getAzkarById(id)
                _zikr.value = result
                _isDownloaded.value = result?.let { localFileFor(it.zikrAddress).exists() } ?: false
            } catch (e: Exception) {
                e.printStackTrace()
                reportException(exception = e, file = "ZikrViewModel", details = "zikr id: $id")
                AppToastManager.show(
                    message = "حصل خطأ يرجى المحاولة لاحقا",
                    type = AppToastType.ERROR
                )
            }
        }
    }

    // ---------------------------------------------------------------------
    // UI state
    // ---------------------------------------------------------------------

    private var _selectedTab = mutableStateOf(ZikrTab.الذكر)
    var selectedTab: ZikrTab
        get() = _selectedTab.value
        set(value) {
            _selectedTab.value = value
        }

    private var _showController = mutableStateOf(true)
    var showController: Boolean
        get() = _showController.value
        set(value) {
            _showController.value = value
        }

    private var _isPlaying = mutableStateOf(false)
    var isPlaying: Boolean
        get() = _isPlaying.value
        set(value) {
            _isPlaying.value = value
        }

    private var _isPaused = mutableStateOf(false)
    var isPaused: Boolean
        get() = _isPaused.value
        set(value) {
            _isPaused.value = value
        }

    private var _sliderPosition = mutableStateOf(0f)
    var sliderPosition: Float
        get() = _sliderPosition.value
        set(value) {
            _sliderPosition.value = value
        }

    private var _maxDuration = mutableStateOf(0f)
    var maxDuration: Float
        get() = _maxDuration.value
        set(value) {
            _maxDuration.value = value
        }

    private var _maxDurationFormatted = ""

    private var _progressTimeCodeDisplay = mutableStateOf("")
    var progressTimeCodeDisplay: String
        get() = _progressTimeCodeDisplay.value
        set(value) {
            _progressTimeCodeDisplay.value = value
        }

    private var _playbackSpeed by mutableStateOf(1.0f)
    var playbackSpeed: Float
        get() = _playbackSpeed
        set(value) {
            _playbackSpeed = value
        }

    // ---------------------------------------------------------------------
    // Download state
    // ---------------------------------------------------------------------

    private var _isDownloading = mutableStateOf(false)
    var isDownloading: Boolean
        get() = _isDownloading.value
        set(value) {
            _isDownloading.value = value
        }

    /** Held as state so the UI doesn't hit the disk on every recomposition. */
    private val _isDownloaded = mutableStateOf(false)
    val isDownloaded: Boolean
        get() = _isDownloaded.value

    fun downloadZikr() {
        val address = zikrAddress ?: return

        if (!Helpers.checkNetworkConnectivity(context)) {
            AppToastManager.show(
                message = "لا يوجد اتصال بالانترنت",
                type = AppToastType.ERROR
            )
            return
        }

        _isDownloading.value = true

        val localFile = localFileFor(address)

        FileDownloader.download(
            context = context,
            url = address,
            relativePath = localFile.name,
            storageBase = StorageBase.EXTERNAL_FILES_DIR
        ).onEach { state ->
            when (state) {
                is DownloadState.Running -> {
                    _isDownloading.value = true
                }

                is DownloadState.Success -> {
                    _isDownloading.value = false
                    _isDownloaded.value = true
                    AppToastManager.show(
                        message = "تم التحميل بنجاح!",
                        type = AppToastType.SUCCESS
                    )
                }

                is DownloadState.Failure -> {
                    _isDownloading.value = false
                    state.error.printStackTrace()

                    reportException(
                        exception = state.error,
                        file = "ZikrViewModel",
                        details = "Cancellation or Failure to download ziker file; link: $address"
                    )

                    println("failed download: ${state.error.message}")

                    if (state.error.message?.contains("ENOSPC") == true) {
                        AppToastManager.show(
                            message = context.resources.getString(R.string.enospc),
                            type = AppToastType.ERROR
                        )
                    } else {
                        AppToastManager.show(
                            message = "حدث خطأ اثناء التحميل",
                            type = AppToastType.ERROR
                        )
                    }
                }
            }
        }.launchIn(viewModelScope)
    }

    // ---------------------------------------------------------------------
    // Playback
    // ---------------------------------------------------------------------

    private var audioService: AudioServiceInterface? = null
    private var clickedPlay = false

    fun playClicked() {
        // stop other services
        terminateAllServices(context, AzkarPlayerService::class.java)

        if (isServiceRunning(AzkarPlayerService::class.java, context)) {
            playZikr()
        } else {
            clickedPlay = true
            startAndBind()
        }
    }

    fun playZikr() {
        val address = zikrAddress ?: return
        audioService?.playZiker(address)
    }

    fun pauseZikr() {
        audioService?.pause()
    }

    fun increasePlaybackSpeed() {
        audioService?.increaseSpeed()
    }

    fun decreasePlaybackSpeed() {
        audioService?.decreaseSpeed()
    }

    fun sliderChanged(value: Float) {
        _sliderPosition.value = value
        audioService?.setSliderPosition(value)
    }

    fun startAndBind() {
        val serviceIntent = Intent(context, AzkarPlayerService::class.java)
        serviceIntent.action = "START"
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            context.startForegroundService(serviceIntent)
        } else {
            context.startService(serviceIntent)
        }
        bindToService()
    }

    private val serviceConnection = object : ServiceConnection {
        override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
            try {
                val binder = service as? AzkarPlayerService.YourBinder ?: return
                audioService = binder.getService()

                // Waits until the zikr is loaded, so it never reads a null zikr
                viewModelScope.launch {
                    _zikr.filterNotNull().collectLatest { z ->
                        audioService?.getPlayingState(z.zikrAddress)?.collect { state ->
                            _isPlaying.value = state
                            if (state) {
                                //TODO placed here this statement will also be called unnecessarily, find a better solution
                                audioService?.setCurrentPlayingTitle(z.azkarTitle)
                            }
                        }
                    }
                }

                viewModelScope.launch {
                    audioService?.getPaused()?.collect { state ->
                        _isPaused.value = state
                    }
                }

                viewModelScope.launch {
                    audioService?.getSliderMaxDuration()?.collect { state ->
                        _maxDuration.value = state
                        _maxDurationFormatted = formatTime(state.toLong())
                    }
                }

                viewModelScope.launch {
                    audioService?.getSliderPosition()?.collect { state ->
                        _sliderPosition.value = state
                        _progressTimeCodeDisplay.value =
                            "${formatTime(state.toLong())}/$_maxDurationFormatted"
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
                    audioService?.getPlaybackSpeed()?.collect { state ->
                        _playbackSpeed = state
                    }
                }

                if (clickedPlay) {
                    clickedPlay = false
                    playZikr()
                }
            } catch (e: Exception) {
                e.printStackTrace()
                reportException(exception = e, file = "ZikrViewModel")
            }
        }

        override fun onServiceDisconnected(name: ComponentName?) {
            // Only drop the reference; calling unbindService here can crash
            audioService = null
        }
    }

    fun bindToService() {
        val intent = Intent(context, AzkarPlayerService::class.java)
        context.bindService(intent, serviceConnection, Context.BIND_AUTO_CREATE)
    }

    fun unbindFromService() {
        if (audioService != null) {
            audioService = null
            try {
                context.unbindService(serviceConnection)
            } catch (_: IllegalArgumentException) {
                // already unbound
            }
        }
    }

    override fun onCleared() {
        unbindFromService()
        super.onCleared()
    }

    init {
        if (isServiceRunning(AzkarPlayerService::class.java, context)) {
            bindToService()
        }
    }

    enum class ZikrTab(override val displayName: String) : TabItem {
        الذكر(displayName = "الذكر"),
        الفيديو(displayName = "الفيديو")
    }
}

class ZikrViewModelFactory(
    private val repository: QuranRepository,
    private val application: Application,
) : ViewModelProvider.AndroidViewModelFactory(application) {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ZikrViewModel::class.java)) {
            return ZikrViewModel(repository, application) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}