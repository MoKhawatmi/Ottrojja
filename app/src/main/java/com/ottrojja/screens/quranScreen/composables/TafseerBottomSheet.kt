package com.ottrojja.screens.quranScreen.composables

import android.content.Context
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material3.BottomSheetDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.ottrojja.composables.OttrojjaText
import com.ottrojja.screens.quranScreen.TafseerSheetMode
import com.ottrojja.screens.quranScreen.classes.TafseerContent
import com.ottrojja.ui.theme.OttrojjaTheme


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TafseerBottomSheet(
    context: Context,
    showTafseerSheet: Boolean,
    onDismiss: () -> Unit,
    verseText: String,
    contentText: List<TafseerContent>,
    selectedTafseer: String,
    onClickTafseerOptions: () -> Unit,
    mode: TafseerSheetMode,
    atFirstVerse: Boolean,
    atLastVerse: Boolean,
    targetNextVerse: () -> Unit,
    targetPreviousVerse: () -> Unit,
    tafseerChapterVerse: String,
    changeTafseerSheetMode: (TafseerSheetMode) -> Unit
) {
    // CHANGED: open fully expanded so an upward swipe scrolls the text instead of growing the sheet
    val modalBottomSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    if (showTafseerSheet) {
        ModalBottomSheet(
            onDismissRequest = { onDismiss() },
            sheetState = modalBottomSheetState,
            containerColor = MaterialTheme.colorScheme.primary,
            dragHandle = {
                BottomSheetDefaults.DragHandle(color = MaterialTheme.colorScheme.onPrimary)
            },
        ) {
            SelectedVerseContentSection(
                context = context,
                verseText = verseText,
                text = contentText,
                copiedMessage = when (mode) {
                    TafseerSheetMode.التفسير -> "تم نسخ التفسير بنجاح"            // CHANGED: تسخ -> نسخ
                    TafseerSheetMode.الإعراب -> "تم نسخ الإعراب بنجاح"            // CHANGED
                    TafseerSheetMode.أسباب_النزول -> "تم نسخ سبب النزول بنجاح"     // CHANGED
                    TafseerSheetMode.معاني_المفردات -> "تم نسخ معاني المفردات بنجاح" // CHANGED
                    TafseerSheetMode.المشابهات -> "تم نسخ الآيات المشابهات بنجاح"
                },
                atFirstVerse = atFirstVerse,
                atLastVerse = atLastVerse,
                targetNextVerse = { targetNextVerse() },
                targetPreviousVerse = { targetPreviousVerse() },
                tafseerChapterVerse = tafseerChapterVerse,
                tafseerSheetMode = mode,
                changeTafseerSheetMode = { mode -> changeTafseerSheetMode(mode) }
            ) {
                if (mode == TafseerSheetMode.التفسير) {
                    Row(horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .fillMaxWidth(0.8f)
                            .background(MaterialTheme.colorScheme.secondary,
                                shape = RoundedCornerShape(50)
                            )
                            .border(width = 2.dp, color = MaterialTheme.colorScheme.primary,
                                shape = RoundedCornerShape(50)
                            )
                            .clickable { onClickTafseerOptions() }) {
                        OttrojjaText(
                            text = selectedTafseer,
                            color = MaterialTheme.colorScheme.onSecondary,
                            style = OttrojjaTheme.typography.bodyMedium,
                            textAlign = TextAlign.Right,
                            modifier = Modifier.padding(6.dp, 8.dp)
                        )
                        Icon(
                            Icons.Default.KeyboardArrowDown,
                            contentDescription = null, // CHANGED: decorative icon, TalkBack skips it
                            tint = MaterialTheme.colorScheme.onSecondary,
                            modifier = Modifier.size(32.dp)
                        )
                    }
                }
            }
        }
    }
}


