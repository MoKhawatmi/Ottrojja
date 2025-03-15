package com.ottrojja

import androidx.compose.foundation.layout.Box
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
import com.ottrojja.screens.CustomTasabeehListScreen.CustomTasabeehListScreen
import com.ottrojja.screens.teacherScreen.TeacherScreen
import com.ottrojja.screens.azkarScreen.AzkarScreen
import com.ottrojja.screens.blessingsScreen.BlessingsScreen
import com.ottrojja.screens.chaptersScreen.ChaptersScreen
import com.ottrojja.screens.khitmahListScreen.KhitmahList
import com.ottrojja.screens.khitmahScreen.KhitmahScreen
import com.ottrojja.screens.loadingScreen.LoadingScreen
import com.ottrojja.screens.mainScreen.MainScreen
import com.ottrojja.screens.quranScreen.QuranScreen
import com.ottrojja.screens.settingsScreen.SettingsScreen
import com.ottrojja.screens.tasbeehScreen.TasbeehScreen
import com.ottrojja.screens.zikrScreen.ZikrScreen

@Composable
fun NavGraph(navController: NavHostController,
             repository: QuranRepository,
             modifier: Modifier = Modifier) {
    NavHost(
        navController = navController,
        startDestination = Screen.LoadingScreen.route,
        modifier = modifier
    )
    {
        composable(route = Screen.MainScreen.route, arguments = listOf(navArgument("section") {
            type = NavType.StringType
            nullable = true
            defaultValue = null
        })) {
            val section = it.arguments?.getString("section")
            MainScreen(
                navController = navController,
                repository = repository,
                section = section ?: ""
            )
            /*Box() {
                MainScreen(
                    navController = navController,
                    repository = repository,
                    section = section ?: ""
                )
                BottomNavigation(
                    navController,
                    Screen.MainScreen.route,
                    modifier = Modifier.align(Alignment.BottomCenter)
                )
            }*/
        }

        composable(route = Screen.AzkarScreen.route) {
            AzkarScreen(
                navController,
                repository
            )
            /*Box() {
                AzkarScreen(
                    navController,
                    repository
                )
                BottomNavigation(
                    navController,
                    Screen.AzkarScreen.route,
                    modifier = Modifier.align(Alignment.BottomCenter)
                )
            }*/
        }

        composable(route = Screen.TeacherScreen.route) {
            TeacherScreen(repository)
            /*Box() {
                TeacherScreen(repository)
                BottomNavigation(
                    navController,
                    Screen.TeacherScreen.route,
                    modifier = Modifier.align(Alignment.BottomCenter)
                )
            }*/
        }
        composable(route = Screen.ChaptersScreen.route) {
            ChaptersScreen(repository)
            /*Box() {
                ChaptersScreen(repository)
                BottomNavigation(
                    navController,
                    Screen.ChaptersScreen.route,
                    modifier = Modifier.align(Alignment.BottomCenter)
                )
            }*/
        }
        composable(route = Screen.TasbeehScreen.route) {
            TasbeehScreen(navController = navController, repository = repository)
            /*Box() {
                TasbeehScreen(navController = navController, repository = repository)
                BottomNavigation(
                    navController,
                    Screen.TasbeehScreen.route,
                    modifier = Modifier.align(Alignment.BottomCenter)
                )
            }*/
        }

        composable(route = Screen.BookmarksScreen.route) {
            BookmarksScreen(
                navController = navController,
                repository = repository
            )
            /*Box() {
                BookmarksScreen(
                    navController = navController,
                    repository = repository
                )
                BottomNavigation(
                    navController,
                    Screen.BookmarksScreen.route,
                    modifier = Modifier.align(Alignment.BottomCenter)
                )
            }*/
        }

        composable(route = Screen.KhitmahListScreen.route) {
            KhitmahList(
                navController = navController,
                repository = repository
            )
            /*Box() {
                KhitmahList(
                    navController,
                    repository = repository
                )
                BottomNavigation(
                    navController,
                    Screen.KhitmahListScreen.route,
                    modifier = Modifier.align(Alignment.BottomCenter)
                )
            }*/
        }


        composable(route = Screen.SettingsScreen.route) {
            SettingsScreen()
            /*Box() {
                SettingsScreen()
                BottomNavigation(
                    navController,
                    Screen.SettingsScreen.route,
                    modifier = Modifier.align(Alignment.BottomCenter)
                )
            }*/
        }

        composable(route = Screen.BlessingsScreen.route) {
            BlessingsScreen()
            /*Box() {
                BlessingsScreen()
                BottomNavigation(
                    navController,
                    Screen.BlessingsScreen.route,
                    modifier = Modifier.align(Alignment.BottomCenter)
                )
            }*/
        }

        composable(route = Screen.LoadingScreen.route) {
            LoadingScreen(navController, repository)
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

        composable(route = Screen.KhitmahScreen.route, arguments = listOf(navArgument("id") {
            type = NavType.IntType
        })) {
            val id = requireNotNull(it.arguments).getInt("id")
            KhitmahScreen(navController, repository, id)
        }

        composable(route = Screen.CustomTasabeehListScreen.route,
            arguments = listOf(navArgument("id") {
                type = NavType.IntType
            })
        ) {
            val id = requireNotNull(it.arguments).getInt("id")
            CustomTasabeehListScreen(navController, repository, id)
        }
    }
}


/*composable(route = Screen.PrayerScreen.route) {
    Box() {
        PrayerScreen(modifier = Modifier.align(Alignment.TopCenter))
        BottomNavigation(
            navController,
            Screen.PrayerScreen.route,
            modifier = Modifier.align(Alignment.BottomCenter)
        )
    }
}*/
