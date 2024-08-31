package com.ottrojja.classes

import android.app.ActivityManager
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast

object Helpers {
    fun convertToIndianNumbers(arabicNumber: String): String {
        val arabicDigits = arrayOf("0", "1", "2", "3", "4", "5", "6", "7", "8", "9")
        val indianDigits = arrayOf("٠", "١", "٢", "٣", "٤", "٥", "٦", "٧", "٨", "٩")

        var result = ""
        for (char in arabicNumber) {
            val digitIndex = arabicDigits.indexOf(char.toString())
            if (digitIndex != -1) {
                result += indianDigits[digitIndex]
            } else {
                // If the character is not a digit, keep it unchanged
                result += char
            }
        }

        return result
    }

    fun convertToArabicNumbers(indianNumber: String): String {
        val arabicDigits = arrayOf("0", "1", "2", "3", "4", "5", "6", "7", "8", "9")
        val indianDigits = arrayOf("٠", "١", "٢", "٣", "٤", "٥", "٦", "٧", "٨", "٩")

        var result = ""
        for (char in indianNumber) {
            val digitIndex = indianDigits.indexOf(char.toString())
            if (digitIndex != -1) {
                result += arabicDigits[digitIndex]
            } else {
                // If the character is not a digit, keep it unchanged
                result += char
            }
        }

        return result
    }

    fun copyToClipboard(context: Context, text: String, successToast: String) {
        try {
            val clipboardManager =
                context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
            val clipData = ClipData.newPlainText("text", text)
            clipboardManager.setPrimaryClip(clipData)
            Toast.makeText(context, successToast, Toast.LENGTH_LONG).show();
        } catch (e: Exception) {
            println(e);
            Toast.makeText(context, "تعذر النسخ", Toast.LENGTH_LONG).show();
        }
    }

    fun isMyServiceRunning(serviceClass: Class<*>, context:Context): Boolean {
        val manager = context.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
        for (service in manager.getRunningServices(Int.MAX_VALUE)) {
            if (serviceClass.name == service.service.className) {
                return service.foreground
            }
        }
        return false
    }



}