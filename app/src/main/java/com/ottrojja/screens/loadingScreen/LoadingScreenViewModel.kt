package com.ottrojja.screens.loadingScreen

import com.ottrojja.classes.JsonParser
import android.app.Application
import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.ottrojja.room.entities.CauseOfRevelation
import com.ottrojja.classes.Helpers
import com.ottrojja.room.entities.QuranPage
import com.ottrojja.classes.QuranRepository
import com.ottrojja.room.entities.Azkar
import com.ottrojja.screens.mainScreen.ChapterData
import com.ottrojja.screens.mainScreen.PartData
import com.ottrojja.room.entities.E3rabData
import com.ottrojja.room.entities.PageContent
import com.ottrojja.room.entities.TafseerData
import com.ottrojja.room.entities.VerseMeanings
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.File
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.TimeZone

class LoadingScreenViewModel(private val repository: QuranRepository, application: Application) :
    AndroidViewModel(application) {
    val context = application.applicationContext;
    var loadingFile = true;
    val jsonParser = JsonParser(context)
    val QURAN_FILE_URL = "https://ottrojja.fra1.cdn.digitaloceanspaces.com/quran.json";

    val sharedPreferences: SharedPreferences =
        application.getSharedPreferences("ottrojja", Context.MODE_PRIVATE)
    val quranFileCreateTime = sharedPreferences.getLong("quranFileCreateTime", 0L)
    val azkarJsonVersion = sharedPreferences.getInt("azkarJsonVersion", 0)
    val chaptersJsonVersion = sharedPreferences.getInt("chaptersJsonVersion", 0)
    val partsJsonVersion = sharedPreferences.getInt("partsJsonVersion", 0)
    val e3rabJsonVersion = sharedPreferences.getInt("e3rabJsonVersion", 0)
    val verseMeaningsJsonVersion = sharedPreferences.getInt("verseMeaningsJsonVersion", 0)
    val causesOfRevelationJsonVersion = sharedPreferences.getInt("causesOfRevelationJsonVersion", 0)
    val tafaseerJsonVersion = sharedPreferences.getInt("tafaseerJsonVersion", 0)
    val pagesContentJsonVersion = sharedPreferences.getInt("pagesContentJsonVersion", 0)

    private var _loaded = mutableStateOf(false)
    var loaded: Boolean
        get() = _loaded.value
        set(value: Boolean) {
            _loaded.value = value
        }


    init {
        viewModelScope.launch(Dispatchers.IO) {

            var versions = hashMapOf<String, Int>()

            withContext(Dispatchers.IO) {
                // fetch json file versions
                versions = jsonParser.getFilesVersions()

                println("checking leftover temp files...")
                // delete any left over temp files from variant download operations
                val dir = context.getExternalFilesDir(null)
                dir?.listFiles { file ->
                    file.name.startsWith("temp_")
                }?.forEach { tempFile ->
                    println("deleting ${tempFile.path}")
                    tempFile.delete()
                }
            }

            if (repository.getChaptersCount() != 114 || versions.get("chapters")!! > chaptersJsonVersion) {
                jsonParser.parseJsonArrayFile<ChapterData>("chaptersList.json")
                    ?.let {
                        try {
                            repository.insertAllChapters(it)
                            sharedPreferences.edit().putInt("chaptersJsonVersion",
                                versions.get("chapters")!!
                            ).apply()
                        } catch (e: Exception) {
                            e.printStackTrace()
                        }
                    }
            }

            if (repository.getPartsCount() != 30 || versions.get("parts")!! > partsJsonVersion) {
                jsonParser.parseJsonArrayFile<PartData>("parts.json")
                    ?.let {
                        try {
                            repository.insertAllParts(it)
                            sharedPreferences.edit().putInt("partsJsonVersion",
                                versions.get("parts")!!
                            ).apply()
                        } catch (e: Exception) {
                            e.printStackTrace()
                        }
                    }
            }

            if (repository.getE3rabsCount() != 6236 || versions.get("e3rab")!! > e3rabJsonVersion) {
                jsonParser.parseJsonArrayFile<E3rabData>("e3rab.json")?.let {
                    try {
                        repository.insertAllE3rabData(it)
                        sharedPreferences.edit().putInt("e3rabJsonVersion", versions.get("e3rab")!!
                        ).apply()
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }
            }

            if (versions.get("verseMeanings")!! > verseMeaningsJsonVersion) {
                jsonParser.parseJsonArrayFile<VerseMeanings>("verseMeanings.json")?.let {
                    try {
                        repository.insertVerseMeanings(it)
                        sharedPreferences.edit().putInt("verseMeaningsJsonVersion",
                            versions.get("verseMeanings")!!
                        ).apply()
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }
            }


            if (repository.getAzkarCount() == 0 || versions.get("azkar")!! > azkarJsonVersion) {
                jsonParser.parseJsonArrayFile<Azkar>("azkar.json")?.let {
                    try {
                        repository.insertAllAzkar(it)
                        sharedPreferences.edit().putInt("azkarJsonVersion", versions.get("azkar")!!
                        ).apply()
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }
            }

            if (repository.getCauseOfRevelationCount() == 0 || versions.get("causesOfRevelation"
                )!! > causesOfRevelationJsonVersion
            ) {
                jsonParser.parseJsonArrayFile<CauseOfRevelation>("causesOfRevelation.json")?.let {
                    try {
                        repository.insertAllCausesOfRevelation(it)
                        sharedPreferences.edit().putInt("causesOfRevelationJsonVersion",
                            versions.get("causesOfRevelation")!!
                        ).apply()
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }
            }

            if (repository.getTafseersCount() != 6236 * 7 || versions.get("tafaseer"
                )!! > tafaseerJsonVersion
            ) {
                //insert the available tafseer files to db
                val tafaseerList = listOf(
                    "saadi.json",
                    "baghawy.json",
                    "muyassar.json",
                    "katheer.json",
                    "waseet.json",
                    "jalalayn.json",
                    "qortoby.json"
                )

                tafaseerList.forEach { tafseerFile ->
                    jsonParser.parseJsonArrayFile<TafseerData>(tafseerFile)?.let {
                        try {
                            repository.insertAllTafseerData(it)
                            sharedPreferences.edit().putInt("tafaseerJsonVersion",
                                versions.get("tafaseer")!!
                            ).apply()
                        } catch (e: Exception) {
                            e.printStackTrace()
                        }
                    }
                }
            }

            runBlocking {
                val quranFile = File(context.filesDir, "quran.json")

                if (!Helpers.checkNetworkConnectivity(context)) {
                    handleLocalQuranFile(quranFile)
                } else {
                    downloadAndUpdateQuranFile(quranFile)
                }

                if(versions.get("pagesContent")!! > pagesContentJsonVersion){
                    jsonParser.parseJsonArrayFile<PageContent>("pagesContent.json")?.let {
                        try {
                            repository.insertPagesContent(it)
                        } catch (e: Exception) {
                            e.printStackTrace()
                        }
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
        jsonParser.parseJsonArrayFileFromFilesDir(file.name)?.let {
            if (it.isNotEmpty()) {
                insertQuranDataInDb(it, updatingData)
            } else {
                throw Exception("Faulty File")
            }
        }
    }

    private fun downloadAndUpdateQuranFile(quranFile: File) {
        val client = OkHttpClient()
        val request = Request.Builder()
            .url(QURAN_FILE_URL)
            .head()  // HEAD request to get metadata
            .build()
        try {
            loadingFile = true
            val response = client.newCall(request).execute()
            if (response.isSuccessful) {
                println("success meta")
                println(response.headers)
                val fileLastModified = response.header("x-amz-meta-uploaded-at")?.toLong() ?: 0L
                println("file last modified $fileLastModified")
                if (fileLastModified > quranFileCreateTime) {
                    println("updating file from online")
                    updateFileCreationTime(fileLastModified)
                    downloadFile(quranFile)
                } else {
                    useLocalQuranFile(quranFile)
                }
            } else {
                println("Failed to fetch metadata: ${response.code}")
                useLocalQuranFile(quranFile)
            }
        } catch (e: IOException) {
            e.printStackTrace()
            handleDownloadError(quranFile, e)
        }
    }

    private fun updateFileCreationTime(timeMillis: Long) {
        sharedPreferences.edit().putLong("quranFileCreateTime", timeMillis).apply()
    }

    private fun downloadFile(quranFile: File) {
        val client = OkHttpClient()
        val request = Request.Builder()
            .url(QURAN_FILE_URL)
            .build()
        try {
            val response = client.newCall(request).execute()
            if (response.isSuccessful) {
                response.body?.string()?.let { jsonData ->
                    quranFile.writeText(jsonData)
                    println("success download")
                    parseAndSetQuranData(quranFile, true)
                    loadingFile = false
                } ?: println("Failed to get JSON content.")
            } else {
                println("Failed to download file: HTTP ${response.code}")
                useLocalQuranFile(quranFile)
                loadingFile = false
            }
        } catch (e: IOException) {
            e.printStackTrace()
            println("failed download: ${e.message}")
            handleDownloadError(quranFile, e)
        }
    }

    private fun useLocalQuranFile(quranFile: File) {
        println("using filesdir file")
        try {
            parseAndSetQuranData(quranFile)
        } catch (e: Exception) {
            Log.d("E", "Error using filesdir file: ${e.message}")
            setQuranDataFromLocalFile()
        } finally {
            loadingFile = false
            _loaded.value = true
        }
    }

    private fun handleDownloadError(quranFile: File, exception: Exception) {
        Log.d("E", "Error downloading JSON file: $exception")
        useLocalQuranFile(quranFile)
        loadingFile = false
    }

    fun setQuranDataFromLocalFile() {
        println("use assets file")
        try {
            val jsonParser = JsonParser(context)
            jsonParser.parseJsonArrayFile<QuranPage>("quran.json")
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