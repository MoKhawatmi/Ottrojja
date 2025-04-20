package com.ottrojja.composables

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.KeyboardArrowLeft
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ottrojja.classes.Helpers

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun ExpandableTextWithDetails(
    mainText: String,
    detailsText: String,
    expanded: Boolean,
    updateExpanded: () -> Unit) {
    val context = LocalContext.current;

    fun copyContent() {
        Helpers.copyToClipboard(context, mainText, "تم النسخ بنجاح")
    }

    val rotation by animateFloatAsState(
        targetValue = if (expanded) 90f else 0f,
        animationSpec = tween(durationMillis = 200)
    )


    Column(
        modifier = Modifier
            .padding(12.dp)
            .shadow(
                elevation = 2.dp,
                shape = RoundedCornerShape(16.dp),
                clip = false
            )
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(MaterialTheme.colorScheme.background)
            .combinedClickable(onClick = { updateExpanded() },
                onLongClick = {
                    copyContent()
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
                contentDescription = "copy supplication",
                modifier = Modifier
                    .padding(horizontal = 2.dp)
                    .clickable {
                        copyContent()
                    },
                tint = MaterialTheme.colorScheme.primary
            )
            Icon(
                Icons.Default.KeyboardArrowLeft,
                contentDescription = "expand supplication",
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
                text = mainText,
                color = MaterialTheme.colorScheme.primary,
                style = MaterialTheme.typography.bodyMedium.copy(fontSize = 20.sp),
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth(),
                lineHeight = 26.sp
            )
        }
        androidx.compose.animation.AnimatedVisibility(
            visible = expanded,
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.background)
                    .padding(4.dp, 8.dp)
            ) {
                Text(
                    text = detailsText,
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