package com.ottrojja.screens.jwam3Screen

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.ottrojja.classes.ButtonAction
import com.ottrojja.composables.ExpandableTextWithDetails
import com.ottrojja.composables.TopBar

@Composable
fun Jwam3Screen(jwam3ViewModel: Jwam3ViewModel = viewModel(), navController: NavController) {


    Column(modifier = Modifier.fillMaxSize(), verticalArrangement = Arrangement.Top) {
        TopBar(title = "جوامع الدعاء",
            mainAction = ButtonAction(Icons.Default.ArrowBack,
                action = { navController.popBackStack() })
        )
        LazyColumn(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight()
                .background(MaterialTheme.colorScheme.tertiary)
        ) {
            items(jwam3ViewModel.supplications) { item ->
                ExpandableTextWithDetails(mainText = item.data.supplication,
                    detailsText = "${item.data.hadith}\n\n${item.data.source}",
                    expanded = item.expanded,
                    updateExpanded = { jwam3ViewModel.updateExpanded(item) }
                )
            }
        }

    }
}