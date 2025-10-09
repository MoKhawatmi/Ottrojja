package com.ottrojja.screens.quranScreen

import android.content.Context
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TafseerBottomSheet(
    context: Context,
    showTafseerSheet: Boolean,
    onDismiss: () -> Unit,
    tafseer: String,
    e3rab: String,
    causeOfRevelation: String,
    verseMeanings: String,
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
    val modalBottomSheetState = rememberModalBottomSheetState()

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
                text = when (mode) {
                    TafseerSheetMode.التفسير -> tafseer
                    TafseerSheetMode.الإعراب -> e3rab
                    TafseerSheetMode.أسباب_النزول -> causeOfRevelation
                    TafseerSheetMode.معاني_المفردات -> verseMeanings
                    else -> ""
                },
                copiedMessage = when (mode) {
                    TafseerSheetMode.التفسير -> "تم تسخ التفسير بنجاح"
                    TafseerSheetMode.الإعراب -> "تم تسخ الإعراب بنجاح"
                    TafseerSheetMode.أسباب_النزول -> "تم تسخ سبب النزول بنجاح"
                    TafseerSheetMode.معاني_المفردات -> "تم تسخ معاني المفردات بنجاح"
                    else -> ""
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
                        Text(
                            text = selectedTafseer,
                            color = MaterialTheme.colorScheme.onSecondary,
                            style = MaterialTheme.typography.bodyMedium,
                            textAlign = TextAlign.Right,
                            modifier = Modifier.padding(6.dp, 8.dp)
                        )
                        Icon(
                            Icons.Default.KeyboardArrowDown,
                            contentDescription = "",
                            tint = MaterialTheme.colorScheme.onSecondary,
                            modifier = Modifier.size(32.dp)
                        )
                    }
                }
            }
        }
    }
}
