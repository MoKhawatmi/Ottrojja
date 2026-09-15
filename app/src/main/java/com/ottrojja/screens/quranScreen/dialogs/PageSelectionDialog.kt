package com.ottrojja.screens.quranScreen.dialogs

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.ottrojja.R
import com.ottrojja.composables.OttrojjaText
import com.ottrojja.composables.dialogs.OttrojjaDialog
import com.ottrojja.composables.PillShapedTextFieldWithIcon
import com.ottrojja.composables.dialogs.OttrojjaItemSelectionDialog
import com.ottrojja.ui.theme.OttrojjaTheme

@Composable
fun PageSelectionDialog(
    onDismissRequest: () -> Unit,
    pages: List<String>,
    scrollPageNumber: Int,
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
        initialScrollIndex = scrollPageNumber - 1,
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