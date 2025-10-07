package com.ottrojja.screens.listeningScreen

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
import com.ottrojja.R
import com.ottrojja.classes.Helpers
import com.ottrojja.classes.Helpers.convertToArabicNumbers
import com.ottrojja.classes.Helpers.formatTime
import com.ottrojja.classes.Helpers.isMyServiceRunning
import com.ottrojja.classes.Helpers.reportException
import com.ottrojja.classes.Helpers.terminateAllServices
import com.ottrojja.classes.QuranListeningMode
import com.ottrojja.classes.QuranPlayingParameters
import com.ottrojja.classes.QuranRepository
import com.ottrojja.screens.mainScreen.ChapterData
import com.ottrojja.services.QuranPlayerService
import com.ottrojja.services.QuranServiceInterface
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.File
import java.io.FileOutputStream
import java.io.IOException

class ListeningViewModel(private val repository: QuranRepository, application: Application) :
    AndroidViewModel(application) {
    val context = application.applicationContext;

    private val chaptersList = CompletableDeferred<List<ChapterData>>()

    private var _selectedSurah = mutableStateOf<ChapterData?>(null)
    var selectedSurah: ChapterData?
        get() = _selectedSurah.value
        set(value) {
            _selectedSurah.value = value
        }

    private var _isPlaying = mutableStateOf(false)
    var isPlaying: Boolean
        get() = _isPlaying.value
        set(value) {
            _isPlaying.value = value
        }

    private var _currentPlayingTitle = mutableStateOf("")
    var currentPlayingTitle: String
        get() = _currentPlayingTitle.value
        set(value) {
            _currentPlayingTitle.value = value
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
        // stop other services
        terminateAllServices(context, QuranPlayerService::class.java)

        if (isMyServiceRunning(QuranPlayerService::class.java, context)) {
            startPlaying()
        } else {
            clickedPlay = true;
            startAndBind();
        }
    }

    fun startPlaying() {
        if (_listeningMode.value == QuranListeningMode.سورة_كاملة) {
            val playListItems = mutableListOf(
                "https://ottrojja.fra1.cdn.digitaloceanspaces.com/chapters/${_selectedSurah.value!!.surahId}.mp3"
            );

            audioService?.playTrack(
                QuranPlayingParameters(
                    startingSurah = null,
                    startingVerse = null,
                    endSurah = null,
                    endVerse = null,
                    playListItems = playListItems,
                    listeningMode = QuranListeningMode.سورة_كاملة,
                    selectedSurah = _selectedSurah.value,
                    surahRepetitions = _surahRepetitions.value,
                    continuousChapterPlaying = _continuousChapterPlaying.value
                )
            )
        } else {
            if (_startingSurah.value != null && _endSurah.value != null) {

                if (_startingSurah.value!!.surahId > _endSurah.value!!.surahId
                    || (_startingSurah.value!!.surahId == _endSurah.value!!.surahId && _startingVerse.value > _endVerse.value)) {
                    Toast.makeText(context, "موضع بداية التلاوة لا يجب ان يرد بعد موضع النهاية",
                        Toast.LENGTH_LONG
                    ).show()
                    return;
                }

                viewModelScope.launch(Dispatchers.IO) {
                    val versesRange = repository.getPagesContentRange(
                        startingSurah = _startingSurah.value!!.surahId,
                        startingVerse = _startingVerse.value,
                        endSurah = _endSurah.value!!.surahId,
                        endVerse = _endVerse.value,
                    )
                    val playListItems = mutableListOf<String>();
                    versesRange.forEach { verse ->
                        if (verse.verseNum == 1 && (verse.surahNum != 1 && verse.surahNum != 9)) {
                            // making sure to add basmalah at the start of each surah
                            playListItems.add(
                                "https://ottrojja.fra1.cdn.digitaloceanspaces.com/verses/basmalah.mp3"
                            );
                        }
                        playListItems.add(
                            "https://ottrojja.fra1.cdn.digitaloceanspaces.com/verses/${verse.pageNum}-${verse.surahNum}-${verse.verseNum}.mp3"
                        );
                    }

                    withContext(Dispatchers.Main) {
                        audioService?.playTrack(
                            QuranPlayingParameters(
                                startingSurah = _startingSurah.value!!,
                                startingVerse = _startingVerse.value,
                                endSurah = _endSurah.value!!,
                                endVerse = _endVerse.value,
                                playListItems = playListItems,
                                listeningMode = QuranListeningMode.مقطع_ايات,
                                selectedSurah = null,
                                verseRepetitions = _verseRepetitions.value,
                                verseRangeRepetitions = _verseRangeRepetitions.value
                            )
                        )
                    }
                }
            }
        }
    }

    fun goNextChapter() {
        audioService?.playNextChapter()
    }

    fun goPreviousChapter() {
        //audioService?.playPreviousChapter()
    }

    private var _sliderPosition = mutableStateOf(0f)
    var sliderPosition: Float
        get() = _sliderPosition.value
        set(value) {
            _sliderPosition.value = value
        }

    private var _playbackSpeed by mutableStateOf(1.0f)
    var playbackSpeed: Float
        get() = _playbackSpeed
        set(value) {
            _playbackSpeed = value
        }

    private var _maxDuration = mutableStateOf(0f)
    var maxDuration: Float
        get() = _maxDuration.value
        set(value) {
            _maxDuration.value = value
        }
    private var _maxDurationFormatted="";

    private var _progressTimeCodeDisplay = mutableStateOf("")
    var progressTimeCodeDisplay: String
        get() = _progressTimeCodeDisplay.value
        set(value) {
            _progressTimeCodeDisplay.value = value
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
        val serviceIntent = Intent(context, QuranPlayerService::class.java)
        serviceIntent.setAction("START")
        context.startService(serviceIntent)
        bindToService()
    }

    private val serviceConnection = object : ServiceConnection {
        override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
            try {
                val binder = service as QuranPlayerService.YourBinder
                audioService = binder.getService()
                viewModelScope.launch {
                    audioService?.getPlayingState()?.collect { state ->
                        println("playing status $state")
                        _isPlaying.value = state;
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
                        _maxDurationFormatted = formatTime(state.toLong());

                    }
                }

                viewModelScope.launch {
                    audioService?.getSliderPosition()?.collect { state ->
                        _sliderPosition.value = state;
                        _progressTimeCodeDisplay.value = "${formatTime(state.toLong())}/${_maxDurationFormatted}"
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
                        audioService?.getPlayingState()
                    }
                }

                viewModelScope.launch {
                    audioService?.getPlaybackSpeed()!!.collect { state ->
                        _playbackSpeed = state;
                    }
                }

                viewModelScope.launch {
                    audioService?.getCurrentPlayingTitle()!!.collect { state ->
                        _currentPlayingTitle.value = state;
                    }
                }


                viewModelScope.launch {
                    audioService?.getCurrentPlayingParameters()?.collect { state ->
                        println("current playing parameters")
                        println(state)
                        if (state != null) {
                            _currentPlayingParameters.value = state;
                            if (state.listeningMode == QuranListeningMode.سورة_كاملة) {
                                // when the service moves to next/previous chapter it doesn't send the full chapter data back, only the id, so we use the id to get the full chapter data at all times
                                _selectedSurah.value = chaptersList.await().find { it.surahId == state.selectedSurah?.surahId };
                                _surahRepetitions.value = state.surahRepetitions;
                                _continuousChapterPlaying.value = state.continuousChapterPlaying

                                audioService?.setCurrentPlayingTitle(
                                    "سورة ${_selectedSurah.value?.chapterName}"
                                )
                            } else {
                                _startingSurah.value = state.startingSurah;
                                _startingVerse.value = state.startingVerse ?: 1;
                                _endSurah.value = state.endSurah;
                                _endVerse.value = state.endVerse ?: 1;
                                _verseRepetitions.value = state.verseRepetitions;
                                _verseRangeRepetitions.value = state.verseRangeRepetitions;

                                audioService?.setCurrentPlayingTitle(
                                    "${state.startingSurah?.chapterName}: ${state.startingVerse} - ${state.endSurah?.chapterName}: ${state.endVerse}"
                                )

                            }
                        }
                    }
                }

                // fetch the current listening mode from the service just once on connection, while let other parameters be collected in realtime
                if (audioService?.getCurrentPlayingParameters()?.value?.listeningMode != null) {
                    _listeningMode.value = audioService?.getCurrentPlayingParameters()?.value?.listeningMode!!
                }

                println("should play $clickedPlay")
                if (clickedPlay) {
                    startPlaying();
                    clickedPlay = false;
                }


            } catch (e: Exception) {
                e.printStackTrace()
                reportException(exception = e, file = "ListeningViewModel")
            }
        }

        override fun onServiceDisconnected(name: ComponentName?) {
            unbindFromService();
        }
    }

    private var audioService: QuranServiceInterface? = null

    fun bindToService() {
        val intent = Intent(context, QuranPlayerService::class.java)
        context.bindService(intent, serviceConnection, Context.BIND_AUTO_CREATE)
    }

    fun unbindFromService() {
        if (audioService != null && serviceConnection != null) {
            try { //this just keeps causing crashes
                context.unbindService(serviceConnection)
            } catch (e: Exception) {
                reportException(exception = e, file = "ListeningViewModel")
                e.printStackTrace()
            }
        }
    }

    fun checkIfChapterDownloaded(surahId: Int): Boolean {
        val localFile = File(context.getExternalFilesDir(null), "$surahId.mp3")
        return localFile.exists()
    }

    fun downloadChapter(surahId: Int) {
        if (!Helpers.checkNetworkConnectivity(context)) {
            Toast.makeText(context, "لا يوجد اتصال بالانترنت", Toast.LENGTH_LONG).show()
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
                            Toast.makeText(context, "تم التحميل بنجاح!", Toast.LENGTH_LONG).show()
                        }
                    }
                } catch (e: Exception) {
                    println("error in download")
                    e.printStackTrace()
                    reportException(exception = e, file = "ListeningViewModel")
                    withContext(Dispatchers.Main) {
                        if (e.message?.contains("ENOSPC") == true) {
                            Toast.makeText(context, context.resources.getString(R.string.enospc), Toast.LENGTH_LONG).show()
                        } else {
                            Toast.makeText(context, "حدث خطأ اثناء التحميل", Toast.LENGTH_LONG).show()
                        }
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

    suspend fun initChaptersList() {
        println("Fetching chapters list")
        viewModelScope.launch(Dispatchers.IO) {
            val chapters = repository.getAllChapters()
            chaptersList.complete(chapters)
            if (_startingSurah.value == null) {
                _startingSurah.value = chaptersList.await().get(0);
            }
            if (_endSurah.value == null) {
                _endSurah.value = chaptersList.await().get(0);
            }
            if (_selectedSurah.value == null) {
                _selectedSurah.value = chaptersList.await().get(0);
            }
        }
    }

    private var _currentPlayingParameters = mutableStateOf<QuranPlayingParameters?>(null)
    var currentPlayingParameters: QuranPlayingParameters?
        get() = _currentPlayingParameters.value
        set(value) {
            _currentPlayingParameters.value = value
        }

    private var _searchFilter = mutableStateOf("")
    var searchFilter: String
        get() = _searchFilter.value
        set(value) {
            _searchFilter.value = value
        }

    suspend fun getChaptersList(): List<ChapterData> {
        return chaptersList.await().filter { chapter ->
            chapter.chapterName.contains(_searchFilter.value)
                    || chapter.surahId.toString() == convertToArabicNumbers(_searchFilter.value)
                    || chapter.surahId.toString() == _searchFilter.value
        };
    }

    private var _showSurahSelectionDialog = mutableStateOf(false)
    var showSurahSelectionDialog: Boolean
        get() = _showSurahSelectionDialog.value
        set(value) {
            _showSurahSelectionDialog.value = value
        }

    private var _showVerseSelectionDialog = mutableStateOf(false)
    var showVerseSelectionDialog: Boolean
        get() = _showVerseSelectionDialog.value
        set(value) {
            _showVerseSelectionDialog.value = value
        }

    private var _selectionPhase = mutableStateOf(SelectionPhase.START)
    var selectionPhase: SelectionPhase
        get() = _selectionPhase.value
        set(value) {
            _selectionPhase.value = value
        }

    private var _startingSurah = mutableStateOf<ChapterData?>(null)
    var startingSurah: ChapterData?
        get() = _startingSurah.value
        set(value) {
            _startingSurah.value = value
        }

    private var _startingVerse = mutableStateOf(1)
    var startingVerse: Int
        get() = _startingVerse.value
        set(value) {
            _startingVerse.value = value
        }

    private var _endSurah = mutableStateOf<ChapterData?>(null)
    var endSurah: ChapterData?
        get() = _endSurah.value
        set(value) {
            _endSurah.value = value
        }

    private var _endVerse = mutableStateOf(1)
    var endVerse: Int
        get() = _endVerse.value
        set(value) {
            _endVerse.value = value
        }

    private var _continuousChapterPlaying = mutableStateOf(true)
    var continuousChapterPlaying: Boolean
        get() = _continuousChapterPlaying.value
        set(value) {
            _continuousChapterPlaying.value = value
        }

    fun toggleContChapterPlaying() {
        _continuousChapterPlaying.value = !_continuousChapterPlaying.value;
        if (_currentPlayingParameters.value != null) {
            audioService?.setCurrentPlayingParameters(_currentPlayingParameters.value!!.copy(
                continuousChapterPlaying = _continuousChapterPlaying.value
            )
            )
        }
    }

    fun surahSelected(surah: ChapterData) {
        when (_selectionPhase.value) {
            SelectionPhase.START -> {
                _startingSurah.value = surah;
                _startingVerse.value = 1;
            }

            SelectionPhase.END -> {
                _endSurah.value = surah;
                _endVerse.value = 1;
            }

            SelectionPhase.PLAY -> {
                _selectedSurah.value = surah;
                play()
            }

        }
    }

    fun verseSelected(verse: Int) {
        if (_selectionPhase.value == SelectionPhase.START) {
            _startingVerse.value = verse;
        } else {
            _endVerse.value = verse;
        }
    }

    private val _listeningMode = mutableStateOf(QuranListeningMode.مقطع_ايات)
    var listeningMode: QuranListeningMode
        get() = _listeningMode.value
        set(value) {
            _listeningMode.value = value
        }

    private val _showRepetitionOptionsDialog = mutableStateOf(false)
    var showRepetitionOptionsDialog: Boolean
        get() = _showRepetitionOptionsDialog.value
        set(value) {
            _showRepetitionOptionsDialog.value = value
        }

    private val _surahRepetitions = mutableStateOf("0")
    var surahRepetitions: String
        get() = _surahRepetitions.value
        set(value) {
            _surahRepetitions.value = value
            if (_currentPlayingParameters.value != null) {
                audioService?.setCurrentPlayingParameters(
                    _currentPlayingParameters.value!!.copy(surahRepetitions = value)
                )
            }
        }

    private val _verseRepetitions = mutableStateOf("0")
    var verseRepetitions: String
        get() = _verseRepetitions.value
        set(value) {
            _verseRepetitions.value = value
            if (_currentPlayingParameters.value != null) {
                audioService?.setCurrentPlayingParameters(
                    _currentPlayingParameters.value!!.copy(verseRepetitions = value)
                )
            }
        }

    private val _verseRangeRepetitions = mutableStateOf("0")
    var verseRangeRepetitions: String
        get() = _verseRangeRepetitions.value
        set(value) {
            _verseRangeRepetitions.value = value
            if (_currentPlayingParameters.value != null) {
                audioService?.setCurrentPlayingParameters(
                    _currentPlayingParameters.value!!.copy(verseRangeRepetitions = value)
                )
            }
        }

    private val _repetitionSelectionMode = mutableStateOf(RepetitionSelectionMode.SURAH)
    var repetitionSelectionMode: RepetitionSelectionMode
        get() = _repetitionSelectionMode.value
        set(value) {
            _repetitionSelectionMode.value = value
        }

    fun switchListeningMode(value: QuranListeningMode) {
        _listeningMode.value = value;
    }

    enum class RepetitionSelectionMode {
        SURAH, VERSE, RANGE
    }

    enum class SelectionPhase {
        START, END, PLAY
    }

    init {
        val sr = isMyServiceRunning(QuranPlayerService::class.java, context);
        println("service running $sr")
        if (sr) {
            bindToService()
        }
    }

}

class ListeningViewModelFactory(
    private val repository: QuranRepository,
    private val application: Application
) : ViewModelProvider.AndroidViewModelFactory(application) {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ListeningViewModel::class.java)) {
            return ListeningViewModel(repository, application) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}