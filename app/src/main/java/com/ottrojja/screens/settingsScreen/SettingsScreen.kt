package com.ottrojja.screens.settingsScreen

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.ottrojja.composables.Header
import com.ottrojja.composables.OttrojjaDialog


@Composable
fun SettingsScreen(
    modifier: Modifier = Modifier,
    settingsScreenViewModel: SettingsScreenViewModel = viewModel()
) {
    val context = LocalContext.current

    Column(modifier = Modifier.fillMaxSize()) {
        Header()

        if (settingsScreenViewModel.ShowAboutDialog) {
            OttrojjaDialog(onDismissRequest = {
                settingsScreenViewModel.ShowAboutDialog = false
            }) {
                Column(
                    verticalArrangement = Arrangement.SpaceBetween,
                    horizontalAlignment = Alignment.Start,
                    modifier = Modifier.fillMaxSize()
                ) {
                    Text(
                        "تطبيق اترجة القرآني للقارئ الشيخ احمد الحراسيس",
                        style = MaterialTheme.typography.displayMedium,
                        fontSize = 20.sp,
                        textAlign = TextAlign.Start
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End
                    ) {
                        Button(onClick = { settingsScreenViewModel.ShowAboutDialog = false }) {
                            Text(
                                "إغلاق",
                                style = MaterialTheme.typography.displayMedium,
                                fontSize = 20.sp,
                                textAlign = TextAlign.End
                            )
                        }
                    }
                }
            }
        }

        SettingsItem("عن التطبيق", { settingsScreenViewModel.ShowAboutDialog = true })
        SettingsItem("مشاركة التطبيق", {
            val shareIntent = Intent()
            shareIntent.setAction(Intent.ACTION_SEND)
            shareIntent.putExtra(
                Intent.EXTRA_TEXT,
                "تطبيق اترجة القرآني للقارئ الشيخ أحمد الحراسيس: https://play.google.com/store/apps/details?id=com.ottrojja"
            )
            shareIntent.setType("text/plain")
            context.startActivity(Intent.createChooser(shareIntent, "مشاركة التطبيق"))

        })
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
fun SettingsItem(content: String, onClick: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.background)
            .clickable { onClick() }
            .padding(12.dp, 2.dp)
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(text = content, color = Color.Black)
        }
        HorizontalDivider(thickness = 1.dp, color = Color.Black.copy(alpha = 0.1f))
    }

}