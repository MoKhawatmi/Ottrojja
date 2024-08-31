package com.ottrojja

import JsonParser
import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageManager.PERMISSION_GRANTED
import android.content.res.Configuration
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.width
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.navigation.compose.rememberNavController
import com.ottrojja.classes.MediaPlayerService
import com.ottrojja.classes.QuranStore
import com.ottrojja.screens.azkarScreen.AzkarStore
import com.ottrojja.ui.theme.TestAppTheme
import com.google.firebase.FirebaseApp
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage
import java.io.File
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
        FirebaseApp.initializeApp(this)
        val jsonParser = JsonParser(this)
        val locale = Locale("ar")
        Locale.setDefault(locale)
        val config = Configuration()
        config.locale = locale
        applicationContext.resources.updateConfiguration(
            config,
            applicationContext.resources.displayMetrics
        )


        val sharedPreferences: SharedPreferences = getPreferences(Context.MODE_PRIVATE)
        val quranFileCreateTime = sharedPreferences.getLong("quranFileCreateTime", 0L)


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


        jsonParser.parseJsonArrayFileChapters("chaptersList.json")
            ?.let { QuranStore.setChaptersData(it) }
        jsonParser.parseJsonArrayFileParts("partsList.json")?.let { QuranStore.setPartsData(it) }
        //jsonParser.parseJsonArrayFileTafseer("tafseer.json")?.let { QuranStore.setTafseerData(it) }
        jsonParser.parseJsonArrayFileTafseer("tafseer.json")?.let { QuranStore.tafseerData = it }
        jsonParser.parseJsonArrayFileE3rab("e3rab.json")?.let { QuranStore.setE3rabData(it) }
        jsonParser.parseJsonArrayFileAzkar("azkar.json")?.let { AzkarStore.setAzkarData(it) }


        setContent {
            var loadingFile: Boolean by rememberSaveable { mutableStateOf(true) }
            val context = this;

            LaunchedEffect(Unit) {
                val quranFile = File(context.filesDir, "quran.json")

                if (!checkNetworkConnectivity(context)) {
                    if (quranFile.exists()) {
                        try {
                            jsonParser.parseJsonArrayFileFromFilesDir(context, "quran.json")
                                ?.let { if(it.size!=0) QuranStore.setQuranData(it) else throw Exception("Faulty File") }
                        } catch (e: Exception) {
                            Log.d("E", "Faulty File");
                            quranFile.delete()
                            setQuranDataFromLocalFile()
                        }
                    } else {
                        setQuranDataFromLocalFile()
                    }
                    loadingFile = false
                } else {
                    val storage = Firebase.storage
                    val storageRef = storage.reference.child("/quran.json")

                    loadingFile = true;
                    storageRef.metadata.addOnSuccessListener { metadata ->
                        // File downloaded successfully, do something with it
                        println("success meta")
                        if (metadata.creationTimeMillis > quranFileCreateTime) {
                            println("downloading file")
                            val sharedPreferences: SharedPreferences =
                                getPreferences(Context.MODE_PRIVATE)
                            val editor: SharedPreferences.Editor = sharedPreferences.edit()
                            editor.putLong("quranFileCreateTime", metadata.creationTimeMillis)
                            editor.apply()
                            try {
                                storageRef.getFile(quranFile)
                                    .addOnSuccessListener {
                                        // File downloaded successfully, do something with it
                                        jsonParser.parseJsonArrayFileFromFilesDir(
                                            context,
                                            "quran.json"
                                        )
                                            ?.let { QuranStore.setQuranData(it) }
                                        println("success download")
                                        loadingFile = false;
                                    }
                                    .addOnFailureListener { exception ->
                                        // Handle any errors that occurred during the download
                                        println("FirebaseDownload " + " Error downloading JSON file: $exception")
                                        setQuranDataFromLocalFile()
                                        loadingFile = false;
                                    }
                            } catch (e: Exception) {
                                setQuranDataFromLocalFile()
                            } finally {
                                if (QuranStore.getQuranData().size == 0) {
                                    setQuranDataFromLocalFile()
                                }
                                loadingFile = false;
                            }
                        } else {
                            println("using local file")
                            try {
                                jsonParser.parseJsonArrayFileFromFilesDir(context, "quran.json")
                                    ?.let { QuranStore.setQuranData(it) }
                            } catch (e: Exception) {
                                e.printStackTrace()
                                println("local file failed")
                                setQuranDataFromLocalFile()
                            } finally {
                                loadingFile = false;
                            }
                        }
                    }.addOnFailureListener { exception ->
                        // Handle any errors that occurred during the download
                        println("FirebaseMeta " + " Error downloading JSON file: $exception")
                        setQuranDataFromLocalFile()
                        loadingFile = false;
                    }
                }
            }



            TestAppTheme {
                // A surface container using the 'background' color from the theme
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    if (loadingFile) {
                        IndeterminateCircularIndicator()
                    } else {
                        val navController = rememberNavController()
                        NavGraph(navController = navController)
                    }
                }
            }
        }
    }


    fun checkNetworkConnectivity(context: Context): Boolean {
        val connectivityManager =
            context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            val networkCapabilities = connectivityManager.activeNetwork ?: return false
            val activeNetwork =
                connectivityManager.getNetworkCapabilities(networkCapabilities) ?: return false

            return when {
                activeNetwork.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) -> true
                activeNetwork.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) -> true
                // Other transports like Bluetooth, Ethernet, etc.
                else -> false
            }
        } else {
            // For devices with SDK < 23
            val activeNetworkInfo = connectivityManager.activeNetworkInfo
            return activeNetworkInfo != null && activeNetworkInfo.isConnected
        }
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        println("configured");
    }


    @Composable
    fun IndeterminateCircularIndicator() {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator(
                modifier = Modifier.width(64.dp),
                color = MaterialTheme.colorScheme.primary,
                trackColor = MaterialTheme.colorScheme.surfaceVariant,
            )
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        try {
            val stopServiceIntent = Intent(this, MediaPlayerService::class.java)
            //stopServiceIntent.setAction("STOP")
            stopServiceIntent.setAction("TERMINATE")
            startService(stopServiceIntent)
        } catch (e: Exception) {
            println(e)
        }

    }

    fun setQuranDataFromLocalFile(){
        println("use assets file")
        try{
            val jsonParser = JsonParser(this)
            jsonParser.parseJsonArrayFile("quran.json")
                ?.let { QuranStore.setQuranData(it) }
        }catch (e:Exception){
            println("assets file failed")
            e.printStackTrace()
        }
    }

}
