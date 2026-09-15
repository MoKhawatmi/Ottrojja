package com.ottrojja.composables.dialogs

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.ottrojja.composables.OttrojjaText
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.runtime.LaunchedEffect
import com.ottrojja.ui.theme.OttrojjaTheme

@Composable
fun <T> OttrojjaItemSelectionDialog(
    title: String? = null,
    onDismiss: () -> Unit,
    searchBar: (@Composable () -> Unit)? = null,
    selectionItems: List<T>,
    onSelect: (T) -> Unit,
    itemContent: @Composable (T) -> Unit,
    emptyText: String = "لا يوجد خيارات حاليا",
    showDivider: Boolean = true,
    listState: LazyListState = rememberLazyListState(),
    initialScrollIndex: Int? = null
) {

    LaunchedEffect(initialScrollIndex, selectionItems) {
        if (
            initialScrollIndex != null &&
            selectionItems.isNotEmpty()
        ) {
            listState.scrollToItem(
                initialScrollIndex.coerceIn(0, selectionItems.lastIndex)
            )
        }
    }


    OttrojjaDialog(onDismissRequest = { onDismiss() },
        contentModifier = Modifier
            .padding(8.dp)
            .fillMaxHeight(0.75f)
            .fillMaxWidth(0.9f)
            .padding(8.dp)
            .clip(shape = RoundedCornerShape(16.dp)),
        useDefaultWidth = false
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            if (title != null) {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center) {
                    OttrojjaText(
                        text = "$title",
                        textAlign = TextAlign.Center,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
                HorizontalDivider(
                    modifier = Modifier.padding(vertical = 6.dp),
                    color = MaterialTheme.colorScheme.primary
                )
            }


            if (selectionItems.isEmpty()) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                )
                {
                    OttrojjaText(
                        text = emptyText,
                        color = MaterialTheme.colorScheme.primary,
                        textAlign = TextAlign.Center,
                        style = OttrojjaTheme.typography.bodyMedium
                    )
                }
            } else {
                Row(
                    modifier = Modifier
                        .padding(10.dp)
                        .fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceAround,
                    verticalAlignment = Alignment.Bottom
                )
                {
                    searchBar?.invoke()
                }

                LazyColumn(
                    Modifier.fillMaxHeight(),
                    state = listState,
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    itemsIndexed(selectionItems) { index, item ->

                        Column(
                            modifier = Modifier
                                .padding(2.dp)
                                .fillMaxWidth()
                                .clickable {
                                    onSelect(item)
                                    onDismiss()
                                }
                        ) {
                            itemContent(item)
                        }

                        if (index < selectionItems.lastIndex && showDivider) {
                            HorizontalDivider(
                                color = MaterialTheme.colorScheme.primary.copy(alpha = 0.3f)
                            )
                        }
                    }
                }
            }
        }
    }
}