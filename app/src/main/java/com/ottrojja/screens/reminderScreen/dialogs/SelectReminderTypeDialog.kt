package com.ottrojja.screens.reminderScreen.dialogs

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.ottrojja.classes.ReminderRepeatType
import com.ottrojja.composables.OttrojjaItemSelectionDialog

@Composable
fun SelectReminderTypeDialog(onDismiss: () -> Unit, onSelect: (ReminderRepeatType) -> Unit) {
    OttrojjaItemSelectionDialog(
        onDismiss = { onDismiss() },
        title = "تكرار المذكر",
        selectionItems = ReminderRepeatType.entries,
        onSelect = { item -> onSelect(item) },
        itemContent = { item ->
            Row(modifier = Modifier.fillMaxWidth().padding(8.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.Start) {
                Text(
                    text = item.displayName,
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSecondary
                )
            }
        }
    )
}