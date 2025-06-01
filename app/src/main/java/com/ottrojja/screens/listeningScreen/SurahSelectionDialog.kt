package com.ottrojja.screens.listeningScreen

import android.text.Selection
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
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
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.ottrojja.R
import com.ottrojja.composables.OttrojjaDialog
import com.ottrojja.composables.PillShapedTextFieldWithIcon
import com.ottrojja.screens.mainScreen.ChapterData

@Composable
fun SurahSelectionDialog(
    onDismiss: () -> Unit,
    chapters: List<ChapterData>,
    searchFilter: String,
    searchFilterChanged: (String) -> Unit,
    selectSurah: (ChapterData) -> Unit,
    selectionPhase: ListeningViewModel.SelectionPhase,
    checkIfChapterDownloaded: (Int) -> Boolean,
    downloadChapter: (Int) -> Unit,
) {
    OttrojjaDialog(onDismissRequest = { onDismiss() },
        contentModifier = Modifier
            .padding(8.dp)
            .fillMaxHeight(0.75f)
            .fillMaxWidth(0.9f)
            .background(MaterialTheme.colorScheme.secondary)
            .padding(8.dp)
            .clip(shape = RoundedCornerShape(12.dp)),
        useDefaultWidth = false
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            Row(
                modifier = Modifier
                    .padding(10.dp)
                    .fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceAround,
                verticalAlignment = Alignment.Bottom
            )
            {
                PillShapedTextFieldWithIcon(
                    value = searchFilter,
                    onValueChange = { newValue -> searchFilterChanged(newValue) },
                    leadingIcon = painterResource(id = R.drawable.search),
                    modifier = Modifier.fillMaxWidth(0.9f),
                    placeHolder = "اسم او رقم السورة"
                )
            }

            LazyColumn(
                Modifier
                    .fillMaxHeight()
            ) {
                items(chapters) { item ->
                    Column(modifier = Modifier
                        .padding(12.dp, 2.dp)
                        .fillMaxWidth()
                        .clickable {
                            selectSurah(item);
                            onDismiss();
                        }
                    ) {
                        Row(
                            modifier = Modifier
                                .padding(12.dp)
                                .fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(text = "${item.surahId}. ${item.chapterName}",
                                    color = MaterialTheme.colorScheme.onSecondary
                                )
                            }
                            Column(
                                verticalArrangement = Arrangement.Bottom,
                                horizontalAlignment = Alignment.End
                            ) {
                                if (selectionPhase == ListeningViewModel.SelectionPhase.PLAY) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        if (!checkIfChapterDownloaded(item.surahId)) {
                                            Image(painter = painterResource(R.drawable.download),
                                                contentDescription = "download",
                                                modifier = Modifier
                                                    .padding(10.dp, 0.dp)
                                                    .clickable { downloadChapter(item.surahId) }
                                                    .size(35.dp),
                                                colorFilter = ColorFilter.tint(
                                                    MaterialTheme.colorScheme.onPrimary
                                                )
                                            )
                                        }
                                    }
                                }
                            }
                        }
                        HorizontalDivider(color = MaterialTheme.colorScheme.onSecondary)
                    }
                }
            }
        }
    }
}