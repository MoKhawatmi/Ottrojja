package com.ottrojja.composables

import android.content.Context
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.ottrojja.classes.Helpers.copyToClipboard

@Composable
fun SelectedVerseContentSection(
    context: Context,
    text: String,
    copiedMessage: String,
    atFirstVerse: Boolean,
    atLastVerse: Boolean,
    targetNextVerse: () -> Unit,
    targetPreviousVerse: () -> Unit,
    content: @Composable() () -> Unit = {},
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.tertiary)
    ) {
        content()
        Row(
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Top,
            modifier = Modifier.fillMaxWidth()

        ) {
            Row(
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.Top,
            ) {
                if (!atFirstVerse) {
                    OttrojjaElevatedButton(
                        onClick = { targetPreviousVerse() },
                        icon = Icons.Default.ChevronRight,
                        iconSize = 30.dp
                    )
                }
                if (!atLastVerse) {
                    OttrojjaElevatedButton(
                        onClick = { targetNextVerse() },
                        icon = Icons.Default.ChevronLeft,
                        iconSize = 30.dp
                    )
                }
            }

            Row(
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.Top,
            ) {
                OttrojjaElevatedButton(onClick = {
                    copyToClipboard(
                        context,
                        text,
                        copiedMessage
                    )
                }, icon = Icons.Default.ContentCopy)
            }
        }
        SelectableText(text)
    }
}