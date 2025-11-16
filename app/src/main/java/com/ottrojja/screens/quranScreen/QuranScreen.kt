package com.ottrojja.screens.quranScreen

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.app.Application
import android.content.res.Configuration
import android.util.Log
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.PageSize
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.Cancel
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.outlined.BookmarkBorder
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.ColorMatrix
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.ottrojja.classes.Helpers
import com.ottrojja.classes.Helpers.reportException
import com.ottrojja.room.entities.PageContentItemType
import com.ottrojja.classes.QuranRepository
import com.ottrojja.classes.Screen
import com.ottrojja.composables.IsTablet
import com.ottrojja.composables.ListHorizontalDivider
import com.ottrojja.composables.MediaController
import com.ottrojja.composables.OttrojjaElevatedButton
import com.ottrojja.composables.OttrojjaTabs
import com.ottrojja.screens.quranScreen.dialogs.RepetitionOptionsDialog
import com.ottrojja.composables.SecondaryTopBar
import com.ottrojja.screens.mainScreen.BrowsingOption
import com.ottrojja.screens.quranScreen.composables.Benefits
import com.ottrojja.screens.quranScreen.composables.TafseerBottomSheet
import com.ottrojja.screens.quranScreen.composables.YouTube
import com.ottrojja.screens.quranScreen.dialogs.AddToKhitmahDialog
import com.ottrojja.screens.quranScreen.dialogs.ListeningOptionsDialog
import com.ottrojja.screens.quranScreen.dialogs.PageSelectionDialog
import com.ottrojja.screens.quranScreen.dialogs.SelectTafseerDialog
import com.ottrojja.screens.quranScreen.dialogs.SelectVerseDialog


@SuppressLint("UnrememberedMutableState", "DiscouragedApi")
@Composable
fun QuranScreen(
    navController: NavController,
    pageNum: String,
    repository: QuranRepository
) {
    val context = LocalContext.current
    val application = context.applicationContext as Application
    val configuration = LocalConfiguration.current

    val quranViewModel: QuranViewModel = viewModel(
        factory = QuranScreenViewModelFactory(repository, application)
    )

    fun handleBackBehaviour() {
        if (quranViewModel.selectedTab != QuranViewModel.PageTab.الصفحة) {
            quranViewModel.selectedTab = QuranViewModel.PageTab.الصفحة
        } else {
            navController.popBackStack()
        }
    }

    LaunchedEffect(Unit) {
        try {
            quranViewModel.setCurrentPage(pageNum)
            quranViewModel.getNightReadingMode()
            quranViewModel.fetchKhitmahList()
        } catch (e: Exception) {
            Log.e("error", "Error getting current page in quran screen: $e")
            reportException(exception = e, file = "QuranScreen")
        }
    }

    BackHandler {
        handleBackBehaviour()
    }

    val isPlaying = quranViewModel.isPlaying && quranViewModel.isCurrentPagePlaying;

    fun confirmRemoveBookmark() {
        val alertDialogBuilder = AlertDialog.Builder(context)
        alertDialogBuilder.setTitle("ازالة اشارة مرجعية")
        alertDialogBuilder.setMessage("هل انت متأكد من ازالة هذه الصفحة من المرجعيات")
        alertDialogBuilder.setPositiveButton("نعم") { dialog, which ->
            quranViewModel.togglePageBookmark()
            dialog.dismiss()
        }
        alertDialogBuilder.setNegativeButton("لا") { dialog, which ->
            dialog.dismiss()
        }

        val alertDialog = alertDialogBuilder.create()
        alertDialog.show()
    }

    var expanded by remember { mutableStateOf(false) }

    if (quranViewModel.currentPageObject?.page?.pageNum?.toInt() !in 1..604) {
        Row(modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Text(text = "جاري التحميل..",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.primary
            )
        }
    } else {
        Column() {
            if (configuration.orientation == Configuration.ORIENTATION_PORTRAIT || (configuration.orientation == Configuration.ORIENTATION_LANDSCAPE && IsTablet())) {
                SecondaryTopBar {
                    Column(modifier = Modifier.fillMaxWidth()) {
                        Row(
                            modifier = Modifier
                                .padding(end = 6.dp, start = 6.dp, bottom = 6.dp, top = 0.dp)
                                .background(MaterialTheme.colorScheme.background)
                                .fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                horizontalArrangement = Arrangement.Start,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                OttrojjaElevatedButton(
                                    onClick = { if (!quranViewModel.isBookmarked) quranViewModel.togglePageBookmark() else confirmRemoveBookmark() },
                                    icon = if (quranViewModel.isBookmarked) Icons.Filled.Bookmark else Icons.Outlined.BookmarkBorder
                                )
                                Column {
                                    OttrojjaElevatedButton(
                                        onClick = { expanded = !expanded },
                                        icon = Icons.Default.MoreVert
                                    )
                                    DropdownMenu(
                                        expanded = expanded,
                                        onDismissRequest = { expanded = false }
                                    ) {
                                        DropdownMenuItem(
                                            text = {
                                                Text(
                                                    "مشاركة",
                                                    style = MaterialTheme.typography.bodyMedium,
                                                    color = MaterialTheme.colorScheme.primary
                                                )
                                            },
                                            trailingIcon = {
                                                Icon(
                                                    Icons.Default.Share,
                                                    contentDescription = "share",
                                                    tint = MaterialTheme.colorScheme.primary
                                                )
                                            },
                                            onClick = { quranViewModel.sharePage(); expanded = false; }
                                        )
                                        ListHorizontalDivider()
                                        DropdownMenuItem(
                                            text = {
                                                Text(
                                                    "إضافة الى ختمة",
                                                    style = MaterialTheme.typography.bodyMedium,
                                                    color = MaterialTheme.colorScheme.primary
                                                )
                                            },
                                            trailingIcon = {
                                                Icon(
                                                    Icons.Default.Add,
                                                    contentDescription = "add to khitmah",
                                                    tint = MaterialTheme.colorScheme.primary
                                                )
                                            },
                                            onClick = { quranViewModel.showAddToKhitmahDialog = true; expanded = false; }
                                        )
                                        ListHorizontalDivider()
                                        DropdownMenuItem(
                                            text = {
                                                Text(
                                                    "إنتقال للبحث",
                                                    style = MaterialTheme.typography.bodyMedium,
                                                    color = MaterialTheme.colorScheme.primary
                                                )
                                            },
                                            trailingIcon = {
                                                Icon(
                                                    Icons.Default.Search,
                                                    contentDescription = "move to search",
                                                    tint = MaterialTheme.colorScheme.primary
                                                )
                                            },
                                            onClick = {
                                                navController.navigate(
                                                    Screen.MainScreen.invokeRoute(
                                                        BrowsingOption.البحث
                                                    )
                                                );
                                                expanded = false;
                                            }
                                        )
                                    }
                                }
                            }

                            OttrojjaElevatedButton(
                                onClick = { handleBackBehaviour() },
                                icon = Icons.Filled.ArrowBack
                            )
                        }

                        OttrojjaTabs(
                            items = QuranViewModel.PageTab.entries,
                            selectedItem = quranViewModel.selectedTab,
                            onClickTab = { item ->
                                quranViewModel.selectedTab = item
                            })
                    }
                }
            }

            when (quranViewModel.selectedTab) {
                QuranViewModel.PageTab.الصفحة -> Column(
                    verticalArrangement = Arrangement.SpaceBetween
                ) {
                    PagesContainer(
                        pageNum = quranViewModel.currentPageObject?.page?.pageNum,
                        onPageChanged = { newPage -> quranViewModel.setCurrentPage(newPage) },
                        onPlayClicked = { quranViewModel.prepareForPlaying() },
                        onPauseClicked = { quranViewModel.pausePlaying() },
                        isPlaying = isPlaying,
                        onSlowerClicked = { quranViewModel.decreasePlaybackSpeed() },
                        onFasterClicked = { quranViewModel.increasePlaybackSpeed() },
                        isDownloading = quranViewModel.isDownloading,
                        onNextClicked = { quranViewModel.goNextVerse() },
                        onPreviousClicked = { quranViewModel.goPreviousVerse() },
                        shouldAutoPlay = quranViewModel.shouldAutoPlay,
                        playbackSpeed = quranViewModel.playbackSpeed,
                        nightReadingMode = quranViewModel.nightReadingMode,
                        listeningOptionsClicked = { quranViewModel.showListeningOptionsDialog = true },
                        vmChangedPage = quranViewModel.vmChangedPage,
                        setVmChangedPage = { value -> quranViewModel.vmChangedPage = value },
                        quranPagesNumbers = quranViewModel.quranPagesNumbers,
                        terminatePagePlayerService = { quranViewModel.terminatePagePlayerService() },
                        disableAutoPageSwiping = { quranViewModel.autoSwipePagesWithAudio = false }
                    )
                }

                QuranViewModel.PageTab.التفسير -> VersesSection(
                    quranViewModel.currentPageObject?.pageContent!!.filter { item -> item.type == PageContentItemType.verse },
                    onSheetRequest = { targetVerse, mode ->
                        quranViewModel.tafseerTargetVerse = targetVerse;
                        quranViewModel.tafseerSheetMode = mode
                        quranViewModel.showTafseerSheet = true
                    },
                    repository
                )

                QuranViewModel.PageTab.الفوائد -> Benefits(
                    quranViewModel.currentPageObject?.page?.benefits!!,
                    quranViewModel.currentPageObject?.page?.appliance!!,
                    quranViewModel.currentPageObject?.page?.guidance!!,
                    quranViewModel.currentPageObject?.page?.pageNum!!
                )

                QuranViewModel.PageTab.الفيديو -> YouTube(
                    quranViewModel.currentPageObject?.page?.ytLink!!.split("v=").last()
                )
            }

            TafseerBottomSheet(
                context,
                showTafseerSheet = quranViewModel.showTafseerSheet,
                onDismiss = { quranViewModel.showTafseerSheet = false },
                quranViewModel.verseTafseer,
                quranViewModel.verseE3rab,
                quranViewModel.verseCauseOfRevelation,
                verseMeanings = quranViewModel.verseMeanings,
                selectedTafseer = quranViewModel.selectedTafseer,
                onClickTafseerOptions = { quranViewModel.showTafseerOptions = true },
                mode = quranViewModel.tafseerSheetMode,
                atFirstVerse = quranViewModel.atFirstVerse(),
                atLastVerse = quranViewModel.atLastVerse(),
                targetNextVerse = { quranViewModel.targetNextVerse() },
                targetPreviousVerse = { quranViewModel.targetPreviousVerse() },
                tafseerChapterVerse = quranViewModel.tafseerChapterVerse,
                changeTafseerSheetMode = { mode -> quranViewModel.tafseerSheetMode = mode }
            )
        }
    }


    if (quranViewModel.showListeningOptionsDialog) {
        ListeningOptionsDialog(
            onDismissRequest = { quranViewModel.showListeningOptionsDialog = false },
            selectedVerse = quranViewModel.selectedVerse,
            selectedEndVerse = quranViewModel.selectedEndVerse,
            onSelectVerseClicked = {
                quranViewModel.versesSelectionMode = VersesSelectionMode.START
                quranViewModel.showVerseOptions = true
            },
            onSelectStartPageClicked = {
                quranViewModel.versesSelectionMode = VersesSelectionMode.START
                quranViewModel.showPageSelectionDialog = true
            },
            onSelectEndVerseClicked = {
                quranViewModel.versesSelectionMode = VersesSelectionMode.END
                quranViewModel.showVerseOptions = true;
            },
            onSelectEndPageClicked = {
                quranViewModel.versesSelectionMode = VersesSelectionMode.END
                quranViewModel.showPageSelectionDialog = true;
            },
            continuousPlay = quranViewModel.continuousPlay,
            selectedRepetition = quranViewModel.selectedRepetition,
            onSelectRepetitionClicked = { quranViewModel.showRepOptions = true },
            setContPlay = { value -> quranViewModel.continuousPlay = value },
            repetitionTabs = RepetitionTab.entries,
            selectedRepetitionTab = quranViewModel.selectedRepetitionTab,
            onSelectRepetitionTab = { value -> quranViewModel.selectedRepetitionTab = value },
            startPlayingPage = quranViewModel.startPlayingPage,
            endPlayingPage = quranViewModel.endPlayingPage
        )
    }

    if (quranViewModel.showRepOptions) {
        RepetitionOptionsDialog(
            onDismissRequest = { quranViewModel.showRepOptions = false },
            onSelect = { selectedRep ->
                quranViewModel.selectedRepetition = selectedRep;
                quranViewModel.showRepOptions = false
            }
        )
    }

    if (quranViewModel.showPageSelectionDialog) {
        PageSelectionDialog(
            onDismissRequest = { quranViewModel.showPageSelectionDialog = false },
            pages = quranViewModel.getPagesList(),
            onSelect = { value ->
                if (quranViewModel.versesSelectionMode == VersesSelectionMode.END) {
                    quranViewModel.endPlayingPage = value.toInt();
                } else {
                    quranViewModel.startPlayingPage = value.toInt();
                }
                quranViewModel.updateSelectionVersesList(value)
                quranViewModel.pagesSearchFilter = "";
                quranViewModel.showPageSelectionDialog = false;
            },
            searchFilter = quranViewModel.pagesSearchFilter,
            searchFilterChanged = { value -> quranViewModel.pagesSearchFilter = value }
        )
    }

    if (quranViewModel.showVerseOptions) {
        SelectVerseDialog(
            onDismissRequest = { quranViewModel.showVerseOptions = false },
            onSelect = { selectedVerse ->
                if (quranViewModel.versesSelectionMode == VersesSelectionMode.END) {
                    quranViewModel.selectedEndVerse = selectedVerse;
                } else {
                    quranViewModel.selectedVerse = selectedVerse;
                }
                quranViewModel.showVerseOptions = false;
            },
            versesList = if (quranViewModel.versesSelectionMode == VersesSelectionMode.END) quranViewModel.selectionEndVersesList else quranViewModel.selectionVersesList
        )
    }

    if (quranViewModel.showTafseerOptions) {
        SelectTafseerDialog(
            { quranViewModel.showTafseerOptions = false },
            { selectedTafseer ->
                quranViewModel.updateSelectedTafseer(selectedTafseer);
                quranViewModel.showTafseerOptions = false
            },
            quranViewModel.tafseerNamesMap
        )
    }

    if (quranViewModel.showAddToKhitmahDialog) {
        AddToKhitmahDialog(
            onDismiss = { quranViewModel.showAddToKhitmahDialog = false },
            khitmahList = quranViewModel.khitmahList,
            assignPageToKhitmah = { khitmah -> quranViewModel.assignPageToKhitmah(khitmah) }
        )
    }
}

@Composable
fun PagesContainer(
    pageNum: String?,
    onPageChanged: (String) -> Unit,
    onPlayClicked: () -> Unit,
    onPauseClicked: () -> Unit,
    isPlaying: Boolean,
    onSlowerClicked: () -> Unit,
    onFasterClicked: () -> Unit,
    isDownloading: Boolean,
    onNextClicked: () -> Unit,
    onPreviousClicked: () -> Unit,
    shouldAutoPlay: Boolean,
    playbackSpeed: Float,
    nightReadingMode: Boolean,
    listeningOptionsClicked: () -> Unit,
    vmChangedPage: Boolean,
    setVmChangedPage: (Boolean) -> Unit,
    quranPagesNumbers: List<String>,
    terminatePagePlayerService: () -> Unit,
    disableAutoPageSwiping: () -> Unit
) {

    val pagerState = rememberPagerState(
        initialPage = Integer.parseInt(pageNum) - 1,
        initialPageOffsetFraction = 0f
    ) {
        quranPagesNumbers.size //number of the pages of quran
    }
    var showController by remember { mutableStateOf(true) }
    val hasPageChanged = remember { mutableStateOf(false) } // To track if the page has changed at least once


    LaunchedEffect(pageNum) {
        if (shouldAutoPlay) {
            println("should auto play ${pageNum!!.toInt()}")
            pagerState.animateScrollToPage(pageNum!!.toInt() - 1)
            // onPlayClicked()
        } else {
            pagerState.scrollToPage(pageNum!!.toInt() - 1)
        }
    }

    val vmForceChangePage by rememberUpdatedState(vmChangedPage)
    LaunchedEffect(pagerState) {
        snapshotFlow { pagerState.currentPage }.collect { page ->
            println("vmChangedPage $vmForceChangePage")
            if (!vmForceChangePage) {
                if (hasPageChanged.value) {
                    Log.d("Page change", "Page changed to $page")
                    onPageChanged("${page + 1}")
                } else {
                    hasPageChanged.value = true // Skip the first value
                }
                // when user manually swipes pages when audio is playing, disable the auto swiping
                disableAutoPageSwiping()
            } else {
                setVmChangedPage(false)
            }
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        HorizontalPager(state = pagerState,
            key = { quranPagesNumbers[it] },
            pageSize = PageSize.Fill,
            modifier = Modifier
                .pointerInput(Unit) {
                    detectVerticalDragGestures { _, dragAmount ->
                        showController = dragAmount <= 0
                    }
                }
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null,
                ) { showController = !showController }) { index ->
            SinglePage(quranPagesNumbers[index], nightReadingMode)
        }

        AnimatedVisibility(
            visible = showController,
            enter = slideInVertically(initialOffsetY = { -it }) + expandVertically(
                expandFrom = Alignment.Bottom
            ),
            exit = slideOutVertically() + shrinkVertically(),
            modifier = Modifier.align(Alignment.BottomCenter)
        ) {
            MediaController(
                isPlaying = isPlaying,
                playbackSpeed = playbackSpeed,
                isDownloading = isDownloading,
                onFasterClicked = onFasterClicked,
                onNextClicked = onNextClicked,
                onPauseClicked = onPauseClicked,
                onPlayClicked = onPlayClicked,
                onPreviousClicked = onPreviousClicked,
                onSlowerClicked = onSlowerClicked,
                hasNextPreviousControl = true,
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        if (isPlaying) {
                            Icon(Icons.Filled.Cancel,
                                tint = MaterialTheme.colorScheme.primary,
                                contentDescription = "cancel playing",
                                modifier = Modifier.clickable { terminatePagePlayerService() }
                            )
                        }
                    }

                    Text(text = "خيارات الإستماع",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onPrimary,
                        modifier = Modifier
                            .background(MaterialTheme.colorScheme.primary,
                                shape = RoundedCornerShape(12.dp)
                            )
                            .clickable { listeningOptionsClicked() }
                            .padding(6.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun SinglePage(pageNum: String, nightReadingMode: Boolean) {
    val pagePath = "p_$pageNum"
    val configuration = LocalConfiguration.current
    val context = LocalContext.current

    Column(
        verticalArrangement = Arrangement.Top,
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = if (configuration.orientation == Configuration.ORIENTATION_LANDSCAPE) Modifier.verticalScroll(
            rememberScrollState()
        ) else Modifier,

        ) {
        Image(
            painter = painterResource(
                LocalContext.current.getResources()
                    .getIdentifier(pagePath, "drawable", context.packageName)
            ),
            contentDescription = "page",
            modifier = Modifier
                .fillMaxWidth(
                    if (configuration.orientation == Configuration.ORIENTATION_LANDSCAPE) 0.75f else 1.0f
                )
                .fillMaxHeight(0.92f),
            contentScale = if (configuration.orientation == Configuration.ORIENTATION_LANDSCAPE) ContentScale.Crop else ContentScale.Fit,
            colorFilter = if (!nightReadingMode) null else ColorFilter.colorMatrix(
                ColorMatrix(
                    floatArrayOf(
                        -1f, 0f, 0f, 0f, 255f,
                        0f, -1f, 0f, 0f, 255f,
                        0f, 0f, -1f, 0f, 255f,
                        0f, 0f, 0f, 1f, 0f
                    )
                )
            )
        )
        Row(
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(12.dp, 2.dp)
        ) {
            Row(
                modifier = Modifier
                    .height(2.dp)
                    .weight(0.5f)
                    .background(MaterialTheme.colorScheme.secondary)
            ) {}
            Text(
                text = "صفحة ${Helpers.convertToIndianNumbers(pageNum)}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.secondary,
                modifier = Modifier
                    .padding(4.dp, 0.dp)
                    .offset(y = -2.dp)
            )
            Row(
                modifier = Modifier
                    .height(2.dp)
                    .weight(0.5f)
                    .background(MaterialTheme.colorScheme.secondary)
            ) {}
        }
    }
}
