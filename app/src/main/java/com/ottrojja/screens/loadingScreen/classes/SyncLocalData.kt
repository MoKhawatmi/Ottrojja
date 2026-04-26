package com.ottrojja.screens.loadingScreen.classes

import android.content.Context
import android.content.SharedPreferences
import com.ottrojja.classes.JsonParser
import com.ottrojja.classes.QuranRepository
import com.ottrojja.classes.Helpers.reportException
import com.ottrojja.room.entities.*
import com.ottrojja.screens.mainScreen.ChapterData
import com.ottrojja.screens.mainScreen.PartData

class SyncLocalData(
    private val context: Context,
    private val repository: QuranRepository,
    private val prefs: SharedPreferences
) {
    private val jsonParser = JsonParser(context)

    // Read once up-front, not lazily scattered around
    private val versions: Map<String, Int> by lazy { jsonParser.getFilesVersions() }

    suspend fun sync() {
        syncChapters()
        syncParts()
        syncE3rab()
        syncVerseMeanings()
        syncAzkar()
        syncAhzab()
        syncCausesOfRevelation()
        syncTafaseer()
    }

    // ── helpers ──────────────────────────────────────────────────────────────

    private fun storedVersion(key: String) = prefs.getInt(key, 0)
    private fun remoteVersion(key: String) = versions[key] ?: 0
    private fun saveVersion(key: String, value: Int) =
        prefs.edit().putInt(key, value).apply()

    private fun needsUpdate(prefKey: String, versionKey: String = prefKey) =
        remoteVersion(versionKey) > storedVersion(prefKey)

    private suspend inline fun <reified T> syncAsset(
        assetFile: String,
        prefKey: String,
        versionKey: String = prefKey,
        needsSync: Boolean,
        crossinline insert: suspend (List<T>) -> Unit
    ) {
        if (!needsSync) return
        try {
            val data = jsonParser.parseJsonArrayFile<T>(assetFile) ?: return
            insert(data)
            saveVersion(prefKey, remoteVersion(versionKey))
        } catch (e: Exception) {
            e.printStackTrace()
            reportException(exception = e, file = "LocalDataSyncUseCase")
        }
    }

    // ── individual sync functions ─────────────────────────────────────────────

    private suspend fun syncChapters() = syncAsset<ChapterData>(
        assetFile = "chaptersList.json",
        prefKey = "chaptersJsonVersion",
        versionKey = "chapters",
        needsSync = repository.getChaptersCount() != 114 || needsUpdate("chaptersJsonVersion", "chapters")
    ) { repository.insertAllChapters(it) }

    private suspend fun syncParts() = syncAsset<PartData>(
        assetFile = "parts.json",
        prefKey = "partsJsonVersion",
        versionKey = "parts",
        needsSync = repository.getPartsCount() != 30 || needsUpdate("partsJsonVersion", "parts")
    ) { repository.insertAllParts(it) }

    private suspend fun syncE3rab() = syncAsset<E3rabData>(
        assetFile = "e3rab.json",
        prefKey = "e3rabJsonVersion",
        versionKey = "e3rab",
        needsSync = repository.getE3rabsCount() != 6236 || needsUpdate("e3rabJsonVersion", "e3rab")
    ) { repository.insertAllE3rabData(it) }

    private suspend fun syncVerseMeanings() = syncAsset<VerseMeanings>(
        assetFile = "verseMeanings.json",
        prefKey = "verseMeaningsJsonVersion",
        versionKey = "verseMeanings",
        needsSync = needsUpdate("verseMeaningsJsonVersion", "verseMeanings")
    ) { repository.insertVerseMeanings(it) }

    private suspend fun syncAzkar() = syncAsset<Azkar>(
        assetFile = "azkar.json",
        prefKey = "azkarJsonVersion",
        versionKey = "azkar",
        needsSync = repository.getAzkarCount() == 0 || needsUpdate("azkarJsonVersion", "azkar")
    ) { repository.insertAllAzkar(it) }

    private suspend fun syncAhzab() = syncAsset<Quarter>(
        assetFile = "ahzab.json",
        prefKey = "ahzabJsonVersion",
        versionKey = "ahzab",
        needsSync = needsUpdate("ahzabJsonVersion", "ahzab")
    ) { repository.insertQuarters(it) }

    private suspend fun syncCausesOfRevelation() = syncAsset<CauseOfRevelation>(
        assetFile = "causesOfRevelation.json",
        prefKey = "causesOfRevelationJsonVersion",
        versionKey = "causesOfRevelation",
        needsSync = repository.getCauseOfRevelationCount() == 0
                || needsUpdate("causesOfRevelationJsonVersion", "causesOfRevelation")
    ) { repository.insertAllCausesOfRevelation(it) }

    private suspend fun syncTafaseer() {
        val needsSync = repository.getTafseersCount() != 6236 * 7
                || needsUpdate("tafaseerJsonVersion", "tafaseer")
        if (!needsSync) return

        val files = listOf("saadi.json", "baghawy.json", "muyassar.json",
            "katheer.json", "waseet.json", "jalalayn.json", "qortoby.json")
        files.forEach { file ->
            syncAsset<TafseerData>(
                assetFile = file,
                prefKey = "tafaseerJsonVersion",
                versionKey = "tafaseer",
                needsSync = true
            ) { repository.insertAllTafseerData(it) }
        }
    }

    internal suspend fun syncPagesContent() = syncAsset<PageContent>(
        assetFile = "pagesContent.json",
        prefKey = "pagesContentJsonVersion",
        versionKey = "pagesContent",
        needsSync = needsUpdate("pagesContentJsonVersion", "pagesContent")
    ) { repository.insertPagesContent(it) }
}