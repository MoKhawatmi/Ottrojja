package com.ottrojja.screens.listeningScreen.dialogs

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import com.ottrojja.composables.ListHorizontalDivider
import com.ottrojja.composables.OttrojjaText
import com.ottrojja.composables.dialogs.OttrojjaDialog
import com.ottrojja.composables.PillShapedTextFieldWithIcon
import com.ottrojja.composables.dialogs.OttrojjaItemSelectionDialog
import com.ottrojja.room.entities.ChapterData
import com.ottrojja.screens.listeningScreen.ListeningViewModel
import com.ottrojja.ui.theme.OttrojjaTheme

@Composable
fun SurahSelectionDialog(
    onDismiss: () -> Unit,
    chapters: List<ChapterData>,
    searchFilter: String,
    searchFilterChanged: (String) -> Unit,
    selectSurah: (ChapterData) -> Unit,
    checkIfChapterDownloaded: (Int) -> Boolean = { false },
    downloadChapter: (Int) -> Unit = {},
    allowDownload: Boolean = false
) {

    OttrojjaItemSelectionDialog(
        title = "إختيار السورة",
        onDismiss = { onDismiss() },
        searchBar = {
            PillShapedTextFieldWithIcon(
                value = searchFilter,
                onValueChange = { newValue -> searchFilterChanged(newValue) },
                leadingIcon = Icons.Default.Search,
                modifier = Modifier.fillMaxWidth(0.9f),
                placeHolder = "اسم او رقم السورة"
            )
        },
        selectionItems = chapters,
        onSelect = { item ->
            selectSurah(item);
            onDismiss();
        },
        itemContent = { item ->

            Row(
                modifier = Modifier
                    .padding(12.dp)
                    .fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    OttrojjaText(
                        text = "${item.surahId}. ${item.chapterName}",
                        color = MaterialTheme.colorScheme.primary,
                        style = OttrojjaTheme.typography.bodyLarge
                    )
                }
                Column(
                    verticalArrangement = Arrangement.Bottom,
                    horizontalAlignment = Alignment.End
                ) {
                    if (allowDownload) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            if (!checkIfChapterDownloaded(item.surahId)) {
                                Icon(
                                    imageVector = Icons.Default.Download,
                                    contentDescription = "download",
                                    modifier = Modifier
                                        .padding(10.dp, 0.dp)
                                        .clickable { downloadChapter(item.surahId) }
                                        .size(32.dp),
                                    tint = MaterialTheme.colorScheme.primary
                                )
                            }
                        }
                    }
                }
            }
        }
    )
}