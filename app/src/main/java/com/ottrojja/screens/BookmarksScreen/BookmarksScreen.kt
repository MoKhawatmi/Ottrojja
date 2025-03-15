package com.ottrojja.screens.BookmarksScreen

import android.app.Application
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Book
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.ottrojja.R
import com.ottrojja.classes.QuranRepository
import com.ottrojja.classes.Screen
import com.ottrojja.composables.Header
import com.ottrojja.composables.ListHorizontalDivider
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun BookmarksScreen(
    navController: NavController,
    repository: QuranRepository,
) {
    val context = LocalContext.current
    val application = context.applicationContext as Application

    val bookmarksViewModel: BookmarksViewModel = viewModel(
        factory = BookmarksViewModelFactory(repository, application)
    )

    LaunchedEffect(Unit) {
        bookmarksViewModel.getBookmarks()
    }

    Column(modifier = Modifier
        .fillMaxSize()
        .background(MaterialTheme.colorScheme.tertiary)
    )
    {
        Header()
        LazyColumn(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight()
        ) {
            if (bookmarksViewModel.bookmarks.isEmpty()) {
                item {
                    Row(
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(20.dp)
                    ) {
                        Text(
                            text = "لا يوجد اشارات مرجعية حاليا",
                            style = MaterialTheme.typography.bodyMedium,
                            textAlign = TextAlign.Center,
                            color = Color(0xFF9E9E9E)
                        )
                    }
                }
            }
            items(bookmarksViewModel.bookmarks, key = { "bookmark_page_${it.pageNum}" }) { item ->
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(MaterialTheme.colorScheme.background)
                        .clickable {
                            navController.navigate(Screen.QuranScreen.invokeRoute(item.pageNum))
                        }
                        .padding(8.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Default.Book,
                            contentDescription = "Bookmark",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.padding(horizontal = 4.dp)
                        )

                        Column(
                            verticalArrangement = Arrangement.Center,
                            horizontalAlignment = Alignment.Start,
                            modifier = Modifier.fillMaxWidth(0.9f)
                        ) {
                            Text(
                                text = "صفحة ${item.pageNum}",
                                color = Color.Black,
                                style = MaterialTheme.typography.bodyLarge,
                                textAlign = TextAlign.Right,
                                modifier = Modifier.fillMaxWidth()
                            )

                            Text(
                                text = SimpleDateFormat("HH:mm dd.MM.yyyy", Locale.US).format(
                                    Date(item.timeStamp)
                                ),
                                color = MaterialTheme.colorScheme.outlineVariant,
                                style = MaterialTheme.typography.bodySmall,
                                textAlign = TextAlign.Right,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 2.dp)
                            )
                        }


                        Image(
                            painter = painterResource(id = R.drawable.more_vert),
                            contentDescription = "",
                            modifier = Modifier.clickable {
                                bookmarksViewModel.updateExpanded(
                                    item
                                )
                            }
                        )
                    }
                    androidx.compose.animation.AnimatedVisibility(
                        visible = item.expanded,
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(MaterialTheme.colorScheme.background)
                                .padding(8.dp)
                        ) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Center,
                                modifier = Modifier
                                    .weight(1.0f)
                                    .background(MaterialTheme.colorScheme.primary)
                                    .clickable {
                                        bookmarksViewModel.removeBookmark(item)
                                    }
                                    .padding(4.dp, 6.dp)
                            ) {
                                Text(
                                    text = "حذف",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onPrimary
                                )
                            }
                        }
                    }
                }
                ListHorizontalDivider()
            }
        }
    }
}

