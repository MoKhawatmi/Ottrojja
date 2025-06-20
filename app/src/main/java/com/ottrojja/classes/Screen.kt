package com.ottrojja.classes

import com.ottrojja.screens.mainScreen.BrowsingOption

sealed class Screen(val route: String) {
    object LoadingScreen : Screen(route = "loading_screen")
    object BookmarksScreen : Screen(route = "bookmarks_screen")
    object AzkarMain : Screen(route = "azkar_main")
    object AzkarScreen : Screen(route = "azkar_screen")
    object Jwam3Screen : Screen(route = "jwam3_screen")
    object GeneralSupplicationsScreen : Screen(route = "general_supplications_screen")
    object NamesOfGodScreen : Screen(route = "god_names_screen")
    object TeacherScreen : Screen(route = "teacher_screen")
    object ListeningScreen : Screen(route = "listening_screen")
    object TasbeehScreen : Screen(route = "tasbeeh_screen")
    object SettingsScreen : Screen(route = "settings_screen")
    object BlessingsScreen : Screen(route = "blessings_screen")
    object QiblaScreen : Screen(route = "qibla_screen")
    object KhitmahListScreen : Screen(route = "khitmah_list_screen")
    object MainScreen : Screen(route = "main_screen?section={section}"){
        fun invokeRoute(section: BrowsingOption?): String {
            return if (section != null) {
                "main_screen?section=${section}"
            } else {
                "main_screen"
            }
        }
    }
    object QuranScreen : Screen(route = "quran_screen/{pageNum}") {
        fun invokeRoute(pageNum: String): String {
            return this.route.replace("{pageNum}", pageNum)
        }
    }
    object ZikrScreen : Screen(route = "zikr_screen/{zikerTitle}") {
        fun invokeRoute(zikerTitle: String): String {
            return this.route.replace("{zikerTitle}", zikerTitle)
        }
    }
    object KhitmahScreen : Screen(route = "khitmah_screen/{id}") {
        fun invokeRoute(id: String): String {
            return this.route.replace("{id}", id)
        }
    }
    object CustomTasabeehListScreen : Screen(route = "tasabeeh_list_screen/{id}") {
        fun invokeRoute(id: String): String {
            return this.route.replace("{id}", id)
        }
    }
}

//object PrayerScreen : Screen(route = "prayer_screen")
