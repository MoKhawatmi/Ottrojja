package com.ottrojja.screens.quranScreen

import JsonParser
import android.app.Application
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.media.MediaPlayer
import android.media.PlaybackParams
import android.widget.Toast
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.OnLifecycleEvent
import androidx.lifecycle.ProcessLifecycleOwner
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.ottrojja.classes.MediaPlayerService
import com.ottrojja.classes.PageContent
import com.ottrojja.classes.QuranPage
import com.ottrojja.classes.Helpers
import com.ottrojja.classes.QuranRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.FileOutputStream
import java.io.IOException


class QuranViewModel(private val repository: QuranRepository, application: Application) :
    AndroidViewModel(application), LifecycleObserver {
    val context = application.applicationContext;
    val sharedPreferences: SharedPreferences =
        application.getSharedPreferences("ottrojja", Context.MODE_PRIVATE)
    val jsonParser = JsonParser(application.applicationContext);


    private var _versesPlayList: Array<PageContent> by mutableStateOf(arrayOf<PageContent>());
    var mediaPlayer = MediaPlayer()
    var currentPlayingIndex = mutableStateOf(0)

    init {
        // Observe the app's lifecycle using ProcessLifecycleOwner
        ProcessLifecycleOwner.get().lifecycle.addObserver(this)
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_STOP)
    fun onEnterBackground() {
        resetPlayer();
    }

    private val _isPlaying = MutableStateFlow(false)
    val isPlaying: StateFlow<Boolean> = _isPlaying.asStateFlow()

    fun setIsPlaying(value: Boolean) {
        _isPlaying.value = value;
    }

    private var _continuousPlay = mutableStateOf(false)
    var continuousPlay: Boolean
        get() = _continuousPlay.value
        set(value: Boolean) {
            _continuousPlay.value = value
        }

    private var _shouldAutoPlay = mutableStateOf(false)
    var shouldAutoPlay: Boolean
        get() = _shouldAutoPlay.value
        set(value: Boolean) {
            _shouldAutoPlay.value = value
        }


    private var _tafseerSheetMode by mutableStateOf("tafseer")
    var tafseerSheetMode: String
        get() = _tafseerSheetMode
        set(value) {
            _tafseerSheetMode = value
        }

    private var _selectedTab by mutableStateOf("page")
    var selectedTab: String
        get() = _selectedTab
        set(value) {
            _selectedTab = value
        }

    fun setCurrentPage(value: String) {
        println("setting current page to $value")
        _selectedRepetition = "0"
        viewModelScope.launch(Dispatchers.IO) {
            try {
                _currentPageObject = repository.getPage(value)
                println(_currentPageObject)
                _versesPlayList = _currentPageObject.pageContent
                resetPlayer()
                val editor: SharedPreferences.Editor = sharedPreferences.edit()
                editor.putString("mostRecentPage", value)
                editor.apply()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    private var _currentPageObject by mutableStateOf(
        QuranPage(
            "1",
            "",
            arrayOf(""),
            arrayOf(""),
            arrayOf(""),
            arrayOf<PageContent>()
        )
    )

    var currentPageObject: QuranPage
        get() = this._currentPageObject
        set(value) {
            this._currentPageObject = value
        }

    val repetitionOptionsMap = linkedMapOf<String, Int>(
        "0" to 0,
        "1" to 1,
        "2" to 2,
        "3" to 3,
        "4" to 4,
        "5" to 5,
        "6" to 6,
        "7" to 7,
        "8" to 8,
        "9" to 9,
        "10" to 10,
        "بلا توقف" to Integer.MAX_VALUE, //smart, right?!
    )


    private var _selectedRepetition by mutableStateOf("0")
    var selectedRepetition: String
        get() = this._selectedRepetition
        set(value) {
            _selectedRepetition = value
        }

    fun updateSelectedRep() {
        val repValue: Int = repetitionOptionsMap.get(_selectedRepetition)!!
        if (repValue >= 0 && repValue < 10) {
            _selectedRepetition = "${repValue + 1}";
        } else {
            _selectedRepetition = "0"
        }
    }

    private var _selectedVerse by mutableStateOf<PageContent>(
        PageContent(
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
        )
    )
    var selectedVerse: PageContent
        get() = this._selectedVerse
        set(value) {
            _selectedVerse = value;
            mediaPlayer.reset();
            setIsPlaying(false)
            _selectedRepetition = "0"
            length = 0; //seek a better way for this length issue
            val index =
                getCurrentPageVerses().indexOf(getCurrentPageVerses().find { item -> item.surahNum == value.surahNum && item.verseNum == value.verseNum });
            if (index == -1) {
                currentPlayingIndex.value = 0;
            } else {
                currentPlayingIndex.value = index;
            }
        }

    private var _playbackSpeed by mutableStateOf(1.0f)
    var playbackSpeed: Float
        get() = _playbackSpeed
        set(value) {
            _playbackSpeed = value
        }

    fun increasePlaybackSpeed() {
        if (_playbackSpeed < 2.0f) {
            _playbackSpeed += 0.25f;
            updatePlaybackSpeed()
        }
    }

    fun decreasePlaybackSpeed() {
        if (_playbackSpeed > 0.25f) {
            _playbackSpeed -= 0.25f;
            updatePlaybackSpeed()
        }
    }

    fun updatePlaybackSpeed() {
        if (_isPlaying.value) {
            try {
                val playbackParams = PlaybackParams()
                playbackParams.speed = _playbackSpeed
                mediaPlayer.playbackParams = playbackParams;
            } catch (e: Exception) {
                e.printStackTrace()
                _playbackSpeed = 1.0f
                val playbackParams = PlaybackParams()
                playbackParams.speed = _playbackSpeed
                mediaPlayer.playbackParams = playbackParams;
            }
        }
    }


    private var _showVersesSheet by mutableStateOf(false)
    var showVersesSheet: Boolean
        get() = this._showVersesSheet
        set(value) {
            this._showVersesSheet = value
        }

    private var _tafseerTargetVerse by mutableStateOf("0-0")
    var tafseerTargetVerse: String
        get() = this._tafseerTargetVerse
        set(value) {
            viewModelScope.launch(Dispatchers.IO) {
                _tafseerTargetVerse = value
                val surah = value.split("-")[0]
                val verse = value.split("-")[1]

                println("tafseer for $surah-$verse at ${tafseerNamesMap.get(_selectedTafseer)}")

                _verseTafseer = repository.getVerseTafseerData(
                    surah,
                    verse,
                    tafseerNamesMap.get(_selectedTafseer)!!
                ).text
                _verseE3rab = repository.getVerseE3rabData(surah, verse).text
            }
        }

    private var _verseTafseer by mutableStateOf("")
    var verseTafseer: String
        get() = _verseTafseer
        set(value) {
            _verseTafseer = value
        }

    private var _verseE3rab by mutableStateOf("")
    var verseE3rab: String
        get() = _verseE3rab
        set(value) {
            _verseE3rab = value
        }

    private var _showTafseerSheet by mutableStateOf(false)
    var showTafseerSheet: Boolean
        get() = _showTafseerSheet
        set(value) {
            _showTafseerSheet = value
        }

    private var _showTafseerOptions by mutableStateOf(false)
    var showTafseerOptions: Boolean
        get() = this._showTafseerOptions
        set(value) {
            this._showTafseerOptions = value
        }

    private var _showRepOptions by mutableStateOf(false)
    var showRepOptions: Boolean
        get() = this._showRepOptions
        set(value) {
            this._showRepOptions = value
        }

    private var _showVerseOptions by mutableStateOf(false)
    var showVerseOptions: Boolean
        get() = this._showVerseOptions
        set(value) {
            this._showVerseOptions = value
        }

    val tafseerNamesMap = hashMapOf<String, String>(
        "تفسير البغوي" to "baghawy",
        "تفسير الجلالين" to "jalalayn",
        "تفسير ابن كثير" to "katheer",
        "التفسير الميسر" to "muyassar",
        "تفسير القرطبي" to "qortoby",
        "تفسير السعدي" to "saadi",
        "تفسير الوسيط" to "waseet",
    )

    private var _selectedTafseer by mutableStateOf("تفسير السعدي")
    var selectedTafseer: String
        get() = this._selectedTafseer
        set(value) {
            this._selectedTafseer = value
        }

    fun updateSelectedTafseer(value: String) {
        _selectedTafseer = value;
        viewModelScope.launch(Dispatchers.IO) {
            val surah = _tafseerTargetVerse.split("-")[0]
            val verse = _tafseerTargetVerse.split("-")[1]
            _verseTafseer = repository.getVerseTafseerData(
                surah,
                verse,
                tafseerNamesMap.get(value)!!
            ).text

        }
    }

    fun getCurrentPageVerses(): Array<PageContent> {
        val versesList = _currentPageObject.pageContent
        return versesList;
    }

    fun resetPlayer() {
        mediaPlayer.reset();
        currentPlayingIndex.value = 0;
        _selectedVerse =
            PageContent("", "", "", "", "", "", "", "");
        setIsPlaying(false);
        length = 0;
        repeatedTimes = 0;
        downloadIndex = 0;
        checkVerseFilesExistance();
    }


    var length = 0;
    var repeatedTimes = 0;

    private val _isDownloading = mutableStateOf(false)
    var isDownloading: Boolean
        get() = this._isDownloading.value
        set(value) {
            this._isDownloading.value = value
        }

    var allVersesExist = false;

    fun checkVerseFilesExistance() {
        allVersesExist = false;
        //check if all verse files exist, if not then download must be initialized
        println("checking verses of page ${currentPageObject.pageNum}");
        for (item in _versesPlayList) {
            var path: String;
            if (item.type == "surah") {
                path = "1-1-1.mp3"
            } else {
                path = "${currentPageObject.pageNum}-${item.surahNum}-${item.verseNum}.mp3"
            }
            val localFile = File(context.getExternalFilesDir(null), path)
            if (!localFile.exists()) {
                println("audio files for page ${currentPageObject.pageNum} need downloading")
                allVersesExist = false;
                return;
            }
        }
        println("audio files for page ${currentPageObject.pageNum} are complete")
        allVersesExist = true;
    }

    fun startPlaying() {
        val sr = Helpers.isMyServiceRunning(MediaPlayerService::class.java, context);
        println("service running $sr")
        if (sr) {
            val stopServiceIntent = Intent(context, MediaPlayerService::class.java)
            stopServiceIntent.setAction("TERMINATE")
            context.startService(stopServiceIntent)
        }
        _shouldAutoPlay.value = false;

        if (length != 0) {
            mediaPlayer.seekTo(length);
            mediaPlayer.start();
        } else {
            if (!allVersesExist) {
                if (Helpers.checkNetworkConnectivity(context)) {
                    initializeDownload()
                } else {
                    Toast.makeText(context, "لا يوجد اتصال بالانترنت", Toast.LENGTH_LONG)
                        .show()
                    return;
                }
            } else if (allVersesExist) {
                playAudio(_versesPlayList[currentPlayingIndex.value])
            }
        }
        setIsPlaying(true);
    }

    fun pausePlaying() {
        length = mediaPlayer.getCurrentPosition();
        mediaPlayer.pause()
        setIsPlaying(false);
    }

    fun goNextVerse() {
        if (currentPlayingIndex.value == _versesPlayList.size - 1) {
            return;
        }
        mediaPlayer.reset()
        currentPlayingIndex.value++;
        playAudio(_versesPlayList[currentPlayingIndex.value])
    }

    fun goPreviousVerse() {
        if (currentPlayingIndex.value == 0) {
            return;
        }
        mediaPlayer.reset()
        currentPlayingIndex.value--;
        playAudio(_versesPlayList[currentPlayingIndex.value])
    }


    private fun playAudio(item: PageContent) {
        val playbackParams = PlaybackParams()
        playbackParams.speed = this._playbackSpeed

        var urlParam: String;
        if (item.type == "surah") {
            urlParam = "1-1-1.mp3"
        } else {
            urlParam = "${currentPageObject.pageNum}-${item.surahNum}-${item.verseNum}.mp3"
        }
        // println(item.toString())

        if (item.type == "surah" && (item.surahNum == "1" || item.surahNum == "9")) {
            //skip basmallah for surah 1 and 9
            currentPlayingIndex.value++;
            playAudio(
                _versesPlayList[currentPlayingIndex.value]
            )
        } else {
            mediaPlayer.apply {
                reset()
                //page link
                //https://firebasestorage.googleapis.com/v0/b/ottrojja-238c0.appspot.com/o/pages%2F1.mp3?alt=media
                //setDataSource("https://firebasestorage.googleapis.com/v0/b/ottrojja-238c0.appspot.com/o/final%2F$urlParam.mp3?alt=media")
                //prepareAsync()
                setDataSource(
                    File(
                        context.getExternalFilesDir(null),
                        urlParam
                    ).absolutePath
                )
                setPlaybackParams(playbackParams)
                prepareAsync()
                setOnPreparedListener {
                    if (_isPlaying.value) {
                        println("current ${currentPlayingIndex.value}")
                        it.start()
                    }
                }
                setOnCompletionListener {
                    if (!_isPlaying.value) {
                    } else if (item.type == "verse" && repeatedTimes < repetitionOptionsMap.get(
                            _selectedRepetition
                        )!!
                    ) {
                        playAudio(
                            _versesPlayList[currentPlayingIndex.value]
                        )
                        repeatedTimes++;
                    } else if (currentPlayingIndex.value < _versesPlayList.size - 1) {
                        currentPlayingIndex.value++
                        repeatedTimes = 0;
                        playAudio(
                            _versesPlayList[currentPlayingIndex.value]
                        )
                    } else {
                        resetPlayer()
                        if (currentPageObject.pageNum != "604" && _continuousPlay.value) {
                            playNextPage()
                        }
                    }
                }
            }
        }
    }

    private fun playNextPage() {
        _shouldAutoPlay.value = true;
        setCurrentPage("${currentPageObject.pageNum.toInt() + 1}")
    }

    var downloadIndex = 0;

    fun initializeDownload() {
        _isDownloading.value = true;
        downloadVerse()
    }

    fun downloadVerse() {
        val item = _versesPlayList[downloadIndex];
        var path: String;
        if (item.type == "surah") {
            path = "1-1-1.mp3"
        } else {
            path = "${currentPageObject.pageNum}-${item.surahNum}-${item.verseNum}.mp3"
        }

        val localFile = File(
            context.getExternalFilesDir(null),
            path
        )
        val tempFile = File.createTempFile("temp_", ".mp3", context.getExternalFilesDir(null))


        val client = OkHttpClient()
        val request = Request.Builder()
            .url("https://ottrojja.fra1.cdn.digitaloceanspaces.com/verses/$path")
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
                        if (downloadIndex >= _versesPlayList.size - 1) {
                            allVersesExist = true;
                            _isDownloading.value = false;
                            playAudio(_versesPlayList[currentPlayingIndex.value])
                        } else {
                            downloadIndex++;
                            downloadVerse()
                        }
                    }
                } catch (e: Exception) {
                    println("error in download")
                    e.printStackTrace()
                    withContext(Dispatchers.Main) {
                        Toast.makeText(context, "حدث خطأ اثناء التحميل", Toast.LENGTH_LONG).show()
                    }
                    localFile.delete()
                    allVersesExist = false;
                    _isPlaying.value = false;
                    _isDownloading.value = false;
                } finally {
                    if (tempFile.exists()) tempFile.delete()
                }
            }
        }


        /* fileReference.getFile(localFile)
             .addOnSuccessListener {
                 // File downloaded successfully
                 // You can now play the file using MediaPlayer
                 //(localFile.absolutePath)
                 if (downloadIndex >= _versesPlayList.size - 1) {
                     isDownloading = false;
                     allVersesExist = true;
                     playAudio(_versesPlayList[currentPlayingIndex.value])
                 } else {
                     downloadIndex++;
                     downloadVerse(storageReference)
                 }
                 /*println(it.toString())
                 val mediaPlayer = MediaPlayer()
                 println("playing ${localFile.absolutePath}")
                 try {
                     mediaPlayer.setDataSource(localFile.absolutePath)
                     mediaPlayer.prepare()
                     mediaPlayer.start()
                 } catch (e: IOException) {
                     e.printStackTrace()
                 }*/

                 //playPage();
             }
             .addOnFailureListener {
                 // Handle failure
                 println("/final/${currentPage.value}-${item.surahNum}-${item.verseNum}.mp3")
                 println("error in download")
                 isDownloading = false;
                 allVersesExist = false;
             }*/
    }

    private val _isBookmarked = mutableStateOf(false)
    var isBookmarked: Boolean
        get() = this._isBookmarked.value
        set(value) {
            this._isBookmarked.value = value
        }

    fun isPageBookmarked() {
        val bookmarks = sharedPreferences.getString("bookmarks", "");
        val bookmarksList = bookmarks?.split(",");
        println("checking bookmarks")
        println(bookmarksList)
        println("for ${currentPageObject.pageNum}")
        if (bookmarksList?.size == 0) {
            this._isBookmarked.value = false;
        } else {
            this._isBookmarked.value = bookmarksList?.indexOf(currentPageObject.pageNum) != -1;
        }
    }

    fun togglePageBookmark() {
        val bookmarks = sharedPreferences.getString("bookmarks", "");
        val bookmarksList = bookmarks?.split(",")?.toMutableList();
        if (bookmarksList?.size == 0) {
            bookmarksList.add(currentPageObject.pageNum)
        } else {
            if (bookmarksList?.indexOf(currentPageObject.pageNum) == -1) {
                bookmarksList.add(currentPageObject.pageNum)
            } else {
                bookmarksList?.remove(currentPageObject.pageNum)
            }
        }

        val editor: SharedPreferences.Editor = sharedPreferences.edit()
        editor.putString("bookmarks", bookmarksList?.joinToString(","))
        editor.apply()

        Toast.makeText(context, " تم تحديث المرجعيات بنجاح", Toast.LENGTH_LONG).show()

        //just to update ui
        isPageBookmarked();
    }


}

class QuranScreenViewModelFactory(
    private val repository: QuranRepository,
    private val application: Application
) : ViewModelProvider.AndroidViewModelFactory(application) {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(QuranViewModel::class.java)) {
            return QuranViewModel(repository, application) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}