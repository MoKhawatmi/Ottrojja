package com.ottrojja.classes

sealed class Screen(val route: String) {
    object MainScreen : Screen(route = "main_screen")
    object BookmarksScreen : Screen(route = "bookmarks_screen")
    object AzkarScreen : Screen(route = "azkar_screen")
    object ChaptersScreen : Screen(route = "chapters_screen")
    object SettingsScreen : Screen(route = "settings_screen")
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
}