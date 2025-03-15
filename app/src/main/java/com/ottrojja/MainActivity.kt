package com.ottrojja

import android.Manifest
import android.content.pm.PackageManager.PERMISSION_GRANTED
import android.content.res.Configuration
import android.os.Build
import android.os.Bundle
import android.view.View
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
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
import androidx.compose.ui.Modifier
import androidx.core.content.ContextCompat
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.room.Room
import com.ottrojja.classes.Helpers
import com.ottrojja.classes.QuranRepository
import com.ottrojja.classes.Screen
import com.ottrojja.composables.BottomNavigation
import com.ottrojja.room.database.MIGRATION_1_2
import com.ottrojja.room.database.MIGRATION_2_3
import com.ottrojja.room.database.MIGRATION_3_4
import com.ottrojja.room.database.MIGRATION_4_5
import com.ottrojja.room.database.QuranDatabase
import com.ottrojja.ui.theme.OttrojjaAppTheme
import java.util.Locale

class MainActivity : ComponentActivity() {

    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    private fun isNotificationPermissionGranted() =
        ContextCompat.checkSelfPermission(
            this,
            Manifest.permission.POST_NOTIFICATIONS
        ) == PERMISSION_GRANTED


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        actionBar?.hide()

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
        } else {
        }


        val db = Room.databaseBuilder(
            application,
            QuranDatabase::class.java, "QuranDB"
        ).addMigrations(MIGRATION_1_2, MIGRATION_2_3, MIGRATION_3_4, MIGRATION_4_5)
            //.fallbackToDestructiveMigration()
            .build()
        val quranRepository = QuranRepository(db.quranDao(), db.khitmahDao(), db.tasabeehDao())

        setContent {
            OttrojjaAppTheme {
                // A surface container using the 'background' color from the theme
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    val navController = rememberNavController()
                    val currentBackStackEntry by navController.currentBackStackEntryAsState()
                    val currentRoute = currentBackStackEntry?.destination?.route
                    val bottomBarRoutes = listOf(Screen.MainScreen.route, Screen.AzkarScreen.route,
                        Screen.TeacherScreen.route, Screen.ChaptersScreen.route,
                        Screen.TasbeehScreen.route, Screen.BookmarksScreen.route,
                        Screen.KhitmahListScreen.route, Screen.SettingsScreen.route,
                        Screen.BlessingsScreen.route
                    )
                    Scaffold(
                        bottomBar = {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .animateContentSize() // Smoothly adjust height
                            ) {
                                if (currentRoute in bottomBarRoutes) {
                                    BottomNavigation(navController)
                                }
                            }
                        }
                    ) { innerPadding ->
                        NavGraph(navController = navController,
                            repository = quranRepository,
                            modifier = Modifier.padding(innerPadding)
                        )
                    }
                }
            }
        }
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        println("configured");
    }

    override fun onDestroy() {
        super.onDestroy()
        try {
            println("terminating services on destroy")
            Helpers.terminateAllServices(this)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}
