package com.ottrojja.classes

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.serialization.Serializable

@Entity
@Serializable
data class QuranPage(
    @PrimaryKey @ColumnInfo(name = "pageNum") val pageNum: String,
    @ColumnInfo(name = "ytLink") val ytLink: String,
    @ColumnInfo(name = "benefits") val benefits: Array<String>,
    @ColumnInfo(name = "appliance") val appliance: Array<String>,
    @ColumnInfo(name = "guidance") val guidance: Array<String>,
    @ColumnInfo(name = "pageContent") val pageContent: Array<PageContent>
) {}
@Serializable
data class PageContent(
    val type: String = "",
    val surahName: String = "",
    val surahNum: String = "",
    val surahTotal: String = "",
    val surahType: String = "",
    val verseNum: String = "",
    val verseText: String = "",
    val verseTextPlain: String = "",
) {}

/*@Entity
@Serializable
data class PageContent(
    @ColumnInfo(name = "type") val type: String,
    @ColumnInfo(name = "surahName") val surahName: String,
    @ColumnInfo(name = "surahNum") val surahNum: String,
    @ColumnInfo(name = "surahTotal") val surahTotal: String,
    @ColumnInfo(name = "surahType") val surahType: String,
    @ColumnInfo(name = "verseNum") val verseNum: String,
    @ColumnInfo(name = "verseText") val verseText: String,
    @ColumnInfo(name = "verseTextPlain") val verseTextPlain: String,
) {}*/

