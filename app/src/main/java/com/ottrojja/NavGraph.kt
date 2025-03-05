package com.ottrojja

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier

import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.ottrojja.classes.QuranRepository
import com.ottrojja.classes.Screen
import com.ottrojja.composables.BottomNavigation
import com.ottrojja.screens.BookmarksScreen.BookmarksScreen
import com.ottrojja.screens.teacherScreen.TeacherScreen
import com.ottrojja.screens.azkarScreen.AzkarScreen
import com.ottrojja.screens.blessingsScreen.BlessingsScreen
import com.ottrojja.screens.chaptersScreen.ChaptersScreen
import com.ottrojja.screens.loadingScreen.LoadingScreen
import com.ottrojja.screens.mainScreen.MainScreen
import com.ottrojja.screens.prayerScreen.PrayerScreen
import com.ottrojja.screens.quranScreen.QuranScreen
import com.ottrojja.screens.settingsScreen.SettingsScreen
import com.ottrojja.screens.tasbeehScreen.TasbeehScreen
import com.ottrojja.screens.zikrScreen.ZikrScreen

@Composable
fun NavGraph(navController: NavHostController, repository: QuranRepository) {
    NavHost(
        navController = navController,
        startDestination = Screen.LoadingScreen.route
    )
    {
        composable(route = Screen.MainScreen.route) {
            Box() {
                MainScreen(
                    navController = navController,
                    modifier = Modifier.align(Alignment.TopCenter),
                    repository = repository
                )
                BottomNavigation(
                    navController,
                    Screen.MainScreen.route,
                    modifier = Modifier.align(Alignment.BottomCenter)
                )
            }
        }

        composable(route = Screen.LoadingScreen.route) {
            LoadingScreen(navController, repository)
        }

        composable(route = Screen.AzkarScreen.route) {
            Box() {
                AzkarScreen(navController, modifier = Modifier.align(Alignment.TopCenter), repository)
                BottomNavigation(
                    navController,
                    Screen.AzkarScreen.route,
                    modifier = Modifier.align(Alignment.BottomCenter)
                )
            }
        }

        composable(route = Screen.TeacherScreen.route) {
            Box() {
                TeacherScreen(modifier = Modifier.align(Alignment.TopCenter), repository)
                BottomNavigation(
                    navController,
                    Screen.TeacherScreen.route,
                    modifier = Modifier.align(Alignment.BottomCenter)
                )
            }
        }
        composable(route = Screen.ChaptersScreen.route) {
            Box() {
                ChaptersScreen(modifier = Modifier.align(Alignment.TopCenter), repository)
                BottomNavigation(
                    navController,
                    Screen.ChaptersScreen.route,
                    modifier = Modifier.align(Alignment.BottomCenter)
                )
            }
        }
        composable(route = Screen.TasbeehScreen.route) {
            Box() {
                TasbeehScreen(modifier = Modifier.align(Alignment.TopCenter))
                BottomNavigation(
                    navController,
                    Screen.TasbeehScreen.route,
                    modifier = Modifier.align(Alignment.BottomCenter)
                )
            }
        }
        composable(route = Screen.PrayerScreen.route) {
            Box() {
                PrayerScreen(modifier = Modifier.align(Alignment.TopCenter))
                BottomNavigation(
                    navController,
                    Screen.PrayerScreen.route,
                    modifier = Modifier.align(Alignment.BottomCenter)
                )
            }
        }

        composable(route = Screen.BookmarksScreen.route) {
            Box() {
                BookmarksScreen(navController, modifier = Modifier.align(Alignment.TopCenter))
                BottomNavigation(
                    navController,
                    Screen.BookmarksScreen.route,
                    modifier = Modifier.align(Alignment.BottomCenter)
                )
            }
        }

        composable(route = Screen.SettingsScreen.route) {
            Box() {
                SettingsScreen(modifier = Modifier.align(Alignment.TopCenter))
                BottomNavigation(
                    navController,
                    Screen.SettingsScreen.route,
                    modifier = Modifier.align(Alignment.BottomCenter)
                )
            }
        }

        composable(route = Screen.BlessingsScreen.route) {
            Box() {
                BlessingsScreen(modifier = Modifier.align(Alignment.TopCenter))
                BottomNavigation(
                    navController,
                    Screen.BlessingsScreen.route,
                    modifier = Modifier.align(Alignment.BottomCenter)
                )
            }
        }

        composable(route = Screen.QuranScreen.route, arguments = listOf(navArgument("pageNum") {
            type = NavType.StringType
        })) {
            val pageNum = requireNotNull(it.arguments).getString("pageNum")
            QuranScreen(navController, pageNum!!, repository)
        }
        composable(route = Screen.ZikrScreen.route, arguments = listOf(navArgument("zikerTitle") {
            type = NavType.StringType
        })) {
            val zikrTitle = requireNotNull(it.arguments).getString("zikerTitle")
            ZikrScreen(zikrTitle!!, navController, repository)
        }
    }
}