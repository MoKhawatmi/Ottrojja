package com.ottrojja.screens.blessingsScreen

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.ottrojja.composables.Header
import com.ottrojja.composables.LoadingDialog

@Composable
fun BlessingsScreen(modifier: Modifier, blessingsViewModel: BlessingsViewModel = viewModel()) {

    LaunchedEffect(Unit) {
        blessingsViewModel.initAndFetch()
    }

    if (blessingsViewModel.loading) {
        LoadingDialog()
    }

    Column(modifier = Modifier.fillMaxSize()) {
        Header()
        LazyColumn(modifier = Modifier.padding(horizontal = 6.dp)) {
            items(blessingsViewModel.blessingsList) { item ->
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp)
                ) {
                    Row(
                        horizontalArrangement = Arrangement.End,
                        modifier = Modifier
                            .fillMaxWidth()
                    ) {
                        Icon(
                            Icons.Default.Share,
                            contentDescription = "share blessing",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.clickable { blessingsViewModel.shareBlessing(item) }
                        )
                    }

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                    ) {
                        Text(
                            text = item.text,
                            color = MaterialTheme.colorScheme.primary,
                            style = MaterialTheme.typography.bodyMedium,
                            textAlign = TextAlign.Right,
                            modifier = Modifier.padding(6.dp, 8.dp)
                        )
                    }
                    HorizontalDivider(thickness = 1.dp, color = Color.Black.copy(alpha = 0.1f))
                }
            }

        }
    }

}
