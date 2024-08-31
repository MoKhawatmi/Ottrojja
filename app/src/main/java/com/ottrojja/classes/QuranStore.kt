package com.ottrojja.classes

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.ottrojja.screens.mainScreen.ChapterData
import com.ottrojja.screens.mainScreen.PartData
import com.ottrojja.screens.quranScreen.E3rabData
import com.ottrojja.screens.quranScreen.TafseerData


object QuranStore {
    private var quranData = listOf<QuranPage>()
    private var chaptersData = listOf<ChapterData>()
    private var partsData = listOf<PartData>()
    //private var tafseerData =  listOf<TafseerData>()
    private var e3rabData = listOf<E3rabData>()


    private var _tafseerData by mutableStateOf(listOf<TafseerData>())
    var tafseerData: List<TafseerData>
        get() = _tafseerData
        set(value) {
            _tafseerData = value
        }


    fun getQuranData():List<QuranPage> {
        return quranData;
    }

    fun setQuranData(data: List<QuranPage>) {
        quranData = data;
    }

    fun getChaptersData():List<ChapterData> {
        return chaptersData;
    }

    fun setChaptersData(data: List<ChapterData>) {
        chaptersData = data;
    }

    fun getPartsData():List<PartData> {
        return partsData;
    }

    fun setPartsData(data: List<PartData>) {
        partsData = data;
    }

    fun getE3rabData():List<E3rabData> {
        return e3rabData;
    }

    fun setE3rabData(data: List<E3rabData>) {
        e3rabData = data;
    }

}