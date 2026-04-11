package com.ottrojja.classes

data class FormValidationResult(
    val isValid: Boolean,
    val errors: Map<String, String?> = emptyMap()
)
