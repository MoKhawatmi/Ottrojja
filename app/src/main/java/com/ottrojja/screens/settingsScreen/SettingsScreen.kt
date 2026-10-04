package com.ottrojja.screens.settingsScreen

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.ottrojja.R
import com.ottrojja.classes.Helpers
import com.ottrojja.composables.TopBar
import com.ottrojja.composables.ListHorizontalDivider
import com.ottrojja.composables.forms.SwitchWithIcon
import androidx.core.net.toUri
import com.ottrojja.composables.OttrojjaText
import com.ottrojja.composables.ToastManager.classes.AppToastManager
import com.ottrojja.composables.ToastManager.classes.AppToastType
import com.ottrojja.screens.settingsScreen.dialogs.AboutDialog
import com.ottrojja.screens.settingsScreen.dialogs.ContactDialog
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
            ContactDialog(
                onDismiss = { settingsScreenViewModel.ShowContactDialog = false },
                context = context
            )
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
        AppToastManager.show(
            message = "خدمة البريد الالكتروني غير متوفرة",
            type = AppToastType.ERROR
        )
        Helpers.reportException(e, "SettingsScreen")
    }
}

