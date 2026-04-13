package com.ottrojja.classes

import android.content.Context

object DynamicAzkarHelper {

    private var azkarList: List<Tasabeeh>? = null

    fun getNextZekr(context: Context): String {
        val list = azkarList ?: JsonParser(context)
            .parseJsonArrayFile<Tasabeeh>("tasabeeh.json")
            ?.mapNotNull { tasbeeh ->
                val text = tasbeeh.ziker.trim()
                if (text.length <= 150) tasbeeh else null
            }
            ?.also { azkarList = it }
        ?: emptyList()

        if (list.isEmpty()) return "ربنا آتنا في الدنيا حسنة وفي الآخرة حسنة وقنا عذاب النار"

        return list.random().ziker
    }
}