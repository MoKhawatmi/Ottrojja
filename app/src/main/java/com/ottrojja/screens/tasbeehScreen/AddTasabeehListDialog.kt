package com.ottrojja.screens.tasbeehScreen

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
import com.ottrojja.composables.OttrojjaButton
import com.ottrojja.composables.OttrojjaText
import com.ottrojja.composables.dialogs.OttrojjaDialog
import com.ottrojja.composables.forms.OttrojjaTextField
import com.ottrojja.ui.theme.OttrojjaTheme

@Composable
fun AddTasabeehListDialog(onDismiss: () -> Unit, onConfirm: (String) -> Unit) {
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
        var listTitle by remember { mutableStateOf("") }

        Column(modifier = Modifier.wrapContentHeight()) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center) {
                OttrojjaText(
                    text = "إضافة قائمة ذكر",
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
                    value = listTitle,
                    onChange = { listTitle = it },
                    label = "عنوان القائمة"
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
                        if (listTitle.isNotBlank()) {
                            onConfirm(listTitle)
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth(0.6f)
                        .padding(vertical = 2.dp),
                    text = "إضافة"

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