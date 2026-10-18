package com.ottrojja.screens.zikrScreen

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.ottrojja.R
import com.ottrojja.classes.DownloadState
import com.ottrojja.classes.FileDownloader
import com.ottrojja.classes.Helpers
import com.ottrojja.classes.Helpers.reportException
import com.ottrojja.classes.StorageBase
import com.ottrojja.composables.ToastManager.classes.AppToastManager
import com.ottrojja.composables.ToastManager.classes.AppToastType
import com.ottrojja.interfaces.TabItem
import com.ottrojja.room.entities.Azkar
import com.ottrojja.room.repositories.QuranRepository
import com.ottrojja.services.azkar.AzkarPlaybackManager
import com.ottrojja.services.azkar.AzkarPlayerState
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File

data class ZikrUiState(
    val zikr: Azkar? = null,
    val selectedTab: ZikrViewModel.ZikrTab = ZikrViewModel.ZikrTab.الذكر,
    val showController: Boolean = true,
    val isDownloading: Boolean = false,
    val isDownloaded: Boolean = false,
)

class ZikrViewModel(
    private val repository: QuranRepository,
    private val playbackManager: AzkarPlaybackManager,
    application: Application,
) : AndroidViewModel(application) {

    private val context = application.applicationContext

    private val _ui = MutableStateFlow(ZikrUiState())
    val ui: StateFlow<ZikrUiState> = _ui.asStateFlow()

    val player: StateFlow<AzkarPlayerState> = playbackManager.state

    private val _sliderPosition = MutableStateFlow(0L)
    val sliderPosition: StateFlow<Long> = _sliderPosition.asStateFlow()

    private var loadedId: Long? = null

    init {
        playbackManager.connect()
        viewModelScope.launch {
            playbackManager.positionMs.collect { _sliderPosition.value = it }
        }
    }

    override fun onCleared() {
        playbackManager.releaseIfIdle()
        super.onCleared()
    }

    // region zikr data

    fun fetchZikr(id: Long) {
        if (loadedId == id) return
        loadedId = id

        viewModelScope.launch {
            try {
                val result = withContext(Dispatchers.IO) { repository.getAzkarById(id) }
                val downloaded = result?.let {
                    withContext(Dispatchers.IO) { localFileFor(it.zikrAddress).exists() }
                } ?: false
                _ui.update { it.copy(zikr = result, isDownloaded = downloaded) }
            } catch (e: Exception) {
                loadedId = null
                e.printStackTrace()
                reportException(exception = e, file = "ZikrViewModel", details = "zikr id: $id")
                AppToastManager.show(message = "حصل خطأ يرجى المحاولة لاحقا", type = AppToastType.ERROR)
            }
        }
    }

    private fun localFileFor(address: String): File =
        File(context.getExternalFilesDir(null), address.substringAfterLast('/'))

    // endregion

    // region UI

    fun selectTab(tab: ZikrTab) = _ui.update { it.copy(selectedTab = tab) }

    fun toggleController() = _ui.update { it.copy(showController = !it.showController) }

    /** Dragging up shows the controller, dragging down hides it. */
    fun onVerticalDrag(dragAmount: Float) = _ui.update { it.copy(showController = dragAmount <= 0) }

    // endregion

    // region playback

    fun play() {
        val zikr = _ui.value.zikr ?: return
        playbackManager.play(address = zikr.zikrAddress, title = zikr.azkarTitle)
    }

    fun pause() {
        playbackManager.pause()
    }

    fun increasePlaybackSpeed() {
        playbackManager.increaseSpeed()
    }

    fun decreasePlaybackSpeed() {
        playbackManager.decreaseSpeed()
    }

    fun onSliderChanged(value: Float) {
        _sliderPosition.value = value.toLong()
        playbackManager.seekTo(value.toLong())
    }

    // endregion

    // region download

    fun downloadZikr() {
        val address = _ui.value.zikr?.zikrAddress ?: return

        if (!Helpers.checkNetworkConnectivity(context)) {
            AppToastManager.show(message = "لا يوجد اتصال بالانترنت", type = AppToastType.ERROR)
            return
        }

        _ui.update { it.copy(isDownloading = true) }

        FileDownloader.download(
            context = context,
            url = address,
            relativePath = localFileFor(address).name,
            storageBase = StorageBase.EXTERNAL_FILES_DIR
        ).onEach { state ->
            when (state) {
                is DownloadState.Running -> _ui.update { it.copy(isDownloading = true) }

                is DownloadState.Success -> {
                    _ui.update { it.copy(isDownloading = false, isDownloaded = true) }
                    AppToastManager.show(message = "تم التحميل بنجاح!", type = AppToastType.SUCCESS)
                }

                is DownloadState.Failure -> {
                    _ui.update { it.copy(isDownloading = false) }
                    state.error.printStackTrace()
                    reportException(
                        exception = state.error,
                        file = "ZikrViewModel",
                        details = "Cancellation or Failure to download ziker file; link: $address"
                    )
                    val message = if (state.error.message?.contains("ENOSPC") == true) {
                        context.resources.getString(R.string.enospc)
                    } else {
                        "حدث خطأ اثناء التحميل"
                    }
                    AppToastManager.show(message = message, type = AppToastType.ERROR)
                }
            }
        }.launchIn(viewModelScope)
    }

    // endregion

    enum class ZikrTab(override val displayName: String) : TabItem {
        الذكر(displayName = "الذكر"),
        الفيديو(displayName = "الفيديو")
    }
}

class ZikrViewModelFactory(
    private val repository: QuranRepository,
    private val playbackManager: AzkarPlaybackManager,
    private val application: Application,
) : ViewModelProvider.AndroidViewModelFactory(application) {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ZikrViewModel::class.java)) {
            return ZikrViewModel(repository, playbackManager, application) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
