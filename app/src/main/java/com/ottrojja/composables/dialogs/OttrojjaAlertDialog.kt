package com.ottrojja.composables.dialogs

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ModifierLocalBeyondBoundsLayout
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.ottrojja.composables.OttrojjaButton

@Composable
fun OttrojjaAlertDialog(
    onDismiss: () -> Unit,
    onConfirm: () -> Unit,
    title: String,
    text: String,
    content: (@Composable (() -> Unit))? = null
) {
    OttrojjaDialog(
        onDismissRequest = onDismiss,
        contentModifier = Modifier
            .padding(8.dp)
            .wrapContentHeight()
            .heightIn(max = LocalConfiguration.current.screenHeightDp.dp * 0.7f)
            .background(MaterialTheme.colorScheme.secondary)
            .padding(8.dp)
            .clip(RoundedCornerShape(12.dp))
    ) {

        val scrollState = rememberScrollState()

        Column(modifier = Modifier.padding(4.dp)) {
            Column(
                modifier = Modifier
                    .weight(1f, fill = false)
                    .verticalScroll(scrollState)
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSecondary,
                )

                Spacer(Modifier.height(8.dp))

                Text(
                    text = text,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSecondary
                )

                content?.invoke()
            }

            Spacer(Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                OttrojjaButton(text = "الغاء", onClick = onDismiss)
                Spacer(modifier = Modifier.width(10.dp))
                OttrojjaButton(text = "الموافقة", onClick = onConfirm)
            }
        }
    }
}