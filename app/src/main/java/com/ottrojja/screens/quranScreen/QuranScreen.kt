package com.ottrojja.screens.quranScreen

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.app.Application
import android.content.Context
import android.content.Intent
import android.content.res.Configuration
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Build
import android.util.Log
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.interaction.Interaction
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.PageSize
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.outlined.BookmarkBorder
import androidx.compose.material3.BottomSheetDefaults
import androidx.compose.material3.ButtonDefaults.elevatedButtonElevation
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ElevatedButton
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.window.Dialog
import androidx.core.content.ContextCompat.startActivity
import androidx.core.content.FileProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.ottrojja.R
import com.ottrojja.classes.Helpers
import com.ottrojja.classes.Helpers.copyToClipboard
import com.ottrojja.classes.MediaPlayerService
import com.ottrojja.classes.PageContent
import com.ottrojja.classes.QuranRepository
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.PlayerConstants
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.YouTubePlayer
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.listeners.AbstractYouTubePlayerListener
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.views.YouTubePlayerView
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow
import java.io.File
import java.io.FileOutputStream
import java.io.IOException


@SuppressLint("UnrememberedMutableState", "DiscouragedApi")
@Composable
fun QuranScreen(
    navController: NavController, pageNum: String, repository: QuranRepository
) {
    val context = LocalContext.current
    val application = context.applicationContext as Application

    val quranViewModel: QuranViewModel = viewModel(
        factory = QuranScreenViewModelFactory(repository, application)
    )

    fun handleBackBehaviour() {
        if (quranViewModel.selectedTab != "page") {
            quranViewModel.selectedTab = "page"
        } else {
            navController.popBackStack()
        }
    }

    LaunchedEffect(Unit) {
        try {
            quranViewModel.setCurrentPage(pageNum)
        } catch (e: Exception) {
            Log.e("error", "Error getting current page in quran screen: $e")
        }
    }

    BackHandler {
        handleBackBehaviour()
    }

    val isPlaying by quranViewModel.isPlaying.collectAsState(initial = false)
    val pageTabsMap: HashMap<String, String> = hashMapOf(
        "الصفحة" to "page", "الآيات" to "verses", "الفوائد" to "benefits", "الفيديو" to "yt"
    )
    val primaryColor = MaterialTheme.colorScheme.primary

    quranViewModel.isPageBookmarked();


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

    Column {
        Row(
            modifier = Modifier
                .padding(6.dp)
                .background(MaterialTheme.colorScheme.background)
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                horizontalArrangement = Arrangement.Start,
                verticalAlignment = Alignment.CenterVertically
            ) {
                ElevatedButton(
                    onClick = { if (!quranViewModel.isBookmarked) quranViewModel.togglePageBookmark() else confirmRemoveBookmark() },
                    elevation = elevatedButtonElevation(2.dp, 2.dp, 2.dp, 2.dp, 2.dp),
                    contentPadding = PaddingValues(0.dp),
                    shape = CircleShape,
                    modifier = Modifier
                        .padding(4.dp, 0.dp)
                        .clip(CircleShape)
                ) {
                    Icon(
                        if (quranViewModel.isBookmarked) Icons.Filled.Bookmark else Icons.Outlined.BookmarkBorder,
                        contentDescription = "Save",
                        tint = MaterialTheme.colorScheme.primary,
                    )
                }
                ElevatedButton(
                    onClick = {
                        val varName = "p_${quranViewModel.currentPageObject.pageNum}"
                        val resourceId: Int = context.getResources()
                            .getIdentifier(varName, "drawable", context.packageName)

                        val bitmap = BitmapFactory.decodeResource(context.resources, resourceId)
                        val fileName = "image.png"
                        val fileOutputStream: FileOutputStream
                        try {
                            fileOutputStream =
                                context.openFileOutput(fileName, Context.MODE_PRIVATE)
                            bitmap.compress(Bitmap.CompressFormat.PNG, 100, fileOutputStream)
                            fileOutputStream.close()
                        } catch (e: IOException) {
                            e.printStackTrace()
                        }

                        val internalFilePath = File(context.filesDir, fileName).absolutePath

                        val shareIntent = Intent(Intent.ACTION_SEND)
                        shareIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK)
                        shareIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                        shareIntent.type = "image/*"
                        val imageUri = FileProvider.getUriForFile(
                            context,
                            context.getPackageName() + ".fileprovider",
                            File(internalFilePath)
                        )
                        shareIntent.putExtra(Intent.EXTRA_STREAM, imageUri)

                        shareIntent.putExtra(Intent.EXTRA_TITLE, "تطبيق اترجة")
                        shareIntent.putExtra(
                            Intent.EXTRA_TEXT,
                            "الصفحة رقم ${quranViewModel.currentPageObject.pageNum}"
                        )

                        val chooserIntent = Intent.createChooser(shareIntent, "تطبيق اترجة")
                        chooserIntent.putExtra(Intent.EXTRA_TITLE, "تطبيق اترجة")
                        chooserIntent.putExtra(
                            Intent.EXTRA_TEXT,
                            "الصفحة رقم ${quranViewModel.currentPageObject.pageNum}"
                        )
                        chooserIntent.putExtra(Intent.EXTRA_CONTENT_QUERY, "image/png")

                        context.startActivity(
                            Intent.createChooser(
                                shareIntent, "مشاركة الصفحة"
                            )
                        );

                        File(internalFilePath).deleteOnExit();
                    },
                    elevation = elevatedButtonElevation(2.dp, 2.dp, 2.dp, 2.dp, 2.dp),
                    contentPadding = PaddingValues(0.dp),
                    shape = CircleShape,
                    modifier = Modifier
                        .padding(4.dp, 0.dp)
                        .clip(CircleShape)
                ) {
                    Icon(
                        Icons.Default.Share,
                        contentDescription = "Share",
                        tint = MaterialTheme.colorScheme.primary,
                    )
                }

                ElevatedButton(
                    onClick = { },
                    elevation = elevatedButtonElevation(2.dp, 2.dp, 2.dp, 2.dp, 2.dp),
                    contentPadding = PaddingValues(0.dp),
                    shape = CircleShape,
                    modifier = Modifier
                        .padding(0.dp)
                        .clip(CircleShape)
                ) {
                    Icon(
                        Icons.Default.Search,
                        contentDescription = "Access Search",
                        tint = MaterialTheme.colorScheme.primary,
                    )
                }

            }

            ElevatedButton(
                onClick = { handleBackBehaviour() },
                elevation = elevatedButtonElevation(2.dp, 2.dp, 2.dp, 2.dp, 2.dp),
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
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            pageTabsMap.keys.forEachIndexed { index, option ->
                Column() {
                    Text(text = option,
                        style = MaterialTheme.typography.bodyMedium,
                        textAlign = TextAlign.Center,
                        color = if (quranViewModel.selectedTab == pageTabsMap.get(option)) MaterialTheme.colorScheme.onPrimary else primaryColor,
                        modifier = Modifier
                            .padding(2.dp, 0.dp)
                            .clip(shape = RoundedCornerShape(50))
                            .drawBehind {
                                if (quranViewModel.selectedTab == pageTabsMap.get(option)) {
                                    drawCircle(
                                        color = primaryColor, radius = this.size.maxDimension
                                    )
                                }
                            }
                            .clickable { quranViewModel.selectedTab = pageTabsMap.get(option)!! }
                            .defaultMinSize(minWidth = 100.dp)
                            .padding(0.dp, 6.dp, 0.dp, 6.dp))
                }
            }
        }
        when (quranViewModel.selectedTab) {
            "page" -> Column(verticalArrangement = Arrangement.SpaceBetween) {
                PagesContainer(
                    quranViewModel.currentPageObject.pageNum,
                    { newPage ->
                        quranViewModel.setCurrentPage(newPage)
                    },
                    {
                        quranViewModel.startPlaying()
                    },
                    { quranViewModel.pausePlaying() },
                    { quranViewModel.showRepOptions = true },
                    { quranViewModel.updateSelectedRep() },
                    quranViewModel.selectedRepetition,
                    { quranViewModel.showVerseOptions = true },
                    quranViewModel.selectedVerse,
                    isPlaying,
                    { quranViewModel.decreasePlaybackSpeed() },
                    { quranViewModel.increasePlaybackSpeed() },
                    { quranViewModel.resetPlayer() },
                    quranViewModel.isDownloading,
                    { quranViewModel.goNextVerse() },
                    { quranViewModel.goPreviousVerse() },
                    quranViewModel.continuousPlay,
                    { value -> quranViewModel.continuousPlay = value },
                    quranViewModel.shouldAutoPlay,
                    quranViewModel.playbackSpeed
                )
            }

            "verses" -> VersesSection(
                quranViewModel.currentPageObject.pageContent.filter { item -> item.type == "verse" },
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
                repository
            )

            "benefits" -> Benefits(
                quranViewModel.currentPageObject.benefits,
                quranViewModel.currentPageObject.appliance,
                quranViewModel.currentPageObject.guidance,
                quranViewModel.currentPageObject.pageNum
            )

            "yt" -> YouTube(quranViewModel.currentPageObject.ytLink.split("v=").last())
        }

        VersesBottomSheet(quranViewModel.showVersesSheet,
            { quranViewModel.showVersesSheet = false },
            { targetVerse ->
                quranViewModel.tafseerTargetVerse = targetVerse;
                quranViewModel.showVersesSheet = false;
                quranViewModel.showTafseerSheet = true
            },
            quranViewModel.currentPageObject.pageContent.filter { item -> item.type == "verse" });

        TafseerBottomSheet(
            context,
            showTafseerSheet = quranViewModel.showTafseerSheet,
            onDismiss = { quranViewModel.showTafseerSheet = false },
            quranViewModel.verseTafseer,
            quranViewModel.verseE3rab,
            quranViewModel.selectedTafseer,
            onClickTafseerOptions = { quranViewModel.showTafseerOptions = true },
            mode = quranViewModel.tafseerSheetMode
        )

        if (quranViewModel.showRepOptions) {
            SelectRepDialog(
                quranViewModel.repetitionOptionsMap.keys.toTypedArray(),
                { quranViewModel.showRepOptions = false },
                { selectedRep ->
                    quranViewModel.selectedRepetition = selectedRep;
                    quranViewModel.showRepOptions = false
                })
        }
    }

    if (quranViewModel.showVerseOptions) {
        SelectVerseDialog({ quranViewModel.showVerseOptions = false }, { selectedVerse ->
            quranViewModel.selectedVerse = selectedVerse;
            quranViewModel.showVerseOptions = false
        }, quranViewModel.getCurrentPageVerses()
        )
    }


    if (quranViewModel.showTafseerOptions) {
        SelectTafseerDialog({ quranViewModel.showTafseerOptions = false }, { selectedTafseer ->
            quranViewModel.updateSelectedTafseer(selectedTafseer);
            quranViewModel.showTafseerOptions = false
        }, quranViewModel.tafseerNamesMap
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
    selectedTafseer: String,
    onClickTafseerOptions: () -> Unit,
    mode: String,
) {
    val modalBottomSheetState = rememberModalBottomSheetState()

    if (showTafseerSheet) {
        ModalBottomSheet(
            onDismissRequest = { onDismiss() },
            sheetState = modalBottomSheetState,
            dragHandle = { BottomSheetDefaults.DragHandle() },
        ) {
            if (mode == "tafseer") {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(MaterialTheme.colorScheme.tertiary)
                ) {
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
                    Row(
                        horizontalArrangement = Arrangement.End,
                        verticalAlignment = Alignment.Top,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        ElevatedButton(
                            onClick = {
                                copyToClipboard(
                                    context,
                                    tafseer,
                                    "تم تسخ التفسير بنجاح"
                                );
                            },
                            elevation = elevatedButtonElevation(2.dp, 2.dp, 2.dp, 2.dp, 2.dp),
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
                    }
                    Text(
                        text = tafseer,
                        color = Color.Black,
                        modifier = Modifier
                            .padding(10.dp)
                            .verticalScroll(rememberScrollState()),
                        style = MaterialTheme.typography.displayMedium,
                        textAlign = TextAlign.Right,
                    )
                }
            } else {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(MaterialTheme.colorScheme.tertiary)
                ) {
                    Row(
                        horizontalArrangement = Arrangement.End,
                        verticalAlignment = Alignment.Top,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        ElevatedButton(
                            onClick = { copyToClipboard(context, e3rab, "تم تسخ الاعراب بنجاح"); },
                            elevation = elevatedButtonElevation(2.dp, 2.dp, 2.dp, 2.dp, 2.dp),
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
                    }
                    Text(
                        text = e3rab,
                        color = Color.Black,
                        modifier = Modifier
                            .padding(10.dp)
                            .verticalScroll(rememberScrollState()),
                        style = MaterialTheme.typography.displayMedium,
                        textAlign = TextAlign.Right,
                    )
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
                        if (option.type == "verse") {
                            Row(modifier = Modifier
                                .fillMaxWidth()
                                .clickable { onOptionClick(option) }
                                .padding(6.dp)) {
                                Text(
                                    text = "الاية ${option.verseNum}",
                                    modifier = Modifier.fillMaxWidth(),
                                    textAlign = TextAlign.Right,
                                    color = MaterialTheme.colorScheme.onSecondary,
                                    style = MaterialTheme.typography.displayMedium,
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
                                    style = MaterialTheme.typography.displayMedium,
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
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
    disposeFunction: () -> Unit,
    isDownloading: Boolean,
    onNextClicked: () -> Unit,
    onPreviousClicked: () -> Unit,
    continuousPlay: Boolean,
    setContPlay: (Boolean) -> Unit,
    shouldAutoPlay: Boolean,
    playbackSpeed: Float
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


    /*val coroutineScope = rememberCoroutineScope()  // Use rememberCoroutineScope
    Button(onClick = {
        coroutineScope.launch {
            pagerState.animateScrollToPage(2)  // Move to page 2 (zero-based index)
        }
    }) {
        Text(text = "Go to Page 2")
    }*/

    LaunchedEffect(pageNum) {
        if (shouldAutoPlay) {
            pagerState.animateScrollToPage(pageNum!!.toInt() - 1)
            onPlayClicked()
        } else {
            pagerState.scrollToPage(pageNum!!.toInt() - 1)

        }
    }

    LaunchedEffect(pagerState) {
        snapshotFlow { pagerState.currentPage }.collect { page ->
            if (hasPageChanged.value) {
                Log.d("Page change", "Page changed to $page")
                onPageChanged("${page + 1}")
            } else {
                hasPageChanged.value = true // Skip the first value
            }
        }
    }

    DisposableEffect(Unit) {
        // This block will be executed when the composable is first composed
        onDispose {
            disposeFunction()
        }
    }


    var offsetY by remember { mutableStateOf(0f) }
    val offsetYAnimatable = remember { Animatable(0f) }


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
            SinglePage(quranPagesNumbers[index])
        }

        AnimatedVisibility(
            visible = showController,
            enter = slideInVertically(initialOffsetY = { -it }) + expandVertically(
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
                Row(
                    modifier = Modifier
                        .padding(0.dp, 0.dp, 0.dp, 5.dp)
                        .fillMaxWidth()
                        .clip(shape = RoundedCornerShape(4.dp))
                        .background(MaterialTheme.colorScheme.primary)
                        .clickable { onClickRepOptions() },
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "مرات التكرار: $selectedRep",
                        color = MaterialTheme.colorScheme.onPrimary,
                        style = MaterialTheme.typography.bodyMedium,
                        textAlign = TextAlign.Right,
                        modifier = Modifier.padding(12.dp)
                    )
                    Icon(
                        Icons.Default.ArrowDropDown,
                        contentDescription = "",
                        tint = MaterialTheme.colorScheme.onPrimary
                    )
                }
                Row(
                    modifier = Modifier
                        .background(color = MaterialTheme.colorScheme.primaryContainer)
                        .fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        horizontalArrangement = Arrangement.Start,
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.clickable { setContPlay(!continuousPlay) }
                    ) {
                        Checkbox(
                            checked = continuousPlay,
                            onCheckedChange = { newCheckedState -> setContPlay(newCheckedState) }
                        )
                        Text(
                            text = "تشغيل متتال",
                            color = MaterialTheme.colorScheme.primary,
                            style = MaterialTheme.typography.bodyMedium,
                            textAlign = TextAlign.Right,
                        )
                    }
                    if (isPlaying) {
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
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceAround,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(modifier = Modifier
                        .padding(0.dp, 6.dp, 6.dp, 5.dp)
                        .fillMaxWidth(0.3f)
                        .clip(shape = RoundedCornerShape(4.dp))
                        .background(MaterialTheme.colorScheme.primary)
                        .clickable { if (!isDownloading) onClickVerseOptions() }) {
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

                    Row(
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.weight(1.0f)
                    ) {
                        if (isPlaying && !isDownloading) {
                            Image(painter = painterResource(R.drawable.faster),
                                contentDescription = "faster",
                                modifier = Modifier
                                    .clickable { onFasterClicked() }
                                    .size(25.dp))
                            Image(painter = painterResource(R.drawable.next),
                                contentDescription = "next",
                                modifier = Modifier
                                    .clickable { onNextClicked() }
                                    .size(25.dp),
                                colorFilter = ColorFilter.tint(MaterialTheme.colorScheme.primary)
                            )
                        }
                        if (isDownloading) {
                            CircularProgressIndicator(
                                modifier = Modifier.width(32.dp),
                                color = MaterialTheme.colorScheme.primary,
                                trackColor = MaterialTheme.colorScheme.surfaceVariant,
                            )
                        } else if (isPlaying) {
                            Image(painter = painterResource(R.drawable.playing),
                                contentDescription = "pause",
                                modifier = Modifier
                                    .padding(10.dp, 0.dp)
                                    .clickable { onPauseClicked() }
                                    .size(35.dp))
                            //  ReplayIcon(selectedRep, onClickUpdateRep)
                        } else {
                            Image(painter = painterResource(R.drawable.play),
                                contentDescription = "play",
                                modifier = Modifier
                                    .clickable { onPlayClicked() }
                                    .size(35.dp),
                                colorFilter = ColorFilter.tint(MaterialTheme.colorScheme.primary))
                        }
                        if (isPlaying && !isDownloading) {
                            Image(painter = painterResource(R.drawable.previous),
                                contentDescription = "prev",
                                modifier = Modifier
                                    .clickable { onPreviousClicked() }
                                    .size(25.dp),
                                colorFilter = ColorFilter.tint(MaterialTheme.colorScheme.primary)
                            )
                            Image(painter = painterResource(R.drawable.slower),
                                contentDescription = "slower",
                                modifier = Modifier
                                    .clickable { onSlowerClicked() }
                                    .size(25.dp))
                        }
                    }
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
fun SinglePage(pageNum: String) {
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
                .fillMaxWidth(if (configuration.orientation == Configuration.ORIENTATION_LANDSCAPE) 0.75f else 1.0f)
                .fillMaxHeight(0.92f),
            contentScale = if (configuration.orientation == Configuration.ORIENTATION_LANDSCAPE) ContentScale.Crop else ContentScale.Fit
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

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun Benefits(
    benefits: Array<String>,
    appliance: Array<String>,
    guidance: Array<String>,
    pageNum: String
) {
    val context = LocalContext.current;
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
                    Text(
                        text = "فوائد الصفحة",
                        color = MaterialTheme.colorScheme.primary,
                        style = MaterialTheme.typography.displayMedium,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
            items(benefits) { benefit ->
                Row(
                    modifier = Modifier
                        .padding(5.dp)
                        .fillMaxWidth()
                        .clip(shape = RoundedCornerShape(10.dp))
                        .background(MaterialTheme.colorScheme.primary)
                        .combinedClickable(
                            onClick = {},
                            onLongClick = {
                                val sendIntent: Intent = Intent().apply {
                                    action = Intent.ACTION_SEND
                                    putExtra(Intent.EXTRA_SUBJECT, "فائدة قرآنية")
                                    putExtra(Intent.EXTRA_TITLE, "تطبيق اترجة")
                                    putExtra(
                                        Intent.EXTRA_TEXT,
                                        "من الفوائد القرآنية للصفحة $pageNum \n $benefit"
                                    )
                                    type = "text/plain"
                                }

                                val shareIntent = Intent.createChooser(sendIntent, "مشاركة الفائدة")
                                startActivity(context, shareIntent, null)
                            },
                        )
                        .padding(5.dp),
                    verticalAlignment = Alignment.Top,
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    Icon(
                        painterResource(id = R.drawable.lightbulb),
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onPrimary,
                        modifier = Modifier
                            .padding(0.dp, 6.dp, 4.dp, 6.dp)
                            .fillMaxWidth(0.08f)
                    )
                    Text(
                        text = benefit,
                        color = MaterialTheme.colorScheme.onPrimary,
                        style = MaterialTheme.typography.displayMedium,
                        modifier = Modifier
                            .padding(0.dp, 6.dp)
                            .fillMaxWidth(0.92f)
                    )
                }
            }

            if (guidance.size != 0) {
                item {
                    Text(
                        text = "توجيهات الصفحة",
                        color = MaterialTheme.colorScheme.primary,
                        style = MaterialTheme.typography.displayMedium,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }

            items(guidance) { guidanceItem ->
                Row(
                    modifier = Modifier
                        .padding(5.dp)
                        .fillMaxWidth()
                        .clip(shape = RoundedCornerShape(10.dp))
                        .background(MaterialTheme.colorScheme.primary)
                        .combinedClickable(
                            onClick = {},
                            onLongClick = {
                                val sendIntent: Intent = Intent().apply {
                                    action = Intent.ACTION_SEND
                                    putExtra(Intent.EXTRA_SUBJECT, "توجيه قرآني")
                                    putExtra(Intent.EXTRA_TITLE, "تطبيق اترجة")
                                    putExtra(
                                        Intent.EXTRA_TEXT,
                                        "من التوجيهات القرآنية للصفحة $pageNum \n $guidanceItem"
                                    )
                                    type = "text/plain"
                                }

                                val shareIntent = Intent.createChooser(sendIntent, "مشاركة التوجيه")
                                startActivity(context, shareIntent, null)
                            },
                        )
                        .padding(5.dp),
                    verticalAlignment = Alignment.Top,
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    Icon(
                        painterResource(id = R.drawable.lightbulb),
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onPrimary,
                        modifier = Modifier
                            .padding(0.dp, 10.dp, 4.dp, 0.dp)
                            .fillMaxWidth(0.08f)
                    )
                    Text(
                        text = guidanceItem,
                        color = MaterialTheme.colorScheme.onPrimary,
                        style = MaterialTheme.typography.displayMedium,
                        modifier = Modifier
                            .padding(0.dp, 6.dp)
                            .fillMaxWidth(0.92f)
                    )
                }
            }

            if (appliance.size != 0) {
                item {
                    Text(
                        text = "الجانب التطبيقي",
                        color = MaterialTheme.colorScheme.primary,
                        style = MaterialTheme.typography.displayMedium,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }

            items(appliance) { applianceItem ->
                Row(
                    modifier = Modifier
                        .padding(5.dp)
                        .fillMaxWidth()
                        .clip(shape = RoundedCornerShape(10.dp))
                        .background(MaterialTheme.colorScheme.primary)
                        .combinedClickable(
                            onClick = {},
                            onLongClick = {
                                val sendIntent: Intent = Intent().apply {
                                    action = Intent.ACTION_SEND
                                    putExtra(Intent.EXTRA_SUBJECT, "تطبيق قرآني")
                                    putExtra(Intent.EXTRA_TITLE, "تطبيق اترجة")
                                    putExtra(
                                        Intent.EXTRA_TEXT,
                                        "من التطبيقات القرآنية للصفحة $pageNum \n $applianceItem"
                                    )
                                    type = "text/plain"
                                }

                                val shareIntent = Intent.createChooser(sendIntent, "مشاركة التطبيق")
                                startActivity(context, shareIntent, null)
                            },
                        )
                        .padding(5.dp),
                    verticalAlignment = Alignment.Top,
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    Icon(
                        painterResource(id = R.drawable.lightbulb),
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onPrimary,
                        modifier = Modifier
                            .padding(0.dp, 10.dp, 4.dp, 0.dp)
                            .fillMaxWidth(0.08f)
                    )
                    Text(
                        text = applianceItem,
                        color = MaterialTheme.colorScheme.onPrimary,
                        style = MaterialTheme.typography.displayMedium,
                        modifier = Modifier
                            .padding(0.dp, 6.dp)
                            .fillMaxWidth(0.92f)
                    )
                }
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
        if (!checkNetworkConnectivity(context)) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center) {
                Text(
                    text = "تعذر عرض المقطع لعدم توفر اتصال بالانترنت",
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
                    val sr = Helpers.isMyServiceRunning(MediaPlayerService::class.java, context);
                    println("service running $sr")
                    if (sr) {
                        val stopServiceIntent = Intent(context, MediaPlayerService::class.java)
                        stopServiceIntent.setAction("TERMINATE")
                        context.startService(stopServiceIntent)
                    }
                }
                println(state)
            }
        })
        view
    })
}

@Composable
fun VersesSection(
    items: List<PageContent>,
    onTafseerClick: (String) -> Unit,
    onE3rabClick: (String) -> Unit,
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

    Column {
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
                            text = "${Helpers.convertToIndianNumbers(item.pageContent.verseNum)} ${item.pageContent.verseText}",
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
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(MaterialTheme.colorScheme.background)
                                .padding(8.dp)
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Center,
                                modifier = Modifier
                                    .weight(1.0f)
                                    .background(MaterialTheme.colorScheme.tertiary)
                                    .clickable { versesSectionViewModel.shareVerse(item.pageContent) }
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
                                        onTafseerClick("${item.pageContent.surahNum}-${item.pageContent.verseNum}")
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
                                        onE3rabClick("${item.pageContent.surahNum}-${item.pageContent.verseNum}")
                                    }
                                    .padding(4.dp, 6.dp)
                            ) {
                                Text(
                                    text = "الإعراب",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = Color.Black
                                )
                            }
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

fun checkNetworkConnectivity(context: Context): Boolean {
    val connectivityManager =
        context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager

    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
        val networkCapabilities = connectivityManager.activeNetwork ?: return false
        val activeNetwork =
            connectivityManager.getNetworkCapabilities(networkCapabilities) ?: return false

        return when {
            activeNetwork.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) -> true
            activeNetwork.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) -> true
            // Other transports like Bluetooth, Ethernet, etc.
            else -> false
        }
    } else {
        // For devices with SDK < 23
        val activeNetworkInfo = connectivityManager.activeNetworkInfo
        return activeNetworkInfo != null && activeNetworkInfo.isConnected
    }
}

fun savePageNumAsLatest(num: Int, context: Context) {
    val sharedPreferences = context.getSharedPreferences("", Context.MODE_PRIVATE);
    sharedPreferences.edit().putInt("lastReadPage", num).apply();
    Toast.makeText(context, "تم حفظ رقم الصفحة للعودة السريعة", Toast.LENGTH_LONG).show();
}