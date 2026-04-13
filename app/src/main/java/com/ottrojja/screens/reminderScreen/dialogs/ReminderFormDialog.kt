package com.ottrojja.screens.reminderScreen.dialogs

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.ottrojja.classes.FormValidationResult
import com.ottrojja.classes.Helpers
import com.ottrojja.classes.ModalFormMode
import com.ottrojja.composables.OttrojjaButton
import com.ottrojja.composables.dialogs.OttrojjaDialog
import com.ottrojja.composables.forms.OttrojjaSelect
import com.ottrojja.composables.forms.OttrojjaTextArea
import com.ottrojja.composables.forms.OttrojjaTextField
import com.ottrojja.room.entities.Reminder

@Composable
fun ReminderFormDialog(
    onDismiss: () -> Unit,
    formValidationResult: FormValidationResult,
    onSubmit: () -> Unit,
    invokeRepetitionOptions: () -> Unit,
    invokeTimePicker: () -> Unit,
    reminderInWork: Reminder,
    onReminderChange: (Reminder) -> Unit,
    mode: ModalFormMode,
) {
    OttrojjaDialog(
        contentModifier = Modifier
            .padding(8.dp)
            .wrapContentHeight()
            .fillMaxWidth(0.9f)
            .background(MaterialTheme.colorScheme.secondary)
            .padding(8.dp)
            .clip(shape = RoundedCornerShape(12.dp)),
        onDismissRequest = { onDismiss() },
        useDefaultWidth = false,
    ) {

        Column(modifier = Modifier.wrapContentHeight()) {
            Row(modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center
            ) {
                Text(text = if (mode == ModalFormMode.ADD) "إضافة مذكر" else "تعديل مذكر",
                    textAlign = TextAlign.Center
                )
            }
            HorizontalDivider(
                modifier = Modifier.padding(vertical = 6.dp),
                color = MaterialTheme.colorScheme.onTertiary
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                OttrojjaTextField(
                    value = reminderInWork.title,
                    onChange = { onReminderChange(reminderInWork.copy(title = it)) },
                    label = "عنوان المذكر",
                    disabled = reminderInWork.isMain,
                    maxLength = 30,
                    error = formValidationResult.errors["title"]
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                OttrojjaTextArea(
                    value = reminderInWork.customMessage,
                    onChange = { onReminderChange(reminderInWork.copy(customMessage = it)) },
                    label = "رسالة الإشعار",
                    disabled = reminderInWork.isMain,
                    maxLength = 150,
                    error = formValidationResult.errors["customMessage"]
                )
            }

            OttrojjaSelect(
                value = reminderInWork.repeatType.displayName,
                onClick = { invokeRepetitionOptions() },
                disabled = reminderInWork.isMain,
                error = formValidationResult.errors["repeatType"]
            )

            OttrojjaSelect(
                value = "التوقيت: ${Helpers.formatMilitaryTime(reminderInWork.hour, reminderInWork.minute)}",
                onClick = { invokeTimePicker() },
                error = formValidationResult.errors["time"]
            )

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                OttrojjaButton(
                    enabled = formValidationResult.isValid,
                    onClick = {
                        onSubmit()
                    },
                    modifier = Modifier
                        .fillMaxWidth(0.6f)
                        .padding(vertical = 2.dp),
                    text = if (mode == ModalFormMode.ADD) "إضافة" else "تعديل"
                )

                OttrojjaButton(
                    onClick = { onDismiss() },
                    modifier = Modifier
                        .fillMaxWidth(0.6f)
                        .padding(vertical = 2.dp),
                    text = "إلغاء"
                )
            }
        }
    }
}

