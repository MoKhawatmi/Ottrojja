package com.ottrojja.screens.chaptersScreen

import android.app.ActivityManager
import android.app.Application
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.IBinder
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.core.content.ContextCompat.getSystemService
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.ottrojja.classes.AudioServiceInterface
import com.ottrojja.classes.Helpers.isMyServiceRunning
import com.ottrojja.classes.MediaPlayerService
import com.ottrojja.classes.QuranStore
import com.ottrojja.screens.mainScreen.ChapterData
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class ChaptersViewModel(application: Application) : AndroidViewModel(application) {
    val context = application.applicationContext;
    val chaptersList: List<ChapterData> = getChapters()

    private var _selectedSurah = mutableStateOf(ChapterData("", "", 0, "", 0))
    var selectedSurah: ChapterData
        get() = this._selectedSurah.value
        set(value) {
            this._selectedSurah.value = value
        }

    fun selectSurah(surah: ChapterData) {
        if (_selectedSurah.value.surahId == surah.surahId) {
            return
        }
        _selectedSurah.value = surah;
        audioService?.setSelectedChapterId("${surah.surahId}")
        println(_selectedSurah.value);
        play();
    }

    private var _isPlaying = mutableStateOf(false)
    var isPlaying: Boolean
        get() = this._isPlaying.value
        set(value) {
            this._isPlaying.value = value
        }

    private var _isChapterPlaying = mutableStateOf(false) //for service binding issues
    var isChapterPlaying: Boolean
        get() = this._isChapterPlaying.value
        set(value) {
            this._isChapterPlaying.value = value
        }


    private var _isPaused = mutableStateOf(false)
    var isPaused: Boolean
        get() = _isPaused.value
        set(value) {
            _isPaused.value = value
        }

    fun increasePlaybackSpeed() {
        audioService?.increaseSpeed();
    }

    fun decreasePlaybackSpeed() {
        audioService?.decreaseSpeed();
    }

    fun pause() {
        audioService?.pause();
        println("is paused " + _isPaused.value)
    }

    var clickedPlay = false;
    fun play() {
        if (isMyServiceRunning(MediaPlayerService::class.java, context)) {
            audioService?.playChapter(
                "https://ottrojja.fra1.cdn.digitaloceanspaces.com/chapters/${_selectedSurah.value.surahId}.mp3"
            );
        } else {
            clickedPlay = true;
            startAndBind();
        }
    }

    fun goNextChapter() {
        audioService?.playNextChapter()
    }

    fun goPreviousChapter() {
        audioService?.playPreviousChapter()
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

    fun sliderChanged(value: Float) {
        println("vm changing position to $value")
        _sliderPosition.value = value;
        audioService?.setSliderPosition(value)
    }


    fun startAndBind() {
        val serviceIntent = Intent(context, MediaPlayerService::class.java)
        serviceIntent.setAction("START")
        context.startService(serviceIntent)
        bindToService()
    }

    private val serviceConnection = object : ServiceConnection {
        override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
            try {
                val binder = service as MediaPlayerService.YourBinder
                audioService = binder.getService()

                viewModelScope.launch {
                    audioService?.getPlayingState("", true)?.collect { state ->
                        withContext(Dispatchers.Main) {
                            println("playing status $state")
                        }
                        _isPlaying.value = state;
                    }
                }

                viewModelScope.launch {
                    audioService?.getIsChapterPlaying()?.collect { state ->
                        // Handle state updates here
                        _isChapterPlaying.value = state;
                    }
                }


                viewModelScope.launch {
                    audioService?.getPaused()!!.collect { state ->
                        _isPaused.value = state;
                    }
                }

                viewModelScope.launch {
                    audioService?.getSelectedChapterId()!!.collect { state ->
                        val tempChapter =
                            chaptersList.find { chapter -> "${chapter.surahId}" == state }
                        if (tempChapter != null) {
                            _selectedSurah.value = tempChapter;
                        }
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
                        audioService?.getPlayingState("", true)
                    }
                }


                println("should play $clickedPlay")
                if (clickedPlay) {
                    audioService?.playChapter(
                        "https://ottrojja.fra1.cdn.digitaloceanspaces.com/chapters/${_selectedSurah.value.surahId}.mp3"
                    );
                    audioService?.setSelectedChapterId("${_selectedSurah.value.surahId}")
                    clickedPlay = false;
                }


            } catch (e: Exception) {
                println(e)
            }
        }

        override fun onServiceDisconnected(name: ComponentName?) {
            unbindFromService();
        }
    }

    private var audioService: AudioServiceInterface? = null

    fun bindToService() {
        val intent = Intent(context, MediaPlayerService::class.java)
        context.bindService(intent, serviceConnection, Context.BIND_AUTO_CREATE)
    }

    fun unbindFromService() {
        _selectedSurah.value = ChapterData("", "", 0, "", 0);
        if (audioService != null) {
            context.unbindService(serviceConnection)
        }
    }

    private fun getChapters(): List<ChapterData> {
        return QuranStore.getChaptersData();
    }

    init {
        val sr = isMyServiceRunning(MediaPlayerService::class.java, context);
        println("service running $sr")
        if (sr) {
            bindToService()
        }
    }

}