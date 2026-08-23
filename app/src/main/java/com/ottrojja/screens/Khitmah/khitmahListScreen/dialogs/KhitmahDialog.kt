package com.ottrojja.screens.Khitmah.khitmahListScreen.dialogs

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
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
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
import com.ottrojja.composables.forms.SwitchRow
import com.ottrojja.room.entities.Khitmah
import com.ottrojja.screens.Khitmah.KhitmahForm.KhitmahFormMode
import com.ottrojja.screens.Khitmah.KhitmahForm.KhitmahFormState
import com.ottrojja.ui.theme.OttrojjaTheme

@Composable
fun KhitmahDialog(
    onDismiss: () -> Unit,
    onConfirm: () -> Unit,
    formState: KhitmahFormState,
    onKhitmahChange: (Khitmah) -> Unit,
    invokeTargetDatePicker: () -> Unit,
    invokeReminderTimePicker: () -> Unit,
    toggleIncludeReminder: () -> Unit,
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

        Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center) {
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

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                OttrojjaTextField(
                    value = formState.khitmah.title,
                    onChange = {
                        onKhitmahChange(
                            formState.khitmah.copy(title = it)
                        )
                    },
                    label = "عنوان الختمة"
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                OttrojjaSelect(
                    label = "موعد هدف الانهاء (اختياري)",
                    value = formState.khitmah.targetDate,
                    onClick = invokeTargetDatePicker,
                    error = ""
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                SwitchRow(
                    label = "إنشاء مذكر للختمة",
                    checked = formState.reminderEnabled,
                    onCheckedChange = {
                        toggleIncludeReminder()
                    }
                )
            }

            AnimatedVisibility(
                visible = formState.reminderEnabled
            ) {
                OttrojjaSelect(
                    label = "توقيت المذكر: ",
                    value = formState.reminderTime.split(":").let {
                        Helpers.formatMilitaryTime(
                            it[0].toInt(),
                            it[1].toInt()
                        )
                    },
                    onClick = invokeReminderTimePicker,
                )
            }

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                OttrojjaButton(
                    onClick = {
                        onConfirm()
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