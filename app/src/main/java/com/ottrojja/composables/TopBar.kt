package com.ottrojja.composables

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ottrojja.R
import com.ottrojja.classes.ButtonAction
import com.ottrojja.classes.ModalFormMode

@Composable
fun TopBar(customContent: Boolean = false,
           title: String = "",
           mainAction: ButtonAction? = null,
           secondaryActions: List<ButtonAction> = emptyList(),
           content: @Composable() () -> Unit = {}) {

    var expanded by remember { mutableStateOf(false) }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.primary)
            .padding(0.dp)
            .fillMaxHeight(0.1f)
    ) {
        Image(
            painter = painterResource(id = R.drawable.splash_background),
            contentDescription = "",
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .align(Alignment.Center)
                .fillMaxSize(1f)
                .fillMaxWidth(1f)
        )
        if (customContent) {
            content()
        } else {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = if (mainAction != null || secondaryActions.isNotEmpty()) Arrangement.SpaceBetween else Arrangement.Center,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(4.dp, 0.dp)
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.displayLarge.copy(fontSize = 28.sp),
                    textAlign = TextAlign.Center,
                    color = Color.White
                )

                if (mainAction != null || secondaryActions.isNotEmpty()) {
                    Row(modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        if (secondaryActions.isNotEmpty()) {
                            Column {
                                OttrojjaElevatedButton(
                                    onClick = { expanded = !expanded },
                                    icon = Icons.Default.MoreVert
                                )
                                DropdownMenu(
                                    expanded = expanded,
                                    onDismissRequest = { expanded = false }
                                ) {
                                    secondaryActions.forEachIndexed { index, it ->
                                        DropdownMenuItem(
                                            text = {
                                                Text(
                                                    it.title,
                                                    style = MaterialTheme.typography.bodyMedium,
                                                    color = MaterialTheme.colorScheme.primary
                                                )
                                            },
                                            trailingIcon = {
                                                Icon(
                                                    it.icon,
                                                    contentDescription = it.title,
                                                    tint = MaterialTheme.colorScheme.primary
                                                )
                                            },
                                            onClick = { it.action(); expanded = false; }
                                        )
                                        if (index != secondaryActions.lastIndex) {
                                            ListHorizontalDivider()
                                        }
                                    }
                                }
                            }
                        }

                        if (mainAction != null) {
                            OttrojjaElevatedButton(icon = mainAction.icon,
                                onClick = { mainAction.action() })
                        }
                    }
                }
            }
        }
    }
}