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
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.outlined.DownloadForOffline
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.ottrojja.R
import com.ottrojja.classes.ButtonAction
import com.ottrojja.classes.Helpers.copyToClipboard
import com.ottrojja.room.repositories.QuranRepository
import com.ottrojja.composables.dialogs.LoadingDialog
import com.ottrojja.composables.MediaController
import com.ottrojja.composables.MediaSlider
import com.ottrojja.composables.OttrojjaTabs
import com.ottrojja.composables.OttrojjaText
import com.ottrojja.composables.SecondaryTopBar
import com.ottrojja.composables.TopBar
import com.ottrojja.room.entities.Azkar
import com.ottrojja.screens.quranScreen.composables.YouTube
import com.ottrojja.ui.theme.OttrojjaTheme

@Composable
fun ZikrScreen(
    zikrId: Long,
    navController: NavController,
    repository: QuranRepository
) {
    val context = LocalContext.current
    val application = context.applicationContext as Application

    val zikrViewModel: ZikrViewModel = viewModel(
        factory = ZikrViewModelFactory(
            repository,
            application,
        )
    )

    LaunchedEffect(zikrId) {
        zikrViewModel.fetchZikr(zikrId)
    }

    // Delegate + local val so Kotlin can smart-cast to non-null
    val zikrState by zikrViewModel.zikr.collectAsState()
    val currentZikr = zikrState

    if (zikrViewModel.isDownloading) {
        LoadingDialog()
    }

    Column {

        if (currentZikr == null) {
            Image(
                painter = painterResource(R.drawable.empty_placeholder),
                modifier = Modifier
                    .padding(8.dp)
                    .fillMaxWidth(),
                contentDescription = "empty placeholder"
            )
        } else {
            TopBar(
                title = currentZikr.azkarTitle,
                mainAction = ButtonAction(
                    icon = Icons.Filled.ArrowBack,
                    action = { navController.popBackStack() }
                ),
                secondaryActions = buildList {
                    add(
                        ButtonAction(
                            icon = Icons.Default.ContentCopy,
                            action = {
                                copyToClipboard(context, currentZikr.azkarText, "تم نسخ الذكر بنجاح")
                            },
                            title = "نسخ"
                        )
                    )
                    if (!zikrViewModel.isDownloaded) {
                        add(
                            ButtonAction(
                                icon = Icons.Outlined.DownloadForOffline,
                                action = { zikrViewModel.downloadZikr() },
                                title = "تحميل"
                            )
                        )
                    }
                }
            )

            SecondaryTopBar {
                OttrojjaTabs(
                    items = ZikrViewModel.ZikrTab.entries,
                    selectedItem = zikrViewModel.selectedTab,
                    onClickTab = { item -> zikrViewModel.selectedTab = item }
                )
            }

            when (zikrViewModel.selectedTab) {
                ZikrViewModel.ZikrTab.الذكر -> ZikrSection(
                    zikr = currentZikr,
                    showController = zikrViewModel.showController,
                    isPlaying = zikrViewModel.isPlaying,
                    checkDrag = { dragAmount -> zikrViewModel.showController = dragAmount <= 0 },
                    toggleController = { zikrViewModel.showController = !zikrViewModel.showController },
                    onPlayClicked = { zikrViewModel.playClicked() },
                    onFasterClicked = { zikrViewModel.increasePlaybackSpeed() },
                    onSlowerClicked = { zikrViewModel.decreasePlaybackSpeed() },
                    onPauseClicked = { zikrViewModel.pauseZikr() },
                    sliderMaxDuration = zikrViewModel.maxDuration,
                    sliderPosition = zikrViewModel.sliderPosition,
                    setSliderPosition = { value -> zikrViewModel.sliderChanged(value) },
                    playbackSpeed = zikrViewModel.playbackSpeed,
                    progressTimeCodeDisplay = zikrViewModel.progressTimeCodeDisplay
                )

                ZikrViewModel.ZikrTab.الفيديو -> YouTube(
                    link = currentZikr.ytLink.substringAfterLast("v=")
                )
            }
        }
    }
}

@Composable
fun ZikrSection(
    zikr: Azkar,
    showController: Boolean,
    isPlaying: Boolean,
    checkDrag: (Float) -> Unit,
    toggleController: () -> Unit,
    onPlayClicked: () -> Unit,
    onFasterClicked: () -> Unit,
    onSlowerClicked: () -> Unit,
    onPauseClicked: () -> Unit,
    sliderMaxDuration: Float,
    sliderPosition: Float,
    setSliderPosition: (Float) -> Unit,
    playbackSpeed: Float,
    progressTimeCodeDisplay: String
) {
    Box(modifier = Modifier
        .fillMaxSize()
        .pointerInput(Unit) {
            detectVerticalDragGestures { _, dragAmount ->
                checkDrag(dragAmount)
            }
        }
        .clickable(
            interactionSource = remember { MutableInteractionSource() },
            indication = null,
        ) { toggleController() }
    ) {
        Column {
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
                    OttrojjaText(
                        text = zikr.azkarText,
                        style = OttrojjaTheme.typography.bodyMedium,
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
                isPlaying = isPlaying,
                playbackSpeed = playbackSpeed,
                isDownloading = false,
                onFasterClicked = { onFasterClicked() },
                onPauseClicked = { onPauseClicked() },
                onPlayClicked = { onPlayClicked() },
                onSlowerClicked = { onSlowerClicked() },
                hasNextPreviousControl = false,
            ) {
                if (isPlaying) {
                    Text(
                        progressTimeCodeDisplay,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.primary
                    )
                    MediaSlider(
                        sliderPosition,
                        { value -> setSliderPosition(value) },
                        sliderMaxDuration
                    )
                }
            }
        }
    }
}