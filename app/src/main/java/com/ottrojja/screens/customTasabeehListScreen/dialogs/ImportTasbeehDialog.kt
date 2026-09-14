package com.ottrojja.screens.customTasabeehListScreen.dialogs

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.ottrojja.classes.Helpers
import com.ottrojja.classes.Tasabeeh
import com.ottrojja.composables.OttrojjaButton
import com.ottrojja.composables.OttrojjaText
import com.ottrojja.composables.dialogs.OttrojjaDialog
import com.ottrojja.composables.dialogs.OttrojjaItemSelectionDialog
import com.ottrojja.ui.theme.OttrojjaTheme

@Composable
fun ImportTasbeehDialog(
    onDismiss: () -> Unit,
    tasabeehList: List<Tasabeeh>,
    importTasbeeh: (Tasabeeh) -> Unit
) {

    OttrojjaItemSelectionDialog(
        title = "إستيراد ذكر",
        onDismiss = { onDismiss() },
        selectionItems = tasabeehList,
        onSelect = { item ->
            importTasbeeh(item); onDismiss()
        },
        emptyText = "لا يوجد أذكار حاليا",
        itemContent = { item ->
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center,
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { importTasbeeh(item); onDismiss() }
                    .padding(vertical = 8.dp)
            ) {
                OttrojjaText(
                    text = item.ziker,
                    color = MaterialTheme.colorScheme.primary,
                    style = OttrojjaTheme.typography.bodyMedium,
                    textAlign = TextAlign.Center,
                )
            }
        }
    )
}