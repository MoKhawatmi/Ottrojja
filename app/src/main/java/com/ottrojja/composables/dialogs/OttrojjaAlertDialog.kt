package com.ottrojja.composables.dialogs

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.unit.dp
import com.ottrojja.composables.buttons.OttrojjaButton
import com.ottrojja.composables.OttrojjaText
import com.ottrojja.composables.buttons.OttrojjaTextButton
import com.ottrojja.ui.theme.OttrojjaTheme

@Composable
fun OttrojjaAlertDialog(
    onDismiss: () -> Unit,
    onConfirm: () -> Unit,
    title: String = "",
    text: String,
    confirmText: String = "الموافقة",
    cancelText: String = "الغاء",
    content: (@Composable (() -> Unit))? = null
) {
    OttrojjaDialog(
        onDismissRequest = onDismiss,
        contentModifier = Modifier
            .padding(8.dp)
            .wrapContentHeight()
            .heightIn(max = LocalConfiguration.current.screenHeightDp.dp * 0.7f)
            .fillMaxWidth(0.9f)
            .padding(8.dp)
            .clip(RoundedCornerShape(12.dp)),
        useDefaultWidth = false
    ) {
        Column(modifier = Modifier.padding(4.dp)) {
            Column {
                if (title.isNotBlank()) {
                    OttrojjaText(
                        text = title,
                        style = OttrojjaTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.primary,
                    )
                    Spacer(Modifier.height(16.dp))
                }

                OttrojjaText(
                    text = text,
                    style = OttrojjaTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.primary
                )

                content?.invoke()
            }

            Spacer(Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                OttrojjaTextButton(text = cancelText, onClick = onDismiss)
                Spacer(modifier = Modifier.width(10.dp))
                OttrojjaTextButton(text = confirmText, onClick = onConfirm)
            }
        }
    }
}