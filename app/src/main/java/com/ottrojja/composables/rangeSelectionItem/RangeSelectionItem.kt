package com.ottrojja.composables.rangeSelectionItem

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.ottrojja.composables.OttrojjaText
import com.ottrojja.ui.theme.OttrojjaTheme


@Composable
fun RangeSelectionItem(segments: List<RangeSelectionSegment>) {
    Column(modifier = Modifier
        .padding(12.dp)
        .shadow(
            elevation = 2.dp,
            shape = RoundedCornerShape(16.dp),
            clip = false
        )
        .clip(RoundedCornerShape(16.dp))
        .background(MaterialTheme.colorScheme.background)
        .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        segments.forEach {
            RangeSelectionTitle(it.title)
            RangeSelectionClickable(
                text = it.value,
                onClick = { it.onClick()  }
            )
        }
    }
}

@Composable
fun RangeSelectionClickable(text: String, onClick: () -> Unit) {
    OttrojjaText(
        text,
        modifier = Modifier
            .padding(vertical = 12.dp)
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.primary,
                shape = RoundedCornerShape(12.dp)
            )
            .border(2.dp, color = MaterialTheme.colorScheme.secondary,
                shape = RoundedCornerShape(12.dp)
            )
            .padding(horizontal = 8.dp, vertical = 16.dp)
            .clickable { onClick() },
        color = MaterialTheme.colorScheme.onPrimary,
        style = OttrojjaTheme.typography.bodyMedium,
        textAlign = TextAlign.Center,
    )
}

@Composable
fun RangeSelectionTitle(title: String) {
    OttrojjaText(
        title,
        color = MaterialTheme.colorScheme.primary,
        style = OttrojjaTheme.typography.bodyMedium
    )
}