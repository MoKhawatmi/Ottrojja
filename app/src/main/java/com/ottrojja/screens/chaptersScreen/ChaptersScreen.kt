package com.ottrojja.screens.chaptersScreen

import android.app.Application
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
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
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Circle
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.ottrojja.R
import com.ottrojja.classes.Helpers
import com.ottrojja.classes.Helpers.isMyServiceRunning
import com.ottrojja.classes.MediaPlayerService
import com.ottrojja.classes.QuranRepository
import com.ottrojja.composables.Header
import com.ottrojja.screens.quranScreen.QuranScreenViewModelFactory
import com.ottrojja.screens.quranScreen.QuranViewModel
import com.ottrojja.screens.quranScreen.checkNetworkConnectivity

@OptIn(ExperimentalMaterial3Api::class)
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


    LaunchedEffect(Unit) {
        //chaptersViewModel.startAndBind()
    }

    Column {
        Header()
        Box {
            LazyColumn(
                Modifier
                    .fillMaxHeight(0.9f)
                    .background(MaterialTheme.colorScheme.background)
                    .align(Alignment.TopCenter)
            ) {
                items(chaptersViewModel.chaptersList) { item ->
                    Column(modifier = Modifier
                        .padding(12.dp, 2.dp)
                        .fillMaxWidth()
                        .background(MaterialTheme.colorScheme.background)
                        .clickable {
                            if (checkNetworkConnectivity(context)) {
                                chaptersViewModel.selectSurah(item)
                            } else {
                                Toast
                                    .makeText(
                                        context,
                                        "لا يوجد اتصال بالانترنت",
                                        Toast.LENGTH_LONG
                                    )
                                    .show()
                            }
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
                                    Text(
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
                                    )
                                }
                            }
                        }
                        Box(
                            Modifier
                                .fillMaxWidth()
                                .height(1.dp)
                                .background(Color.Black.copy(alpha = 0.1f))
                        )
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
                        Row(
                            modifier = Modifier
                                .fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceAround,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Slider(
                                value = chaptersViewModel.sliderPosition,
                                onValueChange = { chaptersViewModel.sliderChanged(it) },
                                colors = SliderDefaults.colors(
                                    thumbColor = MaterialTheme.colorScheme.primary,
                                    activeTrackColor = MaterialTheme.colorScheme.primary,
                                    inactiveTrackColor = MaterialTheme.colorScheme.primaryContainer,
                                ),
                                thumb = {
                                    Image(
                                        painterResource(id = R.drawable.thumb),
                                        "thumb",
                                        colorFilter = ColorFilter.tint(MaterialTheme.colorScheme.primary)
                                    )
                                },
                                valueRange = 0f..chaptersViewModel.maxDuration,
                            )
                        }
                    }
                    if (chaptersViewModel.isPlaying) {
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
                                    text = "x${chaptersViewModel.playbackSpeed}",
                                    color = MaterialTheme.colorScheme.primary,
                                    style = MaterialTheme.typography.bodyMedium,
                                    textAlign = TextAlign.Left,
                                )
                            }
                        }
                    }
                    Row(
                        modifier = Modifier
                            .fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceAround,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            horizontalArrangement = Arrangement.Center,
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.weight(1.0f)
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
                                            if (checkNetworkConnectivity(context)) {
                                                chaptersViewModel.play()
                                            } else {
                                                Toast
                                                    .makeText(
                                                        context,
                                                        "لا يوجد اتصال بالانترنت",
                                                        Toast.LENGTH_LONG
                                                    )
                                                    .show()
                                            }
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