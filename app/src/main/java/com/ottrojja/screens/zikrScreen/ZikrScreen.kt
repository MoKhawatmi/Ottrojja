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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.ottrojja.R
import com.ottrojja.classes.ButtonAction
import com.ottrojja.classes.Helpers.copyToClipboard
import com.ottrojja.classes.OttrojjaDateTime
import com.ottrojja.composables.MediaController
import com.ottrojja.composables.MediaSlider
import com.ottrojja.composables.OttrojjaTabs
import com.ottrojja.composables.OttrojjaText
import com.ottrojja.composables.SecondaryTopBar
import com.ottrojja.composables.TopBar
import com.ottrojja.composables.dialogs.LoadingDialog
import com.ottrojja.room.entities.Azkar
import com.ottrojja.room.repositories.QuranRepository
import com.ottrojja.screens.quranScreen.composables.YouTube
import com.ottrojja.services.azkar.AzkarPlaybackManager
import com.ottrojja.services.azkar.AzkarPlayerState
import com.ottrojja.ui.theme.OttrojjaTheme

@Composable
fun ZikrScreen(
    zikrId: Long,
    navController: NavController,
    repository: QuranRepository
) {
    val context = LocalContext.current
    val application = context.applicationContext as Application

    val vm: ZikrViewModel = viewModel(
        factory = ZikrViewModelFactory(
            repository,
            AzkarPlaybackManager.getInstance(application),
            application,
        )
    )

    LaunchedEffect(zikrId) {
        vm.fetchZikr(zikrId)
    }

    val ui by vm.ui.collectAsStateWithLifecycle()
    val player by vm.player.collectAsStateWithLifecycle()
    val sliderPosition by vm.sliderPosition.collectAsStateWithLifecycle()

    if (ui.isDownloading) {
        LoadingDialog()
    }

    val zikr = ui.zikr

    Column {
        if (zikr == null) {
            Image(
                painter = painterResource(R.drawable.empty_placeholder),
                modifier = Modifier
                    .padding(8.dp)
                    .fillMaxWidth(),
                contentDescription = "empty placeholder"
            )
            return@Column
        }

        TopBar(
            title = zikr.azkarTitle,
            mainAction = ButtonAction(
                icon = Icons.Filled.ArrowBack,
                action = { navController.popBackStack() }
            ),
            secondaryActions = buildList {
                add(
                    ButtonAction(
                        icon = Icons.Default.ContentCopy,
                        action = { copyToClipboard(context, zikr.azkarText, "تم نسخ الذكر بنجاح") },
                        title = "نسخ"
                    )
                )
                if (!ui.isDownloaded) {
                    add(
                        ButtonAction(
                            icon = Icons.Outlined.DownloadForOffline,
                            action = vm::downloadZikr,
                            title = "تحميل"
                        )
                    )
                }
            }
        )

        SecondaryTopBar {
            OttrojjaTabs(
                items = ZikrViewModel.ZikrTab.entries,
                selectedItem = ui.selectedTab,
                onClickTab = { tab -> vm.selectTab(tab) }
            )
        }

        when (ui.selectedTab) {
            ZikrViewModel.ZikrTab.الذكر -> ZikrSection(
                zikr = zikr,
                showController = ui.showController,
                // only reflect the player when it holds THIS zikr
                player = player.takeIf { it.currentAddress == zikr.zikrAddress } ?: AzkarPlayerState(),
                sliderPosition = sliderPosition,
                onVerticalDrag = vm::onVerticalDrag,
                toggleController = vm::toggleController,
                onPlayClicked = vm::play,
                onPauseClicked = vm::pause,
                onFasterClicked = vm::increasePlaybackSpeed,
                onSlowerClicked = vm::decreasePlaybackSpeed,
                onSliderChanged = vm::onSliderChanged,
            )

            ZikrViewModel.ZikrTab.الفيديو -> YouTube(
                link = zikr.ytLink.substringAfterLast("v=")
            )
        }
    }
}

@Composable
fun ZikrSection(
    zikr: Azkar,
    showController: Boolean,
    player: AzkarPlayerState,
    sliderPosition: Long,
    onVerticalDrag: (Float) -> Unit,
    toggleController: () -> Unit,
    onPlayClicked: () -> Unit,
    onPauseClicked: () -> Unit,
    onFasterClicked: () -> Unit,
    onSlowerClicked: () -> Unit,
    onSliderChanged: (Float) -> Unit,
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .pointerInput(Unit) {
                detectVerticalDragGestures { _, dragAmount -> onVerticalDrag(dragAmount) }
            }
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
            ) { toggleController() }
    ) {
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

        AnimatedVisibility(
            visible = showController,
            enter = slideInVertically(initialOffsetY = { -it }) + expandVertically(expandFrom = Alignment.Bottom),
            exit = slideOutVertically() + shrinkVertically(),
            modifier = Modifier.align(Alignment.BottomCenter)
        ) {
            MediaController(
                isPlaying = player.isPlaying,
                playbackSpeed = player.speed,
                isDownloading = false,
                onFasterClicked = onFasterClicked,
                onPauseClicked = onPauseClicked,
                onPlayClicked = onPlayClicked,
                onSlowerClicked = onSlowerClicked,
                hasNextPreviousControl = false,
            ) {
                // shown while playing AND while paused
                if (player.hasMedia && player.durationMs > 0) {
                    Text(
                        "${OttrojjaDateTime.formatDuration(sliderPosition)}/${OttrojjaDateTime.formatDuration(player.durationMs)}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.primary
                    )
                    MediaSlider(
                        sliderPosition.toFloat(),
                        onSliderChanged,
                        player.durationMs.toFloat()
                    )
                }
            }
        }
    }
}
