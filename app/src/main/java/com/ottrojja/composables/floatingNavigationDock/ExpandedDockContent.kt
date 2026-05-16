package com.ottrojja.composables.floatingNavigationDock

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.VerticalDivider
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.ottrojja.R
import com.ottrojja.classes.Screen

@Composable
fun ExpandedDockContent(
    navController: NavController,
    currentRoute: String?,
    onItemClick: (String) -> Unit
) {
    val color = MaterialTheme.colorScheme.primary;

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(12.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Row(modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            DockItem(
                optionText = "إشراقات",
                isCurrent = Screen.BlessingsScreen.route == currentRoute,
                onClick = {
                    onItemClick(Screen.BlessingsScreen.route)
                },
                iconId = R.drawable.twilight,
                alternateIcon = null,
                modifier = Modifier.weight(1f)
            )

            DockVerticalDivider()

            DockItem(
                optionText = "القبلة",
                isCurrent = Screen.QiblaScreen.route == currentRoute,
                onClick = {
                    onItemClick(Screen.QiblaScreen.route)
                },
                iconId = R.drawable.qibla,
                alternateIcon = null,
                modifier = Modifier.weight(1f)
            )

            DockVerticalDivider()

            DockItem(
                optionText = "المعلم",
                isCurrent = Screen.TeacherScreen.route == currentRoute,
                onClick = {
                    onItemClick(Screen.TeacherScreen.route)
                },
                iconId = R.drawable.teacher,
                alternateIcon = null,
                modifier = Modifier.weight(1f)
            )
        }

        HorizontalDivider(modifier = Modifier.padding(horizontal = 24.dp, vertical = 10.dp), color = MaterialTheme.colorScheme.outlineVariant)

        Row(modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            DockItem(
                optionText = "الختمات",
                isCurrent = Screen.KhitmahListScreen.route == currentRoute,
                onClick = {
                    onItemClick(Screen.KhitmahListScreen.route)
                },
                iconId = R.drawable.khitmah,
                alternateIcon = null,
                modifier = Modifier.weight(1f)
            )

            DockVerticalDivider()

            DockItem(
                optionText = "المذكر",
                isCurrent = Screen.ReminderScreen.route == currentRoute,
                onClick = {
                    onItemClick(Screen.ReminderScreen.route)
                },
                iconId = R.drawable.reminder,
                alternateIcon = null,
                modifier = Modifier.weight(1f)
            )

            DockVerticalDivider()

            DockItem(
                optionText = "المرجعيات",
                isCurrent = Screen.BookmarksScreen.route == currentRoute,
                onClick = {
                    onItemClick(Screen.BookmarksScreen.route)
                },
                iconId = R.drawable.bookmarks,
                alternateIcon = null,
                modifier = Modifier.weight(1f)
            )
        }

        HorizontalDivider(modifier = Modifier.padding(horizontal = 24.dp, vertical = 10.dp), color = MaterialTheme.colorScheme.outlineVariant)

        Row(modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            DockItem(
                optionText = "الاعدادات",
                isCurrent = Screen.SettingsScreen.route == currentRoute,
                onClick = {
                    onItemClick(Screen.SettingsScreen.route)
                },
                iconId = R.drawable.settings,
                alternateIcon = null,
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
fun DockVerticalDivider() {
    val thickness = 1.dp;
    val color = MaterialTheme.colorScheme.outlineVariant
    Canvas(Modifier
        .height(36.dp)
        .width(thickness)
    ) {
        drawLine(
            color = color,
            strokeWidth = thickness.toPx(),
            start = Offset(thickness.toPx() / 2, 0f),
            end = Offset(thickness.toPx() / 2, size.height),
        )
    }
}