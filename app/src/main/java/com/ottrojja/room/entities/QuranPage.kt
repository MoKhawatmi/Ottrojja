package com.ottrojja.room.entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
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
//   @ColumnInfo(name = "pageContent") val pageContent: Array<PageContent> = emptyArray()
) {}

@Entity(
    foreignKeys = [
        ForeignKey(
            entity = QuranPage::class, // Parent table
            parentColumns = arrayOf("pageNum"), //primary key in QuranPage
            childColumns = arrayOf("pageNum"), // Foreign key in PageContent
        )
    ]
)
@Serializable
data class PageContent(
    @PrimaryKey val id: Int,
    @ColumnInfo(name = "pageNum") val pageNum: String,
    @ColumnInfo(name = "type") val type: PageContentItemType = PageContentItemType.EMPTY,
    @ColumnInfo(name = "surahName") val surahName: String?,
    @ColumnInfo(name = "surahNum") val surahNum: Int,
    @ColumnInfo(name = "surahTotal") val surahTotal: Int?,
    @ColumnInfo(name = "surahType") val surahType: String?,
    @ColumnInfo(name = "verseNum") val verseNum: Int?,
    @ColumnInfo(name = "verseText") val verseText: String?,
    @ColumnInfo(name = "verseTextPlain") val verseTextPlain: String?,
) {}


/*@Serializable
data class PageContent(
    val type: PageContentItemType = PageContentItemType.EMPTY,
    val surahName: String = "",
    val surahNum: String = "",
    val surahTotal: String = "",
    val surahType: String = "",
    val verseNum: String = "",
    val verseText: String = "",
    val verseTextPlain: String = "",
) {}*/

enum class PageContentItemType{
    verse, surah, EMPTY
}



