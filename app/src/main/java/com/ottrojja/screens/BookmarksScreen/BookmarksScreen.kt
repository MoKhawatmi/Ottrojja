package com.ottrojja.screens.BookmarksScreen

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.ottrojja.R
import com.ottrojja.classes.Screen
import com.ottrojja.composables.Header

@Composable
fun BookmarksScreen(
    navController: NavController,
    modifier: Modifier = Modifier,
    bookmarksViewModel: BookmarksViewModel = viewModel()
) {
    bookmarksViewModel.getBookmarks();
    Column {
        Header()
        LazyColumn(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.9f)
                .background(MaterialTheme.colorScheme.tertiary)
        ) {
            if (bookmarksViewModel.bookmarks.size <= 0) {
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
            items(bookmarksViewModel.bookmarks) { item ->
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
                        Text(
                            text = "صفحة ${item.pageNum}",
                            color = Color.Black,
                            style = MaterialTheme.typography.bodyLarge,
                            textAlign = TextAlign.Right,
                            modifier = Modifier.fillMaxWidth(0.9f)
                        )

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
                                        bookmarksViewModel.removeBookmark(item.pageNum)
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

