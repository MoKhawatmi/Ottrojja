package com.ottrojja.screens.chaptersScreen

import android.app.Application
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.ottrojja.R
import com.ottrojja.classes.QuranRepository
import com.ottrojja.composables.Header
import com.ottrojja.composables.LoadingDialog
import com.ottrojja.composables.MediaSlider
import com.ottrojja.composables.PillShapedTextFieldWithIcon

@Composable
fun ChaptersScreen(
    modifier: Modifier = Modifier,
    repository: QuranRepository
) {
    val interactionSource = remember { MutableInteractionSource() }
    val context = LocalContext.current
    val application = context.applicationContext as Application

    val chaptersViewModel: ChaptersViewModel = viewModel(
        factory = ChaptersViewModelFactory(repository, application)
    )

    if (chaptersViewModel.isDownloading) {
        LoadingDialog()
    }

    Column {
        Header()
        Row(
            modifier = Modifier
                .padding(10.dp)
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceAround,
            verticalAlignment = Alignment.Bottom
        )
        {
            PillShapedTextFieldWithIcon(
                value = chaptersViewModel.searchFilter,
                onValueChange = { newValue -> chaptersViewModel.searchFilter = newValue },
                leadingIcon = painterResource(id = R.drawable.search),
                modifier = Modifier.fillMaxWidth(0.9f),
                placeHolder = "اسم او رقم السورة"
            )
        }
        Box {
            LazyColumn(
                Modifier
                    .fillMaxHeight(0.9f)
                    .background(MaterialTheme.colorScheme.background)
                    .align(Alignment.TopCenter)
            ) {
                items(chaptersViewModel.getChaptersList()) { item ->
                    Column(modifier = Modifier
                        .padding(12.dp, 2.dp)
                        .fillMaxWidth()
                        .background(MaterialTheme.colorScheme.background)
                        .clickable {
                            chaptersViewModel.selectSurah(item)
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
                                Text(text = item.chapterName, color = Color.Black)
                            }
                            Column(
                                verticalArrangement = Arrangement.Bottom,
                                horizontalAlignment = Alignment.End
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    if (chaptersViewModel.selectedSurah == item) {
                                        Image(painter = painterResource(R.drawable.playing),
                                            contentDescription = "pause",
                                            modifier = Modifier
                                                .padding(10.dp, 0.dp)
                                                .clickable { chaptersViewModel.pause() }
                                                .size(35.dp)
                                        )
                                    }
                                    if (!chaptersViewModel.checkIfChapterDownloaded(item.surahId)) {
                                        Image(painter = painterResource(R.drawable.download),
                                            contentDescription = "download",
                                            modifier = Modifier
                                                .padding(10.dp, 0.dp)
                                                .clickable { chaptersViewModel.downloadChapter(item.surahId) }
                                                .size(35.dp),
                                            colorFilter = ColorFilter.tint(MaterialTheme.colorScheme.primary)
                                        )
                                    }
                                    /*Text(
                                        text = "${Helpers.convertToIndianNumbers("${item.verseCount}")} اية",
                                        color = Color.Black,
                                        style = MaterialTheme.typography.bodyMedium,
                                        fontWeight = FontWeight(700)
                                    )
                                    Icon(
                                        Icons.Filled.Circle,
                                        contentDescription = "",
                                        tint = MaterialTheme.colorScheme.secondary,
                                        modifier = Modifier
                                            .padding(6.dp, 0.dp)
                                            .size(10.dp)
                                            .offset(y = 4.dp)
                                    )
                                    Text(
                                        text = item.chapterType,
                                        color = Color.Black,
                                        style = MaterialTheme.typography.bodyMedium
                                    )*/
                                }
                            }
                        }
                        HorizontalDivider(thickness = 1.dp, color = Color.Black.copy(alpha = 0.1f))
                    }
                }
                item {
                    Row(modifier = Modifier.height(160.dp)) {}
                }
            }

            if (((chaptersViewModel.isPlaying && chaptersViewModel.isChapterPlaying) || chaptersViewModel.isPaused) && chaptersViewModel.selectedSurah.surahId != 0) {
                Column(
                    modifier = Modifier
                        .background(MaterialTheme.colorScheme.background)
                        .fillMaxWidth()
                        .align(Alignment.BottomCenter)
                        .clickable(
                            interactionSource = interactionSource,
                            indication = null
                        ) { /*without this, when the column is clicked the element behind it will be clicked*/ }
                        .padding(10.dp),
                    verticalArrangement = Arrangement.SpaceBetween,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = chaptersViewModel.selectedSurah.chapterName,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.primary
                    )
                    if (chaptersViewModel.isPlaying && chaptersViewModel.isChapterPlaying) {
                        MediaSlider(
                            sliderPosition = chaptersViewModel.sliderPosition,
                            setSliderPosition = { value -> chaptersViewModel.sliderChanged(value) },
                            sliderMaxDuration = chaptersViewModel.maxDuration
                        )
                    }
                    /*if (chaptersViewModel.isPlaying && chaptersViewModel.isChapterPlaying) {
                        Row(
                            modifier = Modifier
                                .background(color = MaterialTheme.colorScheme.primaryContainer)
                                .fillMaxWidth()
                                .padding(8.dp),
                            horizontalArrangement = Arrangement.End,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                horizontalArrangement = Arrangement.Start,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "${chaptersViewModel.playbackSpeed}x",
                                    color = MaterialTheme.colorScheme.primary,
                                    style = MaterialTheme.typography.bodyMedium,
                                    textAlign = TextAlign.Left,
                                )
                            }
                        }
                    }*/
                    Row(
                        modifier = Modifier
                            .fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceAround,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        if (chaptersViewModel.isPlaying && chaptersViewModel.isChapterPlaying) {
                            Row(
                                horizontalArrangement = Arrangement.Start,
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.fillMaxWidth(0.15f)
                            ) {
                                Text(
                                    text = "${chaptersViewModel.playbackSpeed}x",
                                    color = MaterialTheme.colorScheme.primary,
                                    style = MaterialTheme.typography.bodySmall,
                                    textAlign = TextAlign.Right,
                                )
                            }
                        }

                        Row(
                            horizontalArrangement = Arrangement.Center,
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            if (chaptersViewModel.isPlaying && chaptersViewModel.isChapterPlaying) {
                                Image(painter = painterResource(R.drawable.faster),
                                    contentDescription = "faster",
                                    modifier = Modifier
                                        .clickable { chaptersViewModel.increasePlaybackSpeed() }
                                        .size(25.dp)
                                )
                                Image(painter = painterResource(R.drawable.next),
                                    contentDescription = "next",
                                    modifier = Modifier
                                        .padding(6.dp)
                                        .clickable { chaptersViewModel.goNextChapter() }
                                        .size(25.dp),
                                    colorFilter = ColorFilter.tint(MaterialTheme.colorScheme.primary)
                                )
                            }
                            if (chaptersViewModel.isPlaying && chaptersViewModel.isChapterPlaying) {
                                Image(painter = painterResource(R.drawable.playing),
                                    contentDescription = "pause",
                                    modifier = Modifier
                                        .padding(10.dp, 0.dp)
                                        .clickable { chaptersViewModel.pause() }
                                        .size(35.dp)
                                )
                            } else {
                                Image(painter = painterResource(R.drawable.play),
                                    contentDescription = "play",
                                    modifier = Modifier
                                        .clickable {
                                            chaptersViewModel.play()
                                        }
                                        .size(35.dp),
                                    colorFilter = ColorFilter.tint(MaterialTheme.colorScheme.primary)
                                )
                            }
                            if (chaptersViewModel.isPlaying && chaptersViewModel.isChapterPlaying) {
                                Image(painter = painterResource(R.drawable.previous),
                                    contentDescription = "prev",
                                    modifier = Modifier
                                        .padding(6.dp)
                                        .clickable { chaptersViewModel.goPreviousChapter() }
                                        .size(25.dp),
                                    colorFilter = ColorFilter.tint(MaterialTheme.colorScheme.primary)
                                )
                                Image(painter = painterResource(R.drawable.slower),
                                    contentDescription = "slower",
                                    modifier = Modifier
                                        .clickable { chaptersViewModel.decreasePlaybackSpeed() }
                                        .size(25.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}