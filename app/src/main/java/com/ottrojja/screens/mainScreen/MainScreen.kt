package com.ottrojja.screens.mainScreen

import android.app.Application
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Circle
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.ottrojja.classes.Screen
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.sp
import com.ottrojja.R
import com.ottrojja.classes.Helpers
import com.ottrojja.classes.PageContent
import com.ottrojja.classes.QuranPage
import com.ottrojja.classes.QuranRepository
import com.ottrojja.classes.SearchResult
import com.ottrojja.composables.Header
import com.ottrojja.screens.quranScreen.QuranScreenViewModelFactory
import com.ottrojja.screens.quranScreen.QuranViewModel

@Composable
fun MainScreen(
    navController: NavController,
    modifier: Modifier = Modifier,
    repository: QuranRepository
) {

    val mainViewModel: MainViewModel = viewModel(
        factory = MainViewModelFactory(repository)
    )


    val browsingOptions = arrayOf("الصفحات", "الاجزاء", "السور", "البحث")
    val primaryColor = MaterialTheme.colorScheme.primary;

    DisposableEffect(Unit) {
        onDispose {
            mainViewModel.showImageList = true;
        }
    }

    BackHandler(enabled = !mainViewModel.showImageList) {
        mainViewModel.showImageList = true;
    }

    if (mainViewModel.showImageList) {
        Column(modifier = Modifier, verticalArrangement = Arrangement.Top) {
            Header()
            Column(
                verticalArrangement = Arrangement.Top,
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                    .padding(12.dp)
                    .background(MaterialTheme.colorScheme.background)
                    .verticalScroll(rememberScrollState())
            ) {
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
                            mainViewModel.selectedSection = 0; mainViewModel.showImageList = false;
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
                            mainViewModel.selectedSection = 2; mainViewModel.showImageList = false;
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
                            mainViewModel.selectedSection = 1; mainViewModel.showImageList = false;
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
            }
        }
    } else {
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
                        if (mainViewModel.selectedSection != 3) {
                            mainViewModel.searchFilter = newText
                        } else {
                            mainViewModel.searchFilter = newText; mainViewModel.searchInQuran(
                                newText
                            )
                        }
                    },
                    leadingIcon = painterResource(id = R.drawable.search),
                    modifier = Modifier.fillMaxWidth(0.9f)
                )
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                browsingOptions.forEachIndexed { index, option ->
                    Column() {
                        Text(
                            text = option,
                            style = MaterialTheme.typography.bodyMedium,
                            textAlign = TextAlign.Center,
                            color = if (mainViewModel.selectedSection == index) MaterialTheme.colorScheme.onPrimary else primaryColor,
                            modifier = Modifier
                                .padding(2.dp, 0.dp)
                                .clip(shape = RoundedCornerShape(50))
                                .drawBehind {
                                    if (mainViewModel.selectedSection == index) {
                                        drawCircle(
                                            color = primaryColor,
                                            radius = this.size.maxDimension
                                        )
                                    }
                                }
                                .clickable {
                                    mainViewModel.selectedSection =
                                        index; mainViewModel.searchFilter = ""
                                }
                                .defaultMinSize(minWidth = 100.dp)
                                .padding(0.dp, 6.dp, 0.dp, 6.dp)
                        )
                    }
                }
            }
            when (mainViewModel.selectedSection) {
                0 -> {
                    BrowseMenu(mainViewModel.getPagesList(), navController)
                }

                1 -> {
                    PartsMenu(mainViewModel.getPartsList(), navController)
                }

                2 -> {
                    ChaptersMenu(mainViewModel.getChaptersList(), navController)
                }

                3 -> {
                    SearchMenu(mainViewModel.quranSearchResults, navController)
                }
            }
        }
    }
}

@OptIn(ExperimentalComposeUiApi::class)
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
                Row(
                    modifier = Modifier.padding(12.dp)
                ) {
                    Text(text = item, color = Color.Black)
                }
                Box(
                    Modifier
                        .fillMaxWidth()
                        .height(1.dp)
                        .background(Color.Black.copy(alpha = 0.1f))
                )
            }
        }
    }
}

@OptIn(ExperimentalComposeUiApi::class)
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
                    modifier = Modifier.padding(12.dp)
                ) {
                    Text(text = item.partName, color = Color.Black)
                }
                Box(
                    Modifier
                        .fillMaxWidth()
                        .height(1.dp)
                        .background(Color.Black.copy(alpha = 0.1f))
                )
            }
        }
    }
}

@OptIn(ExperimentalComposeUiApi::class)
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
                    navController?.navigate(Screen.QuranScreen.invokeRoute(item.chapterStartPage))
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
                Box(
                    Modifier
                        .fillMaxWidth()
                        .height(1.dp)
                        .background(Color.Black.copy(alpha = 0.1f))
                )
            }
        }
    }
}


@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun SearchMenu(
    items: List<SearchResult> = listOf<SearchResult>(),
    navController: NavController
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
                Box(
                    Modifier
                        .fillMaxWidth()
                        .height(1.dp)
                        .background(Color.Black.copy(alpha = 0.1f))
                )
            }
        }
        items(items) { item ->
            Column(modifier = Modifier
                .padding(12.dp, 0.dp)
                .fillMaxWidth()
                .background(MaterialTheme.colorScheme.background)
                .clickable {
                    keyboardController!!.hide();
                    navController.navigate(Screen.QuranScreen.invokeRoute("${item.pageNum}"))
                }
            ) {
                Row(
                    modifier = Modifier
                        .padding(12.dp)
                        .fillMaxWidth(),
                    horizontalArrangement = Arrangement.Start,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "${Helpers.convertToIndianNumbers(item.verseNum)} ${item.verseText}",
                        color = Color.Black,
                        style = MaterialTheme.typography.titleLarge
                    )
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
            Row(modifier = Modifier.height(100.dp)) {}
        }
    }

}


@Composable
fun PillShapedTextFieldWithIcon(
    value: String,
    onValueChange: (String) -> Unit,
    leadingIcon: Painter,
    modifier: Modifier = Modifier
) {
    var isFocused by remember { mutableStateOf(false) }

    val textFieldModifier = modifier
        .background(
            Color.White,
            shape = CircleShape
        )
        .border(1.dp, Color.Black.copy(alpha = 0.2f), shape = CircleShape)
        .padding(horizontal = 16.dp, vertical = 8.dp)
        .onFocusChanged { isFocused = it.isFocused }

    Row(
        modifier = textFieldModifier,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            leadingIcon,
            contentDescription = null,
            tint = Color.Black,
            modifier = Modifier.size(12.dp)
        )
        Spacer(modifier = Modifier.width(8.dp))
        BasicTextField(
            value = value,
            onValueChange = onValueChange,
            textStyle = MaterialTheme.typography.bodySmall,
            visualTransformation = VisualTransformation.None,
            singleLine = true,
            modifier = Modifier
                .weight(0.9f)
                .padding(vertical = 4.dp)
                .onFocusChanged { isFocused = it.isFocused }
        )
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
