package com.ottrojja.screens.quranScreen

import android.app.Application
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.core.content.FileProvider
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.ottrojja.R
import com.ottrojja.classes.Helpers
import com.ottrojja.classes.Helpers.convertToArabicNumbers
import com.ottrojja.classes.Helpers.reportException
import com.ottrojja.composables.ToastManager.classes.AppToastManager
import com.ottrojja.composables.ToastManager.classes.AppToastType
import com.ottrojja.interfaces.TabItem
import com.ottrojja.room.entities.BookmarkEntity
import com.ottrojja.room.entities.PageContent
import com.ottrojja.room.entities.PageContentItemType
import com.ottrojja.room.relations.QuranPageWithContent
import com.ottrojja.room.repositories.QuranRepository
import com.ottrojja.screens.quranScreen.classes.QuranReaderContext
import com.ottrojja.screens.quranScreen.classes.TafseerContent
import com.ottrojja.services.page.PageListeningOptions
import com.ottrojja.services.page.PagePlaybackManager
import com.ottrojja.services.page.PagePlaybackRequest
import com.ottrojja.services.page.PagePlayerState
import com.ottrojja.services.page.PageSegment
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.io.IOException

class QuranViewModel(
    private val repository: QuranRepository,
    private val readerContext: QuranReaderContext,
    private val playbackManager: PagePlaybackManager,
    application: Application
) : AndroidViewModel(application) {
    val context = application.applicationContext;
    val sharedPreferences: SharedPreferences = application.getSharedPreferences(
        "ottrojja",
        Context.MODE_PRIVATE
    )

    val quranPagesNumbers = List(604) { (it + 1).toString() }

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

    /*****************************PAGE PLAYER STATE***********************************/

    /** Everything about playback comes from PagePlaybackService through the manager. */
    val player: StateFlow<PagePlayerState> = playbackManager.state

    /**
     * Pages the pager must scroll to because of the VM (auto pagination while playing,
     * "back to the playing page"). The screen reports pages the user swiped to via onPageSettled().
     */
    private val _pageScrollRequests = Channel<Int>(Channel.CONFLATED)
    val pageScrollRequests: Flow<Int> = _pageScrollRequests.receiveAsFlow()

    /*****************************DISPLAYED PAGE***********************************/

    private var _currentPageObject by mutableStateOf<QuranPageWithContent?>(null)
    var currentPageObject: QuranPageWithContent?
        get() = _currentPageObject
        set(value) {
            _currentPageObject = value
        }

    private var opened = false
    private var pageLoadJob: Job? = null

    /**
     * First page of this screen. While a page recitation is playing and the screen follows it, the
     * screen opens at the playing page instead of [pageNum] (e.g. playback moved on to page 3 in
     * the background, the user comes back, they see page 3).
     * Runs once per ViewModel, so rotating the screen does not jump back to the navigation argument.
     */
    fun openAt(pageNum: String) {
        if (opened) return
        opened = true

        val s = playbackManager.state.value
        val playingPage = s.playingPage
        val target = if ((s.hasMedia || s.isDownloading) && s.following && playingPage != null) {
            playingPage.toString()
        } else {
            pageNum
        }
        showPage(target, scrollPager = false)
    }

    /**
     * The pager settled on [page]. [byUser] is false when it got there because of a scroll request
     * from this ViewModel.
     */
    fun onPageSettled(page: Int, byUser: Boolean) {
        if (byUser) {
            val s = playbackManager.state.value
            if (s.hasMedia) {
                // swiping away from the playing page stops following it; swiping back resumes
                playbackManager.setFollowing(page == s.playingPage)
            }
        }
        if (_currentPageObject?.page?.pageNum == page.toString()) return
        showPage(page.toString(), scrollPager = false)
    }

    /** "Back to the recited page" chip. */
    fun returnToPlayingPage() {
        val page = playbackManager.state.value.playingPage ?: return
        playbackManager.setFollowing(true)
        showPage(page.toString(), scrollPager = true)
    }

    private fun showPage(value: String, scrollPager: Boolean) {
        pageLoadJob?.cancel() // a fast swipe must not be overwritten by an older, slower load
        pageLoadJob = viewModelScope.launch {
            try {
                val page = withContext(Dispatchers.IO) { repository.getPage(value) }
                _currentPageObject = page
                if (scrollPager) _pageScrollRequests.trySend(value.toInt())

                // with nothing playing, the listening selection follows the displayed page
                val s = playbackManager.state.value
                if (!s.hasMedia && !s.isDownloading) takeOnCurrentPageVerses()

                isPageBookmarked()
                sharedPreferences.edit().putString("mostRecentPage", value).apply()

                if (readerContext is QuranReaderContext.Khitmah) {
                    sessionPages += value.toInt()
                }
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                e.printStackTrace()
                reportException(exception = e, file = "QuranViewModel")
            }
        }
    }

    /*****************************LISTENING OPTIONS***********************************/

    private var _continuousPlay = mutableStateOf(false)
    var continuousPlay: Boolean
        get() = _continuousPlay.value
        set(value) {
            _continuousPlay.value = value
            pushOptions()
            val s = playbackManager.state.value
            if (value && !s.hasMedia && !s.isDownloading) {
                takeOnCurrentPageVerses()
            }
        }

    private var _selectedRepetition by mutableStateOf("0")
    var selectedRepetition: String
        get() = _selectedRepetition
        set(value) {
            _selectedRepetition = value
            pushOptions()
        }

    private var _selectedRepetitionTab = mutableStateOf(RepetitionTab.الاية)
    var selectedRepetitionTab: RepetitionTab
        get() = _selectedRepetitionTab.value
        set(value) {
            _selectedRepetitionTab.value = value
            pushOptions()
        }

    private fun currentOptions() = PageListeningOptions(
        repetition = _selectedRepetition,
        repeatRange = _selectedRepetitionTab.value == RepetitionTab.المقطع,
        continuous = _continuousPlay.value,
    )

    /** Repetition and continuous changes apply to what is playing right away. */
    private fun pushOptions() = playbackManager.updateOptions(currentOptions())

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

    private var _selectedVerse by mutableStateOf<PageContent?>(null)
    var selectedVerse: PageContent?
        get() = _selectedVerse
        set(value) {
            _selectedVerse = value
        }

    private var _selectedEndVerse by mutableStateOf<PageContent?>(null)
    var selectedEndVerse: PageContent?
        get() = _selectedEndVerse
        set(value) {
            _selectedEndVerse = value
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

    private var _showListeningOptionsDialog = mutableStateOf(false)
    var showListeningOptionsDialog: Boolean
        get() = _showListeningOptionsDialog.value
        set(value) {
            _showListeningOptionsDialog.value = value
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
        val mode = _versesSelectionMode.value
        viewModelScope.launch {
            val verses = withContext(Dispatchers.IO) { repository.fetchPageVerses(pageNum) }
            if (mode == VersesSelectionMode.END) {
                _selectionEndVersesList.value = verses;
                // the end of a range defaults to the LAST verse of the chosen page (was the first)
                _selectedEndVerse = verses.findLast { it.type == PageContentItemType.verse };
            } else {
                _selectionVersesList.value = verses;
                _selectedVerse = verses.find { it.type == PageContentItemType.verse };
            }
        }
    }

    fun takeOnCurrentPageVerses() {
        val page = _currentPageObject ?: return
        _selectionVersesList.value = page.pageContent
        _selectionEndVersesList.value = page.pageContent
        _startPlayingPage.value = page.page.pageNum.toInt()
        _endPlayingPage.value = page.page.pageNum.toInt()
        _selectedVerse = page.pageContent.find { it.type == PageContentItemType.verse }
        _selectedEndVerse = page.pageContent.findLast { it.type == PageContentItemType.verse }
    }

    /** While playing, the listening options show what the service is playing (it moves on in continuous mode). */
    private suspend fun applySegmentToSelection(segment: PageSegment) {
        val (startRows, endRows) = withContext(Dispatchers.IO) {
            val start = repository.fetchPageVerses(segment.startPage.toString())
            val end = if (segment.endPage == segment.startPage) start
            else repository.fetchPageVerses(segment.endPage.toString())
            start to end
        }
        _startPlayingPage.value = segment.startPage
        _endPlayingPage.value = segment.endPage
        _selectionVersesList.value = startRows
        _selectionEndVersesList.value = endRows
        _selectedVerse = startRows.find {
            it.type == PageContentItemType.verse && it.surahNum == segment.startSurah && it.verseNum == segment.startVerse
        }
        _selectedEndVerse = endRows.find {
            it.type == PageContentItemType.verse && it.surahNum == segment.endSurah && it.verseNum == segment.endVerse
        }
    }

    /*****************************PLAYBACK CONTROL***********************************/

    fun prepareForPlaying() {
        val startVerse = _selectedVerse
        val endVerse = _selectedEndVerse
        val startVerseNum = startVerse?.verseNum
        val endVerseNum = endVerse?.verseNum

        if (startVerse == null || endVerse == null || startVerseNum == null || endVerseNum == null) {
            AppToastManager.show(
                message = "يرجى تحديد مواضع بداية ونهاية التلاوة",
                type = AppToastType.ERROR
            )
            return
        }

        if (startVerse.id > endVerse.id) {
            AppToastManager.show(
                message = "موضع اية البداية يجب ان لا يسبق موضع اية النهاية",
                type = AppToastType.ERROR
            )
            return
        }

        // the service builds the playlist, downloads missing files and plays
        playbackManager.play(
            PagePlaybackRequest(
                startSurah = startVerse.surahNum,
                startVerse = startVerseNum,
                endSurah = endVerse.surahNum,
                endVerse = endVerseNum,
            ),
            currentOptions()
        )
    }

    fun pausePlaying() = playbackManager.pause()

    fun goNextVerse() = playbackManager.nextVerse()

    fun goPreviousVerse() = playbackManager.previousVerse()

    fun increasePlaybackSpeed() = playbackManager.increaseSpeed()

    fun decreasePlaybackSpeed() = playbackManager.decreaseSpeed()

    /** Cancel button: stops playback and any running download. */
    fun stopPlayback() = playbackManager.stop()

    /*****************************BOOKMARKS***********************************/

    private val _isBookmarked = mutableStateOf(false)
    var isBookmarked: Boolean
        get() = _isBookmarked.value
        set(value) {
            _isBookmarked.value = value
        }

    fun isPageBookmarked() {
        val pageNum = currentPageObject?.page?.pageNum ?: return
        viewModelScope.launch(Dispatchers.IO) {
            try {
                println("checking bookmarks for $pageNum")
                _isBookmarked.value = repository.isBookmarked(pageNum)
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
                AppToastManager.show(
                    message = "تم تحديث المرجعيات بنجاح",
                    type = AppToastType.SUCCESS
                )
                //just to update ui
                isPageBookmarked();
            } catch (e: Exception) {
                e.printStackTrace()
                reportException(exception = e, file = "QuranViewModel")
                AppToastManager.show(
                    message = "حصل خطأ يرجى المحاولة لاحقا",
                    type = AppToastType.ERROR
                )
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

    enum class PageTab(override val displayName: String) : TabItem {
        الصفحة(displayName = "الصفحة"),
        الفوائد(displayName = "الفوائد"),
        التفسير(displayName = "التفسير"),
        الفيديو(displayName = "الفيديو")
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

    /*****************************TAFSEER MODAL CONTROL***********************************/

    private var _selectedTafseer by mutableStateOf("تفسير السعدي")
    var selectedTafseer: String
        get() = _selectedTafseer
        set(value) {
            _selectedTafseer = value
        }

    fun updateSelectedTafseer(value: String) {
        _selectedTafseer = value;
        viewModelScope.launch(Dispatchers.IO) {
            val surah = _tafseerTargetVerse?.surahNum.toString()
            val verse = _tafseerTargetVerse?.verseNum.toString()
            _verseTafseer = listOf(TafseerContent(repository.getVerseTafseerData(
                surah,
                verse,
                tafseerNamesMap.get(value)!!
            ).text
            )
            )
        }
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

    private var _tafseerChapterVerse by mutableStateOf("")
    val tafseerChapterVerse: String
        get() = _tafseerChapterVerse

    private var _tafseerTargetVerse by mutableStateOf<PageContent?>(null)
    var tafseerTargetVerse: PageContent?
        get() = _tafseerTargetVerse
        set(value) {
            viewModelScope.launch(Dispatchers.IO) {
                _tafseerTargetVerse = value
                val surah = value?.surahNum.toString()
                val verse = value?.verseNum.toString()
                val chapterName = repository.getChapter(surah.toInt()).chapterName
                if (chapterName.isNotBlank()) {
                    _tafseerChapterVerse = "الاية ${Helpers.convertToIndianNumbers(verse)} من سورة $chapterName"
                } else {
                    _tafseerChapterVerse = ""
                }

                _verseTafseer = listOf(TafseerContent(text = repository.getVerseTafseerData(surah, verse, tafseerNamesMap.get(_selectedTafseer)!!).text))
                _verseE3rab = listOf(TafseerContent(text = repository.getVerseE3rabData(surah, verse).text))
                val causesOfRevelation = repository.getCauseOfRevelation(surah, verse)
                if (causesOfRevelation.size == 0) {
                    _verseCauseOfRevelation = listOf(TafseerContent(text = "لم يرد في المرجع سبب لنزول الآية"))
                } else {
                    var causesList = mutableListOf<TafseerContent>()
                    causesOfRevelation.forEach { cause -> causesList.add(TafseerContent(cause.text)) }
                    _verseCauseOfRevelation = causesList
                }
                _verseMeanings = listOf(TafseerContent(
                    text = repository.getSingleVerseMeanings(surah, verse)?.text
                        ?: "لم يرد في المرجع معاني لمفردات الاية"
                )
                )
                val verses = repository.getSpecificVerses(value?.mutashabihat ?: emptyList())
                val formatted = if (verses.isEmpty()) {
                    listOf(TafseerContent("لم يرد في المرجع آيات مشابهة"))
                } else {
                    verses.map { verse ->
                        TafseerContent(
                            formulateMutashabeh(verse),
                            { viewModelScope.launch { _navigation.send(verse.pageNum) } })
                    }
                }
                withContext(Dispatchers.Main) {
                    _verseMutashabihat = formatted
                }
            }
        }

    suspend private fun formulateMutashabeh(verse: PageContent): String {
        val chapterName = repository.getChapter(verse.surahNum).chapterName
        return "{${verse.verseText}}\n${chapterName} - ${verse.verseNum}"
    }

    private var _verseTafseer by mutableStateOf(listOf(TafseerContent("")))
    var verseTafseer: List<TafseerContent>
        get() = _verseTafseer
        set(value) {
            _verseTafseer = value
        }

    private var _verseE3rab by mutableStateOf(listOf(TafseerContent("")))
    var verseE3rab: List<TafseerContent>
        get() = _verseE3rab
        set(value) {
            _verseE3rab = value
        }

    private var _verseCauseOfRevelation by mutableStateOf(listOf(TafseerContent("")))
    var verseCauseOfRevelation: List<TafseerContent>
        get() = _verseCauseOfRevelation
        set(value) {
            _verseCauseOfRevelation = value
        }

    private var _verseMeanings by mutableStateOf(listOf(TafseerContent("")))
    var verseMeanings: List<TafseerContent>
        get() = _verseMeanings
        set(value) {
            _verseMeanings = value
        }

    private var _verseMutashabihat by mutableStateOf(listOf(TafseerContent("")))
    var verseMutashabihat: List<TafseerContent>
        get() = _verseMutashabihat
        set(value) {
            _verseMutashabihat = value
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
        val surahNum = _tafseerTargetVerse?.surahNum
        val verseNum = _tafseerTargetVerse?.verseNum

        val verses = _currentPageObject?.pageContent!!.filter { it.type == PageContentItemType.verse }

        return verses.indexOfFirst { it.surahNum == surahNum && it.verseNum == verseNum } == 0
    }

    fun atLastVerse(): Boolean {
        val surahNum = _tafseerTargetVerse?.surahNum
        val verseNum = _tafseerTargetVerse?.verseNum

        val verses = _currentPageObject?.pageContent!!.filter { it.type == PageContentItemType.verse }

        return verses.indexOfFirst { it.surahNum == surahNum && it.verseNum == verseNum } == verses.size - 1
    }

    fun targetNextVerse() {
        val surahNum = _tafseerTargetVerse?.surahNum
        val verseNum = _tafseerTargetVerse?.verseNum

        val verses = _currentPageObject?.pageContent!!.filter { it.type == PageContentItemType.verse }

        if (verses.indexOfFirst { it.surahNum == surahNum && it.verseNum == verseNum } + 1 != verses.size) {
            val nextVerse = verses.get(verses.indexOfFirst { it.surahNum == surahNum && it.verseNum == verseNum } + 1)
            tafseerTargetVerse = nextVerse
        }
    }

    fun targetPreviousVerse() {
        val surahNum = _tafseerTargetVerse?.surahNum
        val verseNum = _tafseerTargetVerse?.verseNum

        val verses = _currentPageObject?.pageContent!!.filter { it.type == PageContentItemType.verse }

        if (verses.indexOfFirst { it.surahNum == surahNum && it.verseNum == verseNum } - 1 >= 0) {
            val previousVerse = verses.get(verses.indexOfFirst { it.surahNum == surahNum && it.verseNum == verseNum } - 1)
            tafseerTargetVerse = previousVerse
        }
    }

    /*****************************KHITMAH CONTROL***********************************/
    private val sessionPages = mutableSetOf<Int>()

    fun getFirstSessionPage(): Int {
        return sessionPages.min()
    }

    fun getLastSessionPage(): Int {
        return sessionPages.max()
    }


    private val _showFinishSessionForm = MutableStateFlow<Boolean>(false)
    val showFinishSessionForm: StateFlow<Boolean> = _showFinishSessionForm


    fun toggleFinishSessionForm(value: Boolean) {
        _showFinishSessionForm.value = value
    }

    fun invokeSessionForm() {
        toggleFinishSessionForm(true)
    }

    private val _navigation = Channel<String>()
    val navigation = _navigation.receiveAsFlow()

    /*****************************INIT (keep last: uses the fields above)***********************************/

    init {
        // listening options survive leaving the screen (kept in the app-scoped manager)
        val options = playbackManager.state.value.options
        _selectedRepetition = options.repetition
        _selectedRepetitionTab.value = if (options.repeatRange) RepetitionTab.المقطع else RepetitionTab.الاية
        _continuousPlay.value = options.continuous

        // AUTO pagination: follow the page the service is playing, unless the user swiped away
        viewModelScope.launch {
            playbackManager.state
                .map { if (it.hasMedia && it.following) it.playingPage else null }
                .distinctUntilChanged()
                .filterNotNull()
                .collect { page ->
                    if (opened && _currentPageObject?.page?.pageNum != page.toString()) {
                        showPage(page.toString(), scrollPager = true)
                    }
                }
        }

        // the listening selection mirrors what the service is playing
        viewModelScope.launch {
            playbackManager.state
                .map { if (it.hasMedia || it.isDownloading) it.segment else null }
                .distinctUntilChanged()
                .filterNotNull()
                .collect { segment -> applySegmentToSelection(segment) }
        }
    }

    override fun onCleared() {
        playbackManager.releaseIfIdle()
        super.onCleared()
    }
}

enum class VersesSelectionMode {
    START, END
}


enum class RepetitionTab(override val displayName: String) : TabItem {
    الاية(displayName = "تكرار الآية"),
    المقطع(displayName = "تكرار المقطع")
}

enum class TafseerSheetMode(override val displayName: String) : TabItem {
    التفسير(displayName = "التفسير"),
    الإعراب(displayName = "الإعراب"),
    أسباب_النزول(displayName = "أسباب النزول"),
    معاني_المفردات(displayName = "معاني المفردات"),
    المشابهات(displayName = "المشابهات"),
}

class QuranScreenViewModelFactory(
    private val repository: QuranRepository,
    private val readerContext: QuranReaderContext,
    private val playbackManager: PagePlaybackManager,
    private val application: Application
) : ViewModelProvider.AndroidViewModelFactory(application) {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(QuranViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return QuranViewModel(repository, readerContext, playbackManager, application) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
