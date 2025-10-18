package com.ottrojja.screens.quranScreen

import android.app.Application
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.content.SharedPreferences
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.IBinder
import android.widget.Toast
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.core.content.FileProvider
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.ottrojja.R
import com.ottrojja.room.entities.PageContent
import com.ottrojja.classes.Helpers
import com.ottrojja.classes.Helpers.isMyServiceRunning
import com.ottrojja.classes.Helpers.reportException
import com.ottrojja.classes.Helpers.terminateAllServices
import com.ottrojja.room.entities.PageContentItemType
import com.ottrojja.classes.QuranRepository
import com.ottrojja.room.entities.BookmarkEntity
import com.ottrojja.room.entities.Khitmah
import com.ottrojja.room.entities.KhitmahMark
import com.ottrojja.room.relations.QuranPageWithContent
import com.ottrojja.services.PagePlayerService
import com.ottrojja.services.PageServiceInterface
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.FileOutputStream
import java.io.IOException


class QuranViewModel(private val repository: QuranRepository, application: Application) :
    AndroidViewModel(application) {
    val context = application.applicationContext;
    val sharedPreferences: SharedPreferences = application.getSharedPreferences(
        "ottrojja",
        Context.MODE_PRIVATE
    )

    val quranPagesNumbers = Array(604) { (it + 1).toString() }

    private var _versesPlayList: Array<PageContent> = emptyArray();

    private var _isPlaying = mutableStateOf(false)
    var isPlaying: Boolean
        get() = _isPlaying.value
        set(value: Boolean) {
            _isPlaying.value = value
        }

    private var _isCurrentPagePlaying = mutableStateOf(true)
    var isCurrentPagePlaying: Boolean
        get() = _isCurrentPagePlaying.value
        set(value: Boolean) {
            _isCurrentPagePlaying.value = value
        }

    private var _continuousPlay = mutableStateOf(false)
    var continuousPlay: Boolean
        get() = _continuousPlay.value
        set(value: Boolean) {
            _continuousPlay.value = value
            updateServicePlayingParameters()
        }

    private var _shouldAutoPlay = mutableStateOf(false)
    var shouldAutoPlay: Boolean
        get() = _shouldAutoPlay.value
        set(value: Boolean) {
            _shouldAutoPlay.value = value
        }

    private var _tafseerSheetMode by mutableStateOf(TafseerSheetMode.التفسير)
    var tafseerSheetMode: TafseerSheetMode
        get() = _tafseerSheetMode
        set(value) {
            _tafseerSheetMode = value
        }

    private var _selectedTab by mutableStateOf(PageTab.الصفحة)
    var selectedTab: PageTab
        get() = _selectedTab
        set(value) {
            _selectedTab = value
        }

    fun setCurrentPage(value: String) {
        println("setting current page to $value")
        viewModelScope.launch(Dispatchers.IO) {
            try {
                _currentPageObject = repository.getPage(value)
                println(_currentPageObject)
                _versesPlayList = _currentPageObject?.pageContent!!.toTypedArray()
                //the following couple of lines are made to accommodate the service changes
                resetPlayingUIParams()
                if (isMyServiceRunning(PagePlayerService::class.java, context)) {
                    println("service running")
                    updateIsCurrentPagePlaying()
                    if (_isCurrentPagePlaying.value) {
                        fetchPlayingUIParams()
                    }
                }
                checkVerseFilesExistance()
                //done accommodate
                isPageBookmarked()
                val editor: SharedPreferences.Editor = sharedPreferences.edit()
                editor.putString("mostRecentPage", value)
                editor.apply()
            } catch (e: Exception) {
                e.printStackTrace()
                reportException(exception = e, file = "QuranViewModel")
            }
        }
    }

    private var _currentPageObject by mutableStateOf<QuranPageWithContent?>(null)
    var currentPageObject: QuranPageWithContent?
        get() = _currentPageObject
        set(value) {
            _currentPageObject = value
        }


    private var _selectedRepetition by mutableStateOf("0")
    var selectedRepetition: String
        get() = _selectedRepetition
        set(value) {
            _selectedRepetition = value
            updateServicePlayingParameters()
        }

    /*fun updateSelectedRep() {
        val repValue: Int = repetitionOptionsMap.get(_selectedRepetition)!!
        if (repValue >= 0 && repValue < 10) {
            _selectedRepetition = "${repValue + 1}";
        } else {
            _selectedRepetition = "0"
        }
    }*/


    var startPlayingIndex: Int? = null;
    var endPlayingIndex: Int? = null;
    private var _selectedVerse by mutableStateOf<PageContent?>(null)
    var selectedVerse: PageContent?
        get() = _selectedVerse
        set(value) {
            val pageVerses: List<PageContent> = getCurrentPageVerses();
            val index = pageVerses.indexOf(
                pageVerses.find { item -> item.surahNum == value?.surahNum && item.verseNum == value.verseNum });
            if (index > (endPlayingIndex ?: (pageVerses.size - 1))) {
                Toast.makeText(
                    context,
                    "موضع اية البداية يجب ان لا يكون بعد موضع اية النهاية",
                    Toast.LENGTH_LONG
                ).show()
                return;
            }
            _selectedVerse = value;
            if (_isCurrentPagePlaying.value) {
                audioService?.playingParameterUpdated();
            }
            if (index == -1) {
                startPlayingIndex = 0;
                updateServicePlayingIndex()
            } else {
                startPlayingIndex = index;
                updateServicePlayingIndex()
            }
        }

    private var _selectedEndVerse by mutableStateOf<PageContent?>(null)
    var selectedEndVerse: PageContent?
        get() = _selectedEndVerse
        set(value) {
            val pageVerses: List<PageContent> = getCurrentPageVerses();
            val index = pageVerses.indexOf(
                pageVerses.find { item -> item.surahNum == value?.surahNum && item.verseNum == value.verseNum });
            if (index < (startPlayingIndex ?: 0)) {
                Toast.makeText(
                    context,
                    "موضع اية النهاية يجب ان لا يكون قبل موضع اية البداية",
                    Toast.LENGTH_LONG
                ).show()
                return;
            }
            _selectedEndVerse = value;
            if (_isCurrentPagePlaying.value) {
                audioService?.playingParameterUpdated();
            }
            if (index == -1) {
                Toast.makeText(context, "حصل خطأ يرجى المحاولة لاحقا", Toast.LENGTH_LONG).show()
                endPlayingIndex = null;
                updateServicePlayingParameters();
            } else {
                endPlayingIndex = index;
                updateServicePlayingParameters();
            }
        }

    private var _selectingEndVerse by mutableStateOf(false)
    var selectingEndVerse: Boolean
        get() = _selectingEndVerse
        set(value) {
            _selectingEndVerse = value
        }

    private var _playbackSpeed by mutableStateOf(1.0f)
    var playbackSpeed: Float
        get() = _playbackSpeed
        set(value) {
            _playbackSpeed = value
        }

    fun increasePlaybackSpeed() {
        audioService?.increaseSpeed();
    }

    fun decreasePlaybackSpeed() {
        audioService?.decreaseSpeed();
    }

    private var _showRepOptions by mutableStateOf(false)
    var showRepOptions: Boolean
        get() = _showRepOptions
        set(value) {
            _showRepOptions = value
        }

    private var _showVerseOptions by mutableStateOf(false)
    var showVerseOptions: Boolean
        get() = _showVerseOptions
        set(value) {
            _showVerseOptions = value
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
        get() = _selectedTafseer
        set(value) {
            _selectedTafseer = value
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

    fun getCurrentPageVerses(): List<PageContent> {
        val versesList = _currentPageObject?.pageContent!!
        return versesList;
    }

    private val _isDownloading = mutableStateOf(false)
    var isDownloading: Boolean
        get() = _isDownloading.value
        set(value) {
            _isDownloading.value = value
        }

    var allVersesExist = false;

    fun checkVerseFilesExistance() {
        allVersesExist = false;
        //check if all verse files exist, if not then download must be initialized
        println("checking verses of page ${currentPageObject?.page?.pageNum}");
        for (item in _versesPlayList) {
            var path: String;
            if (item.type == PageContentItemType.surah) {
                path = "1-1-1.mp3"
            } else {
                path = "${currentPageObject?.page?.pageNum}-${item.surahNum}-${item.verseNum}.mp3"
            }
            val localFile = File(context.getExternalFilesDir(null), path)
            if (!localFile.exists()) {
                println("audio files for page ${currentPageObject?.page?.pageNum} need downloading")
                allVersesExist = false;
                return;
            }
        }
        println("audio files for page ${currentPageObject?.page?.pageNum} are complete")
        allVersesExist = true;
    }

    fun prepareForPlaying() {
        // stop other services
        terminateAllServices(context, PagePlayerService::class.java)
        _shouldAutoPlay.value = false;

        if (!allVersesExist) {
            if (Helpers.checkNetworkConnectivity(context)) {
                initializeDownload()
            } else {
                Toast.makeText(context, "لا يوجد اتصال بالانترنت", Toast.LENGTH_LONG)
                    .show()
                return;
            }
        } else if (allVersesExist) {
            startPlaying()
        }
    }

    var clickedPlay = false;
    fun startPlaying() {
        if (isMyServiceRunning(PagePlayerService::class.java, context)) {
            if (!_isCurrentPagePlaying.value) {
                println("preparing for a new page")
                audioService?.resetPlayer();
                audioService?.setPlayingPageNum(_currentPageObject?.page?.pageNum!!)
                updateIsCurrentPagePlaying();
                updateServicePlayingParameters();
                updateServicePlayingIndex();
            }
            updateServicePageValues()
            audioService?.playAudio();
        } else {
            clickedPlay = true;
            startAndBind();
        }
    }

    fun pausePlaying() {
        audioService?.pause();
    }

    fun goNextVerse() {
        audioService?.playNextVerse();
    }

    fun goPreviousVerse() {
        audioService?.playPreviousVerse();
    }

    private var _vmChangedPage = mutableStateOf(false)
    var vmChangedPage: Boolean
        get() = _vmChangedPage.value;
        set(value: Boolean) {
            _vmChangedPage.value = value;
        }

    private fun playNextPage() {
        _shouldAutoPlay.value = true;
        val nextPageToPlay = audioService?.getPlayingPageNum()?.toInt()?.plus(1)
        println("playNextPage $nextPageToPlay")
        _vmChangedPage.value = true;
        setCurrentPage("$nextPageToPlay")
        //setCurrentPage("${currentPageObject.pageNum.toInt() + 1}")
    }

    var downloadIndex = 0;

    fun initializeDownload() {
        downloadIndex = 0;
        _isDownloading.value = true;
        downloadVerse()
    }

    fun updateIsCurrentPagePlaying() {
        println("${audioService?.getPlayingPageNum()} =? ${_currentPageObject?.page?.pageNum}")
        _isCurrentPagePlaying.value = audioService?.getPlayingPageNum() == _currentPageObject?.page?.pageNum
        println("_isCurrentPagePlaying ${_isCurrentPagePlaying.value}")
    }

    fun downloadVerse() {
        val item = _versesPlayList[downloadIndex];
        var path: String;
        if (item.type == PageContentItemType.surah) {
            path = "1-1-1.mp3"
        } else {
            path = "${currentPageObject?.page?.pageNum}-${item.surahNum}-${item.verseNum}.mp3"
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
                            withContext(Dispatchers.Main) {
                                startPlaying()
                            }
                        } else {
                            downloadIndex++;
                            downloadVerse()
                        }
                    }
                } catch (e: Exception) {
                    println("error in download")
                    e.printStackTrace()
                    reportException(exception = e, file = "QuranViewModel")
                    withContext(Dispatchers.Main) {
                        if (e.message?.contains("ENOSPC") == true) {
                            Toast.makeText(context, context.resources.getString(R.string.enospc), Toast.LENGTH_LONG).show()
                        } else {
                            Toast.makeText(context, "حدث خطأ اثناء التحميل", Toast.LENGTH_LONG).show()
                        }
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
    }

    private val _isBookmarked = mutableStateOf(false)
    var isBookmarked: Boolean
        get() = _isBookmarked.value
        set(value) {
            _isBookmarked.value = value
        }

    fun isPageBookmarked() {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                println("checking bookmarks for ${currentPageObject?.page?.pageNum}")
                _isBookmarked.value = repository.isBookmarked(currentPageObject?.page?.pageNum!!)
            } catch (e: Exception) {
                e.printStackTrace()
                reportException(exception = e, file = "QuranViewModel")
            }
        }
    }

    fun togglePageBookmark() {
        val bookmark = BookmarkEntity(pageNum = currentPageObject?.page?.pageNum!!);
        viewModelScope.launch(Dispatchers.IO) {
            try {
                if (!_isBookmarked.value) {
                    repository.insertBookmark(bookmark)
                } else {
                    repository.deleteBookmark(bookmark)
                }
                withContext(Dispatchers.Main) {
                    Toast.makeText(context, " تم تحديث المرجعيات بنجاح", Toast.LENGTH_LONG).show()
                }
                //just to update ui
                isPageBookmarked();
            } catch (e: Exception) {
                e.printStackTrace()
                reportException(exception = e, file = "QuranViewModel")
                withContext(Dispatchers.Main) {
                    Toast.makeText(context, "حصل خطأ يرجى المحاولة لاحقا", Toast.LENGTH_LONG).show()
                }
            }
        }
    }

    private var _nightReadingMode by mutableStateOf(false)
    var nightReadingMode: Boolean
        get() = _nightReadingMode
        set(value) {
            _nightReadingMode = value
        }


    fun getNightReadingMode() {
        _nightReadingMode = sharedPreferences.getBoolean("nightReadingMode", false)
    }

    enum class PageTab {
        الصفحة,
        الفوائد,
        الآيات,
        الفيديو
    }

    fun sharePage() {
        val varName = "p_${_currentPageObject?.page?.pageNum}"
        val resourceId = context.resources.getIdentifier(varName, "drawable", context.packageName)
        val bitmap = BitmapFactory.decodeResource(context.resources, resourceId)
        val fileName = "image.png"
        val file = File(context.filesDir, fileName)
        try {
            FileOutputStream(file).use { fileOutputStream ->
                bitmap.compress(Bitmap.CompressFormat.PNG, 100, fileOutputStream)
            }
        } catch (e: IOException) {
            e.printStackTrace()
            reportException(exception = e, file = "QuranViewModel")
            return
        }
        val imageUri = FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileprovider",
            file
        )
        val shareIntent = Intent(Intent.ACTION_SEND).apply {
            addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_GRANT_READ_URI_PERMISSION)
            type = "image/*"
            putExtra(Intent.EXTRA_STREAM, imageUri)
            putExtra(Intent.EXTRA_TITLE, "تطبيق اترجة")
            putExtra(
                Intent.EXTRA_TEXT,
                "الصفحة رقم ${_currentPageObject?.page?.pageNum} من القرآن الكريم\n${
                    context.resources.getString(R.string.share_app)
                }"
            )
        }
        val chooserIntent = Intent.createChooser(shareIntent, "تطبيق اترجة").apply {
            putExtra(Intent.EXTRA_TITLE, "تطبيق اترجة")
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        context.startActivity(chooserIntent)
        file.deleteOnExit()
    }


    private var _showListeningOptionsDialog = mutableStateOf(false)
    var showListeningOptionsDialog: Boolean
        get() = _showListeningOptionsDialog.value
        set(value) {
            _showListeningOptionsDialog.value = value
        }

    private var _selectedRepetitionTab = mutableStateOf(RepetitionTab.الاية)
    var selectedRepetitionTab: RepetitionTab
        get() = _selectedRepetitionTab.value
        set(value) {
            _selectedRepetitionTab.value = value
            updateServicePlayingParameters()
            if (_isCurrentPagePlaying.value) {
                audioService?.playingParameterUpdated();
            }
        }

    /*****************************TAFSEER MODAL CONTROL***********************************/

    private var _tafseerChapterVerse by mutableStateOf("")
    val tafseerChapterVerse: String
        get() = _tafseerChapterVerse

    private var _tafseerTargetVerse by mutableStateOf("0-0")
    var tafseerTargetVerse: String
        get() = _tafseerTargetVerse
        set(value) {
            viewModelScope.launch(Dispatchers.IO) {
                _tafseerTargetVerse = value
                val surah = value.split("-")[0]
                val verse = value.split("-")[1]
                val chapterName = repository.getChapter(surah.toInt()).chapterName
                if (chapterName.isNotBlank()) {
                    _tafseerChapterVerse = "الاية ${Helpers.convertToIndianNumbers(verse)} من سورة $chapterName"
                } else {
                    _tafseerChapterVerse = ""
                }
                println(value)
                println("tafseer for $surah-$verse at ${tafseerNamesMap.get(_selectedTafseer)}")

                _verseTafseer = repository.getVerseTafseerData(surah, verse,
                    tafseerNamesMap.get(_selectedTafseer)!!
                ).text
                _verseE3rab = repository.getVerseE3rabData(surah, verse).text
                val causesOfRevelation = repository.getCauseOfRevelation(surah, verse)
                if (causesOfRevelation.size == 0) {
                    _verseCauseOfRevelation = "لم يرد في المرجع سبب لنزول الآية"
                } else {
                    var concatCauses = ""
                    causesOfRevelation.forEach { cause -> concatCauses += "${cause.text}\n\n" }
                    _verseCauseOfRevelation = concatCauses
                }
                _verseMeanings = repository.getSingleVerseMeanings(surah, verse)?.text
                    ?: "لم يرد في المرجع معاني لمفردات الاية"
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

    private var _verseCauseOfRevelation by mutableStateOf("")
    var verseCauseOfRevelation: String
        get() = _verseCauseOfRevelation
        set(value) {
            _verseCauseOfRevelation = value
        }

    private var _verseMeanings by mutableStateOf("")
    var verseMeanings: String
        get() = _verseMeanings
        set(value) {
            _verseMeanings = value
        }


    private var _showTafseerSheet by mutableStateOf(false)
    var showTafseerSheet: Boolean
        get() = _showTafseerSheet
        set(value) {
            _showTafseerSheet = value
        }

    private var _showTafseerOptions by mutableStateOf(false)
    var showTafseerOptions: Boolean
        get() = _showTafseerOptions
        set(value) {
            _showTafseerOptions = value
        }

    fun atFirstVerse(): Boolean {
        val surahNum = _tafseerTargetVerse.split("-").get(0).toInt()
        val verseNum = _tafseerTargetVerse.split("-").get(1).toInt()

        val verses = _currentPageObject?.pageContent!!.filter { it.type == PageContentItemType.verse }

        return verses.indexOfFirst { it.surahNum == surahNum && it.verseNum == verseNum } == 0
    }

    fun atLastVerse(): Boolean {
        val surahNum = _tafseerTargetVerse.split("-").get(0).toInt()
        val verseNum = _tafseerTargetVerse.split("-").get(1).toInt()

        val verses = _currentPageObject?.pageContent!!.filter { it.type == PageContentItemType.verse }

        return verses.indexOfFirst { it.surahNum == surahNum && it.verseNum == verseNum } == verses.size - 1
    }

    fun targetNextVerse() {
        val surahNum = _tafseerTargetVerse.split("-").get(0).toInt()
        val verseNum = _tafseerTargetVerse.split("-").get(1).toInt()

        val verses = _currentPageObject?.pageContent!!.filter { it.type == PageContentItemType.verse }

        if (verses.indexOfFirst { it.surahNum == surahNum && it.verseNum == verseNum } + 1 != verses.size) {
            val nextVerse =
                verses.get(
                    verses.indexOfFirst { it.surahNum == surahNum && it.verseNum == verseNum } + 1)
            tafseerTargetVerse = "${nextVerse.surahNum}-${nextVerse.verseNum}"
        }
    }

    fun targetPreviousVerse() {
        val surahNum = _tafseerTargetVerse.split("-").get(0).toInt()
        val verseNum = _tafseerTargetVerse.split("-").get(1).toInt()

        val verses = _currentPageObject?.pageContent!!.filter { it.type == PageContentItemType.verse }

        if (verses.indexOfFirst { it.surahNum == surahNum && it.verseNum == verseNum } - 1 >= 0) {
            val previousVerse =
                verses.get(
                    verses.indexOfFirst { it.surahNum == surahNum && it.verseNum == verseNum } - 1)
            tafseerTargetVerse = "${previousVerse.surahNum}-${previousVerse.verseNum}"
        }
    }

    private var _showPageSelectionDialog = mutableStateOf(false)
    var showPageSelectionDialog: Boolean
        get() = _showPageSelectionDialog.value
        set(value) {
            _showPageSelectionDialog.value = value
        }

    private var _startPlayingPage = mutableStateOf(1)
    var startPlayingPage: Int
        get() = _startPlayingPage.value
        set(value) {
            _startPlayingPage.value = value
        }

    private var _endPlayingPage = mutableStateOf(1)
    var endPlayingPage: Int
        get() = _endPlayingPage.value
        set(value) {
            _endPlayingPage.value = value
        }


    /*****************************SERVICE COMMUNICATION***********************************/

    fun startAndBind() {
        val serviceIntent = Intent(context, PagePlayerService::class.java)
        serviceIntent.setAction("START")
        serviceIntent.putExtra("playingPageNum", _currentPageObject?.page?.pageNum)
        context.startService(serviceIntent)
        bindToService()
    }

    private val serviceConnection = object : ServiceConnection {
        override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
            try {
                val binder = service as PagePlayerService.PageServiceBinder
                audioService = binder.getService()

                updateIsCurrentPagePlaying()
                if (_isCurrentPagePlaying.value) {
                    println("current page playing, should update ui params")
                    println(_currentPageObject)
                    audioService?.setVersesPlayList(_currentPageObject?.pageContent!!)
                    updateServicePlayingParameters()
                    updateServicePlayingIndex()
                }

                viewModelScope.launch {
                    audioService?.getPlaying()?.collect { state ->
                        println("playing status $state")
                        _isPlaying.value = state;
                        updateIsCurrentPagePlaying()
                    }
                }

                viewModelScope.launch {
                    audioService?.getStartPlayingIndex()?.collect { state ->
                        if (_isCurrentPagePlaying.value) {
                            startPlayingIndex = state;
                        }
                    }
                }

                viewModelScope.launch {
                    audioService?.getStartPlayingItem()?.collect { state ->
                        if (_isCurrentPagePlaying.value) {
                            _selectedVerse = state;
                        }
                    }
                }

                viewModelScope.launch {
                    audioService?.getEndPlayingIndex()?.collect { state ->
                        if (_isCurrentPagePlaying.value) {
                            endPlayingIndex = state;
                        }
                    }
                }

                viewModelScope.launch {
                    audioService?.getEndPlayingItem()?.collect { state ->
                        if (_isCurrentPagePlaying.value) {
                            _selectedEndVerse = state;
                        }
                    }
                }

                viewModelScope.launch {
                    audioService?.getPlaybackSpeed()?.collect { state ->
                        _playbackSpeed = state;
                    }
                }

                viewModelScope.launch {
                    audioService?.getSelectedRepetitionTab()?.collect { state ->
                        if (_isCurrentPagePlaying.value) {
                            _selectedRepetitionTab.value = state;
                        }
                    }
                }

                viewModelScope.launch {
                    audioService?.getSelectedRepetition()?.collect { state ->
                        _selectedRepetition = state;
                    }
                }

                viewModelScope.launch {
                    audioService?.getContinuousPlay()?.collect { state ->
                        _continuousPlay.value = state;
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
                    audioService?.getPlayNextPage()?.collect { state ->
                        if (state) {
                            playNextPage()
                            audioService?.setPlayNextPage(false);
                        }
                    }
                }

                /* when we click on the play button while the service is not running, we must start and bind the service first
                 and then here at the connection we check for the button click to play*/
                println("should play $clickedPlay")
                if (clickedPlay) {
                    audioService?.playAudio();
                    clickedPlay = false;
                }

            } catch (e: Exception) {
                e.printStackTrace()
                reportException(exception = e, file = "QuranViewModel")
            }
        }

        override fun onServiceDisconnected(name: ComponentName?) {
            unbindFromService();
        }
    }

    private var audioService: PageServiceInterface? = null

    fun bindToService() {
        val intent = Intent(context, PagePlayerService::class.java)
        context.bindService(intent, serviceConnection, Context.BIND_AUTO_CREATE)
    }

    fun unbindFromService() {
        if (audioService != null && serviceConnection != null) {
            try { //this just keeps causing crashes
                context.unbindService(serviceConnection)
            } catch (e: Exception) {
                e.printStackTrace()
                reportException(exception = e, file = "QuranViewModel")
            }
        }
    }

    fun updateServicePageValues() {
        audioService?.setVersesPlayList(_currentPageObject?.pageContent!!)
        audioService?.setPlayingPageNum(_currentPageObject?.page?.pageNum!!)
        updateIsCurrentPagePlaying()
    }

    fun updateServicePlayingParameters() {
        // update service params only when the page that is being played is updating them
        if (!_isCurrentPagePlaying.value) {
            return;
        }
        println("updating ui params")
        audioService?.setContinuousPlay(_continuousPlay.value)
        audioService?.setSelectedRepetition(_selectedRepetition)
        audioService?.setSelectedMappedRepetition(
            Helpers.repetitionOptionsMap.get(_selectedRepetition)!!
        )
        audioService?.setSelectedRepetitionTab(_selectedRepetitionTab.value)
        audioService?.setStartPlayingItem(_selectedVerse)
        audioService?.setEndPlayingIndex(endPlayingIndex)
        audioService?.setEndPlayingItem(_selectedEndVerse)
    }

    fun updateServicePlayingIndex() {
        // update service playing index only when the page that is being played is updating them
        if (!_isCurrentPagePlaying.value) {
            return;
        }
        println("updating service playing index")
        audioService?.setPlayingIndex(startPlayingIndex ?: 0)
        audioService?.setStartPlayingIndex(startPlayingIndex ?: 0)
    }

    // reset params like first and end verses and repetitions etc only in viewmodel while preserving them in service
    fun resetPlayingUIParams() {
        println("resetting ui params")
        _selectedRepetitionTab.value = RepetitionTab.الاية
        startPlayingIndex = 0
        _selectedVerse = null
        endPlayingIndex = null
        _selectedEndVerse = null
    }

    // fetch params like first and end verses and repetitions from service
    fun fetchPlayingUIParams() {
        println("fetching ui params")
        _selectedRepetition = audioService?.getSelectedRepetition()?.value ?: "0"
        _selectedRepetitionTab.value = audioService?.getSelectedRepetitionTab()?.value
            ?: RepetitionTab.الاية
        startPlayingIndex = audioService?.getStartPlayingIndex()?.value ?: 0
        _selectedVerse = audioService?.getStartPlayingItem()?.value
        endPlayingIndex = audioService?.getEndPlayingIndex()?.value
        _selectedEndVerse = audioService?.getEndPlayingItem()?.value
    }

    init {
        val sr = isMyServiceRunning(PagePlayerService::class.java, context);
        println("PagePlayerService running $sr")
        if (sr) {
            bindToService()
        }
    }

    /*****************************KHITMAH CONTROL***********************************/
    private val _khitmahList = mutableStateOf(emptyList<Khitmah>())
    var khitmahList: List<Khitmah>
        get() = _khitmahList.value
        set(value) {
            _khitmahList.value = value;
        }

    private val _showAddToKhitmahDialog = mutableStateOf(false)
    var showAddToKhitmahDialog: Boolean
        get() = _showAddToKhitmahDialog.value
        set(value) {
            _showAddToKhitmahDialog.value = value;
        }

    fun fetchKhitmahList() {
        try {
            viewModelScope.launch(Dispatchers.IO) {
                repository.getAllKhitmah().collect { state ->
                    withContext(Dispatchers.Main) {
                        _khitmahList.value = state.filter { !it.isComplete };
                    }
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
            reportException(exception = e, file = "QuranViewModel")
            Toast.makeText(context, "حصل خطأ يرجى المحاولة لاحقا", Toast.LENGTH_LONG).show()
        }
    }

    fun assignPageToKhitmah(khitmah: Khitmah) {
        val khitmahMark = KhitmahMark(khitmahId = khitmah.id,
            pageNum = _currentPageObject?.page?.pageNum!!
        )
        try {
            viewModelScope.launch(Dispatchers.IO) {
                repository.insertKhitmahMark(khitmahMark);
                repository.updateKhitmah(
                    khitmah.copy(latestPage = _currentPageObject?.page?.pageNum!!)
                )
                withContext(Dispatchers.Main) {
                    Toast.makeText(context, "تمت إضافة الصفحة للختمة بنجاح", Toast.LENGTH_LONG
                    ).show()
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
            reportException(exception = e, file = "QuranViewModel")
            Toast.makeText(context, "حصل خطأ يرجى المحاولة لاحقا", Toast.LENGTH_LONG).show()
        }
    }

}

enum class RepetitionTab {
    الاية, المقطع
}

enum class TafseerSheetMode {
    التفسير, الإعراب, أسباب_النزول, معاني_المفردات
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