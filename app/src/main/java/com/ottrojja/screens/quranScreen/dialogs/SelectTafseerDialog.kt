package com.ottrojja.screens.quranScreen.dialogs

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.ottrojja.composables.OttrojjaText
import com.ottrojja.composables.dialogs.OttrojjaDialog
import com.ottrojja.composables.dialogs.OttrojjaItemSelectionDialog
import com.ottrojja.room.entities.PageContentItemType
import com.ottrojja.ui.theme.OttrojjaTheme

@Composable
fun SelectTafseerDialog(
    onDismissRequest: () -> Unit,
    onOptionClick: (String) -> Unit,
    tafseerMap: HashMap<String, String>
) {

    OttrojjaItemSelectionDialog(
        title = "إختيار التفسير",
        onDismiss = { onDismissRequest() },
        selectionItems = tafseerMap.keys.toList(),
        onSelect = { item ->
            onOptionClick(item)
        },
        itemContent = { item ->
            Row(modifier = Modifier
                .fillMaxWidth()
                .padding(6.dp)
            ) {
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