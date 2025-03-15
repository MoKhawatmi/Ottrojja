package com.ottrojja.screens.tasbeehScreen

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.ottrojja.classes.Screen
import com.ottrojja.composables.ListHorizontalDivider
import com.ottrojja.room.entities.TasabeehList

@Composable
fun CustomTasabeehLists(navController: NavController,
                        customTasabeehLists: List<TasabeehList>,
                        onClickAdd: () -> Unit) {
    Box(modifier = Modifier
        .fillMaxWidth()
        .fillMaxHeight()
    ) {
        FloatingActionButton(
            onClick = { onClickAdd() },
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(8.dp)
        ) {
            Icon(Icons.Default.Add, contentDescription = "Add")
        }
        Column(modifier = Modifier.fillMaxSize()) {
            LazyColumn {
                if (customTasabeehLists.isEmpty()) {
                    item {
                        Row(
                            horizontalArrangement = Arrangement.Center,
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(20.dp)
                        ) {
                            Text(
                                text = "لا يوجد قوائم ذكر حاليا",
                                style = MaterialTheme.typography.bodyMedium,
                                textAlign = TextAlign.Center,
                                color = Color(0xFF9E9E9E)
                            )
                        }
                    }
                }

                items(customTasabeehLists, key = { it.id }) { item ->
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(MaterialTheme.colorScheme.background)
                            .clickable {
                                navController.navigate(Screen.CustomTasabeehListScreen.invokeRoute("${item.id}"))
                            }
                            .padding(8.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(6.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = item.title,
                                color = MaterialTheme.colorScheme.primary,
                                style = MaterialTheme.typography.bodyLarge,
                                textAlign = TextAlign.Right,
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    }
                    ListHorizontalDivider()
                }
            }
        }
    }
}