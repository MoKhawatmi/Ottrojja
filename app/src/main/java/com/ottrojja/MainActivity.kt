package com.ottrojja

import android.Manifest
import android.content.Intent
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
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.core.content.ContextCompat
import androidx.navigation.compose.rememberNavController
import androidx.room.Room
import com.ottrojja.classes.MediaPlayerService
import com.ottrojja.classes.QuranRepository
import com.ottrojja.room.MIGRATION_1_2
import com.ottrojja.room.MIGRATION_2_3
import com.ottrojja.room.QuranDatabase
import com.ottrojja.ui.theme.TestAppTheme
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
        ).addMigrations(MIGRATION_1_2, MIGRATION_2_3)
         .fallbackToDestructiveMigration()
         .build()
        val quranRepository = QuranRepository(db.quranDao())

        setContent {
            TestAppTheme {
                // A surface container using the 'background' color from the theme
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    val navController = rememberNavController()
                    NavGraph(navController = navController, repository = quranRepository)
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
            println("terminating service on destroy")
            val stopServiceIntent = Intent(this, MediaPlayerService::class.java)
            //stopServiceIntent.setAction("STOP")
            stopServiceIntent.setAction("TERMINATE")
            startService(stopServiceIntent)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}
