package com.ottrojja.screens.quranScreen.composables

import android.R
import android.content.Context
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ottrojja.classes.Helpers
import com.ottrojja.classes.Helpers.copyToClipboard
import com.ottrojja.classes.Helpers.truncate
import com.ottrojja.composables.OttrojjaElevatedButton
import com.ottrojja.composables.OttrojjaTabs
import com.ottrojja.composables.SelectableText
import com.ottrojja.screens.quranScreen.TafseerSheetMode

@Composable
fun SelectedVerseContentSection(
    context: Context,
    verseText: String,
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

    var verseTextExpanded by remember { mutableStateOf(false) }

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
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 6.dp)
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

        SelectionContainer(modifier = Modifier.verticalScroll(rememberScrollState())) {
            Column {
                if (verseText.isNotBlank()) {
                    Text(
                        text = if (verseTextExpanded) "{$verseText}" else "{$verseText}".truncate(30),
                        color = MaterialTheme.colorScheme.secondary,
                        modifier = Modifier
                            .padding(10.dp)
                            .fillMaxWidth()
                            .clickable(onClick = { verseTextExpanded = !verseTextExpanded }),
                        style = MaterialTheme.typography.bodyMedium.copy(fontSize = 21.sp),
                        textAlign = TextAlign.Right,
                    )
                }

                Text(
                    text = text,
                    color = Color.Black,
                    modifier = Modifier
                        .padding(10.dp)
                        .fillMaxWidth(),
                    style = MaterialTheme.typography.bodyMedium.copy(fontSize = 21.sp),
                    textAlign = TextAlign.Right,
                )

            }
        }
    }
}