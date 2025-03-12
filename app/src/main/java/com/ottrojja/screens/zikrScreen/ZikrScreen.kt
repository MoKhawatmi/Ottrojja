package com.ottrojja.screens.zikrScreen

import android.app.Application
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
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
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.ottrojja.classes.Helpers.copyToClipboard
import com.ottrojja.classes.QuranRepository
import com.ottrojja.composables.LoadingDialog
import com.ottrojja.composables.MediaController
import com.ottrojja.composables.MediaSlider
import com.ottrojja.composables.OttrojjaTabs
import com.ottrojja.composables.OttrojjaTopBar
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

    if (zikrViewModel.isDownloading) {
        LoadingDialog()
    }

    Column() {

        OttrojjaTopBar {
            Column(modifier = Modifier.fillMaxWidth()) {
                Row(
                    modifier = Modifier
                        .padding(start = 6.dp, end = 6.dp, top = 0.dp, bottom = 6.dp)
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
                            elevation = ButtonDefaults.elevatedButtonElevation(2.dp, 2.dp, 2.dp,
                                2.dp, 2.dp
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
                                elevation = ButtonDefaults.elevatedButtonElevation(2.dp, 2.dp, 2.dp,
                                    2.dp, 2.dp
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
                        elevation = ButtonDefaults.elevatedButtonElevation(2.dp, 2.dp, 2.dp, 2.dp,
                            2.dp
                        ),
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

                OttrojjaTabs(
                    items = ZikrViewModel.ZikrTab.entries,
                    selectedItem = zikrViewModel.selectedTab,
                    onClickTab = { item -> zikrViewModel.selectedTab = item })
            }
        }


        when (zikrViewModel.selectedTab) {
            ZikrViewModel.ZikrTab.الذكر -> ZikrSection(
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
            ZikrViewModel.ZikrTab.الفيديو -> YouTube(link = zikr.value.ytLink.split("v=").last())
        }
    }
}

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
                        style = MaterialTheme.typography.bodyMedium,
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
            MediaController(
                isPlaying = isPlaying && isZikrPlaying,
                playbackSpeed = playbackSpeed,
                isDownloading = false,
                onFasterClicked = { onFasterClicked() },
                onPauseClicked = { onPauseClicked() },
                onPlayClicked = { onPlayClicked() },
                onSlowerClicked = { onSlowerClicked() },
                hasNextPreviousControl = false,
            ) {
                if (isPlaying && isZikrPlaying) {
                    MediaSlider(
                        sliderPosition,
                        { value -> setSliderPosition(value) },
                        sliderMaxDuration
                    )
                }
            }

            /*
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
                }
                Row(
                    modifier = Modifier
                        .fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceAround,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (isPlaying && isZikrPlaying) {
                        Row(
                            horizontalArrangement = Arrangement.Start,
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth(0.15f)
                        ) {
                            Text(
                                text = "${playbackSpeed}x",
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
            }*/
        }
    }
}
