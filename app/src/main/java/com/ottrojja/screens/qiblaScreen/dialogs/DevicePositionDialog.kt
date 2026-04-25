package com.ottrojja.screens.qiblaScreen.dialogs

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ottrojja.R
import com.ottrojja.composables.dialogs.OttrojjaDialog

@Composable
fun DevicePositionDialog(onDismiss: () -> Unit) {
    OttrojjaDialog(contentModifier = Modifier
        .padding(8.dp)
        .wrapContentHeight()
        .clip(shape = RoundedCornerShape(12.dp)), onDismissRequest = { onDismiss() }) {
        Column(verticalArrangement = Arrangement.SpaceBetween,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Column(modifier = Modifier
                .verticalScroll(rememberScrollState())
            ) {
                Row(modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                    horizontalArrangement = Arrangement.Center
                ) {
                    Text("يرجى إبقاء الجهاز مسطحا لتحديد اتجاه القبلة بدقة",
                        style = MaterialTheme.typography.bodyLarge,
                        textAlign = TextAlign.Center,
                        color = MaterialTheme.colorScheme.onSecondary
                    )
                }
                Image(painter = painterResource(R.drawable.flat_device),
                    contentDescription = "keep device flat",
                    modifier = Modifier.fillMaxWidth()
                )
            }
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 4.dp),
                horizontalArrangement = Arrangement.End,
            ) {
                Button(onClick = { onDismiss() }) {
                    Text(
                        "إغلاق",
                        style = MaterialTheme.typography.bodyMedium,
                        fontSize = 20.sp,
                        textAlign = TextAlign.End
                    )
                }
            }
        }
    }
}