package com.ottrojja.screens.Khitmah.KhitmahForm

import android.content.res.Configuration
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SelectableDates
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalConfiguration
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
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.text.font.FontWeight
import com.ottrojja.classes.FormValidationResult
import com.ottrojja.composables.forms.OttrojjaFormFieldContainer
import com.ottrojja.screens.Khitmah.FormValidation.validateKhitmah
import java.time.LocalDate
import java.time.temporal.ChronoUnit
import java.util.TimeZone
import kotlin.math.ceil

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun KhitmahForm(
    initialState: KhitmahFormState,
    onDismiss: () -> Unit,
    onSubmit: (KhitmahFormState) -> Unit,
) {
    var formState by remember(initialState) { mutableStateOf(initialState) }

    var formValidationResult by remember { mutableStateOf(FormValidationResult(true, emptyMap())) }

    var showDatePicker by remember { mutableStateOf(false) }

    var showTimePicker by remember { mutableStateOf(false) }

    fun calculateSuggestedPagesPerDay(): Int {
        if (formState.khitmah.targetDate.isNullOrBlank()) {
            return 0;
        }
        val pageCount = (604 - formState.khitmah.pagesRead).coerceAtLeast(0)
        val targetDate = LocalDate.parse(formState.khitmah.targetDate)
        val today = LocalDate.now()

        val remainingDays = ChronoUnit.DAYS
            .between(today, targetDate)
            .toInt() + 1

        return if (remainingDays > 0) {
            ceil(pageCount.toDouble() / remainingDays).toInt()
        } else {
            pageCount
        }
    }

    if (showDatePicker) {
        val initialSelectedDateMillis = formState.khitmah.targetDate
            ?.takeIf { it.isNotBlank() }
            ?.let { dateString ->
                LocalDate.parse(dateString)
                    .atStartOfDay(TimeZone.getTimeZone("UTC").toZoneId())
                    .toInstant()
                    .toEpochMilli()
            }

        val datePickerState = rememberDatePickerState(
            initialSelectedDateMillis = initialSelectedDateMillis,
            selectableDates = object : SelectableDates {
                override fun isSelectableDate(utcTimeMillis: Long): Boolean {
                    // Get today's start-of-day in UTC to match the picker's UTC millis
                    val todayUtcMillis = Calendar.getInstance(TimeZone.getTimeZone("UTC")).apply {
                        set(Calendar.HOUR_OF_DAY, 0)
                        set(Calendar.MINUTE, 0)
                        set(Calendar.SECOND, 0)
                        set(Calendar.MILLISECOND, 0)
                    }.timeInMillis

                    return utcTimeMillis >= todayUtcMillis
                }

                override fun isSelectableYear(year: Int): Boolean {
                    return year >= Calendar.getInstance().get(Calendar.YEAR)
                }
            }
        )

        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    datePickerState.selectedDateMillis?.let { millis ->
                        val calendar = Calendar.getInstance(TimeZone.getTimeZone("UTC"))
                            .apply {
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
                            khitmah = formState.khitmah.copy(targetDate = dateString)
                        )
                        showDatePicker = false
                    }
                }) { Text("تأكيد") }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) { Text("إلغاء") }
            }
        ) {
            DatePicker(state = datePickerState)
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
                    style = OttrojjaTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.primary
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
                    );
                    formValidationResult = validateKhitmah(formState.khitmah)
                },
                label = "عنوان الختمة",
                error = formValidationResult.errors.get("khitmahTitle")
            )

            OttrojjaSelect(
                label = "موعد هدف الانهاء (اختياري)",
                value = formState.khitmah.targetDate,
                onClick = {
                    showDatePicker = true
                },
                clearable = true,
                onClear = {
                    formState = formState.copy(
                        khitmah = formState.khitmah.copy(targetDate = null)
                    )
                }
            )

            AnimatedVisibility(
                visible = !formState.khitmah.targetDate.isNullOrBlank()
            ) {
                formState.khitmah.targetDate?.let {
                    val pagesPerDay = calculateSuggestedPagesPerDay()

                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 8.dp)
                            .clip(RoundedCornerShape(16.dp))
                            .background(
                                MaterialTheme.colorScheme.primaryContainer
                            )
                            .padding(
                                horizontal = 16.dp,
                                vertical = 14.dp
                            ),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {

                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.MenuBook,
                                tint = MaterialTheme.colorScheme.primary,
                                contentDescription = ""
                            )

                            OttrojjaText(
                                text = "معدل الصفحات اليومي",
                                style = OttrojjaTheme.typography.bodyMedium,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }

                        OttrojjaText(
                            text = " $pagesPerDay صفحة يومياً",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.padding(top = 6.dp)
                        )
                    }
                }
            }

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
                        formValidationResult = validateKhitmah(formState.khitmah)

                        if (formValidationResult.isValid) {
                            onSubmit(formState)
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth(0.6f)
                        .padding(vertical = 2.dp),
                    text = if (formState.mode == KhitmahFormMode.CREATE)
                        "إضافة"
                    else
                        "تعديل",
                    enabled = formValidationResult.isValid
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