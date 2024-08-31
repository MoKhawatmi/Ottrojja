package com.ottrojja.screens.settingsScreen

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.ottrojja.classes.Screen
import com.ottrojja.composables.Header

@Composable
fun SettingsScreen(modifier: Modifier = Modifier) {
    val context = LocalContext.current

    Column(modifier=Modifier.fillMaxSize()){
        Header()

        Column(modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.background)
            .clickable {
                val intent = Intent(
                    Intent.ACTION_VIEW,
                    Uri.parse("https://doc-hosting.flycricket.io/trj-lshykh-hmd-lhrsys-privacy-policy/202aef51-24ea-40e3-b208-b05c0cb698d6/privacy")
                )
                context.startActivity(intent)
            }.padding(12.dp, 12.dp)
        ) {
            Row() {
                Text(text = "سياسة الخصوصية", color = Color.Black)
            }
            Box(
                Modifier
                    .fillMaxWidth()
                    .height(1.dp)
                    .background(Color.Black.copy(alpha = 0.1f))
            )
        }
    }


}