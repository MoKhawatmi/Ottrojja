package com.ottrojja.screens.mainScreen.Composables

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Circle
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.ottrojja.R
import com.ottrojja.classes.Helpers
import com.ottrojja.classes.Screen
import com.ottrojja.composables.ListHorizontalDivider
import com.ottrojja.composables.OttrojjaText
import com.ottrojja.room.entities.ChapterData
import com.ottrojja.ui.theme.OttrojjaTheme

@Composable
fun ChaptersMenu(
    items: List<ChapterData> = listOf<ChapterData>(),
    navController: NavController
) {
    val keyboardController = LocalSoftwareKeyboardController.current;

    var selectedSurahCard: ChapterData? by remember { mutableStateOf(null) }

    selectedSurahCard?.let { surah ->
        QuranSurahCard(
            onDismiss = {
                selectedSurahCard = null
            },
            surah = surah
        )
    }

    fun toggleSurahCard(surah: ChapterData) {
        selectedSurahCard = surah;
    }

    LazyColumn(
        Modifier
            .fillMaxHeight()
            .background(MaterialTheme.colorScheme.background)
    ) {
        items(items) { item ->
            Column(modifier = Modifier
                .padding(12.dp, 2.dp)
                .fillMaxWidth()
                .background(MaterialTheme.colorScheme.background)
                .combinedClickable(onClick = {
                    keyboardController!!.hide();
                    navController.navigate(Screen.QuranScreen.invokeRoute(item.chapterStartPage))
                }, onLongClick = {
                    keyboardController!!.hide();
                    toggleSurahCard(item)
                })

            ) {
                Row(
                    modifier = Modifier
                        .padding(12.dp)
                        .fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.Center) {
                        OttrojjaText(text = item.chapterName,
                            color = Color.Black,
                            style = OttrojjaTheme.typography.bodyLarge)
                        Spacer(modifier = Modifier.size(8.dp))
                        Icon(painter = painterResource(R.drawable.surah_cards),
                            tint = MaterialTheme.colorScheme.primary,
                            contentDescription = "${item.chapterName} Surah Card",
                            modifier = Modifier.clickable(onClick = { toggleSurahCard(item) })
                        )
                    }
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.End) {
                        OttrojjaText(
                            text = "${Helpers.convertToIndianNumbers("${item.verseCount}")} اية",
                            color = Color.Black,
                            style = OttrojjaTheme.typography.bodyMedium,
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
                        OttrojjaText(
                            text = item.chapterType,
                            color = Color.Black,
                            style = OttrojjaTheme.typography.bodyMedium
                        )
                    }
                }
                ListHorizontalDivider()
            }
        }
    }
}