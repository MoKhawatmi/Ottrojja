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
import com.google.firebase.Firebase
import com.google.firebase.FirebaseApp
import com.google.firebase.storage.StorageReference
import com.google.firebase.storage.storage
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.ottrojja.classes.Helpers
import com.ottrojja.classes.QuranPage
import com.ottrojja.classes.QuranRepository
import com.ottrojja.classes.QuranStore
import com.ottrojja.screens.azkarScreen.AzkarStore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.File
import java.io.IOException
import java.nio.charset.Charset

class LoadingScreenViewModel(private val repository: QuranRepository, application: Application) :
    AndroidViewModel(application) {
    val context = application.applicationContext;
    var loadingFile = true;
    val jsonParser = JsonParser(context)


    val sharedPreferences: SharedPreferences =
        application.getSharedPreferences("ottrojja", Context.MODE_PRIVATE)
    val quranFileCreateTime = sharedPreferences.getLong("quranFileCreateTime", 0L)


    private var _loaded = mutableStateOf(false)
    var loaded: Boolean
        get() = _loaded.value
        set(value: Boolean) {
            _loaded.value = value
        }


    init {
        FirebaseApp.initializeApp(context)

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

        val quranFile = File(context.filesDir, "quran.json")

        if (!Helpers.checkNetworkConnectivity(context)) {
            handleLocalQuranFile(quranFile)
        } else {
            downloadAndUpdateQuranFile(quranFile)
        }

        /*
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
                */

        viewModelScope.launch(Dispatchers.IO) {

            if (repository.getChaptersCount() != 114) {
                jsonParser.parseJsonArrayFileChapters("chaptersList.json")
                    ?.let {
                        try {
                            repository.insertAllChapters(it)
                        } catch (e: Exception) {
                            e.printStackTrace()
                        }
                    }
            }

            if (repository.getPartsCount() != 30) {
                jsonParser.parseJsonArrayFileParts("partsList.json")
                    ?.let {
                        try {
                            repository.insertAllParts(it)
                        } catch (e: Exception) {
                            e.printStackTrace()
                        }
                    }
            }

            if (repository.getE3rabsCount() != 6236) {
                jsonParser.parseJsonArrayFileE3rab("e3rab.json")?.let {
                    try {
                        repository.insertAllE3rabData(it)
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }
            }


            if (repository.getTafseersCount() != 6236 * 7) {
                //insert the available tafseer files to db
                jsonParser.parseJsonArrayFileTafseer("saadi.json")?.let {
                    try {
                        repository.insertAllTafseerData(it)
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }

                jsonParser.parseJsonArrayFileTafseer("baghawy.json")?.let {
                    try {
                        repository.insertAllTafseerData(it)
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }

                jsonParser.parseJsonArrayFileTafseer("muyassar.json")?.let {
                    try {
                        repository.insertAllTafseerData(it)
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }

                jsonParser.parseJsonArrayFileTafseer("katheer.json")?.let {
                    try {
                        repository.insertAllTafseerData(it)
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }

                jsonParser.parseJsonArrayFileTafseer("waseet.json")?.let {
                    try {
                        repository.insertAllTafseerData(it)
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }

                jsonParser.parseJsonArrayFileTafseer("jalalayn.json")?.let {
                    try {
                        repository.insertAllTafseerData(it)
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }

                jsonParser.parseJsonArrayFileTafseer("qortoby.json")?.let {
                    try {
                        repository.insertAllTafseerData(it)
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }
            }
        }
    }

    private fun handleLocalQuranFile(quranFile: File) {
        if (quranFile.exists()) {
            try {
                parseAndSetQuranData(quranFile)
            } catch (e: Exception) {
                e.printStackTrace()
                Log.d("E", "Faulty File")
                quranFile.delete()
                setQuranDataFromLocalFile()
            }
        } else {
            setQuranDataFromLocalFile()
        }
        loadingFile = false
        _loaded.value = true
    }

    private fun parseAndSetQuranData(file: File, updatingData: Boolean = false) {
        jsonParser.parseJsonArrayFileFromFilesDir(context, file.name)?.let {
            if (it.isNotEmpty()) {
                insertQuranDataInDb(it, updatingData)
            } else {
                throw Exception("Faulty File")
            }
        }
    }

    private fun downloadAndUpdateQuranFile(quranFile: File) {
        val storage = Firebase.storage
        val storageRef = storage.reference.child("/quran.json")

        loadingFile = true
        storageRef.metadata.addOnSuccessListener { metadata ->
            println("success meta")
            if (metadata.creationTimeMillis > quranFileCreateTime) {
                updateFileCreationTime(metadata.creationTimeMillis)
                downloadFile(storageRef, quranFile)
            } else {
                useLocalQuranFile(quranFile)
            }
        }.addOnFailureListener { exception ->
            handleDownloadError(exception)
        }
    }

    private fun updateFileCreationTime(timeMillis: Long) {
        sharedPreferences.edit().putLong("quranFileCreateTime", timeMillis).apply()
    }

    private fun downloadFile(storageRef: StorageReference, quranFile: File) {
        storageRef.getFile(quranFile)
            .addOnSuccessListener {
                println("success download")
                parseAndSetQuranData(quranFile, true)
                loadingFile = false
            }
            .addOnFailureListener { exception ->
                handleDownloadError(exception)
            }
    }

    private fun useLocalQuranFile(quranFile: File) {
        try {
            parseAndSetQuranData(quranFile)
        } catch (e: Exception) {
            Log.d("E", "Error using local file: ${e.message}")
            setQuranDataFromLocalFile()
        } finally {
            loadingFile = false
            _loaded.value = true
        }
    }

    private fun handleDownloadError(exception: Exception) {
        Log.d("E", "Error downloading JSON file: $exception")
        setQuranDataFromLocalFile()
        loadingFile = false
    }

    fun setQuranDataFromLocalFile() {
        println("use assets file")
        try {
            val jsonParser = JsonParser(context)
            jsonParser.parseJsonArrayFile("quran.json")
                ?.let { insertQuranDataInDb(it, false) }
        } catch (e: Exception) {
            println("assets file failed")
            e.printStackTrace()
        }
    }

    private fun insertQuranDataInDb(quranData: List<QuranPage>, updatingData: Boolean) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                if (!updatingData) {
                    if (repository.getPagesCount() == 604) {
                        _loaded.value = true;
                        return@launch
                    }
                }
                repository.insertAllPages(quranData)
                _loaded.value = true;
            } catch (e: Exception) {
                e.printStackTrace()
            }
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
