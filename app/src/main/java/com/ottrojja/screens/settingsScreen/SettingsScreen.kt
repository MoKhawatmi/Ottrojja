package com.ottrojja.screens.settingsScreen

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import com.ottrojja.composables.Header
import com.ottrojja.composables.ListHorizontalDivider
import com.ottrojja.composables.OttrojjaDialog
import com.ottrojja.composables.SwitchWithIcon


@Composable
fun SettingsScreen(
    settingsScreenViewModel: SettingsScreenViewModel = viewModel()
) {
    val context = LocalContext.current

    LaunchedEffect(Unit) {
        settingsScreenViewModel.getSettings()
    }

    Column(modifier = Modifier.fillMaxSize()) {
        Header()

        if (settingsScreenViewModel.ShowAboutDialog) {
            OttrojjaDialog(contentModifier = Modifier
                .padding(8.dp)
                .fillMaxHeight(0.7f)
                .background(MaterialTheme.colorScheme.secondary)
                .padding(8.dp)
                .clip(shape = RoundedCornerShape(12.dp)),
                onDismissRequest = {
                    settingsScreenViewModel.ShowAboutDialog = false
                }) {
                Column(
                    verticalArrangement = Arrangement.SpaceBetween,
                    horizontalAlignment = Alignment.Start,
                    modifier = Modifier.fillMaxSize()
                ) {
                    Text(
                        stringResource(R.string.about_app),
                        style = MaterialTheme.typography.bodyMedium,
                        fontSize = 20.sp,
                        textAlign = TextAlign.Start,
                        modifier = Modifier
                            .fillMaxHeight(0.9f)
                            .verticalScroll(rememberScrollState())
                    )
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .fillMaxHeight(),
                        horizontalArrangement = Arrangement.End,
                    ) {
                        Button(onClick = { settingsScreenViewModel.ShowAboutDialog = false }) {
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
                        Text(
                            "لإقتراحاتكم وللإبلاغ عن اي مشاكل تقنية في التطبيق تواصلوا معنا على البريد الإلكتروني للمشروع",
                            style = MaterialTheme.typography.bodyMedium,
                            fontSize = 20.sp,
                            textAlign = TextAlign.Start,
                        )
                        Text(
                            text = "ottrojjaapp@gmail.com",
                            style = MaterialTheme.typography.bodyMedium.copy(
                                textDirection = TextDirection.Ltr,
                                textAlign = TextAlign.Center
                            ),
                            fontSize = 22.sp,
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    Helpers.copyToClipboard(
                                        context,
                                        "ottrojjaapp@gmail.com",
                                        "تم النسخ بنجاح!"
                                    )
                                }
                                .padding(0.dp, 8.dp)
                        )

                        Row(
                            horizontalArrangement = Arrangement.Center,
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(0.dp, 8.dp)
                        ) {
                            Button(onClick = {
                                sendMail(
                                    context = context,
                                    to = "ottrojjaapp@gmail.com",
                                    subject = ""
                                )
                            }) {
                                Text(
                                    "ارسل بريدا",
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontSize = 20.sp,
                                    textAlign = TextAlign.End
                                )
                            }
                        }
                    }
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .fillMaxHeight(),
                        horizontalArrangement = Arrangement.End,
                    ) {
                        Button(onClick = { settingsScreenViewModel.ShowContactDialog = false }) {
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
                    Uri.parse("https://doc-hosting.flycricket.io/trj-lshykh-hmd-lhrsys-privacy-policy/202aef51-24ea-40e3-b208-b05c0cb698d6/privacy")
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
            Text(text = textContent, color = Color.Black)
            content()
        }
        ListHorizontalDivider()
    }
}

fun sendMail(context: Context, to: String, subject: String) {
    try {
        val intent = Intent(Intent.ACTION_SEND)
        intent.type = "vnd.android.cursor.item/email"
        intent.putExtra(Intent.EXTRA_EMAIL, arrayOf(to))
        intent.putExtra(Intent.EXTRA_SUBJECT, subject)
        context.startActivity(intent)
    } catch (e: ActivityNotFoundException) {
        Toast.makeText(context, "خدمة البريد الالكتروني غير متوفرة", Toast.LENGTH_LONG).show()
    } catch (t: Throwable) {
        Toast.makeText(context, "حدث خطأ", Toast.LENGTH_LONG).show()
    }

}