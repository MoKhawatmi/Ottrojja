package com.ottrojja.room.relations

import androidx.room.Embedded
import androidx.room.Relation
import com.ottrojja.room.entities.PageContent
import com.ottrojja.room.entities.QuranPage

data class QuranPageWithContent(
    @Embedded val page: QuranPage,  // Embeds the page data in this class
    @Relation(
        parentColumn = "pageNum",  // Refers to the PRIMARY KEY in the QuranPage table
        entityColumn = "pageNum"   // Refers to the FOREIGN KEY in the PageContent table
    )
    val pageContent: List<PageContent>  // This will automatically fetch all pageContent related to the Page
)
