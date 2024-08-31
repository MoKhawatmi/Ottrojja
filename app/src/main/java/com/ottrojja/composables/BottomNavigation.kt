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
    currentRoute:String,
    modifier: Modifier = Modifier
) {
    Row(
        horizontalArrangement = Arrangement.SpaceEvenly,
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.background)
            .horizontalScroll(rememberScrollState())
            .padding(8.dp)
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier.clickable {
                if (currentRoute != Screen.MainScreen.route) {
                    navController.navigate(Screen.MainScreen.route)
                }
            }.padding(2.dp)
        ) {
            Image(
                painter = painterResource(id = if (currentRoute == Screen.MainScreen.route) R.drawable.chapters_open else R.drawable.chapters_closed),
                contentDescription = "",
                colorFilter = if (currentRoute == Screen.MainScreen.route) ColorFilter.tint(Color.Black) else ColorFilter.tint(
                    Color(0xFF9E9E9E)
                )
            )
            Text(
                text = "المصحف",
                style = MaterialTheme.typography.bodySmall,
                color = if (currentRoute == Screen.MainScreen.route) Color.Black else Color(
                    0xFF9E9E9E
                )
            )
        }
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier.clickable {
                if (currentRoute != Screen.ChaptersScreen.route) {
                    navController.navigate(Screen.ChaptersScreen.route)
                }
            }.padding(2.dp)
        ) {
            Image(
                painter = painterResource(id = R.drawable.chapters_listen),
                contentDescription = "",
                colorFilter = if (currentRoute == Screen.ChaptersScreen.route) ColorFilter.tint(
                    Color.Black
                ) else ColorFilter.tint(
                    Color(0xFF9E9E9E)
                )
            )
            Text(
                text = "الاستماع",
                style = MaterialTheme.typography.bodySmall,
                color = if (currentRoute == Screen.ChaptersScreen.route) Color.Black else Color(
                    0xFF9E9E9E
                )
            )
        }
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier.clickable {
                if (currentRoute != Screen.AzkarScreen.route) {
                    navController.navigate(Screen.AzkarScreen.route)
                }
            }.padding(2.dp)
        ) {
            Image(
                painter = painterResource(id = R.drawable.azkar),
                contentDescription = "",
                colorFilter = if (currentRoute == Screen.AzkarScreen.route) ColorFilter.tint(Color.Black) else ColorFilter.tint(
                    Color(0xFF9E9E9E)
                )
            )
            Text(
                text = "الاذكار",
                style = MaterialTheme.typography.bodySmall,
                color = if (currentRoute == Screen.AzkarScreen.route) Color.Black else Color(
                    0xFF9E9E9E
                )
            )
        }
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier.clickable {
                if (currentRoute != Screen.BookmarksScreen.route) {
                    navController.navigate(Screen.BookmarksScreen.route)
                }
            }.padding(2.dp)
        ) {
            Image(
                painter = painterResource(id = R.drawable.bookmarks),
                contentDescription = "",
                colorFilter = if (currentRoute == Screen.BookmarksScreen.route) ColorFilter.tint(
                    Color.Black
                ) else ColorFilter.tint(Color(0xFF9E9E9E))
            )
            Text(
                text = "المرجعيات",
                style = MaterialTheme.typography.bodySmall,
                color = if (currentRoute == Screen.BookmarksScreen.route) Color.Black else Color(
                    0xFF9E9E9E
                )
            )
        }
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier.clickable {
                if (currentRoute != Screen.SettingsScreen.route) {
                    navController.navigate(Screen.SettingsScreen.route)
                }
            }.padding(2.dp)
        ) {
            Image(
                painter = painterResource(id = R.drawable.settings),
                contentDescription = "",
                colorFilter = if (currentRoute == Screen.SettingsScreen.route) ColorFilter.tint(
                    Color.Black
                ) else ColorFilter.tint(Color(0xFF9E9E9E))
            )
            Text(
                text = "الاعدادات",
                style = MaterialTheme.typography.bodySmall,
                color = if (currentRoute == Screen.SettingsScreen.route) Color.Black else Color(
                    0xFF9E9E9E
                )
            )
        }
    }
}