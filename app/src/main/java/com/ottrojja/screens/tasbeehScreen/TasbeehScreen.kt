package com.ottrojja.screens.tasbeehScreen

import android.app.Application
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.ottrojja.classes.QuranRepository
import com.ottrojja.composables.Header
import com.ottrojja.composables.OttrojjaTabs
import com.ottrojja.composables.OttrojjaTopBar

@Composable
fun TasbeehScreen(
    navController: NavController,
    repository: QuranRepository,
) {
    val context = LocalContext.current
    val application = context.applicationContext as Application

    val tasbeehScreenViewModel: TasbeehScreenViewModel = viewModel(
        factory = TasbeehViewModelFactory(repository, application)
    )

    LaunchedEffect(Unit) {
        tasbeehScreenViewModel.fetchTasabeehLists()
    }

    if (tasbeehScreenViewModel.showAddListDialog) {
        AddTasabeehListDialog(onDismiss = { tasbeehScreenViewModel.showAddListDialog = false },
            onConfirm = { title -> tasbeehScreenViewModel.createTasabeehList(title) })
    }

    val tasbeehCount by tasbeehScreenViewModel.tasbeehCount.collectAsState()


    Column() {
        Header()
        OttrojjaTopBar {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 6.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                OttrojjaTabs(
                    items = TasbeehTab.entries,
                    selectedItem = tasbeehScreenViewModel.selectedTab,
                    onClickTab = { item -> tasbeehScreenViewModel.selectedTab = item })
            }
        }

        when (tasbeehScreenViewModel.selectedTab) {
            TasbeehTab.المسبحة -> CounterContent(
                tasbeehCount = tasbeehCount,
                increaseCount = { tasbeehScreenViewModel.increaseTasbeeh() },
                resetCount = { tasbeehScreenViewModel.resetTasbeeh() }
            )

            TasbeehTab.تسابيح -> TasabeehList(
                tasabeeh = tasbeehScreenViewModel.tasabeeh,
                updateExpanded = { item -> tasbeehScreenViewModel.updateExpanded(item) }
            )

            TasbeehTab.القوائم -> CustomTasabeehLists(
                navController = navController,
                customTasabeehLists = tasbeehScreenViewModel.tasabeehLists,
                onClickAdd = { tasbeehScreenViewModel.showAddListDialog = true }
            )
        }
    }
}


