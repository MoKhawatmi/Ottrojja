package com.ottrojja.screens.customTasabeehListScreen

import android.app.AlertDialog
import android.app.Application
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.ottrojja.classes.ButtonAction
import com.ottrojja.classes.ModalFormMode
import com.ottrojja.classes.QuranRepository
import com.ottrojja.composables.EmptyListMessage
import com.ottrojja.composables.ListHorizontalDivider
import com.ottrojja.composables.OttrojjaElevatedButton
import com.ottrojja.composables.SecondaryTopBar
import com.ottrojja.composables.OttrojjaTopBarTitle
import com.ottrojja.composables.TopBar

@Composable
fun CustomTasabeehListScreen(
    navController: NavController,
    repository: QuranRepository,
    id: Int
) {
    val context = LocalContext.current
    val application = context.applicationContext as Application

    val customTasabeehListScreenViewModel: CustomTasabeehListScreenViewModel = viewModel(
        factory = CustomTasabeehListScreenViewModelFactory(repository, application)
    )

    LaunchedEffect(Unit) {
        customTasabeehListScreenViewModel.fetchCustomTasabeehList(id)
    }

    fun confirmDeleteList() {
        val alertDialogBuilder = AlertDialog.Builder(context)
        alertDialogBuilder.setTitle("حذف القائمة")
        alertDialogBuilder.setMessage("هل انت متأكد من حذف هذه القائمة؟")
        alertDialogBuilder.setPositiveButton("نعم") { dialog, which ->
            customTasabeehListScreenViewModel.deleteCustomTasabeehList()
            dialog.dismiss()
            navController.popBackStack()
            /*val currentDestination = navController.currentBackStackEntry?.destination?.route
            currentDestination?.let {
                navController.navigate(Screen.KhitmahListScreen.route) {
                    popUpTo(it) { inclusive = true }
                }
            }

            navController.navigate(Screen.KhitmahListScreen.route) {
                popUpTo("current_destination") { inclusive = true }
            }*/
        }
        alertDialogBuilder.setNegativeButton("لا") { dialog, which ->
            dialog.dismiss()
        }

        val alertDialog = alertDialogBuilder.create()
        alertDialog.show()
    }

    if (customTasabeehListScreenViewModel.addTasbeehDialog) {
        AddCustomTasbeehDialog(
            onDismiss = { customTasabeehListScreenViewModel.closeCustomTasbeehModal() },
            onConfirm = { customTasabeehListScreenViewModel.upsertCustomTasbeeh() },
            callImportTasbeehDialog = { customTasabeehListScreenViewModel.showImportTasbeehDialog = true },
            tasbeehInWork = customTasabeehListScreenViewModel.tasbeehInWork,
            onTasbeehChange = { value -> customTasabeehListScreenViewModel.tasbeehInWork = value },
            mode = customTasabeehListScreenViewModel.customTasbeehModalMode
        )
    }

    if (customTasabeehListScreenViewModel.showImportTasbeehDialog) {
        ImportTasbeehDialog(
            onDismiss = { customTasabeehListScreenViewModel.showImportTasbeehDialog = false },
            tasabeehList = customTasabeehListScreenViewModel.tasabeeh,
            importTasbeeh = { tasbeeh ->
                customTasabeehListScreenViewModel.tasbeehInWork = customTasabeehListScreenViewModel.tasbeehInWork.copy(
                    text = tasbeeh.ziker
                )
            }
        )
    }

    Column(modifier = Modifier
        .fillMaxSize()
        .background(MaterialTheme.colorScheme.tertiary),
        verticalArrangement = Arrangement.Top
    ) {

        TopBar(title = customTasabeehListScreenViewModel.customTasabeehList?.title ?: "",
            mainAction = ButtonAction(icon = Icons.Filled.ArrowBack,
                action = { navController.popBackStack() }),
            secondaryActions = listOf(
                ButtonAction(icon = Icons.Default.Add, title = "إضافة ذكر",
                    action = {
                    customTasabeehListScreenViewModel.customTasbeehModalMode = ModalFormMode.ADD;
                    customTasabeehListScreenViewModel.addTasbeehDialog = true;
                }),
                ButtonAction(icon = Icons.Default.Close,
                    title = "حذف القائمة",
                    action = { confirmDeleteList(); }
                )
            )
        )

        LazyColumn(horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .fillMaxSize()
                .padding(bottom = 12.dp)
        ) {
            if (customTasabeehListScreenViewModel.customTasabeeh.isEmpty()) {
                item {
                    EmptyListMessage("لا يوجد اذكار مضافة بعد")
                }
            } else {
                items(customTasabeehListScreenViewModel.customTasabeeh, key = { it.id }) { item ->
                    CustomTasbeehCounter(
                        item = item,
                        deleteCustomTasbeeh = {
                            customTasabeehListScreenViewModel.deleteCustomTasbeeh(item)
                        },
                        editCustomTasbeeh = {
                            customTasabeehListScreenViewModel.customTasbeehModalMode = ModalFormMode.EDIT
                            customTasabeehListScreenViewModel.tasbeehInWork = item;
                            customTasabeehListScreenViewModel.addTasbeehDialog = true;
                        },
                        tasbeehCount = customTasabeehListScreenViewModel.itemCounts.get(item.id)
                            ?: item.count,
                        onCountChanged = { value ->
                            customTasabeehListScreenViewModel.itemCounts.put(item.id, value)
                        },
                    )
                }
            }
        }
    }
}