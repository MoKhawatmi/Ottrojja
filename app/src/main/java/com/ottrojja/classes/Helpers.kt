package com.ottrojja.classes

import android.app.ActivityManager
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Build
import android.util.Log
import android.widget.Toast
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.GoogleApiAvailability
import com.ottrojja.services.AzkarPlayerService
import com.ottrojja.services.PagePlayerService
import com.ottrojja.services.QuranPlayerService
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.postgrest.postgrest
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import java.util.Locale

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
        //println("$arabicNumber resulting number $result")

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

    fun copyToClipboard(context: Context, text: String, successToast: String = "") {
        try {
            val clipboardManager =
                context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
            val clipData = ClipData.newPlainText("text", text)
            clipboardManager.setPrimaryClip(clipData)
            if (successToast.isNotBlank()) {
                Toast.makeText(context, successToast, Toast.LENGTH_LONG).show();
            }
        } catch (e: Exception) {
            println(e);
            Toast.makeText(context, "تعذر النسخ", Toast.LENGTH_LONG).show();
        }
    }

    fun isMyServiceRunning(serviceClass: Class<*>, context: Context): Boolean {
        val manager = context.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
        for (service in manager.getRunningServices(Int.MAX_VALUE)) {
            if (serviceClass.name == service.service.className) {
                return service.foreground
            }
        }
        return false
    }

    fun checkNetworkConnectivity(context: Context): Boolean {
        val connectivityManager =
            context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            val networkCapabilities = connectivityManager.activeNetwork ?: return false
            val activeNetwork =
                connectivityManager.getNetworkCapabilities(networkCapabilities) ?: return false

            return when {
                activeNetwork.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) -> true
                activeNetwork.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) -> true
                // Other transports like Bluetooth, Ethernet, etc.
                else -> false
            }
        } else {
            // For devices with SDK < 23
            val activeNetworkInfo = connectivityManager.activeNetworkInfo
            return activeNetworkInfo != null && activeNetworkInfo.isConnected
        }
    }

    fun terminateAllServices(context: Context, except: Class<*>? = null) {
        val servicesList = listOf(AzkarPlayerService::class.java, PagePlayerService::class.java,
            QuranPlayerService::class.java
        )
        servicesList.forEach {
            if (it != except) {
                // val sr = isMyServiceRunning(it, context);
                val stopServiceIntent = Intent(context, it)
                stopServiceIntent.setAction("TERMINATE")
                context.startService(stopServiceIntent)
            }
        }
    }

    fun isGmsAvailable(context: Context): Boolean {
        val apiAvailability = GoogleApiAvailability.getInstance()
        val resultCode = apiAvailability.isGooglePlayServicesAvailable(context)
        return resultCode == ConnectionResult.SUCCESS
    }

    val ottrojjaBrush = Brush.linearGradient(
        colors = listOf(
            Color(0xFF1E5C77), // lighter teal (a bit more bright)
            Color(0xFF194D65), // your base color
            Color(0xFF123C4F), // deeper blue teal
            Color(0xFF194D65), // back to your base
            Color(0xFF1E5C77)  // back to lighter
        )
    )

    fun String.truncate(maxLength: Int): String {
        return if (this.length <= maxLength) this
        else this.take(maxLength) + ".."
    }

    // used in multiple composables and services
    val repetitionOptionsMap = linkedMapOf<String, Int>(
        "0" to 0,
        "1" to 1,
        "2" to 2,
        "3" to 3,
        "4" to 4,
        "5" to 5,
        "6" to 6,
        "7" to 7,
        "8" to 8,
        "9" to 9,
        "10" to 10,
        "بلا توقف" to Integer.MAX_VALUE, //smart, right?!
    )

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    fun reportException(exception: Exception, file: String, details: String = "") {
        var supabase: SupabaseClient = SupabaseProvider.client;
        val report = ExceptionReport(
            stacktrace = exception.stackTraceToString(),
            file = file,
            details = details
        )

        scope.launch {
            try {
                supabase.postgrest["exceptions"].insert(report)
                Log.d("Supabase", "Message inserted successfully")
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun formatTime(millis: Long): String {
        val totalSeconds = millis / 1000
        val seconds = (totalSeconds % 60).toInt()
        val minutes = ((totalSeconds / 60) % 60).toInt()
        val hours = (totalSeconds / 3600).toInt()

        return if (hours > 0) {
            String.format(Locale.ENGLISH, "%d:%02d:%02d", hours, minutes, seconds)
        } else {
            String.format(Locale.ENGLISH, "%02d:%02d", minutes, seconds)
        }
    }

}