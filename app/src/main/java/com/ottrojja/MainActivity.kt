package com.ottrojja

import android.Manifest
import android.content.pm.PackageManager.PERMISSION_GRANTED
import android.content.res.Configuration
import android.os.Build
import android.os.Bundle
import android.view.View
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.core.content.ContextCompat
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.ottrojja.classes.Helpers
import com.ottrojja.classes.Helpers.reportException
import com.ottrojja.classes.QuranRepository
import com.ottrojja.classes.Screen
import com.ottrojja.ui.theme.OttrojjaAppTheme
import java.util.Locale
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.unit.dp
import androidx.core.graphics.toColorInt
import com.ottrojja.composables.floatingNavigationDock.FloatingNavigationDock
import com.ottrojja.room.database.DatabaseProvider
import com.ottrojja.room.repositories.ReminderRepository


class MainActivity : ComponentActivity() {

    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    private fun isNotificationPermissionGranted() =
        ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) == PERMISSION_GRANTED


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        actionBar?.hide()

        enableEdgeToEdge(
            statusBarStyle = SystemBarStyle.dark("#FF194D65".toColorInt()),
            navigationBarStyle = SystemBarStyle.dark(Color.Black.toArgb())
        )

        getResources().getConfiguration().setLayoutDirection(Locale("ar"));
        getWindow().getDecorView().setLayoutDirection(View.LAYOUT_DIRECTION_RTL);
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        val locale = Locale("ar")
        Locale.setDefault(locale)
        val config = Configuration()
        config.locale = locale
        applicationContext.resources.updateConfiguration(
            config,
            applicationContext.resources.displayMetrics
        )

        val permissionLauncher =
            registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
                if (isGranted) {
                }
            }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (isNotificationPermissionGranted()) {
            } else {
                permissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        }


        val db = DatabaseProvider.getDatabase(this)
        val quranRepository = QuranRepository(db.quranDao(), db.khitmahDao(), db.tasabeehDao())
        val reminderRepository = ReminderRepository(db.reminderDao())

        println("Version: ${BuildConfig.VERSION_NAME}")

        setContent {
            var showNavPopUp by remember { mutableStateOf(false) }

            var dockExpanded by rememberSaveable { mutableStateOf(false) }

            // IMPORTANT: this is your ONLY bottom padding source
            val dockHeightCollapsed = 78.dp


            OttrojjaAppTheme {
                // A surface container using the 'background' color from the theme
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    val navController = rememberNavController()
                    val currentBackStackEntry by navController.currentBackStackEntryAsState()
                    val currentRoute = currentBackStackEntry?.destination?.route
                    /*// show bottom bar navigation only in these routes
                    val bottomBarRoutes = listOf(Screen.MainScreen.route, Screen.AzkarMain.route,
                        Screen.TeacherScreen.route, Screen.ListeningScreen.route,
                        Screen.TasbeehScreen.route, Screen.BookmarksScreen.route,
                        Screen.KhitmahListScreen.route, Screen.SettingsScreen.route,
                        Screen.BlessingsScreen.route, Screen.QiblaScreen.route,
                        Screen.ReminderScreen.route
                    )*/
                    val currentScreen = Screen.allScreens.find {
                        Helpers.routesMatch(
                            currentRoute = currentRoute,
                            screenRoute = it.route
                        )
                    }
                    Scaffold(
                        contentWindowInsets = WindowInsets.safeDrawing
                    ) { innerPadding ->

                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(innerPadding) // keeps system insets
                        ) {

                            // 1. NAV GRAPH (NO extra padding inside screens anymore)
                            NavGraph(
                                navController = navController,
                                repository = quranRepository,
                                reminderRepository = reminderRepository,
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(bottom = if (currentScreen?.showsBottomBar == true) dockHeightCollapsed else 0.dp)
                            )

                            // 2. FLOATING DOCK OVERLAY
                            if (currentScreen?.showsBottomBar == true) {
                                FloatingNavigationDock(
                                    navController = navController,
                                    expanded = dockExpanded,
                                    onToggle = { dockExpanded = !dockExpanded }
                                )
                            }
                        }
                    }
                }
            }
        }
    }


    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        println("configuration changed");
    }

    override fun onDestroy() {
        super.onDestroy()
        try {
            println("terminating services on destroy")
            Helpers.terminateAllServices(this)
        } catch (e: Exception) {
            e.printStackTrace()
            reportException(exception = e, file = "MainActivity")
        }
    }
}


