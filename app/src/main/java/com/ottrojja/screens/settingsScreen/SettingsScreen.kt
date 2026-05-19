package com.ottrojja.screens.settingsScreen

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.ottrojja.R
import com.ottrojja.classes.Helpers
import com.ottrojja.composables.TopBar
import com.ottrojja.composables.ListHorizontalDivider
import com.ottrojja.composables.dialogs.OttrojjaDialog
import com.ottrojja.composables.forms.SwitchWithIcon
import androidx.core.net.toUri
import com.ottrojja.composables.OttrojjaButton
import com.ottrojja.composables.OttrojjaText
import com.ottrojja.ui.theme.OttrojjaTheme


@Composable
fun SettingsScreen(
    settingsScreenViewModel: SettingsScreenViewModel = viewModel()
) {
    val context = LocalContext.current

    LaunchedEffect(Unit) {
        settingsScreenViewModel.getSettings()
    }

    Column(modifier = Modifier
        .fillMaxSize()
        .background(MaterialTheme.colorScheme.tertiary)
    ) {
        TopBar(title = "الإعدادات")

        if (settingsScreenViewModel.ShowAboutDialog) {
            AboutDialog(onDismiss = { settingsScreenViewModel.ShowAboutDialog = false })
        }

        if (settingsScreenViewModel.ShowContactDialog) {
            OttrojjaDialog(contentModifier = Modifier
                .padding(8.dp)
                .fillMaxHeight(0.5f)
                .background(MaterialTheme.colorScheme.secondary)
                .padding(8.dp)
                .clip(shape = RoundedCornerShape(12.dp)),
                onDismissRequest = {
                    settingsScreenViewModel.ShowContactDialog = false
                }) {
                Column(
                    verticalArrangement = Arrangement.SpaceBetween,
                    horizontalAlignment = Alignment.Start,
                    modifier = Modifier.fillMaxSize()
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxHeight(0.8f)
                    ) {
                        OttrojjaText(
                            "لإقتراحاتكم وللإبلاغ عن اي مشاكل تقنية في التطبيق بإمكانكم تعبئة الاستبيان عبر الضغط على الزر ادناه او التواصل معنا على البريد الإلكتروني للمشروع",
                            style = OttrojjaTheme.typography.bodyMedium,
                            textAlign = TextAlign.Start,
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
                            .fillMaxWidth()
                            .fillMaxHeight(),
                        horizontalArrangement = Arrangement.End,
                    ) {
                        OttrojjaButton(
                            onClick = { settingsScreenViewModel.ShowContactDialog = false },
                            text = "إغلاق"
                        )
                    }
                }
            }
        }

        SettingsItem(
            "نمط القراءة الليلية",
            { settingsScreenViewModel.toggleNightReadingMode() },
            content = {
                SwitchWithIcon(
                    checked = settingsScreenViewModel.nightReadingMode,
                    onCheckedChange = { newCheckedState -> settingsScreenViewModel.toggleNightReadingMode() },
                    icon = Icons.Default.Check
                )
            })
        SettingsItem("عن التطبيق", { settingsScreenViewModel.ShowAboutDialog = true })
        SettingsItem("مشاركة التطبيق", {
            val shareIntent = Intent()
            shareIntent.setAction(Intent.ACTION_SEND)
            shareIntent.putExtra(
                Intent.EXTRA_TEXT,
                context.resources.getString(R.string.share_app)
            )
            shareIntent.setType("text/plain")
            context.startActivity(Intent.createChooser(shareIntent, "مشاركة التطبيق"))
        })
        SettingsItem("تواصل معنا", { settingsScreenViewModel.ShowContactDialog = true })
        SettingsItem(
            "سياسة الخصوصية", {
                val intent = Intent(
                    Intent.ACTION_VIEW,
                    "https://doc-hosting.flycricket.io/trj-lshykh-hmd-lhrsys-privacy-policy/202aef51-24ea-40e3-b208-b05c0cb698d6/privacy".toUri()
                )
                context.startActivity(intent)
            })
    }
}

@Composable
fun SettingsItem(textContent: String, onClick: () -> Unit, content: @Composable() () -> Unit = {}) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.background)
            .clickable { onClick() }
            .padding(12.dp, 2.dp)
    ) {
        Row(
            modifier = Modifier
                .padding(6.dp, 12.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            OttrojjaText(text = textContent, color = Color.Black, style = OttrojjaTheme.typography.bodyLarge)
            content()
        }
        ListHorizontalDivider()
    }
}

fun sendMail(
    context: Context,
    to: String,
    subject: String
) {
    try {
        val intent = Intent(Intent.ACTION_SENDTO).apply {
            data = Uri.parse("mailto:$to")
            putExtra(Intent.EXTRA_SUBJECT, subject)
        }

        context.startActivity(
            Intent.createChooser(intent, "اختر تطبيق البريد")
        )

    } catch (e: Exception) {
        Toast.makeText(
            context,
            "خدمة البريد الالكتروني غير متوفرة",
            Toast.LENGTH_LONG
        ).show()
        Helpers.reportException(e, "SettingsScreen")
    }
}

@Composable
fun AboutDialog(onDismiss: () -> Unit) {
    OttrojjaDialog(contentModifier = Modifier
        .padding(8.dp)
        .fillMaxHeight(0.7f)
        .padding(8.dp)
        .clip(shape = RoundedCornerShape(12.dp)),
        onDismissRequest = {
            onDismiss()
        }) {
        Column(
            verticalArrangement = Arrangement.SpaceBetween,
            horizontalAlignment = Alignment.Start,
            modifier = Modifier.fillMaxSize()
        ) {
            OttrojjaText(
                stringResource(R.string.about_app),
                style = OttrojjaTheme.typography.bodyMedium,
                textAlign = TextAlign.Start,
                modifier = Modifier
                    .fillMaxHeight(0.9f)
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 2.dp)
            )
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .fillMaxHeight(),
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