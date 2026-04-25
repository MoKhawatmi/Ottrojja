package com.ottrojja.screens.reminderScreen.dialogs

import androidx.compose.foundation.Image
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
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.ottrojja.R
import com.ottrojja.composables.OttrojjaButton
import com.ottrojja.composables.dialogs.OttrojjaDialog

@Composable
fun FloatingAzkarDialog(onDismiss: () -> Unit) {
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

        Column(modifier = Modifier
            .wrapContentHeight()
            .padding(4.dp)) {
            Row(modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center
            ) {
                Text(text = "اذكار الشاشة",
                    textAlign = TextAlign.Center
                )
            }
            HorizontalDivider(
                modifier = Modifier.padding(vertical = 6.dp),
                color = MaterialTheme.colorScheme.onTertiary
            )

            Row(modifier = Modifier
                .fillMaxWidth(), horizontalArrangement = Arrangement.Start, verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "تظهر الأذكار العائمة على شاشة جهازك بشكل خفيف وغير مزعج كل 30 دقيقة، لتذكيرك بذكر الله في خضم انشغالك اليومي. تساعدك هذه الرسائل القصيرة على الحفاظ على صلتك بالله وتجديد النية والطمأنينة أينما كنت.",
                    style = MaterialTheme.typography.bodyMedium,
                    textAlign = TextAlign.Start,
                    color = MaterialTheme.colorScheme.onSecondary
                )
            }

            Row(modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp), horizontalArrangement = Arrangement.Center, verticalAlignment = Alignment.CenterVertically
            ) {
                Image(
                    painter = painterResource(id = R.drawable.floating_zikr_example),
                    contentDescription = "Floating Azkar",
                    modifier = Modifier,
                    contentScale = ContentScale.Fit,
                    alignment = Alignment.Center
                )
            }
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                OttrojjaButton(text = "إغلاق", onClick = { onDismiss() })
            }
        }
    }
}