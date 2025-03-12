package com.ottrojja.composables

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.ottrojja.R
import com.ottrojja.classes.Screen


@Composable
fun BottomNavigation(
    navController: NavController,
    currentRoute: String,
    modifier: Modifier = Modifier
) {
    val separatorColor= MaterialTheme.colorScheme.secondary;

    Row(
        horizontalArrangement = Arrangement.SpaceEvenly,
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.background)
            .horizontalScroll(rememberScrollState())
            .drawBehind {
                val strokeWidth = 1.dp.toPx()
                drawLine(
                    color = separatorColor,
                    start = Offset(0f, 0f),
                    end = Offset(size.width, 0f),
                    strokeWidth = strokeWidth
                )
            }
            .padding(8.dp)

    ) {
        BottomNavigationOption(
            optionText = "المصحف",
            isCurrent = Screen.MainScreen.route == currentRoute,
            onClick = { navController.navigate(Screen.MainScreen.route) },
            iconId = R.drawable.chapters_open,
            alternateIcon = R.drawable.chapters_closed
        )

        BottomNavigationOption(
            optionText = "الاستماع",
            isCurrent = Screen.ChaptersScreen.route == currentRoute,
            onClick = { navController.navigate(Screen.ChaptersScreen.route) },
            iconId = R.drawable.chapters_listen,
            alternateIcon = null
        )

        BottomNavigationOption(
            optionText = "الاذكار",
            isCurrent = Screen.AzkarScreen.route == currentRoute,
            onClick = { navController.navigate(Screen.AzkarScreen.route) },
            iconId = R.drawable.azkar,
            alternateIcon = null
        )

        BottomNavigationOption(
            optionText = "إشراقات",
            isCurrent = Screen.BlessingsScreen.route == currentRoute,
            onClick = { navController.navigate(Screen.BlessingsScreen.route) },
            iconId = R.drawable.twilight,
            alternateIcon = null
        )

        BottomNavigationOption(
            optionText = "المعلم",
            isCurrent = Screen.TeacherScreen.route == currentRoute,
            onClick = { navController.navigate(Screen.TeacherScreen.route) },
            iconId = R.drawable.teacher,
            alternateIcon = null
        )

        BottomNavigationOption(
            optionText = "المسبحة",
            isCurrent = Screen.TasbeehScreen.route == currentRoute,
            onClick = { navController.navigate(Screen.TasbeehScreen.route) },
            iconId = R.drawable.pin,
            alternateIcon = null
        )

        /*BottomNavigationOption(
            optionText = "صلاتي",
            isCurrent = Screen.PrayerScreen.route == currentRoute,
            onClick = { navController.navigate(Screen.PrayerScreen.route) },
            iconId = R.drawable.prayer_times,
            alternateIcon = null
        )*/

        BottomNavigationOption(
            optionText = "الختمات",
            isCurrent = Screen.KhitmahListScreen.route == currentRoute,
            onClick = { navController.navigate(Screen.KhitmahListScreen.route) },
            iconId = R.drawable.khitmah,
            alternateIcon = null
        )

        BottomNavigationOption(
            optionText = "المرجعيات",
            isCurrent = Screen.BookmarksScreen.route == currentRoute,
            onClick = { navController.navigate(Screen.BookmarksScreen.route) },
            iconId = R.drawable.bookmarks,
            alternateIcon = null
        )

        BottomNavigationOption(
            optionText = "الاعدادات",
            isCurrent = Screen.SettingsScreen.route == currentRoute,
            onClick = { navController.navigate(Screen.SettingsScreen.route) },
            iconId = R.drawable.settings,
            alternateIcon = null
        )
    }
}

@Composable
fun BottomNavigationOption(
    optionText: String,
    isCurrent: Boolean,
    onClick: () -> Unit,
    iconId: Int,
    alternateIcon: Int? // used when route is not current
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
        modifier = Modifier
            .clickable {
                if (!isCurrent) {
                    onClick()
                }
            }
            .padding(6.dp, 2.dp)
    ) {
        Image(
            painter = painterResource(id = if (isCurrent) iconId else if (alternateIcon != null) alternateIcon else iconId),
            contentDescription = "",
            colorFilter = if (isCurrent) ColorFilter.tint(
                Color.Black
            ) else ColorFilter.tint(Color(0xFF9E9E9E))
        )
        Text(
            text = optionText,
            style = MaterialTheme.typography.bodySmall,
            color = if (isCurrent) Color.Black else Color(
                0xFF9E9E9E
            )
        )
    }
}