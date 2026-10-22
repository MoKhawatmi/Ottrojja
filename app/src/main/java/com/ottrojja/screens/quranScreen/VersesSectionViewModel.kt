package com.ottrojja.screens.quranScreen

import android.app.Application
import android.content.ClipData
import android.content.Intent
import android.graphics.Bitmap
import androidx.compose.runtime.mutableStateOf
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.ottrojja.R
import com.ottrojja.classes.Helpers
import com.ottrojja.classes.Helpers.reportException
import com.ottrojja.composables.ToastManager.classes.AppToastManager
import com.ottrojja.composables.ToastManager.classes.AppToastType
import com.ottrojja.room.entities.PageContent
import com.ottrojja.room.repositories.QuranRepository
import com.ottrojja.room.entities.ChapterData
import com.ottrojja.room.entities.Khitmah
import com.ottrojja.screens.quranScreen.classes.ShareMode
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import kotlin.coroutines.cancellation.CancellationException

class VersesSectionViewModel(private val repository: QuranRepository, application: Application) :
    AndroidViewModel(application),
    LifecycleObserver {
    val context = application.applicationContext;
    private val itemsList = MutableStateFlow<List<SectionVerse>>(emptyList())
    val items: StateFlow<List<SectionVerse>> get() = itemsList

    fun setItems(pageContentItems: List<PageContent>) {
        val tempList = mutableListOf<SectionVerse>()
        pageContentItems.forEach { item -> tempList.add(SectionVerse(item, false)) }
        this.itemsList.value = tempList;
    }

    fun updateExpanded(item: SectionVerse) {
        val tempList = itemsList.value.toMutableList();
        val indexOfItem =
            tempList.indexOfFirst { verse -> verse.pageContent.verseNum == item.pageContent.verseNum && verse.pageContent.surahNum == item.pageContent.surahNum };
        if (indexOfItem == -1) return
        val newItem = SectionVerse(item.pageContent, !item.expanded);
        tempList.set(indexOfItem, newItem);
        itemsList.value = tempList;
    }


    private var _showVerseShareDialog = mutableStateOf(false)
    var showVerseShareDialog: Boolean
        get() = _showVerseShareDialog.value
        set(value) {
            _showVerseShareDialog.value = value
        }

    private var _startingVerse = mutableStateOf<PageContent?>(null)
    var startingVerse: PageContent?
        get() = _startingVerse.value
        set(value) {
            _startingVerse.value = value
        }

    private var _endVerse = mutableStateOf<PageContent?>(null)
    var endVerse: PageContent?
        get() = _endVerse.value
        set(value) {
            _endVerse.value = value
        }


    private var _selectionVersesList = mutableStateOf(emptyList<PageContent>())
    var selectionVersesList: List<PageContent>
        get() = _selectionVersesList.value
        set(value) {
            _selectionVersesList.value = value
        }


    private var _shareChapter = mutableStateOf<ChapterData?>(null)
    var shareChapter: ChapterData?
        get() = _shareChapter.value
        set(value) {
            _shareChapter.value = value
        }

    private var isFetching = false
    fun fetchChapterVerses(verseItem: PageContent) {
        if (isFetching) return
        isFetching = true

        viewModelScope.launch {
            try {
                val verses = withContext(Dispatchers.IO) {
                    repository.getChapterVerses(verseItem.surahNum)
                }

                val chapter = withContext(Dispatchers.IO) {
                    repository.getChapter(verseItem.surahNum)
                }

                _selectionVersesList.value = verses
                _startingVerse.value = verseItem
                _endVerse.value = verseItem
                _shareChapter.value = chapter
                _showVerseShareDialog.value = true
            } catch (e: CancellationException) {
                return@launch
            } catch (e: Exception) {
                reportException(e, "VersesSectionViewModel", "Surah Id: ${verseItem.surahNum}")
                AppToastManager.show(
                    message = "تعذر تحميل الآيات",
                    type = AppToastType.ERROR
                )
            } finally {
                isFetching = false
            }
        }
    }

    /** Returns the selected verses, or null if the selection is invalid. */
    fun selectedVerses(showErrors: Boolean = false): List<PageContent>? {
        val verses = _selectionVersesList.value
        val startVerse = _startingVerse.value ?: return null
        val endVerse = _endVerse.value ?: return null

        val startIdx = verses.indexOfFirst { it.surahNum == startVerse.surahNum && it.verseNum == startVerse.verseNum }
        val endIdx = verses.indexOfFirst { it.surahNum == endVerse.surahNum && it.verseNum == endVerse.verseNum }

        if (startIdx == -1 || endIdx == -1) {
            if (showErrors) AppToastManager.show(message = "تعذر تحديد الآيات المطلوبة", type = AppToastType.ERROR)
            return null
        }
        if (startIdx > endIdx) {
            if (showErrors) AppToastManager.show(
                message = "موضع آية النهاية لا يجب أن يكون قبل آية البداية",
                type = AppToastType.ERROR
            )
            return null
        }
        return verses.subList(startIdx, endIdx + 1)
    }

    fun validateSelection(): Boolean = selectedVerses(showErrors = true) != null

    fun shareVerses() {
        val selected = selectedVerses(showErrors = true) ?: return
        val first = selected.first()
        val last = selected.last()

        val versesRange =
            if (first.verseNum == last.verseNum) "${first.verseNum}" else "${first.verseNum} - ${last.verseNum}"
        val shareVersesText = selected.joinToString(separator = "") { "${it.verseText} {${Helpers.convertToIndianNumbers("${it.verseNum}")}} " }

        viewModelScope.launch(Dispatchers.IO) {
            val chapterName = _shareChapter.value?.chapterName
            val sendIntent: Intent = Intent().apply {
                action = Intent.ACTION_SEND
                putExtra(Intent.EXTRA_SUBJECT, "آيات قرآنية")
                putExtra(Intent.EXTRA_TITLE, "تطبيق اترجة")
                putExtra(
                    Intent.EXTRA_TEXT,
                    "{${shareVersesText}} \n سورة ${chapterName}: $versesRange\n${context.resources.getString(R.string.share_app)}"
                )
                type = "text/plain"
            }

            val shareIntent =
                Intent.createChooser(sendIntent, "مشاركة")
            shareIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            ContextCompat.startActivity(context, shareIntent, null)
        }
    }

    fun shareVerseImage(bitmap: Bitmap) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val dir = File(context.cacheDir, "shared_images").apply { mkdirs() }
                dir.listFiles()?.forEach { it.delete() } // don't pile up old images
                val file = File(dir, "verses_${System.currentTimeMillis()}.png")
                file.outputStream().use { bitmap.compress(Bitmap.CompressFormat.PNG, 100, it) }

                val uri = FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
                val sendIntent = Intent(Intent.ACTION_SEND).apply {
                    type = "image/png"
                    putExtra(Intent.EXTRA_STREAM, uri)
                    putExtra(Intent.EXTRA_TEXT, context.resources.getString(R.string.share_app))
                    clipData = ClipData.newRawUri("", uri)   // <-- the important line
                    addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                }
                val chooser = Intent.createChooser(sendIntent, "مشاركة").apply {
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                }
                ContextCompat.startActivity(context, chooser, null)
            } catch (e: Exception) {
                e.printStackTrace()
                reportException(e, "VersesSectionViewModel", "shareVerseImage")
                AppToastManager.show(message = "تعذر مشاركة الصورة", type = AppToastType.ERROR)
            }
        }
    }

    companion object {
        const val MAX_IMAGE_CHARS = 1200
    }

    private var _shareMode = MutableStateFlow<ShareMode>(ShareMode.TEXT)
    val shareMode: StateFlow<ShareMode> = _shareMode.asStateFlow()

    fun updateShareMode(value: ShareMode) {
        _shareMode.value = value;
    }


}

class VersesSectionViewModelFactory(
    private val repository: QuranRepository,
    private val application: Application
) : ViewModelProvider.AndroidViewModelFactory(application) {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(VersesSectionViewModel::class.java)) {
            return VersesSectionViewModel(repository, application) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}