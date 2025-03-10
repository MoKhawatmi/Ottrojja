package com.ottrojja.classes

data class ExpandableItem<T>(
    val data: T,
    var expanded: Boolean = false
)
