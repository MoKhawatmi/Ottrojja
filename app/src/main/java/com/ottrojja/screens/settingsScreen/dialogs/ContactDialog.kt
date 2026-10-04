package com.ottrojja.screens.settingsScreen.dialogs

import android.content.Context
import android.content.Intent
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDirection
import androidx.compose.ui.unit.dp
import androidx.core.net.toUri
import com.ottrojja.classes.Helpers
import com.ottrojja.composables.buttons.OttrojjaButton
import com.ottrojja.composables.OttrojjaText
import com.ottrojja.composables.dialogs.OttrojjaDialog
import com.ottrojja.screens.settingsScreen.sendMail
import com.ottrojja.ui.theme.OttrojjaTheme

@Composable
fun ContactDialog(
    onDismiss: () -> Unit,
    context: Context,
) {
    OttrojjaDialog(contentModifier = Modifier
        .padding(8.dp)
        .wrapContentHeight()
        .padding(8.dp)
        .clip(shape = RoundedCornerShape(12.dp)),
        onDismissRequest = { onDismiss() }) {
        Column(
            verticalArrangement = Arrangement.SpaceBetween,
            horizontalAlignment = Alignment.Start,
            modifier = Modifier.fillMaxWidth()
        ) {
            Column {
                OttrojjaText(
                    "لإقتراحاتكم وللإبلاغ عن اي مشاكل تقنية في التطبيق بإمكانكم تعبئة الاستبيان عبر الضغط على الزر ادناه او التواصل معنا على البريد الإلكتروني للمشروع",
                    style = OttrojjaTheme.typography.bodyMedium,
                    textAlign = TextAlign.Start,
                    color = MaterialTheme.colorScheme.primary
                )
                Row(modifier = Modifier
                    .fillMaxWidth()
                    .combinedClickable(
                        onClick = {
                            sendMail(context = context,
                                to = "ottrojjaapp@gmail.com",
                                subject = "ملاحظات لتطبيق الاترجة"
                            )
                        },
                        onLongClick = {
                            Helpers.copyToClipboard(
                                context,
                                "ottrojjaapp@gmail.com",
                                "تم النسخ بنجاح!"
                            )
                        }
                    )
                    .padding(0.dp, 8.dp),
                    horizontalArrangement = Arrangement.Center
                ) {
                    OttrojjaText(
                        text = "ottrojjaapp@gmail.com",
                        style = OttrojjaTheme.typography.bodyMedium.copy(
                            textDirection = TextDirection.Ltr,
                            textAlign = TextAlign.Center
                        ),
                        color = MaterialTheme.colorScheme.primary,
                    )
                }

                Row(
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(0.dp, 8.dp)
                ) {
                    OttrojjaButton(
                        onClick = {
                            val intent = Intent(
                                Intent.ACTION_VIEW,
                                "https://docs.google.com/forms/d/e/1FAIpQLScPRdpxkyl39QC7aK3-KioCddZE2ioXJ8GZ6_XLZsu42eopxA/viewform?usp=header".toUri()
                            )
                            context.startActivity(intent)
                        },
                        text = "فتح الاستبيان"
                    )
                }
            }
            Row(
                modifier = Modifier
                    .padding(top = 24.dp)
                    .fillMaxWidth(),
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