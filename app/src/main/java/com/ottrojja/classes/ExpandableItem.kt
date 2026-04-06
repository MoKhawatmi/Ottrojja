package com.ottrojja.classes

data class ExpandableItem<T>(
    var data: T,
    var expanded: Boolean = false
)
