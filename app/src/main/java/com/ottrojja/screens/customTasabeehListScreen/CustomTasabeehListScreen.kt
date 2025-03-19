@file:Suppress("ASSIGNED_BUT_NEVER_ACCESSED_VARIABLE")

package com.ottrojja.screens.customTasabeehListScreen

import android.app.AlertDialog
import android.app.Application
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.ottrojja.classes.ModalFormMode
import com.ottrojja.classes.QuranRepository
import com.ottrojja.composables.EmptyListMessage
import com.ottrojja.composables.ListHorizontalDivider
import com.ottrojja.composables.OttrojjaElevatedButton
import com.ottrojja.composables.OttrojjaTopBar

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

    var expanded by remember { mutableStateOf(false) }

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

    Column(modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Top
    ) {
        OttrojjaTopBar {
            Row(
                modifier = Modifier
                    .padding(horizontal = 6.dp)
                    .background(MaterialTheme.colorScheme.background)
                    .fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(text = "${customTasabeehListScreenViewModel.customTasabeehList?.title}",
                    style = MaterialTheme.typography.bodyLarge.copy(fontSize = 32.sp),
                    color = MaterialTheme.colorScheme.primary,
                    textAlign = TextAlign.Start,
                    modifier = Modifier.fillMaxWidth(0.6f)
                )

                Row(modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        OttrojjaElevatedButton(
                            onClick = { expanded = !expanded },
                            icon = Icons.Default.MoreVert
                        )
                        DropdownMenu(
                            expanded = expanded,
                            onDismissRequest = { expanded = false }
                        ) {
                            DropdownMenuItem(
                                text = {
                                    Text(
                                        "إضافة ذكر",
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                },
                                trailingIcon = {
                                    Icon(
                                        Icons.Default.Add,
                                        contentDescription = "add zikr",
                                        tint = MaterialTheme.colorScheme.primary
                                    )
                                },
                                onClick = {
                                    customTasabeehListScreenViewModel.customTasbeehModalMode = ModalFormMode.ADD;
                                    customTasabeehListScreenViewModel.addTasbeehDialog = true;
                                    expanded = false;
                                }
                            )
                            ListHorizontalDivider()
                            DropdownMenuItem(
                                text = {
                                    Text(
                                        "حذف القائمة",
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                },
                                trailingIcon = {
                                    Icon(
                                        Icons.Default.Close,
                                        contentDescription = "delete list",
                                        tint = MaterialTheme.colorScheme.primary
                                    )
                                },
                                onClick = { confirmDeleteList(); expanded = false; }
                            )
                        }
                    }

                    OttrojjaElevatedButton(
                        onClick = { navController.popBackStack() },
                        icon = Icons.Filled.ArrowBack
                    )
                }
            }
        }
        LazyColumn(horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .fillMaxSize()
                .padding(bottom = 12.dp)
        ) {
            if (customTasabeehListScreenViewModel.customTasabeeh.isEmpty()) {
                item {
                    EmptyListMessage("لا يوجد اذكار مضافة بعد")
                }
            }
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
                    tasbeehCount = customTasabeehListScreenViewModel.itemCounts.get(item.id) ?: item.count,
                    onCountChanged = { value -> customTasabeehListScreenViewModel.itemCounts.put(item.id, value) },
                )
            }
        }
    }
}