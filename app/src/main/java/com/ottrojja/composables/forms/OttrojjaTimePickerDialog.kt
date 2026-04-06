package com.ottrojja.composables.forms

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.TimeInput
import androidx.compose.material3.TimePickerColors
import androidx.compose.material3.TimePickerDefaults
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.ottrojja.composables.OttrojjaButton
import com.ottrojja.composables.OttrojjaDialog

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OttrojjaTimePickerDialog(
    onDismiss: () -> Unit,
    hour: Int,
    minute: Int,
    onConfirm: (Int, Int) -> Unit,
) {

    val timePickerState = rememberTimePickerState(
        initialHour = hour,
        initialMinute = minute,
        is24Hour = true,
    )

    OttrojjaDialog(onDismissRequest = { onDismiss() }) {
        Column(modifier = Modifier
            .fillMaxSize()
            .padding(start = 2.dp, end = 2.dp, top = 12.dp, bottom = 2.dp), verticalArrangement = Arrangement.SpaceBetween
        ) {
            Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.Center) {
                TimeInput(
                    state = timePickerState,
                    colors = TimePickerDefaults.colors(
                        containerColor = MaterialTheme.colorScheme.secondary,
                        timeSelectorSelectedContainerColor = MaterialTheme.colorScheme.secondary,
                        timeSelectorUnselectedContainerColor = MaterialTheme.colorScheme.secondary,
                        timeSelectorSelectedContentColor = MaterialTheme.colorScheme.onSecondary,
                        timeSelectorUnselectedContentColor = MaterialTheme.colorScheme.onSecondary
                    )
                )
            }

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                OttrojjaButton("تأكيد", onClick = { onConfirm(timePickerState.hour, timePickerState.minute); onDismiss() })
                Spacer(modifier = Modifier.size(8.dp))
                OttrojjaButton("الغاء", onClick = { onDismiss() })
            }
        }
    }
}