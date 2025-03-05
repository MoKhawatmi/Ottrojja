package com.ottrojja.screens.blessingsScreen

import android.app.Application
import android.content.Intent
import android.widget.Toast
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.core.content.ContextCompat.startActivity
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.ottrojja.classes.Blessing
import io.github.jan.supabase.createSupabaseClient
import io.github.jan.supabase.postgrest.Postgrest
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.postgrest.query.Columns
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class BlessingsViewModel(application: Application) : AndroidViewModel(application) {
    val context = application.applicationContext;

    private var _blessingsList = mutableStateOf(emptyList<Blessing>())
    var blessingsList: List<Blessing>
        get() = _blessingsList.value
        set(value: List<Blessing>) {
            _blessingsList.value = value
        }

    private var _loading = mutableStateOf(true)
    var loading: Boolean
        get() = _loading.value
        set(value: Boolean) {
            _loading.value = value
        }


    fun initAndFetch() {
        val supabase = createSupabaseClient(
            supabaseUrl = "https://hitqsffypqgvcbkaityl.supabase.co",
            supabaseKey = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhYmFzZSIsInJlZiI6ImhpdHFzZmZ5cHFndmNia2FpdHlsIiwicm9sZSI6ImFub24iLCJpYXQiOjE3NDExNzI1MDgsImV4cCI6MjA1Njc0ODUwOH0.bDWk072PpG4KdZoyMu_Y8hqMXwG1kfqahZTY65XT3Ok"
        ) {
            install(Postgrest)
        }

        viewModelScope.launch(Dispatchers.IO) {
            _loading.value = true;
            val blessings: List<Blessing> = try {
                supabase.from("blessings").select(Columns.ALL).decodeList<Blessing>();
            } catch (e: Exception) {
                e.printStackTrace()
                withContext(Dispatchers.Main) {
                    Toast.makeText(context, "حصل خطأ اثناء التحميل", Toast.LENGTH_LONG).show()
                }
                emptyList<Blessing>()
            }
            _loading.value = false;
            println(blessings)
            _blessingsList.value = blessings;
        }
    }

    fun shareBlessing(blessing: Blessing) {

        val sendIntent: Intent = Intent().apply {
            action = Intent.ACTION_SEND
            putExtra(Intent.EXTRA_SUBJECT, "إشراقة")
            putExtra(Intent.EXTRA_TITLE, "تطبيق اترجة")
            putExtra(
                Intent.EXTRA_TEXT,
                blessing.text
            )
            type = "text/plain"
        }

        val shareIntent = Intent.createChooser(sendIntent, "مشاركة الإشراقة")
        startActivity(context, shareIntent, null)


    }


}