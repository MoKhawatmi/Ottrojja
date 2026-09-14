package com.ottrojja.screens.customTasabeehListScreen.dialogs

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.ottrojja.composables.OttrojjaButton
import com.ottrojja.composables.OttrojjaText
import com.ottrojja.composables.dialogs.OttrojjaDialog
import com.ottrojja.composables.forms.OttrojjaTextField
import com.ottrojja.ui.theme.OttrojjaTheme

@Composable
fun ExportListDialog(
    onDismiss: () -> Unit,
    onConfirm: () -> Unit,
    fileTitle: String,
    onFileTitleChanged: (String) -> Unit
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
    ) {

        Column(modifier = Modifier.wrapContentHeight()) {
            Row(modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center
            ) {
                OttrojjaText(
                    text = "تصدير الاذكار",
                    textAlign = TextAlign.Center,
                    style = OttrojjaTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.primary
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
                    value = fileTitle,
                    onChange = { newText: String ->
                        onFileTitleChanged(newText)
                    },
                    label = "اسم الملف",
                )
            }


            if (fileTitle.isNotEmpty()) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center
                ) {
                    val displayName = if (fileTitle.endsWith(".json")) fileTitle else "$fileTitle.json"

                    OttrojjaText(
                        text = "سيتم حفظ الملف باسم \"$displayName\" في مجلد Download الخاص بجهازك (وليس سحابيًا)، ويمكنك الوصول إليه من ملفات الجهاز",
                        style = OttrojjaTheme.typography.bodyMedium,
                        textAlign = TextAlign.Center,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(2.dp, alignment = Alignment.CenterVertically)
            ) {
                OttrojjaButton(
                    modifier = Modifier.fillMaxWidth(0.6f),
                    text = "تصدير",
                    onClick = {
                        if (fileTitle.isNotBlank()) {
                            onConfirm()
                        } else {
                            Toast.makeText(context, "يرجى التأكد من البيانات المدخلة", Toast.LENGTH_LONG).show()
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
        }
    }
}