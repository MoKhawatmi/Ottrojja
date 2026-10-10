package com.ottrojja.composables.adviceDisplay

import androidx.annotation.Keep
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Keep
@Serializable
data class Advice(
    val id: Int,
    @SerialName("created_at") val createdAt: String,
    val text: String,
    val details: String?)
