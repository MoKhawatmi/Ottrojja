package com.ottrojja.screens.reminderScreen.dialogs

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.Repeat
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import com.ottrojja.classes.FormValidationResult
import com.ottrojja.classes.Helpers
import com.ottrojja.classes.ModalFormMode
import com.ottrojja.composables.buttons.OttrojjaButton
import com.ottrojja.composables.dialogs.OttrojjaDialog
import com.ottrojja.composables.forms.OttrojjaForm
import com.ottrojja.composables.forms.OttrojjaFormField
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
    val messageMaxLength = 150;

    OttrojjaDialog(
        contentModifier = Modifier
            .padding(8.dp)
            .wrapContentHeight()
            .fillMaxWidth(0.9f)
            .padding(8.dp)
            .clip(shape = RoundedCornerShape(12.dp)),
        onDismissRequest = { onDismiss() },
        useDefaultWidth = false,
        title = if (mode == ModalFormMode.ADD) "إضافة مذكر" else "تعديل مذكر"
    ) {

        OttrojjaForm(
            body = {
                OttrojjaFormField(
                    label = "عنوان المذكر",
                    error = formValidationResult.errors["title"]
                ) {
                    OttrojjaTextField(
                        value = reminderInWork.title,
                        onChange = { onReminderChange(reminderInWork.copy(title = it)) },
                        enabled = !reminderInWork.isMain,
                        maxLength = 30,
                    )
                }

                OttrojjaFormField(
                    label = "رسالة الإشعار",
                    error = formValidationResult.errors["customMessage"],
                    supportingText = reminderInWork.customMessage
                        ?.takeIf { it.isNotEmpty() }
                        ?.let { "${it.length} / $messageMaxLength" }
                ) {
                    OttrojjaTextArea(
                        value = reminderInWork.customMessage ?: "",
                        onChange = { onReminderChange(reminderInWork.copy(customMessage = it)) },
                        maxLength = messageMaxLength,
                        enabled = !reminderInWork.isMain,
                    )
                }

                OttrojjaFormField(
                    label = "نوع التكرار",
                    error = formValidationResult.errors["repeatType"]
                ) {
                    OttrojjaSelect(
                        value = reminderInWork.repeatType.displayName,
                        onClick = { invokeRepetitionOptions() },
                        enabled = !reminderInWork.isMain,
                        leadingIcon = Icons.Default.Repeat
                    )
                }

                OttrojjaFormField(
                    label = "التوقيت",
                    error = formValidationResult.errors["time"]
                ) {
                    OttrojjaSelect(
                        value = "${Helpers.formatMilitaryTime(reminderInWork.hour, reminderInWork.minute)}",
                        onClick = { invokeTimePicker() },
                        leadingIcon = Icons.Default.AccessTime
                    )
                }
            },
            footer = {
                OttrojjaButton(
                    enabled = formValidationResult.isValid,
                    onClick = {
                        onSubmit()
                    },
                    modifier = Modifier
                        .fillMaxWidth(0.6f),
                    text = if (mode == ModalFormMode.ADD) "إضافة" else "تعديل"
                )

                OttrojjaButton(
                    onClick = { onDismiss() },
                    modifier = Modifier
                        .fillMaxWidth(0.6f),
                    text = "إلغاء"
                )
            }
        )
    }
}

