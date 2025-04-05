package com.ottrojja.screens.customTasabeehListScreen

import android.app.AlertDialog
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Replay
import androidx.compose.material.icons.filled.TouchApp
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ElevatedButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ottrojja.composables.ListHorizontalDivider
import com.ottrojja.room.entities.CustomTasbeeh
import com.ottrojja.ui.theme.timeNormal

@Composable
fun CustomTasbeehCounter(item: CustomTasbeeh,
                         deleteCustomTasbeeh: () -> Unit,
                         editCustomTasbeeh: () -> Unit,
                         tasbeehCount: Int,
                         onCountChanged: (Int) -> Unit,
) {
    val context = LocalContext.current;

    var expanded by remember { mutableStateOf(false) }

    fun confirmTasbeehReset() {
        val builder: AlertDialog.Builder = AlertDialog.Builder(context)
        builder
            .setMessage("هل انت متأكد من إعادة العدد الى البداية؟")
            .setPositiveButton("نعم") { dialog, which ->
                onCountChanged(item.count);
            }
            .setNegativeButton("إلغاء") { dialog, which ->
                dialog.dismiss()
            }
        val dialog: AlertDialog = builder.create()
        dialog.show()
    }


    fun confirmDeleteCustomTasbeeh() {
        val alertDialogBuilder = AlertDialog.Builder(context)
        alertDialogBuilder.setTitle("حذف الذكر")
        alertDialogBuilder.setMessage("هل انت متأكد من حذف هذا الذكر؟")
        alertDialogBuilder.setPositiveButton("نعم") { dialog, which ->
            deleteCustomTasbeeh()
            dialog.dismiss()
        }
        alertDialogBuilder.setNegativeButton("لا") { dialog, which ->
            dialog.dismiss()
        }
        val alertDialog = alertDialogBuilder.create()
        alertDialog.show()
    }


    Column(
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .padding(vertical = 12.dp)
            .fillMaxWidth(0.95f)
            .border(
                BorderStroke(4.dp, color = MaterialTheme.colorScheme.primary),
                shape = RoundedCornerShape(12.dp)
            )
            .clip(RoundedCornerShape(12))
            .background(MaterialTheme.colorScheme.background)
            .padding(0.dp)
    ) {
        Row(verticalAlignment = Alignment.Bottom,
            horizontalArrangement = Arrangement.End,
            modifier = Modifier
                .padding(10.dp)
                .fillMaxWidth()
        ) {
            Column {
                Icon(Icons.Default.MoreVert,
                    contentDescription = "tasbeeh options",
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier
                        .clickable { expanded = !expanded }
                )
                DropdownMenu(
                    expanded = expanded,
                    onDismissRequest = { expanded = false }
                ) {
                    DropdownMenuItem(
                        text = {
                            Text(
                                "تعديل",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.primary
                            )
                        },
                        trailingIcon = {
                            Icon(
                                Icons.Default.Edit,
                                contentDescription = "edit custom tasbeeh",
                                tint = MaterialTheme.colorScheme.primary
                            )
                        },
                        onClick = { editCustomTasbeeh(); expanded = false; }
                    )
                    ListHorizontalDivider()
                    DropdownMenuItem(
                        text = {
                            Text(
                                "حذف",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.primary
                            )
                        },
                        trailingIcon = {
                            Icon(
                                Icons.Default.Close,
                                contentDescription = "delete custom tasbeeh",
                                tint = MaterialTheme.colorScheme.primary
                            )
                        },
                        onClick = { confirmDeleteCustomTasbeeh(); expanded = false; }
                    )
                }
            }
        }

        Row(modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp, top = 0.dp),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.Top
        ) {
            Text(
                text = "${tasbeehCount}",
                style = MaterialTheme.typography.bodyLarge,
                fontFamily = timeNormal,
                color = MaterialTheme.colorScheme.primary,
                fontSize = 42.sp,
            )
        }
        ListHorizontalDivider()
        Row(modifier = Modifier
            .padding(vertical = 12.dp, horizontal = 6.dp)
            .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Text(text = item.text,
                style = MaterialTheme.typography.bodyMedium.copy(
                    textAlign = TextAlign.Center,
                    color = MaterialTheme.colorScheme.primary
                )
            )
        }

        Row(
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            ElevatedButton(
                onClick = {
                    if (tasbeehCount > 0) {
                        onCountChanged(tasbeehCount-1)
                    }
                },
                elevation = ButtonDefaults.elevatedButtonElevation(2.dp, 2.dp, 2.dp, 2.dp, 2.dp),
                shape = RoundedCornerShape(4.dp),
                modifier = Modifier.fillMaxWidth(0.8f)
            ) {
                Icon(
                    Icons.Default.TouchApp,
                    contentDescription = "Custom Counter Touch",
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(64.dp)
                )
            }
            ElevatedButton(
                onClick = {
                    confirmTasbeehReset()
                },
                elevation = ButtonDefaults.elevatedButtonElevation(2.dp, 2.dp, 2.dp, 2.dp, 2.dp),
                shape = RoundedCornerShape(4.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 2.dp)
            ) {
                Icon(
                    Icons.Default.Replay,
                    contentDescription = "Custom Counter Reset",
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(64.dp)
                )
            }
        }
    }
}