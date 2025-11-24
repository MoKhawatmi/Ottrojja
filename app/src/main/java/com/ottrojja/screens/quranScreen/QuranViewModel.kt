package com.ottrojja.screens.quranScreen

import android.app.Application
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.content.SharedPreferences
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Build
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
import com.ottrojja.classes.ConnectivityMonitor
import com.ottrojja.room.entities.PageContent
import com.ottrojja.classes.Helpers
import com.ottrojja.classes.Helpers.convertToArabicNumbers
import com.ottrojja.classes.Helpers.isMyServiceRunning
import com.ottrojja.classes.Helpers.reportException
import com.ottrojja.classes.Helpers.terminateAllServices
import com.ottrojja.classes.NetworkClient.ottrojjaClient
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
    private lateinit var connectivityMonitor: ConnectivityMonitor
    var networkConnected = false;

    val externalFilesDir = context.getExternalFilesDir(null);


    val quranPagesNumbers = List(604) { (it + 1).toString() }

    private var _versesPlayList: List<PageContent> = emptyList();

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
            if(value){
                takeOnCurrentPageVerses()
            }
        }

    private var _shouldAutoPlay = mutableStateOf(false)
    var shouldAutoPlay: Boolean
        get() = _shouldAutoPlay.value
        set(value: Boolean) {
            _shouldAutoPlay.value = value
        }

    private var _autoSwipePagesWithAudio = mutableStateOf(false)
    var autoSwipePagesWithAudio: Boolean
        get() = _autoSwipePagesWithAudio.value
        set(value: Boolean) {
            _autoSwipePagesWithAudio.value = value
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

    fun setCurrentPage(value: String, takeOnSelectedPageVerses: Boolean = false) {
        println("setting current page to $value")
        viewModelScope.launch(Dispatchers.IO) {
            try {
                _currentPageObject = repository.getPage(value)

                if (!isMyServiceRunning(PagePlayerService::class.java, context) || takeOnSelectedPageVerses) {
                    withContext(Dispatchers.Main) {
                        takeOnCurrentPageVerses()
                    }
                }

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

    private var _selectedVerse by mutableStateOf<PageContent?>(null)
    var selectedVerse: PageContent?
        get() = _selectedVerse
        set(value) {
            _selectedVerse = value;
        }

    private var _selectedEndVerse by mutableStateOf<PageContent?>(null)
    var selectedEndVerse: PageContent?
        get() = _selectedEndVerse
        set(value) {
            _selectedEndVerse = value;
        }

    private var _versesSelectionMode = mutableStateOf(VersesSelectionMode.START)
    var versesSelectionMode: VersesSelectionMode
        get() = _versesSelectionMode.value
        set(value) {
            _versesSelectionMode.value = value
        }

    private var _showPageSelectionDialog = mutableStateOf(false)
    var showPageSelectionDialog: Boolean
        get() = _showPageSelectionDialog.value
        set(value) {
            _showPageSelectionDialog.value = value
        }

    private var _pagesSearchFilter = mutableStateOf("")
    var pagesSearchFilter: String
        get() = _pagesSearchFilter.value
        set(value) {
            _pagesSearchFilter.value = value
        }

    fun getPagesList(): List<String> {
        return quranPagesNumbers.filter { page ->
            page.contains(_pagesSearchFilter.value) || page.contains(
                convertToArabicNumbers(_pagesSearchFilter.value)
            )
        };
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

    private val _selectionVersesList = mutableStateOf(listOf<PageContent>())
    var selectionVersesList: List<PageContent>
        get() = _selectionVersesList.value
        set(value) {
            _selectionVersesList.value = value
        }

    private val _selectionEndVersesList = mutableStateOf(listOf<PageContent>())
    var selectionEndVersesList: List<PageContent>
        get() = _selectionEndVersesList.value
        set(value) {
            _selectionEndVersesList.value = value
        }

    fun updateSelectionVersesList(pageNum: String) {
        println("fetching verses list for selection for page $pageNum")
        viewModelScope.launch(Dispatchers.IO) {
            val verses = repository.fetchPageVerses(pageNum)
            withContext(Dispatchers.Main) {
                if (_versesSelectionMode.value == VersesSelectionMode.END) {
                    _selectionEndVersesList.value = verses;
                    _selectedEndVerse = verses.first { it.type == PageContentItemType.verse };
                } else {
                    _selectionVersesList.value = verses;
                    _selectedVerse = verses.find { it.type == PageContentItemType.verse };
                }
            }
        }
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
        println("checking verses of selected verses range");
        for (item in _versesPlayList) {
            var path: String;
            if (item.type == PageContentItemType.surah) {
                path = "1-1-1.mp3"
            } else {
                path = "${item.pageNum}-${item.surahNum}-${item.verseNum}.mp3"
            }
            val localFile = File(externalFilesDir, path)
            if (!localFile.exists()) {
                println("audio files for selected verses range need downloading")
                allVersesExist = false;
                return;
            }
        }
        println("audio files for selected verses range are complete")
        allVersesExist = true;
    }

    fun prepareForPlaying() {
        if (_selectedEndVerse == null || _selectedVerse == null) {
            Toast.makeText(context, "يرجى تحديد مواضع بداية ونهاية التلاوة", Toast.LENGTH_LONG
            ).show()
            return;
        }

        if ((_selectedVerse?.surahNum!! > _selectedEndVerse?.surahNum!!) || (_selectedVerse?.surahNum!! == _selectedEndVerse?.surahNum!! && _selectedVerse?.verseNum!! > _selectedEndVerse?.verseNum!!)) {
            Toast.makeText(context, "موضع اية البداية يجب ان لا يأتي بعد موضع اية النهاية",
                Toast.LENGTH_LONG
            ).show()
            return;
        }

        if ((_selectedEndVerse?.surahNum!! < _selectedVerse?.surahNum!!) || (_selectedEndVerse?.surahNum!! == _selectedVerse?.surahNum!! && _selectedEndVerse?.verseNum!! < _selectedVerse?.verseNum!!)) {
            Toast.makeText(context, "موضع اية النهاية يجب ان لا يأتي قبل موضع اية البداية",
                Toast.LENGTH_LONG
            ).show()
            return;
        }

        // stop other services
        terminateAllServices(context, PagePlayerService::class.java)
        _shouldAutoPlay.value = false;
        _autoSwipePagesWithAudio.value = true;
        viewModelScope.launch(Dispatchers.IO) {
            val versesList = repository.getPagesContentRange(
                _selectedVerse?.surahNum!!,
                _selectedVerse?.verseNum!!,
                _selectedEndVerse?.surahNum!!,
                _selectedEndVerse?.verseNum!!
            )

            withContext(Dispatchers.Main) {
                // modify list to make sure to add basmalah
                val modifiedVersesList = mutableListOf<PageContent>()

                for (item in versesList) {
                    if (item.verseNum == 1) {
                        val newItem = PageContent(type = PageContentItemType.surah, surahNum = item.surahNum, pageNum = item.pageNum, id = 0, surahName = null, surahTotal = null, surahType = null, verseNum = null, verseText = null, verseTextPlain = null)
                        modifiedVersesList.add(newItem)
                    }
                    modifiedVersesList.add(item)
                }

                _versesPlayList = modifiedVersesList;
                checkVerseFilesExistance()

                if (!allVersesExist) {
                    if (networkConnected) {
                        initializeDownload()
                    } else {
                        Toast.makeText(context, "لا يوجد اتصال بالانترنت", Toast.LENGTH_LONG)
                            .show()
                        return@withContext;
                    }
                } else {
                    startPlaying()
                }
            }
        }
    }

    var clickedPlay = false;
    fun startPlaying() {
        if (isMyServiceRunning(PagePlayerService::class.java, context)) {
            if (!_isPlaying.value) {
                println("preparing for a new page")
                audioService?.resetPlayer();
                audioService?.resetUIStates();
                updateServicePlayingParameters();
            }
            audioService?.setVersesPlayList(_versesPlayList)
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

    private fun moveToPlayPage(pageNum: String) {
        _shouldAutoPlay.value = true;
        _vmChangedPage.value = true;
        setCurrentPage(pageNum, true)
    }

    var downloadIndex = 0;

    fun initializeDownload() {
        downloadIndex = 0;
        _isDownloading.value = true;
        downloadVerse()
    }

    /*fun updateIsCurrentPagePlaying() {
        println("${audioService?.getPlayingPageNum()} =? ${_currentPageObject?.page?.pageNum}")
        _isCurrentPagePlaying.value = audioService?.getPlayingPageNum() == _currentPageObject?.page?.pageNum
        println("_isCurrentPagePlaying ${_isCurrentPagePlaying.value}")
    }*/

    fun downloadVerse() {
        val item = _versesPlayList[downloadIndex];
        var path: String;
        if (item.type == PageContentItemType.surah) {
            path = "1-1-1.mp3"
        } else {
            path = "${item.pageNum}-${item.surahNum}-${item.verseNum}.mp3"
        }

        val localFile = File(
            externalFilesDir,
            path
        )
        val tempFile = File.createTempFile("temp_", ".mp3", externalFilesDir)

        val request = Request.Builder()
            .url("https://ottrojja.fra1.cdn.digitaloceanspaces.com/verses/$path")
            .build()


        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                try {
                    ottrojjaClient.newCall(request).execute().use { response ->
                        if (!response.isSuccessful) throw IOException("Unexpected code $response")

                        response.body?.let { responseBody ->
                            FileOutputStream(tempFile).use { outputStream ->
                                responseBody.byteStream().use { inputStream ->
                                    inputStream.copyTo(outputStream, bufferSize = 8 * 1024)
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
                    reportException(exception = e, file = "QuranViewModel", details = "link: /verses/$path")
                    withContext(Dispatchers.Main) {
                        if (e.message?.contains("ENOSPC") == true) {
                            Toast.makeText(context, context.resources.getString(R.string.enospc),
                                Toast.LENGTH_LONG
                            ).show()
                        } else {
                            Toast.makeText(context, "حدث خطأ اثناء التحميل", Toast.LENGTH_LONG
                            ).show()
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
        التفسير,
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

    fun takeOnCurrentPageVerses() {
        println("Setting up current page parameters for page num ${_currentPageObject?.page?.pageNum}")
        _selectionVersesList.value = _currentPageObject?.pageContent!!
        _selectionEndVersesList.value = _currentPageObject?.pageContent!!
        _startPlayingPage.value = _currentPageObject?.page!!.pageNum.toInt();
        _endPlayingPage.value = _currentPageObject?.page!!.pageNum.toInt();
        _selectedVerse = _selectionVersesList.value.find { it.type == PageContentItemType.verse };
        _selectedEndVerse = _selectionEndVersesList.value.findLast { it.type == PageContentItemType.verse };
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
                    _tafseerChapterVerse = "الاية ${
                        Helpers.convertToIndianNumbers(verse
                        )
                    } من سورة $chapterName"
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


    /*****************************SERVICE COMMUNICATION***********************************/

    fun startAndBind() {
        val serviceIntent = Intent(context, PagePlayerService::class.java)
        serviceIntent.setAction("START")
        serviceIntent.putExtra("playingPageNum", _startPlayingPage.value.toString())
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
                val binder = service as PagePlayerService.PageServiceBinder
                audioService = binder.getService()

                if (clickedPlay) {
                    updateServicePlayingParameters()
                    moveToPlayPage(_startPlayingPage.value.toString())
                }

                viewModelScope.launch {
                    audioService?.getPlaying()?.collect { state ->
                        println("playing status $state")
                        _isPlaying.value = state;
                    }
                }

                viewModelScope.launch {
                    audioService?.getPlayingPageNum()?.collect { state ->
                        if (state != null && _isPlaying.value && _autoSwipePagesWithAudio.value) {
                            println("current page playing in service $state")
                            moveToPlayPage(state)
                        }
                    }
                }

                /* viewModelScope.launch {
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
                 }*/

                viewModelScope.launch {
                    audioService?.getPlaybackSpeed()?.collect { state ->
                        _playbackSpeed = state;
                    }
                }

                viewModelScope.launch {
                    audioService?.getSelectedRepetitionTab()?.collect { state ->
                        _selectedRepetitionTab.value = state;
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
                            moveToPlayPage("${_currentPageObject!!.page.pageNum.toInt() + 1}")
                            audioService?.setPlayNextPage(false);
                        }
                    }
                }

                viewModelScope.launch {
                    audioService?.getSelectionVersesList()?.collect { state ->
                        _selectionVersesList.value = state;
                    }
                }

                viewModelScope.launch {
                    audioService?.getSelectionEndVersesList()?.collect { state ->
                        _selectionEndVersesList.value = state;
                    }
                }

                viewModelScope.launch {
                    audioService?.getVersesPlayList()?.collect { state ->
                        if (!state.isNullOrEmpty()) {
                            _selectedVerse = state.first { it.type == PageContentItemType.verse }
                            _startPlayingPage.value = _selectedVerse?.pageNum!!.toInt()
                            _selectedEndVerse = state.last()
                            _endPlayingPage.value = _selectedEndVerse?.pageNum!!.toInt()
                        }
                    }
                }

                /* when we click on the play button while the service is not running, we must start and bind the service first
                 and then here at the connection we check for the button click to play*/
                println("should play $clickedPlay")
                if (clickedPlay) {
                    audioService?.setVersesPlayList(_versesPlayList)
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
        // when we start playing with service, the auto swiping sets this to true, to restore normal
        // functionality when the service is destroyed and unbound, we reset it to false
        _vmChangedPage.value = false;
        if (audioService != null && serviceConnection != null) {
            try { //this just keeps causing crashes
                context.unbindService(serviceConnection)
            } catch (e: Exception) {
                e.printStackTrace()
                reportException(exception = e, file = "QuranViewModel")
            }
        }
    }

    fun updateServicePlayingParameters() {
        println("updating service params")
        println("updating reps in service to  ${_selectedRepetition}")
        audioService?.setContinuousPlay(_continuousPlay.value)
        audioService?.setSelectedRepetition(_selectedRepetition)
        audioService?.setSelectedMappedRepetition(Helpers.repetitionOptionsMap.get(_selectedRepetition)!!)
        audioService?.setSelectedRepetitionTab(_selectedRepetitionTab.value)
        audioService?.setSelectionVersesList(_selectionVersesList.value)
        audioService?.setSelectionEndVersesList(_selectionEndVersesList.value)
    }

    fun terminatePagePlayerService() {
        val stopServiceIntent = Intent(context, PagePlayerService::class.java)
        stopServiceIntent.setAction("TERMINATE")
        context.startService(stopServiceIntent)
    }

    init {
        val sr = isMyServiceRunning(PagePlayerService::class.java, context);
        println("PagePlayerService running $sr")
        if (sr) {
            bindToService()
        }

        connectivityMonitor = ConnectivityMonitor(context)
        connectivityMonitor.start()

        viewModelScope.launch {
            connectivityMonitor.online.collect { isOnline ->
                networkConnected = isOnline;
            }
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

enum class VersesSelectionMode {
    START, END
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