package com.ottrojja.screens.quranScreen.dialogs

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.ottrojja.classes.Helpers
import com.ottrojja.classes.Helpers.convertToArabicNumbers
import com.ottrojja.composables.OttrojjaText
import com.ottrojja.composables.PillShapedTextFieldWithIcon
import com.ottrojja.composables.dialogs.OttrojjaDialog
import com.ottrojja.composables.dialogs.OttrojjaItemSelectionDialog
import com.ottrojja.ui.theme.OttrojjaTheme

@Composable
fun RepetitionOptionsDialog(
    onDismissRequest: () -> Unit,
    onSelect: (String) -> Unit
) {

    OttrojjaItemSelectionDialog(
        title = "إختيار مرات التكرار",
        onDismiss = { onDismissRequest() },
        selectionItems = Helpers.repetitionOptionsMap.keys.toList(),
        onSelect = { item ->
            onSelect(item)
        },
        itemContent = { item ->
            Row(modifier = Modifier
                .fillMaxWidth()
                .clickable { onSelect(item) }
                .padding(6.dp)) {
                OttrojjaText(
                    text = item,
                    modifier = Modifier.fillMaxWidth(),
                    textAlign = TextAlign.Right,
                    color = MaterialTheme.colorScheme.primary,
                    style = OttrojjaTheme.typography.bodyLarge
                )
            }
        }
    )
}