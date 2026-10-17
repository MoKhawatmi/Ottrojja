package com.ottrojja.screens.quranScreen.composables

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
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Velocity
import androidx.compose.ui.unit.dp
import com.ottrojja.classes.Helpers.copyToClipboard
import com.ottrojja.classes.Helpers.truncate
import com.ottrojja.composables.OttrojjaElevatedButton
import com.ottrojja.composables.OttrojjaTabs
import com.ottrojja.composables.OttrojjaText
import com.ottrojja.screens.quranScreen.TafseerSheetMode
import com.ottrojja.screens.quranScreen.classes.TafseerContent
import com.ottrojja.ui.theme.OttrojjaTheme


/**
 * Keeps scroll that the tafseer text didn't use (e.g. dragging down while already at the top)
 * from reaching the ModalBottomSheet, so reading never drags or dismisses the sheet.
 * The sheet can still be dragged from the handle / header, or closed by tapping outside or Back.
 */
private object ConsumeRemainingScroll : NestedScrollConnection { // NEW
    override fun onPostScroll(consumed: Offset, available: Offset, source: NestedScrollSource): Offset =
        available.copy(x = 0f)

    override suspend fun onPostFling(consumed: Velocity, available: Velocity): Velocity =
        available.copy(x = 0f)
}


@Composable
fun SelectedVerseContentSection(
    context: Context,
    verseText: String,
    text: List<TafseerContent>,
    copiedMessage: String,
    atFirstVerse: Boolean,
    atLastVerse: Boolean,
    targetNextVerse: () -> Unit,
    targetPreviousVerse: () -> Unit,
    tafseerChapterVerse: String,
    tafseerSheetMode: TafseerSheetMode,
    changeTafseerSheetMode: (TafseerSheetMode) -> Unit,
    content: @Composable () -> Unit = {}
) {

    var verseTextExpanded by remember { mutableStateOf(false) }

    val enclosedVerseText = "﴿$verseText﴾"

    // NEW: one scroll state we can control, reset to the top when the verse or tab changes
    val scrollState = rememberScrollState()
    LaunchedEffect(tafseerChapterVerse, tafseerSheetMode) {
        scrollState.scrollTo(0)
        verseTextExpanded = false
    }

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
                        text.joinToString(separator = " , ") { it.text },
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
            OttrojjaText(text = tafseerChapterVerse,
                style = OttrojjaTheme.typography.bodySpecialLarge,
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

        // CHANGED: the body takes the remaining height (weight), and leftover scroll is consumed
        // before it reaches the sheet. Order matters: nestedScroll must come BEFORE verticalScroll.
        SelectionContainer(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .nestedScroll(ConsumeRemainingScroll)
                .verticalScroll(scrollState)
        ) {
            Column {
                if (verseText.isNotBlank()) {
                    OttrojjaText(
                        text = if (verseTextExpanded) enclosedVerseText else enclosedVerseText.truncate(30),
                        color = MaterialTheme.colorScheme.secondary,
                        modifier = Modifier
                            .padding(10.dp)
                            .fillMaxWidth()
                            .clickable(onClick = { verseTextExpanded = !verseTextExpanded }),
                        style = OttrojjaTheme.typography.quranTextMedium,
                        textAlign = TextAlign.Right,
                    )
                }

                text.forEach {
                    OttrojjaText(
                        text = it.text,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier
                            .padding(10.dp)
                            .fillMaxWidth()
                            .then(
                                if (it.action != null) Modifier.clickable(onClick = { it.action() })
                                else Modifier
                            ),
                        style = OttrojjaTheme.typography.bodyMedium,
                        textAlign = TextAlign.Right,
                    )
                }
            }
        }
    }
}
