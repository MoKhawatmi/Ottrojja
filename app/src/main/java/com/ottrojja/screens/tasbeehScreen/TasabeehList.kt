package com.ottrojja.screens.tasbeehScreen

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.KeyboardArrowLeft
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ottrojja.classes.ExpandableItem
import com.ottrojja.classes.Helpers
import com.ottrojja.classes.Tasabeeh
import com.ottrojja.composables.ListHorizontalDivider
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.runtime.remember
import androidx.compose.ui.draw.shadow


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
            TsabeehItem(item, { updateExpanded(item) })
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun TsabeehItem(item: ExpandableItem<Tasabeeh>,
                updateExpanded: () -> Unit) {
    val context = LocalContext.current;

    val rotation by animateFloatAsState(
        targetValue = if (item.expanded) 90f else 0f,
        animationSpec = tween(durationMillis = 200)
    )


    Column(
        modifier = Modifier
            .padding(12.dp)
            .shadow(
                elevation = 2.dp, // shadow size
                shape = RoundedCornerShape(16.dp), //gives soft edges
                clip = false // keep the shadow outside the bounds
            )
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(MaterialTheme.colorScheme.background)
            .combinedClickable(onClick = { updateExpanded() },
                onLongClick = {
                    Helpers.copyToClipboard(context, item.data.ziker, "تم النسخ بنجاح")
                })
            .padding(8.dp, 16.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.End,
            verticalAlignment = Alignment.Top
        ) {
            Icon(
                Icons.Default.ContentCopy,
                contentDescription = "copy tasbeeh",
                modifier = Modifier
                    .padding(horizontal = 2.dp)
                    .clickable {
                        Helpers.copyToClipboard(context, item.data.ziker, "تم النسخ بنجاح")
                    },
                tint = MaterialTheme.colorScheme.primary
            )
            Icon(
                Icons.Default.KeyboardArrowLeft,
                contentDescription = "expand tasbeeh",
                modifier = Modifier
                    .rotate(rotation)
                    .padding(horizontal = 2.dp)
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null,
                        onClick = { updateExpanded() }
                    ),
                tint = MaterialTheme.colorScheme.primary
            )
        }
        Row(
            modifier = Modifier
                .padding(top = 4.dp)
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.Top
        ) {
            Text(
                text = item.data.ziker,
                color = MaterialTheme.colorScheme.primary,
                style = MaterialTheme.typography.bodyMedium.copy(fontSize = 20.sp),
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth(),
                lineHeight = 26.sp
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