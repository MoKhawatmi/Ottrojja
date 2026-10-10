package com.ottrojja.classes

import androidx.annotation.Keep

@Keep
data class Bookmark(val pageNum: String, val timeStamp: Long, var expanded: Boolean = false)
