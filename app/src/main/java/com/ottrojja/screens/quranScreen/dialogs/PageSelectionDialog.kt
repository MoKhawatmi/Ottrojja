package com.ottrojja.screens.quranScreen.dialogs

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.ottrojja.composables.OttrojjaText
import com.ottrojja.composables.PillShapedTextFieldWithIcon
import com.ottrojja.composables.dialogs.OttrojjaItemSelectionDialog
import com.ottrojja.ui.theme.OttrojjaTheme

@Composable
fun PageSelectionDialog(
    onDismissRequest: () -> Unit,
    pages: List<String>,
    selectedPage: String,
    onSelect: (String) -> Unit,
    searchFilter: String,
    searchFilterChanged: (String) -> Unit
) {
    OttrojjaItemSelectionDialog(
        title = "إختيار الصفحة",
        onDismiss = { onDismissRequest() },
        searchBar = {
            PillShapedTextFieldWithIcon(
                value = searchFilter,
                onValueChange = { newValue -> searchFilterChanged(newValue) },
                leadingIcon = Icons.Default.Search,
                modifier = Modifier.fillMaxWidth(0.9f),
                placeHolder = "الصفحة"
            )
        },
        selectionItems = pages,
        onSelect = { item ->
            onSelect(item)
        },
        selectedItem = selectedPage,
        itemContent = { item ->
            Row(modifier = Modifier
                .fillMaxWidth()
                .padding(6.dp)
            ) {
                OttrojjaText(
                    text = "ص${item}",
                    modifier = Modifier.fillMaxWidth(),
                    textAlign = TextAlign.Right,
                    color = MaterialTheme.colorScheme.primary,
                    style = OttrojjaTheme.typography.bodyLarge
                )
            }
        }
    )
}