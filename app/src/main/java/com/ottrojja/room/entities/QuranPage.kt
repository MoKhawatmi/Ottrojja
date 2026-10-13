package com.ottrojja.room.entities

import androidx.annotation.Keep
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey
import kotlinx.serialization.Serializable

@Keep
@Entity
@Serializable
data class QuranPage(
    @PrimaryKey @ColumnInfo(name = "pageNum") val pageNum: String,
    @ColumnInfo(name = "ytLink") val ytLink: String,
    @ColumnInfo(name = "benefits") val benefits: Array<String>,
    @ColumnInfo(name = "appliance") val appliance: Array<String>,
    @ColumnInfo(name = "guidance") val guidance: Array<String>,
)

@Entity(
    foreignKeys = [
        ForeignKey(
            entity = QuranPage::class, // Parent table
            parentColumns = arrayOf("pageNum"), //primary key in QuranPage
            childColumns = arrayOf("pageNum"), // Foreign key in PageContent
        )
    ]
)
@Keep
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
    @ColumnInfo(name = "mutashabihat") val mutashabihat: List<String> = emptyList(),
    /**
     * Tap regions of this verse on the page image, in the 740×1150 source-pixel grid.
     * Each inner list is one closed polygon as flat [x1, y1, x2, y2, …]. A verse that wraps from
     * the end of one line to the next is two (or more) disjoint polygons. Empty for surah rows.
     */
    @ColumnInfo(name = "polygons") val polygons: List<List<Int>> = emptyList(),
)
@Keep
enum class PageContentItemType{
    verse, surah, EMPTY
}
