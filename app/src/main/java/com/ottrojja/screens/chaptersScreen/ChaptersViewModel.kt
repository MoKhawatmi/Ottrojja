package com.ottrojja.screens.chaptersScreen

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
import com.ottrojja.classes.AudioServiceInterface
import com.ottrojja.classes.Helpers
import com.ottrojja.classes.Helpers.convertToArabicNumbers
import com.ottrojja.classes.Helpers.isMyServiceRunning
import com.ottrojja.classes.MediaPlayerService
import com.ottrojja.classes.QuranRepository
import com.ottrojja.screens.mainScreen.ChapterData
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.File
import java.io.FileOutputStream
import java.io.IOException

class ChaptersViewModel(private val repository: QuranRepository, application: Application) :
    AndroidViewModel(application) {
    val context = application.applicationContext;

    lateinit private var chaptersList: List<ChapterData>;

    private var _selectedSurah = mutableStateOf(ChapterData("", "", 0, "", 0))
    var selectedSurah: ChapterData
        get() = _selectedSurah.value
        set(value) {
            _selectedSurah.value = value
        }

    fun selectSurah(surah: ChapterData) {
        if (_selectedSurah.value.surahId == surah.surahId && _isChapterPlaying.value) {
            return
        }
        _selectedSurah.value = surah;
        audioService?.setSelectedChapterId("${surah.surahId}")
        println(_selectedSurah.value);
        play();
    }

    private var _isPlaying = mutableStateOf(false)
    var isPlaying: Boolean
        get() = _isPlaying.value
        set(value) {
            _isPlaying.value = value
        }

    private var _isChapterPlaying = mutableStateOf(false) //for service binding issues
    var isChapterPlaying: Boolean
        get() = _isChapterPlaying.value
        set(value) {
            _isChapterPlaying.value = value
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

    private var _playbackSpeed by mutableStateOf(1.0f)
    var playbackSpeed: Float
        get() = this._playbackSpeed
        set(value) {
            this._playbackSpeed = value
        }

    private var _maxDuration = mutableStateOf(0f)
    var maxDuration: Float
        get() = _maxDuration.value
        set(value) {
            _maxDuration.value = value
        }

    fun sliderChanged(value: Float) {
        //  println("vm changing position to $value")
        _sliderPosition.value = value;
        audioService?.setSliderPosition(value)
    }

    private var _isDownloading = mutableStateOf(false)
    var isDownloading: Boolean
        get() = _isDownloading.value
        set(value) {
            _isDownloading.value = value
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
                        println("playing status $state")
                        _isPlaying.value = state;
                    }
                }

                viewModelScope.launch {
                    audioService?.getIsChapterPlaying()?.collect { state ->
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
                        // println("current slider position $state")
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

                viewModelScope.launch {
                    audioService?.getPlaybackSpeed()!!.collect { state ->
                        _playbackSpeed = state;
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
        if (audioService != null && serviceConnection != null) {
            try { //this just keeps causing crashes
                context.unbindService(serviceConnection)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun checkIfChapterDownloaded(surahId: Int): Boolean {
        // println("checking download of chapter $surahId")
        val localFile = File(context.getExternalFilesDir(null), "$surahId.mp3")
        return localFile.exists()
    }

    fun downloadChapter(surahId: Int) {
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
            "$surahId.mp3"
        )
        val tempFile = File.createTempFile("temp_", ".mp3", context.getExternalFilesDir(null))

        val client = OkHttpClient()
        val request = Request.Builder()
            .url("https://ottrojja.fra1.cdn.digitaloceanspaces.com/chapters/$surahId.mp3")
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

                    if (tempFile.exists()) {
                        println("deleting temp")
                        tempFile.delete()
                    }
                    _isDownloading.value = false;
                }
            }
        }
    }

    init {
        viewModelScope.launch(Dispatchers.IO) {
            chaptersList = repository.getAllChapters();
        }

        val sr = isMyServiceRunning(MediaPlayerService::class.java, context);
        println("service running $sr")
        if (sr) {
            bindToService()
        }
    }

    private var _searchFilter by mutableStateOf("")
    var searchFilter: String
        get() = _searchFilter
        set(value) {
            _searchFilter = value
        }

    fun getChaptersList(): List<ChapterData> {
        return chaptersList.filter { chapter ->
            chapter.chapterName.contains(_searchFilter) || chapter.surahId.toString() == convertToArabicNumbers(
                _searchFilter
            )
                    || chapter.surahId.toString() == convertToArabicNumbers(_searchFilter)
        };
    }


}

class ChaptersViewModelFactory(
    private val repository: QuranRepository,
    private val application: Application
) : ViewModelProvider.AndroidViewModelFactory(application) {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ChaptersViewModel::class.java)) {
            return ChaptersViewModel(repository, application) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}