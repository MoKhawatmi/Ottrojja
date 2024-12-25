package com.ottrojja.screens.teacherScreen

import android.app.Application
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
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
import com.ottrojja.classes.AnswerStatus
import com.ottrojja.classes.Helpers
import com.ottrojja.classes.Helpers.convertToArabicNumbers
import com.ottrojja.services.MediaPlayerService
import com.ottrojja.classes.PageContent
import com.ottrojja.classes.PageContentItemType
import com.ottrojja.classes.QuranPage
import com.ottrojja.classes.QuranRepository
import com.ottrojja.classes.TeacherAnswer
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

    private val pagesList: List<String> = (1..604).map { "صفحة $it" };

    private val _selectedPage = mutableStateOf(
        QuranPage(
            "",
            "",
            arrayOf(""),
            arrayOf(""),
            arrayOf(""),
            arrayOf<PageContent>(),
        )
    )
    var selectedPage: QuranPage
        get() = _selectedPage.value
        set(value: QuranPage) {
            _selectedPage.value = value
        }

    private val _selectedPageVerses = mutableStateOf(
        emptyList<PageContent>()
    )
    var selectedPageVerses: List<PageContent>
        get() = _selectedPageVerses.value
        set(value: List<PageContent>) {
            _selectedPageVerses.value = value
        }


    private val _currentVerse = mutableStateOf(PageContent())
    var currentVerse: PageContent
        get() = _currentVerse.value
        set(value: PageContent) {
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


    private val _mode = mutableStateOf(TeacherMode.PAGE_SELECTION)
    var mode: TeacherMode
        get() = _mode.value
        set(value) {
            _mode.value = value
        }


    private var _searchFilter by mutableStateOf("")
    var searchFilter: String
        get() = this._searchFilter
        set(value) {
            this._searchFilter = value
        }

    val maxTries = 3;

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

    fun getPagesList(): List<String> {
        return pagesList.filter { page ->
            page.contains(_searchFilter) || page.contains(convertToArabicNumbers(_searchFilter))
        };
    }

    fun pageSelected(pageNum: String) {
        viewModelScope.launch(Dispatchers.IO) {
            _selectedPage.value = repository.getPage(pageNum)
            _selectedPageVerses.value =
                _selectedPage.value.pageContent.filter { it.type == PageContentItemType.verse }.toList()
            println(_selectedPage.value)
            println(_selectedPageVerses.value)
            _currentVerseIndex.value = 0
            _currentVerse.value = _selectedPageVerses.value[_currentVerseIndex.value]
            checkLastVerseReached()
            _mode.value = TeacherMode.PAGE_TRAINING;
        }
    }

    fun startTeaching() {
        _hasStarted.value = true;
        println("vm ${_lastVerseReached.value}")
        generateCutVerse();
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
        } else if (!allRight) {

            if (_reachedMaxTries.value) {

            }
        }

        if (_currentTry.value == maxTries) {
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
            _currentVerse.value = _selectedPageVerses.value[_currentVerseIndex.value]
            generateCutVerse()
        }
        checkLastVerseReached()
    }

    fun checkLastVerseReached() {
        if (_currentVerseIndex.value >= _selectedPageVerses.value.size - 1) {
            _lastVerseReached.value = true;
        }
    }

    fun backToPages() {
        _mode.value = TeacherMode.PAGE_SELECTION
        resetAll()
    }

    fun resetAll() {
        _inputSolutions.clear();
        solutionMap.clear()
        _reachedMaxTries.value = false;
        _currentTry.value = 0;
        _allRight.value = false;
        _currentVerseIndex.value = 0;
        _selectedPage.value = QuranPage(
            "",
            "",
            arrayOf(""),
            arrayOf(""),
            arrayOf(""),
            arrayOf<PageContent>(),
        )
        _selectedPageVerses.value = emptyList<PageContent>();
        _hasStarted.value = false;
        _correctVersesAnswered.value = 0;
        _lastVerseReached.value = false;
        resetMedia()
    }

    val solutionMap = hashMapOf<Int, List<String>>()

    fun generateCutVerse() {
        val verseWordsPlain = _currentVerse.value.verseTextPlain.split("\\s+".toRegex())
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

    // i really don't understand how this works
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


    //private var _mediaPlayer = MediaPlayer()

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
            .url("https://ottrojja.fra1.cdn.digitaloceanspaces.com/verses/${_selectedPage.value.pageNum}-${_currentVerse.value.surahNum}-${_currentVerse.value.verseNum}.mp3")
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

    var length: Long = 0;

    fun playVerse() {
        if (length > 0) {
            exoPlayer.play()
            //_isPlaying.value = true;
            return;
        }
        val path =
            "${_selectedPage.value.pageNum}-${_currentVerse.value.surahNum}-${_currentVerse.value.verseNum}.mp3"
        val localFile = File(
            context.getExternalFilesDir(null),
            path
        )
        if (!localFile.exists()) {
            println("need to download verse first")
            downloadVerse(localFile)
            return
        }
        val sr = Helpers.isMyServiceRunning(MediaPlayerService::class.java, context);
        println("service running $sr")
        if (sr) {
            val stopServiceIntent = Intent(context, MediaPlayerService::class.java)
            stopServiceIntent.setAction("TERMINATE")
            context.startService(stopServiceIntent)
        }

        exoPlayer.apply {
            val mediaItem =
                MediaItem.fromUri(Uri.fromFile(File(context.getExternalFilesDir(null), path)))
            setMediaItem(mediaItem)
            prepare()
            play()
        }


        /*_mediaPlayer.apply {
            reset()
            setDataSource(
                File(
                    context.getExternalFilesDir(null),
                    path
                ).absolutePath
            )
            setPlaybackParams(playbackParams)
            prepareAsync()
            setOnPreparedListener {
                _isPlaying.value = true;
                it.start()
            }
            setOnCompletionListener {
                _isPlaying.value = false;
                length = 0
            }
        }*/
    }

    fun pauseVerse() {
        length = exoPlayer.currentPosition;
        println(length)
        //_mediaPlayer.pause()
        exoPlayer.pause()
        // _isPlaying.value = false;
    }

    fun resetMedia() {
        //_mediaPlayer.reset()
        exoPlayer.stop()
        exoPlayer.clearMediaItems()
        _isPlaying.value = false;
        length = 0
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_STOP)
    fun onEnterBackground() {
        resetMedia();
    }

    fun releasePlayer(){
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
                    /*if (isPlaying) {
                        // Active playback.
                    } else {
                        // Not playing because playback is paused, ended, suppressed, or the player
                        // is buffering, stopped or failed. Check player.playWhenReady,
                        // player.playbackState, player.playbackSuppressionReason and
                        // player.playerError for details.
                    }*/
                }

                override fun onPlaybackStateChanged(playbackState: Int) {
                    super.onPlaybackStateChanged(playbackState)
                    if (playbackState == Player.STATE_ENDED) {
                        length = 0;
                    }
                }

                override fun onPlayerError(error: PlaybackException) {
                    super.onPlayerError(error)
                    println(error.printStackTrace())
                    Toast.makeText(context, "حصل خطأ، يرجى المحاولة مجددا", Toast.LENGTH_LONG)
                        .show()
                }
            }
        )
    }

    enum class TeacherMode {
        PAGE_SELECTION, PAGE_TRAINING
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