package com.ottrojja.screens.tasbeehScreen

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Replay
import androidx.compose.material.icons.filled.TouchApp
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ElevatedButton
import androidx.compose.material3.Icon
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
import com.ottrojja.composables.OttrojjaText
import com.ottrojja.composables.dialogs.OttrojjaAlertDialog
import com.ottrojja.ui.theme.OttrojjaTheme

@Composable
fun CounterContent(
    tasbeehCount: Int,
    increaseCount: () -> Unit,
    resetCount: () -> Unit
) {

    var alertTasbeehReset by remember { mutableStateOf(false) }

    if (alertTasbeehReset) {
        OttrojjaAlertDialog(
            onDismiss = { alertTasbeehReset = false },
            onConfirm = { resetCount(); alertTasbeehReset = false },
            text = "هل انت متأكد من إعادة العدد الى البداية؟",
            confirmText = "نعم"
        )
    }


    Row(
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .padding(0.dp, 18.dp, 0.dp, 24.dp)
    ) {
        Row(
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .padding(8.dp)
                .fillMaxWidth(0.8F)
                .border(
                    BorderStroke(4.dp, color = MaterialTheme.colorScheme.primary),
                    shape = RoundedCornerShape(12.dp)
                )
                .clip(RoundedCornerShape(12))
                .background(MaterialTheme.colorScheme.background, shape = RoundedCornerShape(12.dp))
                .padding(16.dp)

        ) {
            OttrojjaText(
                text = "${tasbeehCount}",
                style = OttrojjaTheme.typography.counter,
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.primary,
            )
        }
    }

    Row(
        horizontalArrangement = Arrangement.End,
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .padding(0.dp, 0.dp, 10.dp, 0.dp)
            .fillMaxWidth()
    ) {
        Button(
            onClick = { alertTasbeehReset = true },
            modifier = Modifier
                .size(50.dp),
            shape = CircleShape,
            contentPadding = PaddingValues(8.dp)
        ) {
            Icon(
                Icons.Default.Replay,
                contentDescription = "Reset",
                tint = MaterialTheme.colorScheme.onPrimary,
                modifier = Modifier.fillMaxSize()
            )
        }
    }

    Row(
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.fillMaxWidth()
    ) {
        ElevatedButton(
            onClick = { increaseCount() },
            elevation = ButtonDefaults.elevatedButtonElevation(2.dp, 2.dp, 2.dp, 2.dp, 2.dp),
            contentPadding = PaddingValues(0.dp),
            shape = CircleShape,
            modifier = Modifier
                .padding(0.dp, 2.dp, 0.dp, 12.dp)
                .fillMaxWidth(0.9F)
                .fillMaxHeight(0.8F)
                .padding(24.dp, 24.dp)
                .clip(CircleShape),
        ) {
            Icon(
                Icons.Default.TouchApp,
                contentDescription = "Counter",
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(128.dp)
            )
        }
    }
}
