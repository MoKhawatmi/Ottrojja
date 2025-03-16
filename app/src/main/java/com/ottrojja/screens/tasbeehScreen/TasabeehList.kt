package com.ottrojja.screens.tasbeehScreen

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ottrojja.classes.ExpandableItem
import com.ottrojja.classes.Helpers
import com.ottrojja.classes.Tasabeeh
import com.ottrojja.composables.ListHorizontalDivider

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun TasabeehList(tasabeeh: MutableList<ExpandableItem<Tasabeeh>>,
                 updateExpanded: (ExpandableItem<Tasabeeh>) -> Unit) {
    val context = LocalContext.current;
    LazyColumn(
        modifier = Modifier
            .fillMaxWidth()
            .fillMaxHeight()
            .background(MaterialTheme.colorScheme.tertiary)
    ) {
        items(tasabeeh) { item ->
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.background)
                    .combinedClickable(onClick = { updateExpanded(item) },
                        onLongClick = {
                            Helpers.copyToClipboard(context, item.data.ziker, "تم النسخ بنجاح")
                        })
                    .padding(8.dp, 16.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Top
                ) {
                    Text(
                        text = item.data.ziker,
                        color = Color.Black,
                        style = MaterialTheme.typography.bodyMedium,
                        textAlign = TextAlign.Right,
                        modifier = Modifier.fillMaxWidth(0.9f),
                        lineHeight = 26.sp
                    )

                    Icon(
                        Icons.Default.MoreVert,
                        contentDescription = "expand tasbeeh",
                        modifier = Modifier.clickable {
                            updateExpanded(item)
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
                            .padding(4.dp, 8.dp)
                    ) {
                        Text(
                            text = item.data.benefit,
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color.Black,
                            textAlign = TextAlign.Right,
                            lineHeight = 26.sp
                        )
                    }
                }
            }
            ListHorizontalDivider()
        }
    }
}