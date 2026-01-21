package com.ottrojja.composables

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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.ottrojja.R
import com.ottrojja.screens.listeningScreen.ListeningViewModel


@Composable
fun <T> OttrojjaItemSelectionDialog(
    title: String? = null,
    onDismiss: () -> Unit,
    searchBar: (@Composable () -> Unit)? = null,
    selectionItems: List<T>,
    onSelect: (T) -> Unit,
    itemContent: @Composable (T) -> Unit
) {

    OttrojjaDialog(onDismissRequest = { onDismiss() },
        contentModifier = Modifier
            .padding(8.dp)
            .fillMaxHeight(0.75f)
            .fillMaxWidth(0.9f)
            .background(MaterialTheme.colorScheme.secondary)
            .padding(8.dp)
            .clip(shape = RoundedCornerShape(12.dp)),
        useDefaultWidth = false
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            if (title != null) {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center) {
                    Text(text = "$title", textAlign = TextAlign.Center)
                }
                HorizontalDivider(
                    modifier = Modifier.padding(vertical = 6.dp),
                    color = MaterialTheme.colorScheme.onTertiary
                )
            }

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
                Modifier
                    .fillMaxHeight()
            ) {
                items(selectionItems) { item ->

                    Column(modifier = Modifier
                        .padding(2.dp)
                        .fillMaxWidth()
                        .clickable {
                            onSelect(item)
                            onDismiss()
                        }
                    ) {
                        itemContent(item)
                    }

                    HorizontalDivider(color = MaterialTheme.colorScheme.onSecondary)

                }
            }
        }
    }
}