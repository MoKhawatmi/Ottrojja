package com.ottrojja.screens.mainScreen

import android.app.Application
import android.content.Intent
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Circle
import androidx.compose.material.icons.outlined.ArrowCircleLeft
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.ottrojja.classes.Screen
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import com.ottrojja.R
import com.ottrojja.classes.Helpers
import com.ottrojja.classes.QuranRepository
import com.ottrojja.classes.SearchResult
import com.ottrojja.composables.Header
import com.ottrojja.composables.ListHorizontalDivider
import com.ottrojja.composables.OttrojjaTabs
import com.ottrojja.composables.PillShapedTextFieldWithIcon

@Composable
fun MainScreen(
    navController: NavController,
    repository: QuranRepository,
    section: String
) {
    val context = LocalContext.current;
    val application = context.applicationContext as Application
    val focusManager = LocalFocusManager.current
    val keyboardController = LocalSoftwareKeyboardController.current



    val mainViewModel: MainViewModel = viewModel(
        factory = MainViewModelFactory(repository, application)
    )

    val primaryColor = MaterialTheme.colorScheme.primary;

    LaunchedEffect(Unit) {
        mainViewModel.invokeMostRecentPage()
        if (section.length > 0) {
            println("section $section")
            mainViewModel.showImageList = false;
            mainViewModel.clickBrowsingOption(BrowsingOption.valueOf(section))
        }
    }

    DisposableEffect(Unit) {
        onDispose {
            mainViewModel.showImageList = true;
            mainViewModel.searchFilter = "";
            mainViewModel.quranSearchResults.clear()
        }
    }


    BackHandler() {
        if (!mainViewModel.showImageList) {
            mainViewModel.showImageList = true;
        } else {
            val homeIntent = Intent(Intent.ACTION_MAIN).apply {
                addCategory(Intent.CATEGORY_HOME)
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }
            context.startActivity(homeIntent)
        }
    }


    if (mainViewModel.showImageList) {
        Column(modifier = Modifier, verticalArrangement = Arrangement.Top) {
            Header(isMain = true)
            Column(
                verticalArrangement = Arrangement.Top,
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                    .padding(12.dp)
                    .background(MaterialTheme.colorScheme.background)
                    .verticalScroll(rememberScrollState())
            ) {
                if (mainViewModel.mostRecentPage.length > 0) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier
                            .padding(12.dp, 8.dp)
                            .fillMaxWidth()
                            .background(Color.Transparent)
                            .border(
                                BorderStroke(1.dp, color = MaterialTheme.colorScheme.primary),
                                shape = RoundedCornerShape(10.dp)
                            )
                            .clip(RoundedCornerShape(10))
                            .clickable {
                                navController.navigate(
                                    Screen.QuranScreen.invokeRoute(
                                        mainViewModel.mostRecentPage
                                    )
                                )
                            }
                            .padding(12.dp, 8.dp)
                    ) {
                        Text(
                            text = "عودة الى اخر صفحة (${mainViewModel.mostRecentPage})",
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Icon(
                            Icons.Outlined.ArrowCircleLeft,
                            contentDescription = "Quick Transition",
                            tint = MaterialTheme.colorScheme.primary,
                        )
                    }
                }
                Row(
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .padding(12.dp, 24.dp)
                        .clip(RoundedCornerShape(10))
                        .background(
                            brush = Brush.horizontalGradient(
                                colors = listOf(
                                    Color.Red,
                                    Color(0xDD660000)
                                )
                            )
                        )
                        .fillMaxWidth()
                        .clickable {
                            mainViewModel.selectedSection = BrowsingOption.الصفحات;
                            mainViewModel.showImageList = false;
                        }
                        .padding(12.dp)
                ) {
                    Text(
                        text = "صفحات القرآن",
                        style = MaterialTheme.typography.bodyLarge,
                        color = Color.White
                    );
                    Image(
                        painter = painterResource(id = R.drawable.q_image1),
                        contentDescription = "",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .size(112.dp)
                            .clip(CircleShape)
                    )
                }
                Row(
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .padding(12.dp, 24.dp)
                        .clip(RoundedCornerShape(10))
                        .background(
                            brush = Brush.horizontalGradient(
                                colors = listOf(
                                    Color.Green,
                                    Color(0xDD006600)
                                )
                            )
                        )
                        .fillMaxWidth()
                        .clickable {
                            mainViewModel.selectedSection = BrowsingOption.السور;
                            mainViewModel.showImageList = false;
                        }
                        .padding(12.dp)
                ) {
                    Text(
                        text = "سور القرآن",
                        style = MaterialTheme.typography.bodyLarge,
                        color = Color.White
                    );
                    Image(
                        painter = painterResource(id = R.drawable.q_image2),
                        contentDescription = "",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .size(112.dp)
                            .clip(CircleShape)
                    )
                }
                Row(
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .padding(12.dp, 24.dp)
                        .clip(RoundedCornerShape(10))
                        .background(
                            brush = Brush.horizontalGradient(
                                colors = listOf(
                                    Color.Blue,
                                    Color(0xDD000066)
                                )
                            )
                        )
                        .fillMaxWidth()
                        .clickable {
                            mainViewModel.selectedSection = BrowsingOption.الاجزاء;
                            mainViewModel.showImageList = false;
                        }
                        .padding(12.dp)
                ) {
                    Text(
                        text = "اجزاء القرآن",
                        style = MaterialTheme.typography.bodyLarge,
                        color = Color.White
                    );
                    Image(
                        painter = painterResource(id = R.drawable.q_image3),
                        contentDescription = "",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .size(112.dp)
                            .clip(CircleShape)
                    )
                }
                Row(
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .padding(12.dp, 24.dp)
                        .clip(RoundedCornerShape(10))
                        .background(
                            brush = Brush.horizontalGradient(
                                colors = listOf(
                                    Color(0xFFD0B968),
                                    Color(0xFFA86809)
                                )
                            )
                        )
                        .fillMaxWidth()
                        .clickable {
                            mainViewModel.selectedSection = BrowsingOption.البحث;
                            mainViewModel.invokeLatestSearchOperation();
                            mainViewModel.showImageList =
                                false;
                        }
                        .padding(12.dp)
                ) {
                    Text(
                        text = "البحث",
                        style = MaterialTheme.typography.bodyLarge,
                        color = Color.White
                    );
                    Image(
                        painter = painterResource(id = R.drawable.q_image4),
                        contentDescription = "",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .size(112.dp)
                            .clip(CircleShape)
                    )
                }

                Row(modifier = Modifier.height(75.dp)) {}
            }
        }
    }
    else {
        Column {
            Row(
                modifier = Modifier
                    .padding(10.dp)
                    .fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceAround,
                verticalAlignment = Alignment.Bottom
            )
            {
                Image(
                    painter = painterResource(R.drawable.logo),
                    contentDescription = null,
                    modifier = Modifier.size(50.dp),
                    colorFilter = ColorFilter.tint(primaryColor)
                )

                PillShapedTextFieldWithIcon(
                    value = mainViewModel.searchFilter,
                    onValueChange = { newText ->
                        mainViewModel.searchFilter = newText
                        if (mainViewModel.selectedSection == BrowsingOption.البحث) {
                            mainViewModel.searchInQuran(newText)
                        }
                    },
                    leadingIcon = painterResource(id = R.drawable.search),
                    modifier = Modifier.fillMaxWidth(0.9f)
                )
            }

            OttrojjaTabs(
                items = BrowsingOption.entries,
                selectedItem = mainViewModel.selectedSection,
                onClickTab = { option ->
                    mainViewModel.clickBrowsingOption(option);
                    focusManager.clearFocus()
                    keyboardController?.hide()
                })

            when (mainViewModel.selectedSection) {
                BrowsingOption.الصفحات -> {
                    BrowseMenu(mainViewModel.getPagesList(), navController)
                }

                BrowsingOption.السور -> {
                    ChaptersMenu(mainViewModel.getChaptersList(), navController)
                }

                BrowsingOption.الاجزاء -> {
                    PartsMenu(mainViewModel.getPartsList(), navController)
                }

                BrowsingOption.البحث -> {
                    SearchMenu(
                        mainViewModel.quranSearchResults,
                        navController,
                        { value -> mainViewModel.shareVerse(context, value) })
                }
            }
        }
    }
}

@Composable
fun BrowseMenu(
    items: List<String> = listOf<String>(),
    navController: NavController
) {
    val keyboardController = LocalSoftwareKeyboardController.current;

    LazyColumn(
        Modifier
            .fillMaxHeight(0.9f)
            .background(MaterialTheme.colorScheme.background)
    ) {
        items(items) { item ->
            Column(modifier = Modifier
                .padding(12.dp, 2.dp)
                .fillMaxWidth()
                .background(MaterialTheme.colorScheme.background)
                .clickable {
                    keyboardController!!.hide();
                    navController.navigate(Screen.QuranScreen.invokeRoute(item.split(" ")[1]))
                }
            ) {
                Row( //Materialtheme.typography.bodylarge
                    modifier = Modifier.padding(12.dp)
                ) {
                    Text(text = item, color = Color.Black)
                }
                ListHorizontalDivider()
            }
        }
    }
}

@Composable
fun PartsMenu(
    items: List<PartData> = listOf<PartData>(),
    navController: NavController
) {
    val keyboardController = LocalSoftwareKeyboardController.current;

    LazyColumn(
        Modifier
            .fillMaxHeight(0.9f)
            .background(MaterialTheme.colorScheme.background)
    ) {
        items(items) { item ->
            Column(modifier = Modifier
                .padding(12.dp, 2.dp)
                .fillMaxWidth()
                .background(MaterialTheme.colorScheme.background)
                .clickable {
                    keyboardController!!.hide();
                    navController.navigate(Screen.QuranScreen.invokeRoute(item.partStartPage))
                }
            ) {
                Row(
                    modifier = Modifier.padding(12.dp, 12.dp, 12.dp, 0.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(text = item.partName, color = Color.Black)
                }
                Row(
                    modifier = Modifier.padding(12.dp, 0.dp, 0.dp, 0.dp),
                    verticalAlignment = Alignment.Top
                ) {
                    Text(
                        text = "{${item.firstWords}}",
                        color = Color.Black,
                        style = MaterialTheme.typography.labelSmall,
                    )
                }
                ListHorizontalDivider()
            }
        }
    }
}

@Composable
fun ChaptersMenu(
    items: List<ChapterData> = listOf<ChapterData>(),
    navController: NavController
) {
    val keyboardController = LocalSoftwareKeyboardController.current;
    LazyColumn(
        Modifier
            .fillMaxHeight(0.9f)
            .background(MaterialTheme.colorScheme.background)
    ) {
        items(items) { item ->
            Column(modifier = Modifier
                .padding(12.dp, 2.dp)
                .fillMaxWidth()
                .background(MaterialTheme.colorScheme.background)
                .clickable {
                    keyboardController!!.hide();
                    navController.navigate(Screen.QuranScreen.invokeRoute(item.chapterStartPage))
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
                ListHorizontalDivider()
            }
        }
    }
}


@OptIn(ExperimentalFoundationApi::class)
@Composable
fun SearchMenu(
    items: List<SearchResult> = listOf<SearchResult>(),
    navController: NavController,
    shareVerse: (SearchResult) -> Unit
) {
    val keyboardController = LocalSoftwareKeyboardController.current;

    LazyColumn(
        Modifier
            .background(MaterialTheme.colorScheme.background)
    ) {
        item {
            Column(
                modifier = Modifier
                    .padding(12.dp, 2.dp)
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.background)
            ) {
                Row(
                    modifier = Modifier
                        .padding(12.dp)
                        .fillMaxWidth(),
                    horizontalArrangement = Arrangement.Start,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "نتائج البحث: ${items.size}",
                        color = Color.Black,
                        style = MaterialTheme.typography.titleLarge
                    )
                }
                ListHorizontalDivider()
            }
        }
        items(items) { item ->
            Column(
                modifier = Modifier
                    .padding(12.dp, 0.dp)
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.background)
                    .combinedClickable(
                        onClick = {
                            keyboardController!!.hide();
                            navController.navigate(Screen.QuranScreen.invokeRoute(item.pageNum))
                        },
                        onLongClick = {
                            shareVerse(item)
                        },
                    )
            ) {
                Row(
                    modifier = Modifier
                        .padding(12.dp)
                        .fillMaxWidth(),
                    horizontalArrangement = Arrangement.Start,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "{${item.verseText} ${Helpers.convertToIndianNumbers(item.verseNum)}} - ${item.surahName}",
                        color = Color.Black,
                        style = MaterialTheme.typography.titleLarge
                    )
                }
                ListHorizontalDivider()
            }
        }
        item {
            Row(modifier = Modifier.height(100.dp)) {}
        }
    }

}

/*
@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun QuickAccessBar(onMoveClicked: (String) -> Unit) {
    var inputValue by remember { mutableStateOf("") }
    val keyboardController = LocalSoftwareKeyboardController.current;
    Row(
        Modifier
            .fillMaxWidth()
            .height(IntrinsicSize.Min)
            .padding(2.dp, 6.dp)
    ) {
        TextField(
            value = inputValue,
            onValueChange = { inputValue = it.digitsOnly() },
            modifier = Modifier
                .fillMaxHeight()
                .weight(4.5f),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
        )
        Button(modifier = Modifier
            .fillMaxHeight()
            .weight(1.5f),
            shape = RoundedCornerShape(0.dp, 10.dp, 10.dp, 0.dp),
            onClick = { keyboardController!!.hide(); onMoveClicked(inputValue) }) {
            Text(text = "انتقال")
        }
    }
}

fun String.digitsOnly(): String {
    val regex = Regex("[^0-9]")
    return regex.replace(this, "")
}
@Composable
fun FilterBar(inputValue: String, onChange: (String) -> Unit) {
    Row(
        Modifier
            .fillMaxWidth()
            .height(IntrinsicSize.Min)
            .padding(2.dp, 6.dp)
    ) {
        TextField(
            value = inputValue,
            onValueChange = { onChange(it) },
            modifier = Modifier
                .fillMaxHeight()
                .weight(5f),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text)
        )
    }
}*/
