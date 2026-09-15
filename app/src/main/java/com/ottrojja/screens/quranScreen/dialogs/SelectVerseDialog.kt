package com.ottrojja.screens.quranScreen.dialogs

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.ottrojja.composables.OttrojjaText
import com.ottrojja.composables.PillShapedTextFieldWithIcon
import com.ottrojja.composables.dialogs.OttrojjaDialog
import com.ottrojja.composables.dialogs.OttrojjaItemSelectionDialog
import com.ottrojja.room.entities.PageContent
import com.ottrojja.room.entities.PageContentItemType
import com.ottrojja.ui.theme.OttrojjaTheme
import kotlin.collections.forEach

@Composable
fun SelectVerseDialog(
    onDismissRequest: () -> Unit,
    onSelect: (PageContent) -> Unit,
    versesList: List<PageContent>
) {
    OttrojjaItemSelectionDialog(
        title = "إختيار الآية",
        onDismiss = { onDismissRequest() },
        showDivider = false,
        selectionItems = versesList,
        onSelect = { item ->
            onSelect(item)
        },
        itemContent = { item ->
            if (item.type == PageContentItemType.verse) {
                Row(modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onSelect(item) }
                    .padding(6.dp)) {
                    OttrojjaText(
                        text = "الاية ${item.verseNum}",
                        modifier = Modifier.fillMaxWidth(),
                        textAlign = TextAlign.Right,
                        color = MaterialTheme.colorScheme.primary,
                        style = OttrojjaTheme.typography.bodyMedium,
                    )
                }
            } else {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(MaterialTheme.colorScheme.primary)
                        .padding(bottom = 6.dp, top = 2.dp),
                    horizontalArrangement = Arrangement.Center
                ) {
                    OttrojjaText(
                        text = " سورة " + "${item.surahName}",
                        modifier = Modifier.fillMaxWidth(),
                        textAlign = TextAlign.Center,
                        color = MaterialTheme.colorScheme.onPrimary,
                        style = OttrojjaTheme.typography.bodySpecialMedium,
                    )
                }
            }
        }
    )
}