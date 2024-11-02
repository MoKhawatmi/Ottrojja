package com.ottrojja.screens.zikrScreen

import android.app.Application
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.outlined.DownloadForOffline
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ElevatedButton
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.ottrojja.R
import com.ottrojja.classes.Helpers.copyToClipboard
import com.ottrojja.classes.QuranRepository
import com.ottrojja.composables.LegacySeekBar
import com.ottrojja.composables.LoadingDialog
import com.ottrojja.composables.MediaSeekBar
import com.ottrojja.composables.MediaSlider
import com.ottrojja.screens.azkarScreen.Azkar
import com.ottrojja.screens.quranScreen.NoRippleInteractionSource
import com.ottrojja.screens.quranScreen.YouTube

@Composable
fun ZikrScreen(
    zikrTitle: String,
    navController: NavController,
    repository: QuranRepository
) {
    val context = LocalContext.current;
    val application = context.applicationContext as Application

    val zikrViewModel: ZikrViewModel = viewModel(
        factory = ZikrViewModelFactory(repository, application, zikrTitle)
    )

    val zikr = zikrViewModel._zikr.collectAsState()

    val primaryColor = MaterialTheme.colorScheme.primary

    if (zikrViewModel.isDownloading) {
        LoadingDialog()
    }

    Column() {
        Row(
            modifier = Modifier
                .padding(6.dp)
                .background(MaterialTheme.colorScheme.background)
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {

            Row {
                ElevatedButton(
                    onClick = {
                        copyToClipboard(
                            context,
                            zikr.value.azkarText,
                            "تم تسخ الذكر بنجاح"
                        );
                    },
                    elevation = ButtonDefaults.elevatedButtonElevation(
                        2.dp,
                        2.dp,
                        2.dp,
                        2.dp,
                        2.dp
                    ),
                    contentPadding = PaddingValues(0.dp),
                    shape = CircleShape,
                    modifier = Modifier
                        .padding(4.dp, 0.dp)
                        .clip(CircleShape)
                ) {
                    Icon(
                        Icons.Default.ContentCopy,
                        contentDescription = "Copy",
                        tint = MaterialTheme.colorScheme.primary,
                    )
                }
                if (!zikrViewModel.checkIfZikrDownloaded()) {
                    ElevatedButton(
                        onClick = { zikrViewModel.downloadZikr() },
                        elevation = ButtonDefaults.elevatedButtonElevation(
                            2.dp,
                            2.dp,
                            2.dp,
                            2.dp,
                            2.dp
                        ),
                        contentPadding = PaddingValues(0.dp),
                        shape = CircleShape,
                        modifier = Modifier
                            .padding(4.dp, 0.dp)
                            .clip(CircleShape)
                    ) {
                        Icon(
                            Icons.Outlined.DownloadForOffline,
                            contentDescription = "Download",
                            tint = MaterialTheme.colorScheme.primary,
                        )
                    }
                }
            }


            ElevatedButton(
                onClick = { navController.popBackStack() },
                elevation = ButtonDefaults.elevatedButtonElevation(2.dp, 2.dp, 2.dp, 2.dp, 2.dp),
                contentPadding = PaddingValues(0.dp),
                shape = CircleShape,
                modifier = Modifier
                    .padding(0.dp)
                    .clip(CircleShape)
            ) {
                Icon(
                    Icons.Filled.ArrowBack,
                    contentDescription = "Back",
                    tint = MaterialTheme.colorScheme.primary,
                )
            }
        }
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            zikrViewModel.zikrTabs.forEach { option ->
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        text = option,
                        style = MaterialTheme.typography.bodyMedium,
                        textAlign = TextAlign.Center,
                        color = if (zikrViewModel.selectedTab == option) MaterialTheme.colorScheme.onPrimary else primaryColor,
                        modifier = Modifier
                            .padding(4.dp, 0.dp)
                            .clip(shape = RoundedCornerShape(50))
                            .drawBehind {
                                if (zikrViewModel.selectedTab == option) {
                                    drawCircle(
                                        color = primaryColor,
                                        radius = this.size.maxDimension
                                    )
                                }
                            }
                            .clickable { zikrViewModel.selectedTab = option }
                            .defaultMinSize(minWidth = 100.dp)
                            .padding(16.dp, 6.dp, 16.dp, 6.dp)
                    )
                }
            }
        }
        when (zikrViewModel.selectedTab) {
            "الذكر" -> ZikrSection(
                zikr = zikr.value,
                zikrViewModel.showController,
                zikrViewModel.isPlaying,
                zikrViewModel.isZikrPlaying,
                { dragAmount -> zikrViewModel.showController = dragAmount <= 0 },
                { zikrViewModel.showController = !zikrViewModel.showController },
                { zikrViewModel.playClicked() },
                { zikrViewModel.increasePlaybackSpeed() },
                { zikrViewModel.decreasePlaybackSpeed() },
                { zikrViewModel.pauseZikr() },
                zikrViewModel.maxDuration,
                zikrViewModel.sliderPosition,
                { value -> zikrViewModel.sliderChanged(value) },
                zikrViewModel.playbackSpeed
            );
            "الفيديو" -> YouTube(link = zikr.value.ytLink.split("v=").last())
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ZikrSection(
    zikr: Azkar,
    showController: Boolean,
    isPlaying: Boolean,
    isZikrPlaying: Boolean,
    checkDrag: (Float) -> Unit,
    toggleController: () -> Unit,
    onPlayClicked: () -> Unit,
    onFasterClicked: () -> Unit,
    onSlowerClicked: () -> Unit,
    onPauseClicked: () -> Unit,
    sliderMaxDuration: Float,
    sliderPosition: Float,
    setSliderPosition: (Float) -> Unit,
    playbackSpeed: Float
) {
    Box(modifier = Modifier
        .fillMaxSize()
        .pointerInput(Unit) {
            detectVerticalDragGestures { _, dragAmount ->
                checkDrag(dragAmount)
            }
        }
        .clickable(
            indication = null,
            interactionSource = NoRippleInteractionSource()
        ) { toggleController() }
    ) {
        Column {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                Text(
                    text = zikr.azkarTitle,
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(2.dp)
                )
            }

            Column(
                modifier = Modifier
                    .padding(12.dp)
                    .border(
                        BorderStroke(1.dp, MaterialTheme.colorScheme.onPrimary),
                        shape = RoundedCornerShape(12)
                    )
                    .clip(RoundedCornerShape(12))
                    .verticalScroll(rememberScrollState())
                    .background(MaterialTheme.colorScheme.primary)
                    .padding(8.dp, 20.dp, 8.dp, 8.dp)
            ) {
                Row(modifier = Modifier.fillMaxWidth()) {
                    Text(
                        text = zikr.azkarText,
                        style = MaterialTheme.typography.displayMedium,
                        color = MaterialTheme.colorScheme.onPrimary,
                        modifier = Modifier.padding(2.dp, 8.dp)
                    )
                }
            }
        }


        AnimatedVisibility(
            visible = showController,
            enter = slideInVertically(
                initialOffsetY = { -it }
            ) + expandVertically(
                expandFrom = Alignment.Bottom
            ),
            exit = slideOutVertically() + shrinkVertically(),
            modifier = Modifier.align(Alignment.BottomCenter)
        ) {
            Column(
                modifier = Modifier
                    .background(MaterialTheme.colorScheme.background.copy(alpha = 0.5f))
                    .fillMaxWidth()
                    .padding(10.dp),
                verticalArrangement = Arrangement.SpaceBetween,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                if (isPlaying && isZikrPlaying) {
                    MediaSlider(
                        sliderPosition,
                        { value -> setSliderPosition(value) },
                        sliderMaxDuration
                    )
                    /*Row(
                        modifier = Modifier
                            .fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceAround,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Slider(
                            value = sliderPosition,
                            onValueChange = { setSliderPosition(it) },
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
                            valueRange = 0f..sliderMaxDuration,
                        )
                    }*/
                }
                if (isPlaying && isZikrPlaying) {
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
                                text = "${playbackSpeed}x",
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
                        if (isPlaying && isZikrPlaying) {
                            Image(painter = painterResource(R.drawable.faster),
                                contentDescription = "faster",
                                modifier = Modifier
                                    .clickable { onFasterClicked() }
                                    .size(25.dp)
                            )
                        }
                        if (isPlaying && isZikrPlaying) {
                            Image(painter = painterResource(R.drawable.playing),
                                contentDescription = "pause",
                                modifier = Modifier
                                    .padding(10.dp, 0.dp)
                                    .clickable { onPauseClicked() }
                                    .size(35.dp)
                            )
                            //  ReplayIcon(selectedRep, onClickUpdateRep)
                        } else {
                            Image(painter = painterResource(R.drawable.play),
                                contentDescription = "play",
                                modifier = Modifier
                                    .clickable { onPlayClicked() }
                                    .size(35.dp),
                                colorFilter = ColorFilter.tint(MaterialTheme.colorScheme.primary)
                            )
                        }
                        if (isPlaying && isZikrPlaying) {
                            Image(painter = painterResource(R.drawable.slower),
                                contentDescription = "slower",
                                modifier = Modifier
                                    .clickable { onSlowerClicked() }
                                    .size(25.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}
