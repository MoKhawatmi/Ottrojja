package com.ottrojja.screens.customTasabeehListScreen.dialogs

import android.widget.Toast
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Link
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.ottrojja.classes.ModalFormMode
import com.ottrojja.composables.buttons.OttrojjaButton
import com.ottrojja.composables.OttrojjaText
import com.ottrojja.composables.ToastManager.classes.AppToastManager
import com.ottrojja.composables.ToastManager.classes.AppToastType
import com.ottrojja.composables.dialogs.OttrojjaDialog
import com.ottrojja.composables.forms.OttrojjaForm
import com.ottrojja.composables.forms.OttrojjaFormField
import com.ottrojja.composables.forms.OttrojjaTextArea
import com.ottrojja.composables.forms.OttrojjaTextField
import com.ottrojja.composables.forms.TextFieldInputType
import com.ottrojja.room.entities.CustomTasbeeh
import com.ottrojja.ui.theme.OttrojjaTheme

@Composable
fun AddCustomTasbeehDialog(
    onDismiss: () -> Unit,
    onConfirm: () -> Unit,
    callImportTasbeehDialog: () -> Unit,
    tasbeehInWork: CustomTasbeeh,
    onTasbeehChange: (CustomTasbeeh) -> Unit,
    mode: ModalFormMode
) {
    val context = LocalContext.current

    OttrojjaDialog(
        contentModifier = Modifier
            .padding(8.dp)
            .wrapContentHeight()
            .fillMaxWidth(0.9f)
            .padding(8.dp)
            .clip(shape = RoundedCornerShape(12.dp)),
        onDismissRequest = { onDismiss() },
        useDefaultWidth = false,
        title = if (mode == ModalFormMode.ADD) "إضافة ذكر" else "تعديل الذكر"
    ) {

        OttrojjaForm(
            body = {
                Row(
                    modifier = Modifier
                        .padding(top = 8.dp)
                        .fillMaxWidth()
                        .border(2.dp,
                            MaterialTheme.colorScheme.primary,
                            RoundedCornerShape(16.dp)
                        )
                        .clickable { callImportTasbeehDialog() }
                        .padding(8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OttrojjaText(
                        text = "إستيراد من قائمة الاذكار",
                        style = OttrojjaTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Icon(Icons.Default.Link,
                        contentDescription = "import tasbeeh",
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OttrojjaFormField(label = "نص الذكر") {
                        OttrojjaTextArea(
                            value = tasbeehInWork.text,
                            onChange = { value -> onTasbeehChange(tasbeehInWork.copy(text = value)) },
                        )
                    }
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OttrojjaFormField(label = "مرات الذكر") {
                        OttrojjaTextField(
                            value = tasbeehInWork.count.toString(),
                            onChange = { value ->
                                onTasbeehChange(
                                    tasbeehInWork.copy(
                                        count = value.toIntOrNull() ?: 0,
                                        currentCount = value.toIntOrNull() ?: 0
                                    )
                                )
                            },
                            inputType = TextFieldInputType.NUMBER,
                            maxValue = 1_000_000
                        )
                    }
                }

            },
            footer = {
                OttrojjaButton(
                    modifier = Modifier.fillMaxWidth(0.6f),
                    text = if (mode == ModalFormMode.ADD) "إضافة" else "تعديل",
                    onClick = {
                        if (tasbeehInWork.text.isNotBlank() && tasbeehInWork.count.toString().isNotBlank()) {
                            onConfirm()
                        } else {
                            AppToastManager.show(
                                message = "يرجى التأكد من البيانات المدخلة",
                                type = AppToastType.ERROR
                            )
                        }
                    },
                )

                OttrojjaButton(
                    modifier = Modifier.fillMaxWidth(0.6f),
                    text = "إلغاء",
                    onClick = {
                        onDismiss()
                    },
                )

            }
        )
    }

}