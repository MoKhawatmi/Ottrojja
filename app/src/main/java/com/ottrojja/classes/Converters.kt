package com.ottrojja.classes

import androidx.room.TypeConverter
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.ottrojja.room.entities.PageContent
import com.ottrojja.room.entities.QuranPage
import com.ottrojja.screens.reminderScreen.classes.ReminderRepeatType
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

    @TypeConverter
    fun fromRepeatType(value: ReminderRepeatType): String = value.name

    @TypeConverter
    fun toRepeatType(value: String): ReminderRepeatType =
        ReminderRepeatType.valueOf(value)

    @TypeConverter
    fun fromList(list: List<String>?): String {
        if (list.isNullOrEmpty()) return "[]"
        return Json.encodeToString(list)
    }

    @TypeConverter
    fun toList(value: String?): List<String> {
        if (value.isNullOrBlank()) return emptyList()
        return Json.decodeFromString(value)
    }

    /** PageContent.polygons ⇄ "[[x1,y1,x2,y2,…],[…]]" */
    @TypeConverter
    fun fromPolygons(polygons: List<List<Int>>?): String {
        if (polygons.isNullOrEmpty()) return "[]"
        return Json.encodeToString(polygons)
    }

    @TypeConverter
    fun toPolygons(value: String?): List<List<Int>> {
        if (value.isNullOrBlank()) return emptyList()
        return Json.decodeFromString(value)
    }


}