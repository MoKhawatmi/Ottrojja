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
import com.ottrojja.composables.OttrojjaButton
import com.ottrojja.composables.OttrojjaText
import com.ottrojja.composables.dialogs.OttrojjaDialog
import com.ottrojja.ui.theme.OttrojjaTheme

@Composable
fun CalibrationDialog(onDismiss: () -> Unit) {
    OttrojjaDialog(contentModifier = Modifier
        .padding(8.dp)
        .wrapContentHeight()
        .clip(shape = RoundedCornerShape(12.dp)),
        onDismissRequest = { onDismiss() }) {
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
                    OttrojjaText("معايرة البوصلة",
                        style = OttrojjaTheme.typography.bodyLarge,
                        textAlign = TextAlign.Center,
                        color = MaterialTheme.colorScheme.onSecondary
                    )
                }

                Image(painter = painterResource(R.drawable.calibration),
                    contentDescription = "calibration",
                    modifier = Modifier.fillMaxWidth()
                )

                OttrojjaText("لمعايرة بوصلة الجهاز لتحديد اتجاه القبلة بدقة يرجى:",
                    style = OttrojjaTheme.typography.bodyMedium,
                    textAlign = TextAlign.Start, color = MaterialTheme.colorScheme.onSecondary
                )
                OttrojjaText("١. أمساك الجهاز بشكل مسطح", style = OttrojjaTheme.typography.bodyMedium,
                    textAlign = TextAlign.Start, color = MaterialTheme.colorScheme.onSecondary
                )
                OttrojjaText("٢. تحريكه في نمط بشكل رقم 8", style = OttrojjaTheme.typography.bodyMedium,
                    textAlign = TextAlign.Start, color = MaterialTheme.colorScheme.onSecondary
                )
                OttrojjaText(
                    "٣. تجنب التداخل المغناطيسي وأبعد الجهاز عن أي أجهزة كهربائية أو معادن أو مغناطيسات",
                    style = OttrojjaTheme.typography.bodyMedium,
                    textAlign = TextAlign.Start, color = MaterialTheme.colorScheme.onSecondary
                )
            }
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 4.dp),
                horizontalArrangement = Arrangement.End,
            ) {
                OttrojjaButton(
                    onClick = { onDismiss() },
                    text = "إغلاق"
                )
            }
        }
    }
}