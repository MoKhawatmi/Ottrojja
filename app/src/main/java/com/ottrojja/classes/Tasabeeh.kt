package com.ottrojja.classes

import androidx.annotation.Keep

@Keep
data class Tasabeeh(val ziker: String, val benefit: String, var expanded: Boolean = false)
