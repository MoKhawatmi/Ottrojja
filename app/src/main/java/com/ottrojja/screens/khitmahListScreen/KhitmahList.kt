package com.ottrojja.screens.khitmahListScreen

import android.app.Application
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Circle
import androidx.compose.material3.Button
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.ottrojja.classes.ButtonAction
import com.ottrojja.classes.Helpers
import com.ottrojja.classes.QuranRepository
import com.ottrojja.classes.Screen
import com.ottrojja.composables.EmptyListMessage
import com.ottrojja.composables.TopBar
import com.ottrojja.composables.ListHorizontalDivider
import com.ottrojja.composables.OttrojjaDialog
import com.ottrojja.ui.theme.complete_green

@Composable
fun KhitmahList(
    navController: NavController,
    repository: QuranRepository
) {
    val context = LocalContext.current
    val application = context.applicationContext as Application

    val khitmahListViewModel: KhitmahListViewModel = viewModel(
        factory = KhitmahListViewModelFactory(repository, application)
    )

    LaunchedEffect(Unit) {
        khitmahListViewModel.fetchKhitmahList()
    }

    if (khitmahListViewModel.showAddKhitmahDialog) {
        AddKhitmahDialog(
            onDismiss = { khitmahListViewModel.showAddKhitmahDialog = false },
            onConfirm = { title -> khitmahListViewModel.createKhitmah(title) })
    }


    Column(modifier = Modifier
        .fillMaxSize()
        .background(MaterialTheme.colorScheme.tertiary)
    ) {
        TopBar(title = "الختمات", mainAction = ButtonAction(Icons.Default.Add,
            action = { khitmahListViewModel.showAddKhitmahDialog = true })
        )

        LazyColumn(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight()
        ) {
            if (khitmahListViewModel.khitmahList.size <= 0) {
                item {
                    EmptyListMessage("لا يوجد ختمات حاليا")
                }
            }

            items(khitmahListViewModel.khitmahList, key = { it.id }) { item ->
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(MaterialTheme.colorScheme.background)
                        .clickable {
                            navController.navigate(Screen.KhitmahScreen.invokeRoute("${item.id}"))
                        }
                        .padding(8.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Start,
                        modifier = Modifier.fillMaxWidth(0.7f)
                    ) {
                        Icon(Icons.Filled.Circle,
                            contentDescription = "Khitmah Status",
                            tint = if (item.isComplete) complete_green else MaterialTheme.colorScheme.primary,
                            modifier = Modifier
                                .padding(end = 4.dp)
                                .size(16.dp)
                        )
                        Text(
                            text = item.title,
                            color = MaterialTheme.colorScheme.primary,
                            style = MaterialTheme.typography.bodyLarge,
                            textAlign = TextAlign.Right,
                        )
                    }


                    if (item.latestPage.isNotBlank()) {
                        Button(onClick = {
                            navController.navigate(Screen.QuranScreen.invokeRoute(item.latestPage))
                        }, modifier = Modifier.fillMaxWidth()) {
                            Text(
                                "ص${Helpers.convertToIndianNumbers(item.latestPage)}",
                                style = MaterialTheme.typography.bodyMedium,
                            )
                        }
                    }
                }
                ListHorizontalDivider()
            }
        }
    }
}


@Composable
fun AddKhitmahDialog(onDismiss: () -> Unit, onConfirm: (String) -> Unit) {
    OttrojjaDialog(
        contentModifier = Modifier
            .padding(8.dp)
            .wrapContentHeight()
            .fillMaxWidth(0.9f)
            .background(MaterialTheme.colorScheme.secondary)
            .padding(8.dp)
            .clip(shape = RoundedCornerShape(12.dp)),
        onDismissRequest = { onDismiss() },
        useDefaultWidth = false,
    ) {
        var khitmahTitle by remember { mutableStateOf("") }


        Column(modifier = Modifier.wrapContentHeight()) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center) {
                Text(text = "إضافة ختمة", textAlign = TextAlign.Center)
            }
            HorizontalDivider(
                modifier = Modifier.padding(vertical = 6.dp),
                color = MaterialTheme.colorScheme.onTertiary
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedTextField(
                    value = khitmahTitle,
                    onValueChange = { khitmahTitle = it },
                    label = { Text("عنوان الختمة") },
                    colors = TextFieldDefaults.colors(
                        focusedTextColor = MaterialTheme.colorScheme.onSecondary,
                        unfocusedTextColor = MaterialTheme.colorScheme.onSecondary,
                        focusedContainerColor = MaterialTheme.colorScheme.secondary,
                        unfocusedContainerColor = MaterialTheme.colorScheme.secondary,
                        cursorColor = MaterialTheme.colorScheme.onSecondary,
                        focusedIndicatorColor = MaterialTheme.colorScheme.onSecondary,
                        unfocusedIndicatorColor = MaterialTheme.colorScheme.onSecondary,
                        focusedLabelColor = MaterialTheme.colorScheme.onSecondary,
                        unfocusedLabelColor = MaterialTheme.colorScheme.onSecondary
                    )
                )
            }


            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Button(
                    onClick = {
                        if (khitmahTitle.isNotBlank()) {
                            onConfirm(khitmahTitle)
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth(0.6f)
                        .padding(vertical = 2.dp)
                ) {
                    Text(
                        text = "إضافة",
                        style = MaterialTheme.typography.bodyMedium,
                    )
                }

                Button(
                    onClick = { onDismiss() },
                    modifier = Modifier
                        .fillMaxWidth(0.6f)
                        .padding(vertical = 2.dp)
                ) {
                    Text(
                        text = "إلغاء",
                        style = MaterialTheme.typography.bodyMedium,
                    )
                }
            }

        }
    }
}