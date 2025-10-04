package com.ottrojja.screens.teacherScreen

import android.app.Application
import android.net.Uri
import android.widget.Toast
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.snapshots.SnapshotStateMap
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.OnLifecycleEvent
import androidx.lifecycle.ProcessLifecycleOwner
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.media3.common.MediaItem
import androidx.media3.common.PlaybackException
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import com.ottrojja.R
import com.ottrojja.classes.AnswerStatus
import com.ottrojja.classes.Helpers
import com.ottrojja.classes.Helpers.convertToArabicNumbers
import com.ottrojja.classes.Helpers.reportException
import com.ottrojja.room.entities.PageContent
import com.ottrojja.classes.QuranRepository
import com.ottrojja.classes.TeacherAnswer
import com.ottrojja.classes.VerseWithAnswer
import com.ottrojja.screens.listeningScreen.ListeningViewModel
import com.ottrojja.screens.listeningScreen.ListeningViewModel.SelectionPhase
import com.ottrojja.screens.mainScreen.ChapterData
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import kotlin.random.Random

class TeacherScreenViewModel(private val repository: QuranRepository, application: Application) :
    AndroidViewModel(application), LifecycleObserver {
    val context = application.applicationContext;

    private val chaptersList = CompletableDeferred<List<ChapterData>>()
    private var completedChaptersList = emptyList<ChapterData>()

    suspend fun initChaptersList() {
        println("Fetching chapters list")
        viewModelScope.launch(Dispatchers.IO) {
            val chapters = repository.getAllChapters()
            chaptersList.complete(chapters)
            completedChaptersList = chaptersList.await()
            if (_startingSurah.value == null) {
                _startingSurah.value = chaptersList.await().get(0);
            }
            if (_endSurah.value == null) {
                _endSurah.value = chaptersList.await().get(0);
            }
        }
    }

    /*private val _selectedTrainingVerses = mutableStateOf(emptyList<PageContent>())
    var selectedTrainingVerses: List<PageContent>
        get() = _selectedTrainingVerses.value
        set(value: List<PageContent>) {
            _selectedTrainingVerses.value = value
        }*/

    private val _selectedTrainingVerses = mutableStateListOf<VerseWithAnswer>()
    val selectedTrainingVerses: MutableList<VerseWithAnswer>
        get() = _selectedTrainingVerses


    private val _currentVerse = mutableStateOf<PageContent?>(null)
    var currentVerse: PageContent?
        get() = _currentVerse.value
        set(value: PageContent?) {
            _currentVerse.value = value
        }

    private var _currentVerseIndex = mutableStateOf(0)
    var currentVerseIndex: Int
        get() = _currentVerseIndex.value
        set(value) {
            _currentVerseIndex.value = value
        }

    private var _correctVersesAnswered = mutableStateOf(0)
    var correctVersesAnswered: Int
        get() = _correctVersesAnswered.value
        set(value) {
            _correctVersesAnswered.value = value
        }

    private var _hasStarted = mutableStateOf(false)
    var hasStarted: Boolean
        get() = _hasStarted.value
        set(value) {
            _hasStarted.value = value
        }


    private val _mode = mutableStateOf(TeacherMode.SELECTION)
    var mode: TeacherMode
        get() = _mode.value
        set(value) {
            _mode.value = value
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

    fun getChapterName(id: Int): String {
        return completedChaptersList.find { chapter -> chapter.surahId == id }?.chapterName ?: "";
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

    private var _selectionPhase = mutableStateOf(ListeningViewModel.SelectionPhase.START)
    var selectionPhase: ListeningViewModel.SelectionPhase
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

            SelectionPhase.PLAY -> {}

        }
    }

    fun verseSelected(verse: Int) {
        if (_selectionPhase.value == SelectionPhase.START) {
            _startingVerse.value = verse;
        } else {
            _endVerse.value = verse;
        }
    }


    val MAX_TRIES = 3;

    private var _reachedMaxTries = mutableStateOf(false)
    var reachedMaxTries: Boolean
        get() = _reachedMaxTries.value
        set(value) {
            _reachedMaxTries.value = value
        }

    private var _lastVerseReached = mutableStateOf(false)
    var lastVerseReached: Boolean
        get() = _lastVerseReached.value
        set(value) {
            _lastVerseReached.value = value
        }


    private var _currentTry = mutableStateOf(0)
    var currentTry: Int
        get() = _currentTry.value
        set(value) {
            _currentTry.value = value
        }

    fun startTraining() {

        if (_startingSurah.value != null && _endSurah.value != null) {
            if (_startingSurah.value!!.surahId > _endSurah.value!!.surahId
                || (_startingSurah.value!!.surahId == _endSurah.value!!.surahId && _startingVerse.value > _endVerse.value)) {
                Toast.makeText(context, "موضع البداية لا يجب ان يرد بعد موضع النهاية",
                    Toast.LENGTH_LONG
                ).show()
                return;
            }
        }

        viewModelScope.launch(Dispatchers.IO) {
            val versesRange = repository.getPagesContentRange(
                startingSurah = _startingSurah.value!!.surahId,
                startingVerse = _startingVerse.value,
                endSurah = _endSurah.value!!.surahId,
                endVerse = _endVerse.value,
            )

            _selectedTrainingVerses.addAll(
                versesRange.map { VerseWithAnswer(verse = it, answerCorrect = false) });

            println(_selectedTrainingVerses)
            _currentVerseIndex.value = 0
            _currentVerse.value = _selectedTrainingVerses.get(_currentVerseIndex.value).verse
            checkLastVerseReached()
            println("vm ${_lastVerseReached.value}")
            generateCutVerse();
            _mode.value = TeacherMode.TRAINING;
        }
    }

    private var _allRight = mutableStateOf(false)
    var allRight: Boolean
        get() = _allRight.value
        set(value) {
            _allRight.value = value
        }


    fun checkVerse() {
        if (!_reachedMaxTries.value) {
            _currentTry.value++;
        } else {
            return;
        }
        println(_inputSolutions.toMap())

        var allRight = true;

        // compare inputsolution with solutionmap
        solutionMap.keys.forEach { key ->
            val keyInput = _inputSolutions.get(key)?.answer?.trim()
            if (keyInput != null && keyInput.length > 0) {
                if (solutionMap.get(key)?.contains(keyInput) == true) {
                    keyInput.let { TeacherAnswer(it, AnswerStatus.RIGHT) }
                        .let { _inputSolutions.set(key, it) }
                    println("solution $key right")
                } else {
                    keyInput.let { TeacherAnswer(it, AnswerStatus.WRONG) }
                        .let { _inputSolutions.set(key, it) }
                    println("solution $key wrong")
                    allRight = false;
                }
            } else {
                println("solution $key empty")
                _inputSolutions.set(key, TeacherAnswer("", AnswerStatus.WRONG))
                allRight = false;
            }
        }

        // need to get all solutions right to move on
        if (allRight) {
            _allRight.value = true;
            _correctVersesAnswered.value++;
            _selectedTrainingVerses.set(_currentVerseIndex.value, VerseWithAnswer(verse = _currentVerse.value!!, answerCorrect = true))
        }/* else if (!allRight) {

            if (_reachedMaxTries.value) {

            }
        }*/

        if (_currentTry.value == MAX_TRIES) {
            _reachedMaxTries.value = true;
        }
    }

    fun proceedVerse() {
        if (!_lastVerseReached.value) {
            resetMedia()
            solutionMap.clear()
            _reachedMaxTries.value = false;
            _currentTry.value = 0;
            _allRight.value = false;
            _currentVerseIndex.value++;
            _currentVerse.value = _selectedTrainingVerses.get(_currentVerseIndex.value).verse
            generateCutVerse()
        }
        checkLastVerseReached()
    }

    fun checkLastVerseReached() {
        if (_currentVerseIndex.value >= _selectedTrainingVerses.size - 1) {
            _lastVerseReached.value = true;
        }
    }

    fun backToSelection() {
        _mode.value = TeacherMode.SELECTION
        resetAll()
    }

    fun showResults() {
        _mode.value = TeacherMode.RESULTS
    }

    fun resetAll() {
        _inputSolutions.clear();
        solutionMap.clear()
        _reachedMaxTries.value = false;
        _currentTry.value = 0;
        _allRight.value = false;
        _currentVerseIndex.value = 0;
        //_selectedTrainingVerses.value = emptyList<PageContent>();
        _selectedTrainingVerses.clear()
        _hasStarted.value = false;
        _correctVersesAnswered.value = 0;
        _lastVerseReached.value = false;
        resetMedia()
    }

    val solutionMap = hashMapOf<Int, List<String>>()

    fun generateCutVerse() {
        val verseWordsPlain = _currentVerse.value?.verseTextPlain!!.split("\\s+".toRegex())
        val verseWordsNum = verseWordsPlain.size;

        val indices = (0 until verseWordsNum).toList().shuffled(Random)
        val hiddenIndices = if (verseWordsNum == 1) indices else indices.take(
            Math.ceil((verseWordsNum * 50) / 100.0).toInt()
        )
        println(verseWordsNum)
        println(hiddenIndices)
        hiddenIndices.forEach { index ->
            solutionMap.put(
                index,
                listOf(
                    verseWordsPlain.get(index),
                    verseWordsPlain.get(index).replace(Regex("[أإآ]"), "ا")
                )
            )
        }
        _inputSolutions.clear();
        _inputSolutions.putAll(solutionMap.keys.associateWith {
            TeacherAnswer(
                "",
                AnswerStatus.UNCHECKED
            )
        }.toMap(HashMap()))

        println(solutionMap)
    }

    private var _inputSolutions = mutableStateMapOf<Int, TeacherAnswer>()
    var inputSolutions: SnapshotStateMap<Int, TeacherAnswer>
        get() = _inputSolutions
        set(value: SnapshotStateMap<Int, TeacherAnswer>) {
            _inputSolutions = value
        }

    private val _isDownloading = mutableStateOf(false)
    var isDownloading: Boolean
        get() = _isDownloading.value
        set(value) {
            _isDownloading.value = value
        }

    private val _showInstructionsDialog = mutableStateOf(false)
    var showInstructionsDialog: Boolean
        get() = _showInstructionsDialog.value
        set(value) {
            _showInstructionsDialog.value = value
        }

    private val _isPlaying = mutableStateOf(false)
    var isPlaying: Boolean
        get() = _isPlaying.value
        set(value) {
            _isPlaying.value = value
        }

    var exoPlayer: ExoPlayer;

    fun downloadVerse(localFile: File) {
        if (!Helpers.checkNetworkConnectivity(context)) {
            Toast.makeText(context, "لا يوجد اتصال بالانترنت", Toast.LENGTH_LONG).show()
            return;
        }
        val tempFile = File.createTempFile("temp_", ".mp3", context.getExternalFilesDir(null))

        _isDownloading.value = true;

        val client = OkHttpClient()
        val request = Request.Builder()
            .url(
                "https://ottrojja.fra1.cdn.digitaloceanspaces.com/verses/${_currentVerse.value?.pageNum}-${_currentVerse.value?.surahNum}-${_currentVerse.value?.verseNum}.mp3"
            )
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
                        println("download successful")
                        withContext(Dispatchers.Main) {
                            playVerse()
                        }
                    }

                } catch (e: Exception) {
                    println("error in download")
                    e.printStackTrace()
                    reportException(exception = e, file = "TeacherScreenViewModel")
                    withContext(Dispatchers.Main) {
                        if (e.message?.contains("ENOSPC") == true) {
                            Toast.makeText(context, context.resources.getString(R.string.enospc), Toast.LENGTH_LONG).show()
                        } else {
                            Toast.makeText(context, "حدث خطأ اثناء التحميل", Toast.LENGTH_LONG).show()
                        }
                    }
                    localFile.delete()
                } finally {
                    if (tempFile.exists()) tempFile.delete()
                    _isDownloading.value = false;
                }
            }
        }
    }

    var length: Long = 0;

    fun playVerse() {
        if (length > 0) {
            exoPlayer.play()
            return;
        }
        val path = "${_currentVerse.value?.pageNum}-${_currentVerse.value?.surahNum}-${_currentVerse.value?.verseNum}.mp3"
        val localFile = File(
            context.getExternalFilesDir(null),
            path
        )
        if (!localFile.exists()) {
            println("need to download verse first")
            downloadVerse(localFile)
            return
        }

        Helpers.terminateAllServices(context)

        try {
            exoPlayer.apply {
                val mediaItem =
                    MediaItem.fromUri(Uri.fromFile(File(context.getExternalFilesDir(null), path)))
                setMediaItem(mediaItem)
                prepare()
                play()
            }
        } catch (e: Exception) {
            e.printStackTrace()
            reportException(exception = e, file = "TeacherScreenViewModel")
            Toast.makeText(context, "حصل خطأ، يرجى المحاولة مجددا", Toast.LENGTH_LONG)
                .show()
        }
    }

    fun pauseVerse() {
        length = exoPlayer.currentPosition;
        println(length)
        exoPlayer.pause()
    }

    fun resetMedia() {
        exoPlayer.stop()
        exoPlayer.clearMediaItems()
        _isPlaying.value = false;
        length = 0
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_STOP)
    fun onEnterBackground() {
        resetMedia();
    }

    fun releasePlayer() {
        exoPlayer.release()
    }

    init {
        // Observe the app's lifecycle using ProcessLifecycleOwner
        ProcessLifecycleOwner.get().lifecycle.addObserver(this)

        exoPlayer = ExoPlayer.Builder(context).build()

        exoPlayer.addListener(
            object : Player.Listener {
                override fun onIsPlayingChanged(isPlaying: Boolean) {
                    _isPlaying.value = isPlaying;
                }

                override fun onPlaybackStateChanged(playbackState: Int) {
                    super.onPlaybackStateChanged(playbackState)
                    if (playbackState == Player.STATE_ENDED) {
                        length = 0;
                    }
                }

                override fun onPlayerError(error: PlaybackException) {
                    super.onPlayerError(error)
                    error.printStackTrace()
                    Toast.makeText(context, "حصل خطأ، يرجى المحاولة مجددا", Toast.LENGTH_LONG)
                        .show()
                }
            }
        )
    }


    enum class TeacherMode {
        SELECTION, TRAINING, RESULTS
    }
}

class TeacherScreenViewModelFactory(
    private val repository: QuranRepository,
    private val application: Application
) : ViewModelProvider.AndroidViewModelFactory(application) {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(TeacherScreenViewModel::class.java)) {
            return TeacherScreenViewModel(repository, application) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}