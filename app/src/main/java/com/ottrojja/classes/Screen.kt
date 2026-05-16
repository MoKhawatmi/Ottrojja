package com.ottrojja.classes

import com.ottrojja.screens.mainScreen.BrowsingOption

sealed class Screen(val route: String, val showsBottomBar: Boolean = false) {
    object LoadingScreen : Screen(route = "loading_screen", showsBottomBar = false)
    object BookmarksScreen : Screen(route = "bookmarks_screen", showsBottomBar = true)
    object AzkarMain : Screen(route = "azkar_main", showsBottomBar = true)
    object AzkarScreen : Screen(route = "azkar_screen", showsBottomBar = false)
    object Jwam3Screen : Screen(route = "jwam3_screen", showsBottomBar = false)
    object GeneralSupplicationsScreen :
        Screen(route = "general_supplications_screen", showsBottomBar = false)

    object NamesOfGodScreen : Screen(route = "god_names_screen", showsBottomBar = false)
    object TeacherScreen : Screen(route = "teacher_screen", showsBottomBar = true)
    object ListeningScreen : Screen(route = "listening_screen", showsBottomBar = true)
    object TasbeehScreen : Screen(route = "tasbeeh_screen", showsBottomBar = true)
    object SettingsScreen : Screen(route = "settings_screen", showsBottomBar = true)
    object BlessingsScreen : Screen(route = "blessings_screen", showsBottomBar = true)
    object QiblaScreen : Screen(route = "qibla_screen", showsBottomBar = true)
    object KhitmahListScreen : Screen(route = "khitmah_list_screen", showsBottomBar = true)
    object ReminderScreen : Screen(route = "reminder_screen", showsBottomBar = true)
    object MainScreen : Screen(route = "main_screen?section={section}", showsBottomBar = true) {
        fun invokeRoute(section: BrowsingOption?): String {
            return if (section != null) {
                "main_screen?section=${section}"
            } else {
                "main_screen"
            }
        }
    }

    object QuranScreen : Screen(route = "quran_screen/{pageNum}", showsBottomBar = false) {
        fun invokeRoute(pageNum: String): String {
            return this.route.replace("{pageNum}", pageNum)
        }
    }

    object ZikrScreen : Screen(route = "zikr_screen/{zikerTitle}", showsBottomBar = false) {
        fun invokeRoute(zikerTitle: String): String {
            return this.route.replace("{zikerTitle}", zikerTitle)
        }
    }

    object KhitmahScreen : Screen(route = "khitmah_screen/{id}", showsBottomBar = false) {
        fun invokeRoute(id: String): String {
            return this.route.replace("{id}", id)
        }
    }

    object CustomTasabeehListScreen :
        Screen(route = "tasabeeh_list_screen/{id}", showsBottomBar = false) {
        fun invokeRoute(id: String): String {
            return this.route.replace("{id}", id)
        }
    }

    companion object {
        val allScreens = listOf(
            LoadingScreen,
            BookmarksScreen,
            AzkarMain,
            AzkarScreen,
            Jwam3Screen,
            GeneralSupplicationsScreen,
            NamesOfGodScreen,
            TeacherScreen,
            ListeningScreen,
            TasbeehScreen,
            SettingsScreen,
            BlessingsScreen,
            QiblaScreen,
            KhitmahListScreen,
            ReminderScreen,
            MainScreen,
            QuranScreen,
            ZikrScreen,
            KhitmahScreen,
            CustomTasabeehListScreen
        )
    }

}

//object PrayerScreen : Screen(route = "prayer_screen")
