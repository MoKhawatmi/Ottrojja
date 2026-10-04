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
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.EaseIn
import androidx.compose.animation.core.EaseOut
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
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
import com.ottrojja.room.repositories.QuranRepository
import com.ottrojja.classes.Screen
import com.ottrojja.ui.theme.OttrojjaAppTheme
import java.util.Locale
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.exclude
import androidx.compose.foundation.layout.ime
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.unit.dp
import androidx.core.graphics.toColorInt
import androidx.core.view.WindowCompat
import com.ottrojja.classes.Toast.AppToastHost
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
            statusBarStyle = SystemBarStyle.dark("#FF08428E".toColorInt()),
            navigationBarStyle = SystemBarStyle.dark(Color.Black.toArgb())
        )

        WindowCompat.setDecorFitsSystemWindows(window, false)
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
            var dockExpanded by rememberSaveable { mutableStateOf(false) }

            // IMPORTANT: this is your ONLY bottom padding source
            val dockHeightCollapsed = 82.dp


            OttrojjaAppTheme {
                // A surface container using the 'background' color from the theme
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    val navController = rememberNavController()
                    val currentBackStackEntry by navController.currentBackStackEntryAsState()
                    val currentRoute = currentBackStackEntry?.destination?.route
                    val currentScreen = Screen.allScreens.find {
                        Helpers.routesMatch(
                            currentRoute = currentRoute,
                            screenRoute = it.route
                        )
                    }
                    Scaffold(
                        contentWindowInsets = WindowInsets.safeDrawing.exclude(WindowInsets.ime)
                    ) { innerPadding ->

                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(innerPadding) // keeps system insets
                        ) {

                            NavGraph(
                                navController = navController,
                                repository = quranRepository,
                                reminderRepository = reminderRepository,
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(bottom = if (currentScreen?.showsBottomBar == true) dockHeightCollapsed else 0.dp)
                            )

                            AnimatedVisibility(
                                visible = currentScreen?.showsBottomBar == true,
                                enter = slideInVertically(
                                    animationSpec = spring(
                                        dampingRatio = 0.8f,  // 0.65
                                        stiffness = 250f           // 400
                                    ),
                                    initialOffsetY = { it }  // start fully below
                                ) + fadeIn(
                                    animationSpec = tween(220, easing = EaseOut)
                                ),
                                exit = slideOutVertically(
                                    animationSpec = tween(180, easing = EaseIn),
                                    targetOffsetY = { it }
                                ) + fadeOut(
                                    animationSpec = tween(150)
                                )
                            ) {
                                FloatingNavigationDock(
                                    navController = navController,
                                    expanded = dockExpanded,
                                    onToggle = { dockExpanded = !dockExpanded }
                                )
                            }
                            AppToastHost(
                                bottomOffset = if (currentScreen?.showsBottomBar == true) {
                                    dockHeightCollapsed + 16.dp
                                } else {
                                    16.dp
                                }
                            )
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


