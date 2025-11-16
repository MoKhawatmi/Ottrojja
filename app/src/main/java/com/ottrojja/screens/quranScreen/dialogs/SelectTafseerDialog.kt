package com.ottrojja.screens.quranScreen.dialogs

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.ottrojja.composables.OttrojjaDialog

@Composable
fun SelectTafseerDialog(
    onDismissRequest: () -> Unit,
    onOptionClick: (String) -> Unit,
    tafseerMap: HashMap<String, String>
) {
    OttrojjaDialog(contentModifier = Modifier
        .padding(8.dp)
        .wrapContentHeight()
        .background(MaterialTheme.colorScheme.secondary)
        .padding(8.dp)
        .clip(shape = RoundedCornerShape(12.dp)),
        onDismissRequest= {onDismissRequest()}){
        Column() {
            tafseerMap.keys.forEach { tafseer ->
                Row(modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onOptionClick(tafseer) }
                    .padding(6.dp)) {
                    Text(
                        text = tafseer,
                        modifier = Modifier.fillMaxWidth(),
                        textAlign = TextAlign.Right,
                        color = MaterialTheme.colorScheme.onSecondary
                    )
                }
            }
        }
    }
}