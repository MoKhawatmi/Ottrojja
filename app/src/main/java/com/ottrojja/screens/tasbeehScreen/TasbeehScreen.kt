package com.ottrojja.screens.tasbeehScreen

import android.app.AlertDialog
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.RadioButtonChecked
import androidx.compose.material.icons.filled.RadioButtonUnchecked
import androidx.compose.material.icons.filled.Replay
import androidx.compose.material.icons.filled.TouchApp
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.ElevatedButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.ottrojja.R
import com.ottrojja.classes.Tasabeeh
import com.ottrojja.composables.Header
import com.ottrojja.composables.ListHorizontalDivider
import com.ottrojja.composables.OttrojjaTabs
import com.ottrojja.composables.OttrojjaTopBar
import com.ottrojja.ui.theme.timeNormal

@Composable
fun TasbeehScreen(
    tasbeehScreenViewModel: TasbeehScreenViewModel = viewModel()
) {

    Column() {
        Header()
        OttrojjaTopBar{
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


        if (tasbeehScreenViewModel.selectedTab == TasbeehTab.المسبحة) {
            CounterContent(
                tasbeehCount = tasbeehScreenViewModel.tasbeehCount,
                increaseCount = { tasbeehScreenViewModel.increaseTasbeeh() },
                resetCount = { tasbeehScreenViewModel.resetTasbeeh() }
            )
        } else if (tasbeehScreenViewModel.selectedTab == TasbeehTab.الاذكار) {
            TasabeehList(
                tasabeeh = tasbeehScreenViewModel.tasabeeh,
                updateExpanded = { item -> tasbeehScreenViewModel.updateExpanded(item) })
        }

    }
}

@Composable
fun CounterContent(
    tasbeehCount: Int,
    increaseCount: () -> Unit,
    resetCount: () -> Unit
) {
    val context = LocalContext.current

    fun confirmTasbeehReset() {
        val builder: AlertDialog.Builder = AlertDialog.Builder(context)
        builder
            .setMessage("هل انت متأكد من إعادة العدد الى البداية؟")
            .setPositiveButton("نعم") { dialog, which ->
                resetCount()
            }
            .setNegativeButton("إلغاء") { dialog, which ->
                dialog.dismiss()
            }

        val dialog: AlertDialog = builder.create()
        dialog.show()
    }


    Row(
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .padding(0.dp, 18.dp, 0.dp, 24.dp)
    ) {
        Row(
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .padding(8.dp)
                .fillMaxWidth(0.8F)
                .border(
                    BorderStroke(4.dp, color = MaterialTheme.colorScheme.primary),
                    shape = RoundedCornerShape(12.dp)
                )
                .clip(RoundedCornerShape(12))
                .padding(16.dp)

        ) {
            Text(
                text = "${tasbeehCount}",
                style = MaterialTheme.typography.bodyLarge,
                fontFamily = timeNormal,
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.primary,
                fontSize = 42.sp,
            )
        }
    }

    Row(
        horizontalArrangement = Arrangement.End,
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .padding(0.dp, 0.dp, 10.dp, 0.dp)
            .fillMaxWidth()
    ) {
        Button(
            onClick = { confirmTasbeehReset() },
            modifier = Modifier
                .size(50.dp),
            shape = CircleShape,
            contentPadding = PaddingValues(8.dp)
        ) {
            Icon(
                Icons.Default.Replay,
                contentDescription = "Reset",
                tint = MaterialTheme.colorScheme.onPrimary,
                modifier = Modifier.fillMaxSize()
            )
        }
    }

    Row(
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.fillMaxWidth()
    ) {
        ElevatedButton(
            onClick = { increaseCount() },
            elevation = ButtonDefaults.elevatedButtonElevation(2.dp, 2.dp, 2.dp, 2.dp, 2.dp),
            contentPadding = PaddingValues(0.dp),
            shape = CircleShape,
            modifier = Modifier
                .padding(0.dp, 2.dp, 0.dp, 12.dp)
                .fillMaxWidth(0.9F)
                .fillMaxHeight(0.7F)
                .padding(24.dp, 24.dp)
                .clip(CircleShape)
        ) {
            Icon(
                Icons.Default.TouchApp,
                contentDescription = "Counter",
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(128.dp)
            )
        }
    }
}

@Composable
fun TasabeehList(tasabeeh: MutableList<Tasabeeh>, updateExpanded: (Tasabeeh) -> Unit) {
    LazyColumn(
        modifier = Modifier
            .fillMaxWidth()
            .fillMaxHeight(0.9f)
            .background(MaterialTheme.colorScheme.tertiary)
    ) {
        items(tasabeeh) { item ->
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.background)
                    .clickable {
                        updateExpanded(item)
                    }
                    .padding(8.dp, 16.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Top
                ) {
                    Text(
                        text = item.ziker,
                        color = Color.Black,
                        style = MaterialTheme.typography.bodyMedium,
                        textAlign = TextAlign.Right,
                        modifier = Modifier.fillMaxWidth(0.9f),
                        lineHeight = 26.sp
                    )

                    Image(
                        painter = painterResource(id = R.drawable.more_vert),
                        contentDescription = "",
                        modifier = Modifier.clickable {
                            updateExpanded(item)
                        }
                    )
                }
                androidx.compose.animation.AnimatedVisibility(
                    visible = item.expanded,
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(MaterialTheme.colorScheme.background)
                            .padding(4.dp, 8.dp)
                    ) {
                        Text(
                            text = item.benefit,
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color.Black,
                            textAlign = TextAlign.Right,
                            lineHeight = 26.sp
                        )
                    }
                }
            }
            ListHorizontalDivider()
        }
    }
}