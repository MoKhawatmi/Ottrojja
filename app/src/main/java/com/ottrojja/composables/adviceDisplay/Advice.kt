package com.ottrojja.composables.adviceDisplay

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class Advice(val id: Int,
                  @SerialName("created_at") val createdAt: String,
                  val text: String,
                  val details: String?)
