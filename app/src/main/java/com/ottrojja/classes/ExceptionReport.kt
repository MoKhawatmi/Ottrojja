package com.ottrojja.classes

import kotlinx.serialization.Serializable

@Serializable
data class ExceptionReport(
    val id: Int? = null,
    val stacktrace: String,
    // line and file are usually mentioned in stacktrace but this is just in case sometimes this does not happen
    val file: String,
    val details: String = ""
)
