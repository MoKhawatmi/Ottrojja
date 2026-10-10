package com.ottrojja.classes

import androidx.annotation.Keep

@Keep
data class GeneralSupplications(
    val id: Int,
    val category: String,
    val array: List<GeneralSupplication>)

@Keep
data class GeneralSupplication(val id: Int, val text: String, val count: Int)