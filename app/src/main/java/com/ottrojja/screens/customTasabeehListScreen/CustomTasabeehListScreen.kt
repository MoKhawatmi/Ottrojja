package com.ottrojja.screens.customTasabeehListScreen

import android.app.AlertDialog
import android.app.Application
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.TouchApp
import androidx.compose.material.icons.rounded.DragHandle
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ElevatedButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
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
import com.ottrojja.composables.TopBar
import sh.calvin.reorderable.ReorderableItem
import sh.calvin.reorderable.rememberReorderableLazyListState

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

    val lazyListState = rememberLazyListState()
    val reorderableLazyListState = rememberReorderableLazyListState(lazyListState) { from, to ->
        // Update the list
        customTasabeehListScreenViewModel.updateUIListOnDrag(to.index, from.index)
    }


    fun triggerAddTasbeehDialog() {
        customTasabeehListScreenViewModel.customTasbeehModalMode = ModalFormMode.ADD;
        customTasabeehListScreenViewModel.addTasbeehDialog = true;
    }

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
                        triggerAddTasbeehDialog()
                    }),
                ButtonAction(icon = Icons.Default.Close,
                    title = "حذف القائمة",
                    action = { confirmDeleteList(); }
                )
            )
        )

        Row(
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            ElevatedButton(
                onClick = { triggerAddTasbeehDialog() },
                elevation = ButtonDefaults.elevatedButtonElevation(2.dp, 2.dp, 2.dp, 2.dp, 2.dp),
                shape = RoundedCornerShape(4.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(
                    Icons.Default.Add,
                    contentDescription = "Add Tasbeeh",
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(40.dp)
                )
            }
        }


        LazyColumn(
            state = lazyListState,
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp),
            contentPadding = PaddingValues(top = 10.dp, bottom = 10.dp),
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
        ) {
            if (customTasabeehListScreenViewModel.customTasabeeh.isEmpty()) {
                item {
                    EmptyListMessage("لا يوجد اذكار مضافة بعد")
                }
            } else {
                items(customTasabeehListScreenViewModel.customTasabeeh, key = { it.id }) { item ->

                    ReorderableItem(reorderableLazyListState, key = item.id) { isDragging ->
                        val elevation by animateDpAsState(if (isDragging) 4.dp else 0.dp)

                        Surface(shadowElevation = elevation, shape = RoundedCornerShape(12.dp)) {
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
                            IconButton(
                                modifier = Modifier.draggableHandle(
                                    onDragStarted = {
                                    },
                                    onDragStopped = {
                                        customTasabeehListScreenViewModel.updateTasabeehListPositions()
                                    },
                                ),
                                onClick = {},
                            ) {
                                Icon(Icons.Rounded.DragHandle, contentDescription = "Reorder")
                            }

                        }
                    }
                }
            }
        }
    }
}