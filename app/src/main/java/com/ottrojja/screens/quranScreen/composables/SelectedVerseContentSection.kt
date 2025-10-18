package com.ottrojja.screens.quranScreen.composables

import android.content.Context
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.ottrojja.classes.Helpers.copyToClipboard
import com.ottrojja.composables.OttrojjaElevatedButton
import com.ottrojja.composables.OttrojjaTabs
import com.ottrojja.composables.SelectableText
import com.ottrojja.screens.quranScreen.TafseerSheetMode

@Composable
fun SelectedVerseContentSection(
    context: Context,
    text: String,
    copiedMessage: String,
    atFirstVerse: Boolean,
    atLastVerse: Boolean,
    targetNextVerse: () -> Unit,
    targetPreviousVerse: () -> Unit,
    tafseerChapterVerse: String,
    tafseerSheetMode: TafseerSheetMode,
    changeTafseerSheetMode: (TafseerSheetMode) -> Unit,
    content: @Composable() () -> Unit = {}
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.tertiary)
    ) {
        Row(
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Top,
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Column {
                    if (!atFirstVerse) {
                        OttrojjaElevatedButton(
                            onClick = { targetPreviousVerse() },
                            icon = Icons.Default.ChevronRight,
                            iconSize = 32.dp
                        )
                    }
                }

                Column {
                    if (!atLastVerse) {
                        OttrojjaElevatedButton(
                            onClick = { targetNextVerse() },
                            icon = Icons.Default.ChevronLeft,
                            iconSize = 32.dp
                        )
                    }
                }
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                OttrojjaElevatedButton(onClick = {
                    copyToClipboard(
                        context,
                        text,
                        copiedMessage
                    )
                }, icon = Icons.Default.ContentCopy)
            }
        }
        Row(verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center,
            modifier = Modifier.fillMaxWidth().padding(bottom = 6.dp)
        ) {
            Text(text = tafseerChapterVerse,
                style = MaterialTheme.typography.displayLarge,
                color = MaterialTheme.colorScheme.secondary,
                textAlign = TextAlign.Center
            )
        }

        OttrojjaTabs(
            items = TafseerSheetMode.entries,
            selectedItem = tafseerSheetMode,
            onClickTab = { changeTafseerSheetMode(it) }
        )

        HorizontalDivider(color = MaterialTheme.colorScheme.secondary,
            modifier = Modifier.padding(vertical = 8.dp, horizontal = 8.dp)
        )

        content()

        SelectableText(text)
    }
}