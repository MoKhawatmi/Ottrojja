package com.ottrojja.screens.quranScreen

import android.app.Application
import android.content.res.Configuration
import android.util.Log
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.PageSize
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.Cancel
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.StopCircle
import androidx.compose.material.icons.outlined.BookmarkBorder
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.ottrojja.classes.Helpers.convertToIndianNumbers
import com.ottrojja.classes.Helpers.reportException
import com.ottrojja.room.entities.PageContent
import com.ottrojja.room.entities.PageContentItemType
import com.ottrojja.room.repositories.QuranRepository
import com.ottrojja.classes.Screen
import com.ottrojja.composables.IsTablet
import com.ottrojja.composables.ListHorizontalDivider
import com.ottrojja.composables.MediaController
import com.ottrojja.composables.OttrojjaElevatedButton
import com.ottrojja.composables.OttrojjaTabs
import com.ottrojja.composables.OttrojjaText
import com.ottrojja.screens.quranScreen.dialogs.RepetitionOptionsDialog
import com.ottrojja.composables.SecondaryTopBar
import com.ottrojja.composables.dialogs.OttrojjaAlertDialog
import com.ottrojja.screens.Khitmah.SessionForm.SessionForm
import com.ottrojja.screens.mainScreen.BrowsingOption
import com.ottrojja.screens.quranScreen.classes.QuranReaderContext
import com.ottrojja.screens.quranScreen.composables.Benefits
import com.ottrojja.screens.quranScreen.composables.SinglePage
import com.ottrojja.screens.quranScreen.composables.TafseerBottomSheet
import com.ottrojja.screens.quranScreen.composables.VerseAnchor
import com.ottrojja.screens.quranScreen.composables.YouTube
import com.ottrojja.screens.quranScreen.dialogs.ListeningOptionsDialog
import com.ottrojja.screens.quranScreen.dialogs.PageSelectionDialog
import com.ottrojja.screens.quranScreen.dialogs.SelectTafseerDialog
import com.ottrojja.screens.quranScreen.dialogs.SelectVerseDialog
import com.ottrojja.services.page.PagePlaybackManager
import com.ottrojja.services.page.PagePlayerState
import com.ottrojja.ui.theme.OttrojjaTheme
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.drop
import kotlin.math.abs


@Composable
fun QuranScreen(
    navController: NavController,
    pageNum: String,
    readerContext: QuranReaderContext,
    repository: QuranRepository
) {
    val context = LocalContext.current
    val application = context.applicationContext as Application
    val configuration = LocalConfiguration.current


    val quranViewModel: QuranViewModel = viewModel(
        factory = QuranScreenViewModelFactory(
            repository,
            readerContext,
            PagePlaybackManager.getInstance(application),
            application
        )
    )
    // Same instance VersesSection uses (same ViewModelStoreOwner), so the share sheet can be
    // opened both from the verses tab and from a long-press on the page.
    val versesSectionViewModel: VersesSectionViewModel = viewModel(
        factory = VersesSectionViewModelFactory(repository, application)
    )

    fun openVerseSheet(verse: PageContent, mode: TafseerSheetMode) {
        quranViewModel.tafseerTargetVerse = verse
        quranViewModel.tafseerSheetMode = mode
        quranViewModel.showTafseerSheet = true
    }

    val player by quranViewModel.player.collectAsStateWithLifecycle()

    var showFinishSessionAlert by remember { mutableStateOf(false) }
    var showToggleBookmarkAlert by remember { mutableStateOf(false) }
    val showFinishSessionForm by quranViewModel.showFinishSessionForm.collectAsState()

    when (readerContext) {

        QuranReaderContext.Normal -> {
            println(readerContext)
        }

        is QuranReaderContext.Khitmah -> {
            println(readerContext)
        }
    }

    fun handleBackBehaviour() {
        if (quranViewModel.selectedTab != QuranViewModel.PageTab.الصفحة) {
            quranViewModel.selectedTab = QuranViewModel.PageTab.الصفحة
        } else if (readerContext is QuranReaderContext.Khitmah) {
            showFinishSessionAlert = true;
            return;
        } else {
            navController.popBackStack()
        }
    }

    if (showFinishSessionAlert) {
        OttrojjaAlertDialog(
            onDismiss = { showFinishSessionAlert = false; },
            onConfirm = {
                quranViewModel.invokeSessionForm();
                showFinishSessionAlert = false;
            },
            title = "إنهاء جلسة القراءة",
            text = "هل انت بصدد انهاء جلسة القراءة للختمة الحالية؟",
            confirmText = "نعم"
        )
    }

    if (showToggleBookmarkAlert) {
        OttrojjaAlertDialog(
            onDismiss = { showToggleBookmarkAlert = false; },
            onConfirm = {
                quranViewModel.togglePageBookmark();
                showToggleBookmarkAlert = false;
            },
            title = "إزالة اشارة مرجعية",
            text = "هل انت متأكد من ازالة هذه الصفحة من المرجعيات؟",
            confirmText = "نعم",
            cancelText = "لا"
        )
    }

    if (showFinishSessionForm) {
        val readerContext = readerContext as QuranReaderContext.Khitmah
        SessionForm(
            khitmahId = readerContext.khitmahId,
            startPage = quranViewModel.getFirstSessionPage(),
            endPage = quranViewModel.getLastSessionPage(),
            onDismiss = {
                quranViewModel.toggleFinishSessionForm(false)
            },
            onFinished = {
                navController.navigate(Screen.KhitmahScreen.invokeRoute("${readerContext.khitmahId}")) {
                    popUpTo(navController.currentDestination?.route ?: return@navigate) {
                        inclusive = true
                    }
                }
            },
            repository = repository
        )
    }

    LaunchedEffect(Unit) {
        try {
            // opens at the playing page instead when a recitation is playing and being followed
            quranViewModel.openAt(pageNum)
            quranViewModel.getNightReadingMode()
        } catch (e: Exception) {
            Log.e("error", "Error getting current page in quran screen: $e")
            reportException(exception = e, file = "QuranScreen")
        }
    }

    LaunchedEffect(Unit) {
        quranViewModel.navigation.collect { pageNum ->
            navController.navigate(Screen.QuranScreen.invokeRoute(pageNum))
        }
    }


    BackHandler {
        handleBackBehaviour()
    }

    var expanded by remember { mutableStateOf(false) }

    if (quranViewModel.currentPageObject?.page?.pageNum?.toInt() !in 1..604) {
        Row(modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            OttrojjaText(text = "جاري التحميل..",
                style = OttrojjaTheme.typography.bodyLarge,
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
                                    onClick = { if (!quranViewModel.isBookmarked) quranViewModel.togglePageBookmark() else showToggleBookmarkAlert = true },
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
                                                OttrojjaText(
                                                    "مشاركة",
                                                    style = OttrojjaTheme.typography.bodyMedium,
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
                                                OttrojjaText(
                                                    "إنتقال للبحث",
                                                    style = OttrojjaTheme.typography.bodyMedium,
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
                    val displayedPage = quranViewModel.currentPageObject?.page?.pageNum
                    PagesContainer(
                        initialPage = displayedPage!!.toInt(),
                        pageScrollRequests = quranViewModel.pageScrollRequests,
                        onPageSettled = { page, byUser -> quranViewModel.onPageSettled(page, byUser) },
                        player = player,
                        showReturnToPlayingPage = player.hasMedia && !player.following
                                && player.playingPage != null && player.playingPage.toString() != displayedPage,
                        onReturnToPlayingPage = { quranViewModel.returnToPlayingPage() },
                        onPlayClicked = { quranViewModel.prepareForPlaying() },
                        onPauseClicked = { quranViewModel.pausePlaying() },
                        onSlowerClicked = { quranViewModel.decreasePlaybackSpeed() },
                        onFasterClicked = { quranViewModel.increasePlaybackSpeed() },
                        onNextClicked = { quranViewModel.goNextVerse() },
                        onPreviousClicked = { quranViewModel.goPreviousVerse() },
                        onStopClicked = { quranViewModel.stopPlayback() },
                        nightReadingMode = quranViewModel.nightReadingMode,
                        listeningOptionsClicked = { quranViewModel.showListeningOptionsDialog = true },
                        quranPagesNumbers = quranViewModel.quranPagesNumbers,
                        readerContext = readerContext,
                        khitmahPillClick = { showFinishSessionAlert = true; },
                        currentPageContent = quranViewModel.currentPageObject?.pageContent,
                        onShareVerse = { verse -> versesSectionViewModel.fetchChapterVerses(verse) },
                        onTafseerVerse = { verse -> openVerseSheet(verse, TafseerSheetMode.التفسير) }
                    )
                }

                QuranViewModel.PageTab.التفسير -> VersesSection(
                    quranViewModel.currentPageObject?.pageContent!!.filter { item -> item.type == PageContentItemType.verse },
                    onSheetRequest = { targetVerse, mode -> openVerseSheet(targetVerse, mode) },
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
                verseText = quranViewModel.tafseerTargetVerse?.verseText ?: "",
                contentText = when (quranViewModel.tafseerSheetMode) {
                    TafseerSheetMode.التفسير -> quranViewModel.verseTafseer
                    TafseerSheetMode.الإعراب -> quranViewModel.verseE3rab
                    TafseerSheetMode.أسباب_النزول -> quranViewModel.verseCauseOfRevelation
                    TafseerSheetMode.معاني_المفردات -> quranViewModel.verseMeanings
                    TafseerSheetMode.المشابهات -> quranViewModel.verseMutashabihat
                },
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

            ShareVersesHost(versesSectionViewModel, repository)
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
            endPlayingPage = quranViewModel.endPlayingPage,
            takeOnCurrentPageParameters = { quranViewModel.takeOnCurrentPageVerses() }
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
            selectedPage = if (quranViewModel.versesSelectionMode == VersesSelectionMode.END) "${quranViewModel.endPlayingPage}" else "${quranViewModel.startPlayingPage}",
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
}

@Composable
fun PagesContainer(
    initialPage: Int,
    pageScrollRequests: Flow<Int>,
    onPageSettled: (page: Int, byUser: Boolean) -> Unit,
    player: PagePlayerState,
    showReturnToPlayingPage: Boolean,
    onReturnToPlayingPage: () -> Unit,
    onPlayClicked: () -> Unit,
    onPauseClicked: () -> Unit,
    onSlowerClicked: () -> Unit,
    onFasterClicked: () -> Unit,
    onNextClicked: () -> Unit,
    onPreviousClicked: () -> Unit,
    onStopClicked: () -> Unit,
    nightReadingMode: Boolean,
    listeningOptionsClicked: () -> Unit,
    quranPagesNumbers: List<String>,
    readerContext: QuranReaderContext,
    khitmahPillClick: () -> Unit,
    currentPageContent: List<PageContent>?,
    onShareVerse: (PageContent) -> Unit,
    onTafseerVerse: (PageContent) -> Unit,
) {

    val pagerState = rememberPagerState(
        initialPage = initialPage - 1,
        initialPageOffsetFraction = 0f
    ) {
        quranPagesNumbers.size //number of the pages of quran
    }
    var showController by remember { mutableStateOf(true) }

    // Long-pressed verse + where to anchor its card (page-box coordinates). Cleared on rotation,
    // page change or page data reload, since the page box size changes with those.
    val orientation = LocalConfiguration.current.orientation
    var verseMenu by remember { mutableStateOf<VerseAnchor?>(null) }
    LaunchedEffect(orientation, pagerState.currentPage, currentPageContent) { verseMenu = null }

    /*
     * Pagination has two sources:
     * - AUTO: the ViewModel asks for a page (the recitation moved on, "back to recited page").
     *   We remember that target; when the pager settles on it, the change is reported as byUser=false.
     * - MANUAL: any other settled page is a user swipe (also a swipe that interrupted an auto scroll).
     * Only settled pages are reported, so a long swipe does not load every page in between.
     */
    var autoScrollTarget by remember { mutableStateOf<Int?>(null) }
    val currentOnPageSettled by rememberUpdatedState(onPageSettled)

    LaunchedEffect(pagerState) {
        pageScrollRequests.collect { page ->
            val index = page - 1
            if (index == pagerState.settledPage && !pagerState.isScrollInProgress) return@collect
            autoScrollTarget = index
            try {
                // far jumps (e.g. returning to the recited page) are instant, near ones animate
                if (abs(index - pagerState.currentPage) > 2) {
                    pagerState.scrollToPage(index)
                } else {
                    pagerState.animateScrollToPage(index)
                }
            } catch (e: CancellationException) {
                // the user grabbed the pager mid-animation: keep collecting unless we are leaving
                currentCoroutineContext().ensureActive()
            }
        }
    }

    LaunchedEffect(pagerState) {
        snapshotFlow { pagerState.settledPage }
            .drop(1) // the initial page is already the ViewModel's page
            .collect { index ->
                val byUser = index != autoScrollTarget
                autoScrollTarget = null
                currentOnPageSettled(index + 1, byUser)
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
                ) {
                    // A tap outside the page box while the verse card is open just closes it.
                    if (verseMenu != null) verseMenu = null else showController = !showController
                }) { index ->
            val thisPage = quranPagesNumbers[index]
            // Only the page the user is on is interactive; its rows are what the VM has loaded.
            val content = currentPageContent?.takeIf { rows -> rows.firstOrNull()?.pageNum == thisPage }
            SinglePage(
                pageNum = thisPage,
                nightReadingMode = nightReadingMode,
                pageContent = content,
                menuAnchor = verseMenu,
                onVerseLongPress = { anchor -> verseMenu = anchor },
                onDismissMenu = { verseMenu = null },
                onShareVerse = onShareVerse,
                onTafseerVerse = onTafseerVerse
            )
        }

        AnimatedVisibility(
            visible = showController && readerContext is QuranReaderContext.Khitmah,
            enter = fadeIn(),
            exit = fadeOut(),
            modifier = Modifier
                .align(Alignment.TopCenter)
                .offset(y = 16.dp)
        ) {
            val khitmahContext = readerContext as? QuranReaderContext.Khitmah

            if (khitmahContext != null) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .clip(RoundedCornerShape(16.dp))
                        .background(MaterialTheme.colorScheme.primary)
                        .clickable {
                            khitmahPillClick()
                        }
                        .padding(
                            horizontal = 14.dp,
                            vertical = 10.dp
                        )
                ) {
                    Icon(
                        imageVector = Icons.Default.MenuBook,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onPrimary,
                        modifier = Modifier.size(16.dp)
                    )

                    Spacer(modifier = Modifier.width(8.dp))

                    OttrojjaText(
                        text = khitmahContext.khitmahName,
                        style = OttrojjaTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onPrimary,
                        maxLines = 1
                    )

                    Spacer(modifier = Modifier.width(10.dp))

                    Box(
                        modifier = Modifier
                            .width(1.dp)
                            .height(20.dp)
                            .background(
                                MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.25f)
                            )
                    )

                    Spacer(modifier = Modifier.width(10.dp))

                    Icon(
                        imageVector = Icons.Default.StopCircle,
                        contentDescription = "إنهاء الجلسة",
                        tint = MaterialTheme.colorScheme.onPrimary,
                        modifier = Modifier.size(16.dp)
                    )

                    Spacer(modifier = Modifier.width(5.dp))

                    OttrojjaText(
                        text = "إنهاء",
                        style = OttrojjaTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                }
            }
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
                isPlaying = player.isPlaying,
                playbackSpeed = player.speed,
                isDownloading = player.isDownloading,
                onFasterClicked = onFasterClicked,
                onNextClicked = onNextClicked,
                onPauseClicked = onPauseClicked,
                onPlayClicked = onPlayClicked,
                onPreviousClicked = onPreviousClicked,
                onSlowerClicked = onSlowerClicked,
                hasNextPreviousControl = true,
            ) {
                if (player.isDownloading && player.downloadTotal > 0) {
                    OttrojjaText(
                        text = "جاري تحميل الآيات ${convertToIndianNumbers("${player.downloadDone}")} / ${convertToIndianNumbers("${player.downloadTotal}")}",
                        style = OttrojjaTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        if (player.hasMedia || player.isDownloading) {
                            Icon(Icons.Filled.Cancel,
                                tint = MaterialTheme.colorScheme.primary,
                                contentDescription = "cancel playing",
                                modifier = Modifier.clickable { onStopClicked() }
                            )
                        }
                    }

                    if (showReturnToPlayingPage) {
                        OttrojjaText(text = "العودة لصفحة التلاوة ${convertToIndianNumbers("${player.playingPage}")}",
                            style = OttrojjaTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier
                                .clip(RoundedCornerShape(12.dp))
                                .clickable { onReturnToPlayingPage() }
                                .padding(6.dp)
                        )
                    }

                    OttrojjaText(text = "خيارات الإستماع",
                        style = OttrojjaTheme.typography.bodySmall,
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


