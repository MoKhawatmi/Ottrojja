package com.ottrojja.screens.tasbeehScreen

import android.app.AlertDialog
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Replay
import androidx.compose.material.icons.filled.TouchApp
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ElevatedButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ottrojja.ui.theme.timeNormal

@Composable
fun CounterContent(
    tasbeehCount: Int,
    increaseCount: () -> Unit,
    resetCount: () -> Unit
) {
    val context = LocalContext.current

    fun confirmTasbeehReset() {
        val builder: AlertDialog.Builder = AlertDialog.Builder(context)
        builder
            .setMessage("هل انت متأكد من إعادة العدد الى البداية؟")
            .setPositiveButton("نعم") { dialog, which ->
                resetCount()
            }
            .setNegativeButton("إلغاء") { dialog, which ->
                dialog.dismiss()
            }

        val dialog: AlertDialog = builder.create()
        dialog.show()
    }


    Row(
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .padding(0.dp, 18.dp, 0.dp, 24.dp)
    ) {
        Row(
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .padding(8.dp)
                .fillMaxWidth(0.8F)
                .border(
                    BorderStroke(4.dp, color = MaterialTheme.colorScheme.primary),
                    shape = RoundedCornerShape(12.dp)
                )
                .clip(RoundedCornerShape(12))
                .background(MaterialTheme.colorScheme.background, shape = RoundedCornerShape(12.dp))
                .padding(16.dp)

        ) {
            Text(
                text = "${tasbeehCount}",
                style = MaterialTheme.typography.bodyLarge,
                fontFamily = timeNormal,
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.primary,
                fontSize = 42.sp,
            )
        }
    }

    Row(
        horizontalArrangement = Arrangement.End,
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .padding(0.dp, 0.dp, 10.dp, 0.dp)
            .fillMaxWidth()
    ) {
        Button(
            onClick = { confirmTasbeehReset() },
            modifier = Modifier
                .size(50.dp),
            shape = CircleShape,
            contentPadding = PaddingValues(8.dp)
        ) {
            Icon(
                Icons.Default.Replay,
                contentDescription = "Reset",
                tint = MaterialTheme.colorScheme.onPrimary,
                modifier = Modifier.fillMaxSize()
            )
        }
    }

    Row(
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.fillMaxWidth()
    ) {
        ElevatedButton(
            onClick = { increaseCount() },
            elevation = ButtonDefaults.elevatedButtonElevation(2.dp, 2.dp, 2.dp, 2.dp, 2.dp),
            contentPadding = PaddingValues(0.dp),
            shape = CircleShape,
            modifier = Modifier
                .padding(0.dp, 2.dp, 0.dp, 12.dp)
                .fillMaxWidth(0.9F)
                .fillMaxHeight(0.8F)
                .padding(24.dp, 24.dp)
                .clip(CircleShape)
        ) {
            Icon(
                Icons.Default.TouchApp,
                contentDescription = "Counter",
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(128.dp)
            )
        }
    }
}
