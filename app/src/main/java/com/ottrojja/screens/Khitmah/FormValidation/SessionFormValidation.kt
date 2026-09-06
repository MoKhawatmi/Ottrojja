package com.ottrojja.screens.Khitmah.FormValidation

import com.ottrojja.classes.FormValidationResult
import com.ottrojja.screens.Khitmah.SessionForm.SessionFormState

fun validateSession(
    session: SessionFormState
): FormValidationResult {
    val errors = mutableMapOf<String, String?>()

    if (session.quranRange == null) {
        errors["sessionRange"] = "يرجى إدخال نطاق الآيات المقروءة"
    } else if (
        session.quranRange.startVerse == null ||
        session.quranRange.endVerse == null
    ) {
        errors["sessionRange"] = "يرجى تحديد بداية ونهاية نطاق القراءة"
    } else if (session.quranRange.startVerse.id > session.quranRange.endVerse.id) {
        errors["sessionRange"] = "موضع البداية يجب ألا يرد بعد موضع النهاية"
    }


    if (session.reflection != null && session.reflection.length > 250) {
        errors["sessionReflection"] = "يجب ان لا يتعدى نص التأمل 250 حرفا"
    }

    return FormValidationResult(
        isValid = errors.isEmpty(),
        errors = errors
    )
}