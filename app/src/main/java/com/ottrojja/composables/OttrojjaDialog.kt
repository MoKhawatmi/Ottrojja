package com.ottrojja.composables

import android.annotation.SuppressLint
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties

@Composable
fun OttrojjaDialog(
    @SuppressLint("ModifierParameter") contentModifier: Modifier = Modifier
        .padding(8.dp)
        .fillMaxHeight(0.4f)
        .background(MaterialTheme.colorScheme.secondary)
        .padding(8.dp)
        .clip(shape = RoundedCornerShape(12.dp)),
    onDismissRequest: () -> Unit,
    useDefaultWidth: Boolean = true,
    content: @Composable() () -> Unit,
) {
    Dialog(
        onDismissRequest = { onDismissRequest() },
        properties = DialogProperties(usePlatformDefaultWidth = useDefaultWidth)
    ) {
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.secondary,
            ),
        ) {
            Box(
                modifier = contentModifier
            ) {
                content()
            }
        }
    }
}