package com.ottrojja.screens.reminderScreen.dialogs

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Link
import androidx.compose.material3.Button
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ottrojja.classes.Helpers
import com.ottrojja.classes.ModalFormMode
import com.ottrojja.composables.OttrojjaDialog
import com.ottrojja.composables.forms.OttrojjaSelect
import com.ottrojja.composables.forms.OttrojjaTextArea
import com.ottrojja.composables.forms.OttrojjaTextField
import com.ottrojja.room.entities.Reminder

@Composable
fun ReminderFormDialog(
    onDismiss: () -> Unit,
    onConfirm: () -> Unit,
    invokeRepetitionOptions: () -> Unit,
    invokeTimePicker: () -> Unit,
    reminderInWork: Reminder,
    onReminderChange: (Reminder) -> Unit,
    mode: ModalFormMode

) {
    val context = LocalContext.current

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
                    label = "عنوان المذكر"
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                OttrojjaTextArea(
                    value = "${reminderInWork.customMessage}",
                    onChange = { onReminderChange(reminderInWork.copy(customMessage = it)) },
                    label = "رسالة الإشعار"
                )
            }

            OttrojjaSelect(
                value = reminderInWork.repeatType.displayName,
                onClick = { invokeRepetitionOptions() },
            )

            OttrojjaSelect(
                value = "التوقيت: ${Helpers.formatMilitaryTime(reminderInWork.hour, reminderInWork.minute)}",
                onClick = { invokeTimePicker() },
            )

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Button(
                    onClick = {
                        if (validateReminder(reminderInWork)) {
                            onConfirm();
                            onDismiss();
                        } else {
                            Toast.makeText(context, "يرجى التأكد من البيانات المدخلة", Toast.LENGTH_LONG).show()
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth(0.6f)
                        .padding(vertical = 2.dp)
                ) {
                    Text(
                        text = if (mode == ModalFormMode.ADD) "إضافة" else "تعديل",
                        style = MaterialTheme.typography.bodyMedium,
                    )
                }

                Button(
                    onClick = { onDismiss() },
                    modifier = Modifier
                        .fillMaxWidth(0.6f)
                        .padding(vertical = 2.dp)
                ) {
                    Text(
                        text = "إلغاء",
                        style = MaterialTheme.typography.bodyMedium,
                    )
                }
            }

        }
    }
}

fun validateReminder(reminder: Reminder): Boolean {

    return true;
}