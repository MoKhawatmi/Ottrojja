package com.ottrojja.screens.blessingsScreen

import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.animation.core.tween
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.ottrojja.classes.Blessing
import com.ottrojja.composables.ListHorizontalDivider


@Composable
fun BlessingItem(item: Blessing, onShareClick: () -> Unit) {

    var expanded by rememberSaveable { mutableStateOf(false) }

    Box(modifier = Modifier
        .padding(bottom = 12.dp)
        .fillMaxWidth()
        .background(MaterialTheme.colorScheme.background)
        .animateContentSize(animationSpec = tween(
            durationMillis = 300,
            easing = LinearOutSlowInEasing,
        )
        )
        .then(
            if (expanded) Modifier.fillMaxHeight() else Modifier.height(250.dp)
        )
        .clickable { expanded = !expanded }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 4.dp)
        ) {
            Row(
                horizontalArrangement = Arrangement.End,
                modifier = Modifier
                    .fillMaxWidth()
            ) {
                Icon(
                    Icons.Default.ContentCopy,
                    contentDescription = "share blessing",
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier
                        .padding(top = 4.dp, end = 4.dp, start = 4.dp, bottom = 0.dp
                        )
                        .clickable { onShareClick() }
                )
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
            ) {
                Text(
                    text = item.text,
                    color = MaterialTheme.colorScheme.primary,
                    style = MaterialTheme.typography.bodyMedium,
                    textAlign = TextAlign.Right,
                    modifier = Modifier.padding(6.dp, 8.dp)
                )
            }
            ListHorizontalDivider()
        }

        if (!expanded) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center,
                modifier = Modifier
                    .align(alignment = Alignment.BottomCenter)
                    .fillMaxWidth()
                    .background(
                        MaterialTheme.colorScheme.tertiary.copy(alpha = 0.7f)
                    )
            ) {
                Text(text = "عرض المزيد",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.secondary,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(vertical = 6.dp)
                )
            }
        }
    }
}
