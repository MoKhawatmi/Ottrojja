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
import androidx.room.Room
import com.ottrojja.classes.MediaPlayerService
import com.ottrojja.classes.PageContent
import com.ottrojja.classes.QuranPage
import com.ottrojja.classes.QuranStore
import com.ottrojja.classes.Helpers
import com.ottrojja.classes.QuranRepository
import com.ottrojja.room.QuranDatabase
import com.ottrojja.screens.loadingScreen.LoadingScreenViewModel
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


class QuranViewModel(repository: QuranRepository, application: Application) : AndroidViewModel(application), LifecycleObserver {
    val context = application.applicationContext;
    val sharedPreferences: SharedPreferences =
        application.getSharedPreferences("ottrojja", Context.MODE_PRIVATE)
    val jsonParser = JsonParser(application.applicationContext);

    //val tafseerData: List<TafseerData> = QuranStore.getTafseerData();
    var tafseerData: List<TafseerData> = QuranStore.tafseerData;
    val e3rabData: List<E3rabData> = QuranStore.getE3rabData();

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
        get() = this._tafseerSheetMode
        set(value) {
            this._tafseerSheetMode = value
        }

    private var _selectedTab by mutableStateOf("page")
    var selectedTab: String
        get() = this._selectedTab
        set(value) {
            this._selectedTab = value
        }

    private val _currentPage = MutableStateFlow("1")
    val currentPage: StateFlow<String> = _currentPage.asStateFlow()

    fun setCurrentPage(value: String) {
        _currentPage.value = value
        resetPlayer()
        this._selectedRepetition = "0"
        checkVerseFilesExistance()
        _currentPageObject = QuranStore.getQuranData().get(Integer.parseInt(value) - 1)
    }

    private var _currentPageObject by mutableStateOf(
        QuranPage(
            "",
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

    val repetitionOptionsMap = hashMapOf<String, Int>(
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
            this._selectedRepetition = value
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
            this._selectedVerse = value;
            mediaPlayer.reset();
            setIsPlaying(false)
            this._selectedRepetition = "0"
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
        get() = this._playbackSpeed
        set(value) {
            this._playbackSpeed = value
        }

    fun increasePlaybackSpeed() {
        if (_playbackSpeed < 2.0f) {
            this._playbackSpeed += 0.25f;
            updatePlaybackSpeed()
        }
    }

    fun decreasePlaybackSpeed() {
        if (_playbackSpeed > 0.25f) {
            this._playbackSpeed -= 0.25f;
            updatePlaybackSpeed()
        }
    }

    fun updatePlaybackSpeed() {
        if (_isPlaying.value) {
            val playbackParams = PlaybackParams()
            playbackParams.speed = this._playbackSpeed
            mediaPlayer.playbackParams = playbackParams;
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
            this._tafseerTargetVerse = value
            val surah = value.split("-")[0]
            val verse = value.split("-")[1]
            this._verseTafseer =
                tafseerData.find { item -> item.sura == surah && item.aya == verse }!!.text
            this._verseE3rab =
                e3rabData.find { item -> item.sura == surah && item.aya == verse }!!.text
        }

    private var _verseTafseer by mutableStateOf("Tafseer")
    var verseTafseer: String
        get() = this._verseTafseer
        set(value) {
            this._verseTafseer = value
        }

    private var _verseE3rab by mutableStateOf("E3rab")
    var verseE3rab: String
        get() = this._verseE3rab
        set(value) {
            this._verseE3rab = value
        }


    private var _showTafseerSheet by mutableStateOf(false)
    var showTafseerSheet: Boolean
        get() = this._showTafseerSheet
        set(value) {
            this._showTafseerSheet = value
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
        "تفسير السعدي" to "tafseer",
        "تفسير الوسيط" to "waseet",
    )

    private var _selectedTafseer by mutableStateOf("تفسير السعدي")
    var selectedTafseer: String
        get() = this._selectedTafseer
        set(value) {
            this._selectedTafseer = value
        }

    fun updateSelectedTafseer(value: String) {
        this._selectedTafseer = value;
        jsonParser.parseJsonArrayFileTafseer("${tafseerNamesMap.get(value).toString()}.json")
            ?.let { tafseerData = it }
        val surah = this._tafseerTargetVerse.split("-")[0]
        val verse = this._tafseerTargetVerse.split("-")[1]
        this._verseTafseer =
            tafseerData.find { item -> item.sura == surah && item.aya == verse }!!.text
    }

    fun getCurrentPageVerses(): Array<PageContent> {
        val versesList = _currentPageObject.pageContent
        //      QuranStore.getQuranData()[Integer.parseInt(_currentPage.value) - 1].pageContent;
        return versesList;
    }

    fun resetPlayer() {
        mediaPlayer.reset();
        _versesPlayList = _currentPageObject.pageContent
        //    QuranStore.getQuranData()[Integer.parseInt(_currentPage.value) - 1].pageContent//getCurrentPageVersesUrls();
        currentPlayingIndex.value = 0;
        _selectedVerse =
            PageContent("", "", "", "", "", "", "", "")//getCurrentPageVerses().first();
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
        println("checking verses of page ${currentPage.value}");
        for (item in _versesPlayList) {
            var path: String;
            if (item.type == "surah") {
                path = "1-1-1.mp3"
            } else {
                path = "${currentPage.value}-${item.surahNum}-${item.verseNum}.mp3"
            }
            val localFile = File(context.getExternalFilesDir(null), path)
            if (!localFile.exists()) {
                println("audio files for page ${_currentPage.value} need downloading")
                allVersesExist = false;
                return;
            }
        }
        println("audio files for page ${_currentPage.value} are complete")
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
        setIsPlaying(true);
        if (length != 0) {
            mediaPlayer.seekTo(length);
            mediaPlayer.start();
        } else {
            if (!allVersesExist) {
                initializeDownload()
            } else if (allVersesExist) {
                playAudio(_versesPlayList[currentPlayingIndex.value])
            }
            /*
            println("index ${currentPlayingIndex.value}")
            println(_versesPlayList[currentPlayingIndex.value])
          */
        }
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
            urlParam = "${_currentPage.value}-${item.surahNum}-${item.verseNum}.mp3"
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
                        if (_currentPage.value != "604" && _continuousPlay.value) {
                            playNextPage()
                        }
                    }
                }
            }
        }
    }

    private fun playNextPage() {
        _shouldAutoPlay.value = true;
        setCurrentPage("${_currentPage.value.toInt() + 1}")
    }

    var downloadIndex = 0;

    fun initializeDownload() {
        _isDownloading.value = true;
        // val storage = FirebaseStorage.getInstance()
        // val storageReference = storage.getReference()
        downloadVerse(/*storageReference*/)
    }

    fun downloadVerse(/*storageReference: StorageReference*/) {
        val item = _versesPlayList[downloadIndex];
        var path: String;
        if (item.type == "surah") {
            path = "1-1-1.mp3"
        } else {
            path = "${currentPage.value}-${item.surahNum}-${item.verseNum}.mp3"
        }
        //   val fileReference = storageReference.child("/final/$path")

        val localFile = File(
            context.getExternalFilesDir(null),
            path
        )

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
                            FileOutputStream(localFile).use { outputStream ->
                                responseBody.byteStream().use { inputStream ->
                                    inputStream.copyTo(outputStream)

                                    if (downloadIndex >= _versesPlayList.size - 1) {
                                        isDownloading = false;
                                        allVersesExist = true;
                                        playAudio(_versesPlayList[currentPlayingIndex.value])
                                    } else {
                                        downloadIndex++;
                                        downloadVerse(/*storageReference*/)
                                    }
                                }
                            }
                        }
                    }
                } catch (e: Exception) {
                    println("error in download")
                    println(e)
                    withContext(Dispatchers.Main) {
                        Toast.makeText(context, "حدث خطأ اثناء التحميل", Toast.LENGTH_LONG).show()
                    }
                    isDownloading = false;
                    allVersesExist = false;
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
        println("checking")
        println(bookmarksList)
        println("for ${_currentPage.value}")
        if (bookmarksList?.size == 0) {
            this._isBookmarked.value = false;
        } else {
            this._isBookmarked.value = bookmarksList?.indexOf(_currentPage.value) != -1;
        }
    }

    fun togglePageBookmark() {
        val bookmarks = sharedPreferences.getString("bookmarks", "");
        val bookmarksList = bookmarks?.split(",")?.toMutableList();
        if (bookmarksList?.size == 0) {
            bookmarksList.add(_currentPage.value)
        } else {
            if (bookmarksList?.indexOf(_currentPage.value) == -1) {
                bookmarksList.add(_currentPage.value)
            } else {
                bookmarksList?.remove(_currentPage.value)
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