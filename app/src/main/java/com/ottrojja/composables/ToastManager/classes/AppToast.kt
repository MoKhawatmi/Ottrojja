package com.ottrojja.composables.ToastManager.classes

enum class AppToastType {
    SUCCESS,
    ERROR,
    WARNING,
    INFO
}

data class AppToast(
    val id: Long,
    val message: String,
    val type: AppToastType,
    val durationMillis: Long = 3000L
)
