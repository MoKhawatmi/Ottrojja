package com.ottrojja

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
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
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
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
import androidx.core.view.WindowCompat
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.room.Room
import com.ottrojja.classes.Helpers
import com.ottrojja.classes.Helpers.reportException
import com.ottrojja.classes.QuranRepository
import com.ottrojja.classes.Screen
import com.ottrojja.composables.BottomNavigation
import com.ottrojja.composables.NavigationModalBottomSheet
import com.ottrojja.room.database.MIGRATION_1_2
import com.ottrojja.room.database.MIGRATION_2_3
import com.ottrojja.room.database.MIGRATION_3_4
import com.ottrojja.room.database.MIGRATION_4_5
import com.ottrojja.room.database.MIGRATION_5_6
import com.ottrojja.room.database.MIGRATION_6_7
import com.ottrojja.room.database.MIGRATION_7_8
import com.ottrojja.room.database.QuranDatabase
import com.ottrojja.ui.theme.OttrojjaAppTheme
import java.util.Locale
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.safeContent
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.core.graphics.toColorInt
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat


class MainActivity : ComponentActivity() {

    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    private fun isNotificationPermissionGranted() =
        ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) == PERMISSION_GRANTED


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        actionBar?.hide()

        // WindowCompat.setDecorFitsSystemWindows(window, false)

        enableEdgeToEdge(
            statusBarStyle = SystemBarStyle.dark("#FF194D65".toColorInt()),
            navigationBarStyle = SystemBarStyle.dark(Color.Black.toArgb())
        )

        /*
        val controller = WindowInsetsControllerCompat(window, window.decorView)

        controller.show(WindowInsetsCompat.Type.systemBars())
        controller.systemBarsBehavior =
            WindowInsetsControllerCompat.BEHAVIOR_SHOW_BARS_BY_SWIPE


        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            window.attributes.layoutInDisplayCutoutMode = WindowManager.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_SHORT_EDGES
        }*/

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


        val db = Room.databaseBuilder(
            application,
            QuranDatabase::class.java, "QuranDB"
        ).addMigrations(MIGRATION_1_2, MIGRATION_2_3, MIGRATION_3_4, MIGRATION_4_5, MIGRATION_5_6,
            MIGRATION_6_7, MIGRATION_7_8
        )
            .fallbackToDestructiveMigration(false)
            .build()
        val quranRepository = QuranRepository(db.quranDao(), db.khitmahDao(), db.tasabeehDao())

        println("Version: ${BuildConfig.VERSION_NAME}")

        setContent {
            var showNavPopUp by remember { mutableStateOf(false) }

            OttrojjaAppTheme {
                // A surface container using the 'background' color from the theme
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    val navController = rememberNavController()
                    val currentBackStackEntry by navController.currentBackStackEntryAsState()
                    val currentRoute = currentBackStackEntry?.destination?.route
                    // show bottom bar navigation only in these routes
                    val bottomBarRoutes = listOf(Screen.MainScreen.route, Screen.AzkarMain.route,
                        Screen.TeacherScreen.route, Screen.ListeningScreen.route,
                        Screen.TasbeehScreen.route, Screen.BookmarksScreen.route,
                        Screen.KhitmahListScreen.route, Screen.SettingsScreen.route,
                        Screen.BlessingsScreen.route, Screen.QiblaScreen.route,
                        Screen.ReminderScreen.route
                    )
                    Scaffold(
                        contentWindowInsets = WindowInsets.safeDrawing,
                        bottomBar = {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .animateContentSize()
                                    .windowInsetsPadding(WindowInsets.navigationBars)
                            ) {
                                if (currentRoute in bottomBarRoutes) {
                                    BottomNavigation(navController,
                                        onMoreClick = { showNavPopUp = true })
                                }
                            }
                        }
                    ) { innerPadding ->
                        NavGraph(navController = navController,
                            repository = quranRepository,
                            modifier = Modifier.padding(innerPadding)
                        )
                    }

                    if (showNavPopUp == true) {
                        NavigationModalBottomSheet(navController = navController,
                            onDismissRequest = { showNavPopUp = false },
                            onItemClick = { route ->
                                navController.navigate(route);
                                showNavPopUp = false;
                            })
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


