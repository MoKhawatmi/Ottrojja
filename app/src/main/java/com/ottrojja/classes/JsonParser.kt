package com.ottrojja.classes

import android.content.Context
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.ottrojja.room.entities.QuranPage
import org.json.JSONObject
import java.io.File
import java.io.IOException
import java.lang.reflect.Type
import java.nio.charset.Charset

class JsonParser(private val context: Context) {


    fun parseJsonArrayFileFromFilesDir(fileName: String): List<QuranPage>? {
        val json: String? = readJsonFromFile(fileName)

        return if (json != null) {
            val listType: Type = object : TypeToken<List<QuranPage>>() {}.type
            Gson().fromJson(json, listType)
        } else {
            null
        }
    }

    fun readJsonFromFile(fileName: String): String? {
        val file = File(context.filesDir, fileName)

        return try {
            val content = file.readText()
            content
        } catch (e: Exception) {
            e.printStackTrace()
            throw Exception("File reading error")
        }
    }


    inline fun <reified T> parseJsonArrayFile(fileName: String): List<T>? {
        val json: String? = loadJsonFromAsset(fileName)

        return if (json != null) {
            val listType = object : TypeToken<List<T>>() {}.type
            Gson().fromJson(json, listType)
        } else {
            null
        }
    }

    fun getFilesVersions(): HashMap<String, Int> {
        val json: String? = loadJsonFromAsset("files_versions.json")
        val jsonObject = JSONObject(json)

        return hashMapOf<String, Int>(
            "azkar" to jsonObject.getInt("azkar"),
            "chapters" to jsonObject.getInt("chapters"),
            "parts" to jsonObject.getInt("parts"),
            "e3rab" to jsonObject.getInt("e3rab"),
            "verseMeanings" to jsonObject.getInt("verseMeanings"),
            "causesOfRevelation" to jsonObject.getInt("causesOfRevelation"),
            "tafaseer" to jsonObject.getInt("tafaseer"),
        )
    }


    fun loadJsonFromAsset(fileName: String): String? {
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
