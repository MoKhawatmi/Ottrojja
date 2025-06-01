package com.ottrojja.classes

import androidx.room.TypeConverter
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.ottrojja.room.entities.PageContent
import com.ottrojja.room.entities.QuranPage
import kotlinx.serialization.json.Json

class Converters {
    @TypeConverter
    fun fromQuranPage(value : Array<QuranPage>) = Json.encodeToString(value)

    @TypeConverter
    fun toQuranPage(value: String) = Json.decodeFromString<Array<QuranPage>>(value)

    @TypeConverter
    fun fromPageContent(value : Array<PageContent>) = Json.encodeToString(value)

    @TypeConverter
    fun toPageContent(value: String) = Json.decodeFromString<Array<PageContent>>(value)

    @TypeConverter
    fun fromString(value: String): Array<String> {
        val listType = object : TypeToken<Array<String>>() {}.type
        return Gson().fromJson(value, listType)
    }

    @TypeConverter
    fun fromArray(array: Array<String>): String {
        return Gson().toJson(array)
    }


}