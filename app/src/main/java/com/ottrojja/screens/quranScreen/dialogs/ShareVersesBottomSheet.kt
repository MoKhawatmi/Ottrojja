package com.ottrojja.screens.quranScreen.dialogs

import android.graphics.Bitmap
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCompositionContext
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.ottrojja.composables.OttrojjaText
import com.ottrojja.composables.buttons.OttrojjaButton
import com.ottrojja.composables.quranRangeSelector.QuranRangeSelector
import com.ottrojja.composables.quranRangeSelector.QuranRangeState
import com.ottrojja.composables.selectableChips.ChipSelector
import com.ottrojja.room.entities.ChapterData
import com.ottrojja.room.entities.PageContent
import com.ottrojja.room.repositories.QuranRepository
import com.ottrojja.screens.quranScreen.classes.ShareMode
import com.ottrojja.screens.quranScreen.composables.VerseImageCard
import com.ottrojja.screens.quranScreen.helpers.findActivity
import com.ottrojja.screens.quranScreen.helpers.renderCardBitmap
import com.ottrojja.ui.theme.OttrojjaTheme
import kotlinx.coroutines.launch


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ShareVersesBottomSheet(
    onDismissRequest: () -> Unit,
    title: String,
    surah: ChapterData?,
    startVerse: PageContent?,
    endVerse: PageContent?,
    onRangeChanged: (QuranRangeState) -> Unit,
    repository: QuranRepository,
    shareMode: ShareMode,
    onShareModeChange: (ShareMode) -> Unit,
    selectedVerses: List<PageContent>?,
    maxImageChars: Int,
    onShareTextClick: () -> Unit,
    onShareImageClick: (Bitmap) -> Unit,
    onValidate: () -> Boolean
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val scope = rememberCoroutineScope()
    val activity = LocalContext.current.findActivity()
    val parent = rememberCompositionContext()
    val charCount = selectedVerses?.sumOf { it.verseText?.length ?: 0 } ?: 0
    val tooLong = (shareMode== ShareMode.IMAGE) && charCount > maxImageChars

    ModalBottomSheet(onDismissRequest = onDismissRequest, sheetState = sheetState) {
        Column(modifier = Modifier.verticalScroll(rememberScrollState()), horizontalAlignment = Alignment.CenterHorizontally) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                OttrojjaText(title, style = OttrojjaTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.primary)
            }

            QuranRangeSelector(
                modifier = Modifier.padding(16.dp),
                range = QuranRangeState(startSurah = surah, startVerse = startVerse, endSurah = surah, endVerse = endVerse),
                onRangeChange = { onRangeChanged(it) },
                repository = repository,
                allowSurahSelection = false,
            )

            Row(
                modifier = Modifier.fillMaxWidth(0.5f),
                horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterHorizontally)
            ) {
                ChipSelector(ShareMode.entries, selected = shareMode, onSelected = {onShareModeChange(it)} )
            }

            if ((shareMode== ShareMode.IMAGE) && selectedVerses != null && surah != null) {
                Spacer(Modifier.height(12.dp))
                if (tooLong) {
                    OttrojjaText(
                        text = "الحد الأقصى للصورة $maxImageChars حرف (الحالي: $charCount). قلّل عدد الآيات.",
                        color = MaterialTheme.colorScheme.error,
                        textAlign = TextAlign.Center,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp)
                    )
                } else {
                    Box(Modifier.padding(horizontal = 2.dp).fillMaxWidth(), contentAlignment = Alignment.Center) {
                        VerseImageCard(
                            chapterName = surah.chapterName ?: "",
                            verses = selectedVerses,
                        )
                    }
                }
            }

            Spacer(Modifier.height(16.dp))
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center) {
                OttrojjaButton(
                    text = "مشاركة",
                    onClick = {
                        if (shareMode== ShareMode.TEXT) {
                            onShareTextClick()
                        } else if (onValidate() && !tooLong && selectedVerses != null && surah != null && activity != null) {
                            scope.launch {
                                try {
                                    val bmp = renderCardBitmap(activity, parent) {
                                        VerseImageCard(chapterName = surah.chapterName
                                            ?: "", verses = selectedVerses
                                        )
                                    }
                                    onShareImageClick(bmp)
                                } catch (e: Exception) {
                                    e.printStackTrace()
                                }
                            }
                        }
                    }
                )
            }
            Spacer(Modifier.height(24.dp))
        }
    }
}