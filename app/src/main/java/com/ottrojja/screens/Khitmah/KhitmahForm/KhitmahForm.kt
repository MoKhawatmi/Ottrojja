package com.ottrojja.screens.Khitmah.KhitmahForm

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.ottrojja.classes.Helpers
import com.ottrojja.composables.OttrojjaButton
import com.ottrojja.composables.OttrojjaText
import com.ottrojja.composables.dialogs.OttrojjaDialog
import com.ottrojja.composables.forms.OttrojjaSelect
import com.ottrojja.composables.forms.OttrojjaTextField
import com.ottrojja.composables.forms.OttrojjaTimePickerDialog
import com.ottrojja.composables.forms.SwitchRow
import com.ottrojja.ui.theme.OttrojjaTheme
import java.util.Calendar
import java.util.Locale

@Composable
fun KhitmahForm(
    initialState: KhitmahFormState,
    onDismiss: () -> Unit,
    onSubmit: (KhitmahFormState) -> Unit
) {
    var formState by remember(initialState) {
        mutableStateOf(initialState)
    }

    var showDatePicker by remember {
        mutableStateOf(false)
    }

    var showTimePicker by remember {
        mutableStateOf(false)
    }

    if (showDatePicker) {
        val datePickerState = rememberDatePickerState()

        DatePickerDialog(
            onDismissRequest = {
                showDatePicker = false
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        datePickerState.selectedDateMillis?.let { millis ->

                            val calendar = Calendar.getInstance().apply {
                                timeInMillis = millis
                            }

                            val dateString = String.format(
                                Locale.US,
                                "%04d-%02d-%02d",
                                calendar.get(Calendar.YEAR),
                                calendar.get(Calendar.MONTH) + 1,
                                calendar.get(Calendar.DAY_OF_MONTH)
                            )

                            formState = formState.copy(
                                khitmah = formState.khitmah.copy(
                                    targetDate = dateString
                                )
                            )

                            showDatePicker = false
                        }
                    }
                ) {
                    Text("تأكيد")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        showDatePicker = false
                    }
                ) {
                    Text("إلغاء")
                }
            }
        ) {
            DatePicker(
                state = datePickerState
            )
        }
    }

    if (showTimePicker) {
        OttrojjaTimePickerDialog(
            onDismiss = {
                showTimePicker = false
            },
            hour = formState.reminderTime
                .split(":")[0]
                .toInt(),
            minute = formState.reminderTime
                .split(":")[1]
                .toInt(),
            label = "توقيت مذكر الختمة",
            onConfirm = { hour, minute ->
                formState = formState.copy(
                    reminderTime = "%02d:%02d".format(hour, minute)
                )

                showTimePicker = false
            }
        )
    }

    OttrojjaDialog(
        contentModifier = Modifier
            .padding(8.dp)
            .wrapContentHeight()
            .fillMaxWidth(0.9f)
            .background(MaterialTheme.colorScheme.secondary)
            .padding(8.dp)
            .clip(RoundedCornerShape(12.dp)),
        onDismissRequest = onDismiss,
        useDefaultWidth = false,
    ) {
        Column(
            modifier = Modifier.verticalScroll(
                rememberScrollState()
            )
        ) {

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center
            ) {
                OttrojjaText(
                    text = if (formState.mode == KhitmahFormMode.CREATE)
                        "إضافة ختمة"
                    else
                        "تعديل الختمة",
                    textAlign = TextAlign.Center,
                    style = OttrojjaTheme.typography.bodyLarge
                )
            }

            HorizontalDivider(
                modifier = Modifier.padding(vertical = 6.dp),
                color = MaterialTheme.colorScheme.onTertiary
            )

            OttrojjaTextField(
                value = formState.khitmah.title,
                onChange = { value ->
                    formState = formState.copy(
                        khitmah = formState.khitmah.copy(
                            title = value
                        )
                    )
                },
                label = "عنوان الختمة"
            )

            OttrojjaSelect(
                label = "موعد هدف الانهاء (اختياري)",
                value = formState.khitmah.targetDate,
                onClick = {
                    showDatePicker = true
                },
                error = ""
            )

            SwitchRow(
                label = "إنشاء مذكر للختمة",
                checked = formState.reminderEnabled,
                onCheckedChange = { enabled ->
                    formState = formState.copy(
                        reminderEnabled = enabled
                    )
                }
            )

            AnimatedVisibility(
                visible = formState.reminderEnabled
            ) {
                OttrojjaSelect(
                    label = "توقيت المذكر",
                    value = formState.reminderTime.split(":").let {
                        Helpers.formatMilitaryTime(
                            it[0].toInt(),
                            it[1].toInt()
                        )
                    },
                    onClick = {
                        showTimePicker = true
                    }
                )
            }

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {

                OttrojjaButton(
                    onClick = {
                        onSubmit(formState)
                    },
                    modifier = Modifier
                        .fillMaxWidth(0.6f)
                        .padding(vertical = 2.dp),
                    text = if (formState.mode == KhitmahFormMode.CREATE)
                        "إضافة"
                    else
                        "تعديل"
                )

                OttrojjaButton(
                    onClick = onDismiss,
                    modifier = Modifier
                        .fillMaxWidth(0.6f)
                        .padding(vertical = 2.dp),
                    text = "إلغاء"
                )
            }
        }
    }
}