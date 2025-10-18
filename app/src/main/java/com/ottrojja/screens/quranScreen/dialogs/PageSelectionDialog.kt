package com.ottrojja.screens.quranScreen.dialogs

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.ottrojja.classes.Helpers
import com.ottrojja.composables.OttrojjaDialog

@Composable
fun PageSelectionDialog(
    onDismissRequest: () -> Unit,
    pages: Array<String>,
    onSelect: (String) -> Unit
) {
    OttrojjaDialog(onDismissRequest = { onDismissRequest() }) {
        LazyColumn {
            items(items=pages) { item ->
                Row(modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onSelect(item) }
                    .padding(6.dp)) {
                    Text(
                        text = "ص${item}",
                        modifier = Modifier.fillMaxWidth(),
                        textAlign = TextAlign.Right,
                        color = MaterialTheme.colorScheme.onSecondary
                    )
                }
            }
        }
    }
}