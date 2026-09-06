package com.ottrojja.screens.Khitmah.FormValidation

import com.ottrojja.classes.FormValidationResult
import com.ottrojja.room.entities.Khitmah

fun validateKhitmah(
    khitmah: Khitmah
): FormValidationResult {
    val errors = mutableMapOf<String, String?>()

    when {
        khitmah.title.isBlank() -> {
            errors["khitmahTitle"] = "يرجى إدخال عنوان الختمة"
        }

        khitmah.title.length > 150 -> {
            errors["khitmahTitle"] = "يجب ان لا يتعدى عنوان الختمة 150 حرفا"
        }
    }

    return FormValidationResult(
        isValid = errors.isEmpty(),
        errors = errors
    )
}