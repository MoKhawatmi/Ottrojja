package com.ottrojja.composables

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardDoubleArrowUp
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.SheetValue
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import androidx.navigation.NavController
import androidx.navigation.compose.currentBackStackEntryAsState
import com.ottrojja.R
import com.ottrojja.classes.Screen


@Composable
fun BottomNavigation(
    navController: NavController,
    onMoreClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val separatorColor = MaterialTheme.colorScheme.secondary;
    val currentBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = currentBackStackEntry?.destination?.route


    Box(modifier = Modifier
        .fillMaxWidth()
        .background(MaterialTheme.colorScheme.background)
        .drawBehind {
            val strokeWidth = 2.dp.toPx()
            drawLine(
                color = separatorColor,
                start = Offset(0f, 0f),
                end = Offset(size.width, 0f),
                strokeWidth = strokeWidth
            )
        }
        .padding(start = 4.dp, end = 4.dp, top = 12.dp, bottom = 4.dp)
    ) {

        Row(modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(modifier = Modifier.weight(1f),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                BottomNavigationOption(
                    optionText = "المصحف",
                    isCurrent = Screen.MainScreen.route == currentRoute,
                    onClick = {
                        navController.navigate(Screen.MainScreen.route);
                    },
                    iconId = R.drawable.chapters_open,
                    alternateIcon = R.drawable.chapters_closed,
                    modifier = Modifier.weight(1f)

                )

                BottomNavigationOption(
                    optionText = "الاستماع",
                    isCurrent = Screen.ListeningScreen.route == currentRoute,
                    onClick = {
                        navController.navigate(Screen.ListeningScreen.route);
                    },
                    iconId = R.drawable.chapters_listen,
                    alternateIcon = null,
                    modifier = Modifier.weight(1f)

                )
            }

            Spacer(modifier = Modifier.width(50.dp))

            Row(modifier = Modifier.weight(1f),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                BottomNavigationOption(
                    optionText = "الاذكار",
                    isCurrent = Screen.AzkarMain.route == currentRoute,
                    onClick = { navController.navigate(Screen.AzkarMain.route); },
                    iconId = R.drawable.azkar,
                    alternateIcon = null,
                    modifier = Modifier.weight(1f)

                )

                BottomNavigationOption(
                    optionText = "المسبحة",
                    isCurrent = Screen.TasbeehScreen.route == currentRoute,
                    onClick = {
                        navController.navigate(Screen.TasbeehScreen.route);
                    },
                    iconId = R.drawable.pin,
                    alternateIcon = null,
                    modifier = Modifier.weight(1f)
                )
            }
        }


        FloatingActionButton(
            onClick = { onMoreClick() },
            modifier = Modifier
                .align(Alignment.TopCenter)
                .offset(y = (-12).dp)
                .zIndex(1f)
                .border(
                    width = 2.dp,
                    color = MaterialTheme.colorScheme.secondary,
                    shape = CircleShape
                ),
            shape = CircleShape,
            containerColor = MaterialTheme.colorScheme.primary,
            contentColor = Color.White,

            ) {
            Icon(Icons.Default.KeyboardDoubleArrowUp, contentDescription = "more navigation items",
                modifier = Modifier.offset(y = (-3).dp)
            )
        }
    }
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NavigationModalBottomSheet(navController: NavController ,onDismissRequest: () -> Unit, onItemClick:(String)->Unit) {
    val currentBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = currentBackStackEntry?.destination?.route


    val sheetState = rememberModalBottomSheetState(
        skipPartiallyExpanded = true,
        confirmValueChange = { newValue ->
            // Prevent expansion to the Expanded state
            newValue != SheetValue.Expanded
        }
    )


    ModalBottomSheet(
        onDismissRequest = { onDismissRequest() },
        sheetState = sheetState,
        dragHandle = null // Optional: removes the drag handle
    ) {
        Column(modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.background)
            .padding(vertical = 20.dp, horizontal = 12.dp)
        ) {
            FlowRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalArrangement = Arrangement.spacedBy(12.dp),
                maxItemsInEachRow = 3,
            ) {
                BottomNavigationOption(
                    optionText = "إشراقات",
                    isCurrent = Screen.BlessingsScreen.route == currentRoute,
                    onClick = {
                        onItemClick(Screen.BlessingsScreen.route)
                    },
                    iconId = R.drawable.twilight,
                    alternateIcon = null,
                    modifier = Modifier.weight(1f)

                )

                BottomNavigationOption(
                    optionText = "القبلة",
                    isCurrent = Screen.QiblaScreen.route == currentRoute,
                    onClick = {
                        onItemClick(Screen.QiblaScreen.route)
                    },
                    iconId = R.drawable.qibla,
                    alternateIcon = null,
                    modifier = Modifier.weight(1f)

                )

                BottomNavigationOption(
                    optionText = "المعلم",
                    isCurrent = Screen.TeacherScreen.route == currentRoute,
                    onClick = {
                        onItemClick(Screen.TeacherScreen.route)
                    },
                    iconId = R.drawable.teacher,
                    alternateIcon = null,
                    modifier = Modifier.weight(1f)

                )

                BottomNavigationOption(
                    optionText = "الختمات",
                    isCurrent = Screen.KhitmahListScreen.route == currentRoute,
                    onClick = {
                        onItemClick(Screen.KhitmahListScreen.route)
                    },
                    iconId = R.drawable.khitmah,
                    alternateIcon = null,
                    modifier = Modifier.weight(1f)

                )

                BottomNavigationOption(
                    optionText = "المرجعيات",
                    isCurrent = Screen.BookmarksScreen.route == currentRoute,
                    onClick = {
                        onItemClick(Screen.BookmarksScreen.route)
                    },
                    iconId = R.drawable.bookmarks,
                    alternateIcon = null,
                    modifier = Modifier.weight(1f)

                )

                BottomNavigationOption(
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

}


@Composable
fun BottomNavigationOption(
    optionText: String,
    isCurrent: Boolean,
    onClick: () -> Unit,
    iconId: Int,
    alternateIcon: Int?, // used when route is not current
    modifier: Modifier = Modifier,
    overrideColor: Color? = null
) {
    val unselectedColor = Color(0xFF9E9E9E);

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
        modifier = modifier
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = {
                    if (!isCurrent) {
                        onClick()
                    }
                }
            )
            .padding(6.dp, 2.dp)
    ) {
        Image(
            painter = painterResource(
                id = if (isCurrent) iconId else if (alternateIcon != null) alternateIcon else iconId
            ),
            contentDescription = "",
            colorFilter = if (overrideColor != null) ColorFilter.tint(overrideColor
            ) else if (isCurrent) ColorFilter.tint(Color.Black) else ColorFilter.tint(
                unselectedColor
            )
        )
        Text(
            text = optionText,
            style = MaterialTheme.typography.bodySmall,
            color = if (overrideColor != null) overrideColor else if (isCurrent) Color.Black else unselectedColor
        )
    }
}

/*BottomNavigationOption(
    optionText = "صلاتي",
    isCurrent = Screen.PrayerScreen.route == currentRoute,
    onClick = { navController.navigate(Screen.PrayerScreen.route) },
    iconId = R.drawable.prayer_times,
    alternateIcon = null
)*/
