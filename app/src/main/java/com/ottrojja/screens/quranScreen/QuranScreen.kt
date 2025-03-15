package com.ottrojja.screens.quranScreen

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.app.Application
import android.content.Context
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
import androidx.compose.foundation.interaction.Interaction
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.PageSize
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.outlined.BookmarkBorder
import androidx.compose.material3.BottomSheetDefaults
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.material3.windowsizeclass.ExperimentalMaterial3WindowSizeClassApi
import androidx.compose.material3.windowsizeclass.WindowSizeClass
import androidx.compose.material3.windowsizeclass.calculateWindowSizeClass
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.ColorMatrix
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.ottrojja.R
import com.ottrojja.classes.Helpers
import com.ottrojja.classes.PageContent
import com.ottrojja.classes.PageContentItemType
import com.ottrojja.classes.QuranRepository
import com.ottrojja.classes.Screen
import com.ottrojja.composables.BenefitItem
import com.ottrojja.composables.BenefitSectionTitle
import com.ottrojja.composables.ListHorizontalDivider
import com.ottrojja.composables.MediaController
import com.ottrojja.composables.OttrojjaDialog
import com.ottrojja.composables.OttrojjaElevatedButton
import com.ottrojja.composables.OttrojjaTabs
import com.ottrojja.composables.OttrojjaTopBar
import com.ottrojja.composables.SelectedVerseContentSection
import com.ottrojja.room.entities.Khitmah
import com.ottrojja.screens.mainScreen.BrowsingOption
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.PlayerConstants
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.YouTubePlayer
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.listeners.AbstractYouTubePlayerListener
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.views.YouTubePlayerView
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.serialization.json.Json.Default.configuration


@SuppressLint("UnrememberedMutableState", "DiscouragedApi")
@Composable
fun QuranScreen(
    navController: NavController, pageNum: String, repository: QuranRepository
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

    if (quranViewModel.currentPageObject.pageNum.toInt() !in 1..604) {
        Text(text = "جاري التحميل")
    } else {
        Column() {
            OttrojjaTopBar {
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

            when (quranViewModel.selectedTab) {
                QuranViewModel.PageTab.الصفحة -> Column(
                    verticalArrangement = Arrangement.SpaceBetween
                ) {
                    PagesContainer(
                        quranViewModel.currentPageObject.pageNum,
                        onPageChanged = { newPage ->
                            quranViewModel.setCurrentPage(newPage)
                        },
                        onPlayClicked = {
                            quranViewModel.prepareForPlaying()
                        },
                        { quranViewModel.pausePlaying() },
                        { quranViewModel.showRepOptions = true },
                        { /*quranViewModel.updateSelectedRep()*/ },
                        quranViewModel.selectedRepetition,
                        { quranViewModel.showVerseOptions = true },
                        quranViewModel.selectedVerse,
                        isPlaying,
                        { quranViewModel.decreasePlaybackSpeed() },
                        { quranViewModel.increasePlaybackSpeed() },
                        quranViewModel.isDownloading,
                        { quranViewModel.goNextVerse() },
                        { quranViewModel.goPreviousVerse() },
                        quranViewModel.continuousPlay,
                        { value -> quranViewModel.continuousPlay = value },
                        quranViewModel.shouldAutoPlay,
                        quranViewModel.playbackSpeed,
                        quranViewModel.nightReadingMode,
                        { quranViewModel.showListeningOptionsDialog = true },
                        vmChangedPage = quranViewModel.vmChangedPage,
                        setVmChangedPage = { value -> quranViewModel.vmChangedPage = value }
                    )
                }

                QuranViewModel.PageTab.الآيات -> VersesSection(
                    quranViewModel.currentPageObject.pageContent.filter { item -> item.type == PageContentItemType.verse },
                    { targetVerse ->
                        quranViewModel.tafseerTargetVerse = targetVerse;
                        quranViewModel.tafseerSheetMode = "tafseer"
                        quranViewModel.showTafseerSheet = true
                    },
                    { targetVerse ->
                        quranViewModel.tafseerTargetVerse = targetVerse;
                        quranViewModel.tafseerSheetMode = "e3rab"
                        quranViewModel.showTafseerSheet = true
                    },
                    { targetVerse ->
                        quranViewModel.tafseerTargetVerse = targetVerse;
                        quranViewModel.tafseerSheetMode = "causeOfRevelation"
                        quranViewModel.showTafseerSheet = true
                    },
                    repository
                )

                QuranViewModel.PageTab.الفوائد -> Benefits(
                    quranViewModel.currentPageObject.benefits,
                    quranViewModel.currentPageObject.appliance,
                    quranViewModel.currentPageObject.guidance,
                    quranViewModel.currentPageObject.pageNum
                )

                QuranViewModel.PageTab.الفيديو -> YouTube(
                    quranViewModel.currentPageObject.ytLink.split(
                        "v="
                    ).last()
                )
            }

            VersesBottomSheet(quranViewModel.showVersesSheet,
                { quranViewModel.showVersesSheet = false },
                { targetVerse ->
                    quranViewModel.tafseerTargetVerse = targetVerse;
                    quranViewModel.showVersesSheet = false;
                    quranViewModel.showTafseerSheet = true
                },
                quranViewModel.currentPageObject.pageContent.filter { item -> item.type == PageContentItemType.verse });

            TafseerBottomSheet(
                context,
                showTafseerSheet = quranViewModel.showTafseerSheet,
                onDismiss = { quranViewModel.showTafseerSheet = false },
                quranViewModel.verseTafseer,
                quranViewModel.verseE3rab,
                quranViewModel.verseCauseOfRevelation,
                quranViewModel.selectedTafseer,
                onClickTafseerOptions = { quranViewModel.showTafseerOptions = true },
                mode = quranViewModel.tafseerSheetMode,
                atFirstVerse = quranViewModel.atFirstVerse(),
                atLastVerse = quranViewModel.atLastVerse(),
                targetNextVerse = { quranViewModel.targetNextVerse() },
                targetPreviousVerse = { quranViewModel.targetPreviousVerse() }
            )
        }
    }

    if (quranViewModel.showListeningOptionsDialog) {
        ListeningOptionsDialog(
            onDismissRequest = { quranViewModel.showListeningOptionsDialog = false },
            selectedVerse = quranViewModel.selectedVerse,
            selectedEndVerse = quranViewModel.selectedEndVerse,
            onSelectVerseClicked = { quranViewModel.showVerseOptions = true },
            onSelectEndVerseClicked = {
                quranViewModel.selectingEndVerse = true;
                quranViewModel.showVerseOptions = true;
            },
            continuousPlay = quranViewModel.continuousPlay,
            selectedRepetition = quranViewModel.selectedRepetition,
            onSelectRepetitionClicked = { quranViewModel.showRepOptions = true },
            setContPlay = { value -> quranViewModel.continuousPlay = value },
            repetitionTabs = RepetitionTab.entries,
            selectedRepetitionTab = quranViewModel.selectedRepetitionTab,
            onSelectRepetitionTab = { value -> quranViewModel.selectedRepetitionTab = value }
        )
    }

    if (quranViewModel.showRepOptions) {
        SelectRepDialog(
            quranViewModel.repetitionOptionsMap.keys.toTypedArray(),
            { quranViewModel.showRepOptions = false },
            { selectedRep ->
                quranViewModel.selectedRepetition = selectedRep;
                quranViewModel.showRepOptions = false
            })
    }

    if (quranViewModel.showVerseOptions) {
        SelectVerseDialog(
            { quranViewModel.showVerseOptions = false },
            { selectedVerse ->
                if (quranViewModel.selectingEndVerse) {
                    quranViewModel.selectedEndVerse = selectedVerse;
                    quranViewModel.selectingEndVerse = false;
                    quranViewModel.showVerseOptions = false;
                } else {
                    quranViewModel.selectedVerse = selectedVerse;
                    quranViewModel.showVerseOptions = false;
                }
            },
            quranViewModel.getCurrentPageVerses()
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TafseerBottomSheet(
    context: Context,
    showTafseerSheet: Boolean,
    onDismiss: () -> Unit,
    tafseer: String,
    e3rab: String,
    causeOfRevelation: String,
    selectedTafseer: String,
    onClickTafseerOptions: () -> Unit,
    mode: String,
    atFirstVerse: Boolean,
    atLastVerse: Boolean,
    targetNextVerse: () -> Unit,
    targetPreviousVerse: () -> Unit
) {
    val modalBottomSheetState = rememberModalBottomSheetState()

    if (showTafseerSheet) {
        ModalBottomSheet(
            onDismissRequest = { onDismiss() },
            sheetState = modalBottomSheetState,
            dragHandle = { BottomSheetDefaults.DragHandle() },
        ) {

            SelectedVerseContentSection(
                context = context,
                text = when (mode) {
                    "tafseer" -> tafseer
                    "e3rab" -> e3rab
                    "causeOfRevelation" -> causeOfRevelation
                    else -> ""
                },
                copiedMessage = when (mode) {
                    "tafseer" -> "تم تسخ التفسير بنجاح"
                    "e3rab" -> "تم تسخ الإعراب بنجاح"
                    "causeOfRevelation" -> "تم تسخ سبب النزول بنجاح"
                    else -> ""
                },
                atFirstVerse = atFirstVerse,
                atLastVerse = atLastVerse,
                targetNextVerse = { targetNextVerse() },
                targetPreviousVerse = { targetPreviousVerse() }
            ) {
                if (mode == "tafseer") {
                    Row(horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(MaterialTheme.colorScheme.secondary)
                            .clickable { onClickTafseerOptions() }) {
                        Text(
                            text = selectedTafseer,
                            color = MaterialTheme.colorScheme.onSecondary,
                            style = MaterialTheme.typography.bodyMedium,
                            textAlign = TextAlign.Right,
                            modifier = Modifier.padding(6.dp, 8.dp)
                        )
                        Icon(
                            Icons.Default.ArrowDropDown,
                            contentDescription = "",
                            tint = MaterialTheme.colorScheme.onSecondary
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun SelectTafseerDialog(
    onDismissRequest: () -> Unit,
    onOptionClick: (String) -> Unit,
    tafseerMap: HashMap<String, String>
) {
    Dialog(onDismissRequest = { onDismissRequest() }) {

        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.secondary,
            ),
        ) {
            Box(
                modifier = Modifier
                    .padding(8.dp)
                    .background(MaterialTheme.colorScheme.secondary)
                    .padding(8.dp)
                    .clip(shape = RoundedCornerShape(12.dp))
            ) {
                Column() {
                    tafseerMap.keys.forEach { tafseer ->
                        Row(modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onOptionClick(tafseer) }
                            .padding(6.dp)) {
                            Text(
                                text = tafseer,
                                modifier = Modifier.fillMaxWidth(),
                                textAlign = TextAlign.Right,
                                color = MaterialTheme.colorScheme.onSecondary
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun SelectRepDialog(
    repOptions: Array<String>,
    onDismissRequest: () -> Unit,
    onOptionClick: (String) -> Unit
) {
    Dialog(onDismissRequest = { onDismissRequest() }) {
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.secondary,
            ),
        ) {
            Box(
                modifier = Modifier
                    .padding(8.dp)
                    .fillMaxHeight(0.4f)
                    .background(MaterialTheme.colorScheme.secondary)
                    .padding(8.dp)
                    .clip(shape = RoundedCornerShape(12.dp))
            ) {
                Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
                    repOptions.forEach { option ->
                        Row(modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onOptionClick(option) }
                            .padding(6.dp)) {
                            Text(
                                text = option,
                                modifier = Modifier.fillMaxWidth(),
                                textAlign = TextAlign.Right,
                                color = MaterialTheme.colorScheme.onSecondary
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun SelectVerseDialog(
    onDismissRequest: () -> Unit,
    onOptionClick: (PageContent) -> Unit,
    versesList: Array<PageContent>
) {
    Dialog(onDismissRequest = { onDismissRequest() }) {
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.secondary,
            ),
        ) {
            Box(
                modifier = Modifier
                    .padding(8.dp)
                    .fillMaxHeight(0.4f)
                    .background(MaterialTheme.colorScheme.secondary)
                    .padding(8.dp)
                    .clip(shape = RoundedCornerShape(12.dp))
            ) {
                Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
                    versesList.forEach { option ->
                        if (option.type == PageContentItemType.verse) {
                            Row(modifier = Modifier
                                .fillMaxWidth()
                                .clickable { onOptionClick(option) }
                                .padding(6.dp)) {
                                Text(
                                    text = "الاية ${option.verseNum}",
                                    modifier = Modifier.fillMaxWidth(),
                                    textAlign = TextAlign.Right,
                                    color = MaterialTheme.colorScheme.onSecondary,
                                    style = MaterialTheme.typography.bodyMedium,
                                )
                            }
                        } else {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(MaterialTheme.colorScheme.primary)
                                    .padding(6.dp)
                            ) {
                                Text(
                                    text = " سورة " + "${option.surahName}",
                                    modifier = Modifier.fillMaxWidth(),
                                    textAlign = TextAlign.Right,
                                    color = MaterialTheme.colorScheme.onPrimary,
                                    style = MaterialTheme.typography.bodyMedium,
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ListeningOptionsDialog(
    onDismissRequest: () -> Unit,
    selectedVerse: PageContent,
    selectedEndVerse: PageContent,
    onSelectVerseClicked: () -> Unit,
    onSelectEndVerseClicked: () -> Unit,
    selectedRepetition: String,
    onSelectRepetitionClicked: () -> Unit,
    continuousPlay: Boolean,
    setContPlay: (Boolean) -> Unit,
    repetitionTabs: List<RepetitionTab>,
    selectedRepetitionTab: RepetitionTab,
    onSelectRepetitionTab: (RepetitionTab) -> Unit
) {
    OttrojjaDialog(
        contentModifier = Modifier
            .padding(8.dp)
            .wrapContentHeight()
            .fillMaxWidth(0.9f)
            .background(MaterialTheme.colorScheme.secondary)
            .padding(8.dp)
            .clip(shape = RoundedCornerShape(12.dp)),
        onDismissRequest = onDismissRequest,
        useDefaultWidth = false,
    ) {
        Column(modifier = Modifier.wrapContentHeight()) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center) {
                Text(text = "خيارات التشغيل", textAlign = TextAlign.Center)
            }
            HorizontalDivider(
                modifier = Modifier.padding(vertical = 6.dp),
                color = MaterialTheme.colorScheme.onTertiary
            )
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onSelectVerseClicked() },
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("من اية", style = MaterialTheme.typography.bodyMedium)
                Row(
                    modifier = Modifier
                        .padding(0.dp, 6.dp, 6.dp, 5.dp)
                        .fillMaxWidth(0.5f)
                        .clip(shape = RoundedCornerShape(4.dp))
                        .background(MaterialTheme.colorScheme.primary)
                ) {
                    Text(
                        text = if (selectedVerse.verseNum != null && selectedVerse.verseNum.length > 0) "اية: ${selectedVerse.verseNum}" else "الاية",
                        color = MaterialTheme.colorScheme.onPrimary,
                        style = MaterialTheme.typography.bodySmall,
                        textAlign = TextAlign.Right,
                        modifier = Modifier
                            .padding(8.dp)
                            .fillMaxWidth()
                    )
                }
            }
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onSelectEndVerseClicked() },
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("الى اية", style = MaterialTheme.typography.bodyMedium)
                Row(
                    modifier = Modifier
                        .padding(0.dp, 6.dp, 6.dp, 5.dp)
                        .fillMaxWidth(0.5f)
                        .clip(shape = RoundedCornerShape(4.dp))
                        .background(MaterialTheme.colorScheme.primary)
                ) {
                    Text(
                        text = if (selectedEndVerse.verseNum != null && selectedEndVerse.verseNum.length > 0) "اية: ${selectedEndVerse.verseNum}" else "الاية",
                        color = MaterialTheme.colorScheme.onPrimary,
                        style = MaterialTheme.typography.bodySmall,
                        textAlign = TextAlign.Right,
                        modifier = Modifier
                            .padding(8.dp)
                            .fillMaxWidth()
                    )
                }
            }
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onSelectRepetitionClicked() },
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("مرات التكرار", style = MaterialTheme.typography.bodyMedium)
                Row(
                    modifier = Modifier
                        .padding(0.dp, 6.dp, 6.dp, 5.dp)
                        .fillMaxWidth(0.5f)
                        .clip(shape = RoundedCornerShape(4.dp))
                        .background(MaterialTheme.colorScheme.primary)
                ) {
                    Text(
                        text = selectedRepetition,
                        color = MaterialTheme.colorScheme.onPrimary,
                        style = MaterialTheme.typography.bodySmall,
                        textAlign = TextAlign.Right,
                        modifier = Modifier
                            .padding(8.dp)
                            .fillMaxWidth()
                    )
                }
            }
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onSelectEndVerseClicked() },
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                OttrojjaTabs(
                    items = repetitionTabs,
                    selectedItem = selectedRepetitionTab,
                    onClickTab = { value -> onSelectRepetitionTab(value) },
                    tabPrefix = "تكرار "
                )
            }

            Row(
                modifier = Modifier
                    .padding(vertical = 6.dp)
                    .fillMaxWidth()
                    .clickable { setContPlay(!continuousPlay) },
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text("تشغيل متتال للصفحات", style = MaterialTheme.typography.bodyMedium)
                    Text("(غير متوفر في الخلفية حاليا)", style = MaterialTheme.typography.bodySmall)
                }
                Checkbox(
                    checked = continuousPlay,
                    onCheckedChange = { newCheckedState -> setContPlay(newCheckedState) }
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Button(onClick = { onDismissRequest() }) {
                    Text(
                        text = "إغلاق",
                        style = MaterialTheme.typography.bodyMedium,
                    )
                }
            }

        }
    }
}

@Composable
fun PagesContainer(
    pageNum: String?,
    onPageChanged: (String) -> Unit,
    onPlayClicked: () -> Unit,
    onPauseClicked: () -> Unit,
    onClickRepOptions: () -> Unit,
    onClickUpdateRep: () -> Unit,
    selectedRep: String,
    onClickVerseOptions: () -> Unit,
    selectedVerse: PageContent,
    isPlaying: Boolean,
    onSlowerClicked: () -> Unit,
    onFasterClicked: () -> Unit,
    isDownloading: Boolean,
    onNextClicked: () -> Unit,
    onPreviousClicked: () -> Unit,
    continuousPlay: Boolean,
    setContPlay: (Boolean) -> Unit,
    shouldAutoPlay: Boolean,
    playbackSpeed: Float,
    nightReadingMode: Boolean,
    listeningOptionsClicked: () -> Unit,
    vmChangedPage: Boolean,
    setVmChangedPage: (Boolean) -> Unit
) {
    val quranPagesNumbers = Array(604) { (it + 1).toString() }

    val pagerState = rememberPagerState(
        initialPage = Integer.parseInt(pageNum) - 1,
        initialPageOffsetFraction = 0f
    ) {
        quranPagesNumbers.size //number of the pages of quran
    }
    var showController by remember {
        mutableStateOf(true)
    }
    val hasPageChanged =
        remember { mutableStateOf(false) } // To track if the page has changed at least once


    LaunchedEffect(pageNum) {
        if (shouldAutoPlay) {
            println("should auto play ${pageNum!!.toInt()}")
            pagerState.animateScrollToPage(pageNum!!.toInt() - 1)
            onPlayClicked()
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
                    indication = null, interactionSource = NoRippleInteractionSource()
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
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        Icons.Default.Settings,
                        contentDescription = "More Options",
                        modifier = Modifier.clickable { listeningOptionsClicked() },
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
            }
        }
    }
}

class NoRippleInteractionSource : MutableInteractionSource {

    override val interactions: Flow<Interaction> = emptyFlow()

    override suspend fun emit(interaction: Interaction) {}

    override fun tryEmit(interaction: Interaction) = true

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

@Composable
fun Benefits(
    benefits: Array<String>,
    appliance: Array<String>,
    guidance: Array<String>,
    pageNum: String
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(10.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Top
    ) {

        LazyColumn() {
            if (benefits.size != 0) {
                item {
                    BenefitSectionTitle("فوائد الصفحة")
                }
            }
            items(benefits) { benefit ->
                BenefitItem(
                    benefitContent = benefit,
                    shareSubject = "فائدة قرآنية",
                    shareTitle = "مشاركة الفائدة",
                    shareContent = "من الفوائد القرآنية للصفحة $pageNum \n $benefit\n${
                        stringResource(
                            R.string.share_app
                        )
                    }"
                )
            }

            if (guidance.size != 0) {
                item {
                    BenefitSectionTitle("توجيهات الصفحة");
                }
            }
            items(guidance) { guidanceItem ->
                BenefitItem(
                    benefitContent = guidanceItem,
                    shareSubject = "توجيه قرآني",
                    shareTitle = "مشاركة التوجيه",
                    shareContent = "من التوجيهات القرآنية للصفحة $pageNum \n $guidanceItem\n${
                        stringResource(
                            R.string.share_app
                        )
                    }"
                )
            }

            if (appliance.size != 0) {
                item {
                    BenefitSectionTitle("الجانب التطبيقي")
                }
            }
            items(appliance) { applianceItem ->
                BenefitItem(
                    benefitContent = applianceItem,
                    shareSubject = "تطبيق قرآني",
                    shareTitle = "مشاركة التطبيق",
                    shareContent = "من التطبيقات القرآنية للصفحة $pageNum \n $applianceItem\n${
                        stringResource(
                            R.string.share_app
                        )
                    }"
                )
            }
        }
    }
}

@Composable
fun YouTube(link: String) {
    val context = LocalContext.current
    println(link)
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.primary)
            .padding(10.dp)
    ) {
        if (!Helpers.checkNetworkConnectivity(context)) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center) {
                Text(
                    text = "تعذر عرض المقطع لعدم توفر اتصال بالشبكة",
                    color = MaterialTheme.colorScheme.onPrimary,
                    style = MaterialTheme.typography.bodyLarge,
                    textAlign = TextAlign.Center
                )
            }
        } else {
            YoutubeScreen(videoId = link, modifier = Modifier)
        }
    }
}


@Composable
fun YoutubeScreen(
    videoId: String, modifier: Modifier
) {
    val context = LocalContext.current
    AndroidView(factory = {
        var view = YouTubePlayerView(it)
        val fragment = view.addYouTubePlayerListener(object : AbstractYouTubePlayerListener() {
            override fun onReady(youTubePlayer: YouTubePlayer) {
                super.onReady(youTubePlayer)
                youTubePlayer.cueVideo(videoId, 0f)
            }

            override fun onStateChange(
                youTubePlayer: YouTubePlayer, state: PlayerConstants.PlayerState
            ) {
                super.onStateChange(youTubePlayer, state)
                if (state.toString() == "PLAYING") {
                    Helpers.terminateAllServices(context)
                }
                println(state)
            }
        })
        view
    })
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun VersesSection(
    items: List<PageContent>,
    onTafseerClick: (String) -> Unit,
    onE3rabClick: (String) -> Unit,
    onCauseOfRevelationClick: (String) -> Unit,
    repository: QuranRepository
) {
    val context = LocalContext.current
    val application = context.applicationContext as Application

    val versesSectionViewModel: VersesSectionViewModel = viewModel(
        factory = VersesSectionViewModelFactory(repository, application)
    )


    LaunchedEffect(null) {
        versesSectionViewModel.setItems(items);
    }
    val versesList = versesSectionViewModel.items.collectAsState();

    Column() {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.tertiary)
                .padding(10.dp)
        ) {
            items(versesList.value) { item ->
                Column(modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.background)
                    .clickable {
                        versesSectionViewModel.updateExpanded(
                            item
                        )
                    }) {
                    Row(
                        modifier = Modifier
                            .padding(12.dp)
                            .fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "${
                                Helpers.convertToIndianNumbers(item.pageContent.verseNum
                                )
                            } ${item.pageContent.verseText}",
                            color = Color.Black,
                            style = MaterialTheme.typography.titleLarge,
                            textAlign = TextAlign.Right,
                            modifier = Modifier.fillMaxWidth(0.9f),
                        )
                        Image(painter = painterResource(id = R.drawable.more_vert),
                            contentDescription = "",
                            modifier = Modifier.clickable {
                                versesSectionViewModel.updateExpanded(
                                    item
                                )
                            })
                    }
                    AnimatedVisibility(
                        visible = item.expanded,
                    ) {
                        FlowRow(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(MaterialTheme.colorScheme.background)
                                .padding(8.dp), maxItemsInEachRow = 3
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Center,
                                modifier = Modifier
                                    .weight(1.0f)
                                    .background(MaterialTheme.colorScheme.tertiary)
                                    .clickable {
                                        versesSectionViewModel.shareVerse(item.pageContent
                                        )
                                    }
                                    .padding(4.dp, 6.dp)
                            ) {
                                Text(
                                    text = "مشاركة",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = Color.Black
                                )
                            }
                            Column(horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Center,
                                modifier = Modifier
                                    .weight(1.0f)
                                    .background(MaterialTheme.colorScheme.primary)
                                    .clickable {
                                        onTafseerClick(
                                            "${item.pageContent.surahNum}-${item.pageContent.verseNum}"
                                        )
                                    }
                                    .padding(4.dp, 6.dp)
                            ) {
                                Text(
                                    text = "التفسير",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onPrimary
                                )
                            }
                            Column(horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Center,
                                modifier = Modifier
                                    .weight(1.0f)
                                    .background(MaterialTheme.colorScheme.tertiary)
                                    .clickable {
                                        onE3rabClick(
                                            "${item.pageContent.surahNum}-${item.pageContent.verseNum}"
                                        )
                                    }
                                    .padding(4.dp, 6.dp)
                            ) {
                                Text(
                                    text = "الإعراب",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = Color.Black
                                )
                            }
                            Column(horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Center,
                                modifier = Modifier
                                    .weight(1.0f)
                                    .background(MaterialTheme.colorScheme.primary)
                                    .clickable {
                                        onCauseOfRevelationClick(
                                            "${item.pageContent.surahNum}-${item.pageContent.verseNum}"
                                        )
                                    }
                                    .padding(4.dp, 6.dp)
                            ) {
                                Text(
                                    text = "سبب النزول",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onPrimary
                                )
                            }
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Center,
                                modifier = Modifier
                                    .weight(1.0f)
                                    .background(Color.Transparent)
                                    .padding(4.dp, 6.dp)
                            ) {}
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Center,
                                modifier = Modifier
                                    .weight(1.0f)
                                    .background(Color.Transparent)
                                    .padding(4.dp, 6.dp)
                            ) {}
                        }
                    }
                }

                HorizontalDivider(
                    modifier = Modifier.padding(14.dp),
                    thickness = 1.dp,
                    color = Color.Black.copy(alpha = 0.1f)
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VersesBottomSheet(
    showVersesSheet: Boolean,
    onDismiss: () -> Unit,
    onVerseClicked: (String) -> Unit,
    items: List<PageContent>
) {
    val modalBottomSheetState = rememberModalBottomSheetState()

    if (showVersesSheet) {
        ModalBottomSheet(
            onDismissRequest = { onDismiss() },
            sheetState = modalBottomSheetState,
            dragHandle = { BottomSheetDefaults.DragHandle() },
        ) {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .background(MaterialTheme.colorScheme.primary)
            ) {
                items(items) { item ->
                    Column(modifier = Modifier
                        .fillMaxWidth()
                        .background(MaterialTheme.colorScheme.primary)
                        .clickable {
                            onVerseClicked("${item.surahNum}-${item.verseNum}")
                        }) {
                        Row(
                            modifier = Modifier
                                .padding(12.dp)
                                .fillMaxWidth(),
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Text(
                                text = "{${item.verseNum}}  ${item.verseText}",
                                color = MaterialTheme.colorScheme.onPrimary,
                                style = TextStyle(textDirection = TextDirection.Rtl),
                                textAlign = TextAlign.Right,
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                        Box(
                            Modifier
                                .fillMaxWidth()
                                .height(2.dp)
                                .background(MaterialTheme.colorScheme.onPrimary)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun AddToKhitmahDialog(
    onDismiss: () -> Unit,
    khitmahList: List<Khitmah>,
    assignPageToKhitmah: (Khitmah) -> Unit
) {
    OttrojjaDialog(
        contentModifier = Modifier
            .padding(8.dp)
            .wrapContentHeight()
            .fillMaxWidth(0.9f)
            .background(MaterialTheme.colorScheme.secondary)
            .padding(8.dp)
            .clip(shape = RoundedCornerShape(12.dp)),
        onDismissRequest = { onDismiss() },
        useDefaultWidth = false,
    ) {


        Column(modifier = Modifier.wrapContentHeight()) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center) {
                Text(text = "إضافة الى ختمة", textAlign = TextAlign.Center)
            }
            HorizontalDivider(
                modifier = Modifier.padding(vertical = 6.dp),
                color = MaterialTheme.colorScheme.onTertiary
            )

            LazyColumn(modifier = Modifier.fillMaxHeight(0.4f)) {
                if (khitmahList.size <= 0) {
                    item {
                        Row(
                            horizontalArrangement = Arrangement.Center,
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(20.dp)
                        ) {
                            Text(
                                text = "لا يوجد ختمات حاليا",
                                style = MaterialTheme.typography.bodyMedium,
                                textAlign = TextAlign.Center,
                                color = MaterialTheme.colorScheme.onSecondary
                            )
                        }
                    }
                }

                items(khitmahList, key = { it.id }) { item ->
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { assignPageToKhitmah(item); onDismiss() }
                            .padding(vertical = 8.dp)
                    ) {
                        Text(
                            text = item.title,
                            color = MaterialTheme.colorScheme.onSecondary,
                            style = MaterialTheme.typography.bodyLarge,
                            textAlign = TextAlign.Center,
                        )
                    }
                    HorizontalDivider(
                        modifier = Modifier.padding(vertical = 2.dp),
                        color = MaterialTheme.colorScheme.onSecondary
                    )
                }
            }

            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Button(
                    onClick = { onDismiss() },
                    modifier = Modifier
                        .fillMaxWidth(0.6f)
                        .padding(vertical = 6.dp)
                ) {
                    Text(
                        text = "إلغاء",
                        style = MaterialTheme.typography.bodyMedium,
                    )
                }
            }
        }
    }
}
