import android.content.Context
import com.ottrojja.screens.mainScreen.ChapterData
import com.ottrojja.screens.mainScreen.PartData
import com.ottrojja.screens.quranScreen.E3rabData
import com.ottrojja.classes.QuranPage
import com.ottrojja.screens.azkarScreen.Azkar
import com.ottrojja.screens.quranScreen.TafseerData
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.io.File
import java.io.IOException
import java.lang.reflect.Type
import java.nio.charset.Charset

class JsonParser(private val context: Context) {


    fun parseJsonArrayFileFromFilesDir(context: Context, fileName: String): List<QuranPage>? {
        val json: String? = readJsonFromFile(context, fileName)

        return if (json != null) {
            val listType: Type = object : TypeToken<List<QuranPage>>() {}.type
            Gson().fromJson(json, listType)
        } else {
            null
        }
    }

    fun readJsonFromFile(context: Context, fileName: String): String? {
        val file = File(context.filesDir, fileName)

        return try {
            val content = file.readText()
            content
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }


    fun parseJsonArrayFile(fileName: String): List<QuranPage>? {
        val json: String? = loadJsonFromAsset(fileName)

        return if (json != null) {
            val listType = object : TypeToken<List<QuranPage>>() {}.type
            Gson().fromJson(json, listType)
        } else {
            null
        }
    }

    fun parseJsonArrayFileChapters(fileName: String): List<ChapterData>? {
        val json: String? = loadJsonFromAsset(fileName)

        return if (json != null) {
            val listType = object : TypeToken<List<ChapterData>>() {}.type
            Gson().fromJson(json, listType)
        } else {
            null
        }
    }

    fun parseJsonArrayFileAzkar(fileName: String): List<Azkar>? {
        val json: String? = loadJsonFromAsset(fileName)

        return if (json != null) {
            val listType = object : TypeToken<List<Azkar>>() {}.type
            Gson().fromJson(json, listType)
        } else {
            null
        }
    }


    fun parseJsonArrayFileParts(fileName: String): List<PartData>? {
        val json: String? = loadJsonFromAsset(fileName)

        return if (json != null) {
            val listType = object : TypeToken<List<PartData>>() {}.type
            Gson().fromJson(json, listType)
        } else {
            null
        }
    }

    fun parseJsonArrayFileTafseer(fileName: String): List<TafseerData>? {
        val json: String? = loadJsonFromAsset(fileName)

        return if (json != null) {
            val listType = object : TypeToken<List<TafseerData>>() {}.type
            Gson().fromJson(json, listType)
        } else {
            null
        }
    }

    fun parseJsonArrayFileE3rab(fileName: String): List<E3rabData>? {
        val json: String? = loadJsonFromAsset(fileName)

        return if (json != null) {
            val listType = object : TypeToken<List<E3rabData>>() {}.type
            Gson().fromJson(json, listType)
        } else {
            null
        }
    }



    private fun loadJsonFromAsset(fileName: String): String? {
        var json: String? = null
        try {
            val inputStream = context.assets.open(fileName)
            val size = inputStream.available()
            val buffer = ByteArray(size)
            inputStream.read(buffer)
            inputStream.close()
            json = String(buffer, Charset.defaultCharset())
        } catch (e: IOException) {
            e.printStackTrace()
        }
        return json
    }
}