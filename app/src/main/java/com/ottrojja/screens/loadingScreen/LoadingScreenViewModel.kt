package com.ottrojja.screens.loadingScreen

import JsonParser
import android.app.Application
import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.google.firebase.FirebaseApp
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.ottrojja.classes.Helpers
import com.ottrojja.classes.QuranPage
import com.ottrojja.classes.QuranRepository
import com.ottrojja.classes.QuranStore
import com.ottrojja.classes.Screen
import com.ottrojja.screens.azkarScreen.AzkarStore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.File
import java.io.IOException
import java.nio.charset.Charset

class LoadingScreenViewModel(repository: QuranRepository, application: Application) :
    AndroidViewModel(application) {
    val context = application.applicationContext;
    var loadingFile = true;

    private var _loaded = mutableStateOf(false)
    var loaded: Boolean
        get() = _loaded.value
        set(value: Boolean) {
            _loaded.value = value
        }


    init {
        FirebaseApp.initializeApp(context)
        val jsonParser = JsonParser(context)

        val sharedPreferences: SharedPreferences =
            application.getSharedPreferences("name", Context.MODE_PRIVATE)
        val quranFileCreateTime = sharedPreferences.getLong("quranFileCreateTime", 0L)

        jsonParser.parseJsonArrayFileChapters("chaptersList.json")
            ?.let { QuranStore.setChaptersData(it) }
        jsonParser.parseJsonArrayFileParts("partsList.json")?.let { QuranStore.setPartsData(it) }
        //jsonParser.parseJsonArrayFileTafseer("tafseer.json")?.let { QuranStore.setTafseerData(it) }
        jsonParser.parseJsonArrayFileTafseer("tafseer.json")?.let { QuranStore.tafseerData = it }
        jsonParser.parseJsonArrayFileE3rab("e3rab.json")?.let { QuranStore.setE3rabData(it) }
        jsonParser.parseJsonArrayFileAzkar("azkar.json")?.let { AzkarStore.setAzkarData(it) }


        /* val quranFile = File(context.filesDir, "quran.json")

         if (!Helpers.checkNetworkConnectivity(context)) {
             if (quranFile.exists()) {
                 try {
                     jsonParser.parseJsonArrayFileFromFilesDir(context, "quran.json")
                         ?.let {
                             if (it.size != 0) QuranStore.setQuranData(it) else throw Exception(
                                 "Faulty File"
                             )
                         }
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
                         e.printStackTrace()
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
         }*/

        var quranJson: String? = null
        try {
            val inputStream = context.assets.open("quran.json")
            val size = inputStream.available()
            val buffer = ByteArray(size)
            inputStream.read(buffer)
            inputStream.close()
            quranJson = String(buffer, Charset.defaultCharset())
        } catch (e: IOException) {
            e.printStackTrace()
        }
        val gson = Gson()
        val quranPages: List<QuranPage> =
            gson.fromJson(quranJson!!, object : TypeToken<List<QuranPage>>() {}.type)
        // val quranPages: List<QuranPage> = Json.decodeFromString(quranJson!!)

        viewModelScope.launch(Dispatchers.IO) {
            try {
                repository.insertAll(quranPages)
                _loaded.value = true;
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun setQuranDataFromLocalFile() {
        println("use assets file")
        try {
            val jsonParser = JsonParser(context)
            jsonParser.parseJsonArrayFile("quran.json")
                ?.let { QuranStore.setQuranData(it) }
        } catch (e: Exception) {
            println("assets file failed")
            e.printStackTrace()
        }
    }
}

class LoadingScreenViewModelFactory(
    private val repository: QuranRepository,
    private val application: Application
) : ViewModelProvider.AndroidViewModelFactory(application) {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(LoadingScreenViewModel::class.java)) {
            return LoadingScreenViewModel(repository, application) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
