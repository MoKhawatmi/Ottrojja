package com.ottrojja.screens.tasbeehScreen

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.ottrojja.classes.ExpandableItem
import com.ottrojja.classes.Tasabeeh
import com.ottrojja.composables.ExpandableTextWithDetails


@Composable
fun TasabeehList(tasabeeh: MutableList<ExpandableItem<Tasabeeh>>,
                 updateExpanded: (ExpandableItem<Tasabeeh>) -> Unit) {
    LazyColumn(
        modifier = Modifier
            .fillMaxWidth()
            .fillMaxHeight()
            .background(MaterialTheme.colorScheme.tertiary)
    ) {
        items(tasabeeh) { item ->
            ExpandableTextWithDetails(mainText = item.data.ziker,
                detailsText = item.data.benefit,
                expanded = item.expanded,
                updateExpanded = { updateExpanded(item) }
            )
        }
    }
}
