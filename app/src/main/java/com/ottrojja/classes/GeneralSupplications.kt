package com.ottrojja.classes

data class GeneralSupplications(val id: Int,
                                val category: String,
                                val array: List<GeneralSupplication>)

data class GeneralSupplication(val id: Int, val text: String, val count: Int)