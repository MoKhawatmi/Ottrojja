package com.ottrojja.screens.blessingsScreen

import android.app.Application
import android.content.Intent
import android.widget.Toast
import androidx.compose.runtime.mutableStateOf
import androidx.core.content.ContextCompat.startActivity
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.ottrojja.R
import com.ottrojja.classes.Blessing
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.createSupabaseClient
import io.github.jan.supabase.postgrest.Postgrest
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.postgrest.query.Columns
import io.github.jan.supabase.postgrest.query.Order
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class BlessingsViewModel(application: Application) : AndroidViewModel(application) {
    val context = application.applicationContext;

    lateinit var supabase: SupabaseClient;

    init {
        supabase = createSupabaseClient(
            supabaseUrl = "https://hitqsffypqgvcbkaityl.supabase.co",
            supabaseKey = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhYmFzZSIsInJlZiI6ImhpdHFzZmZ5cHFndmNia2FpdHlsIiwicm9sZSI6ImFub24iLCJpYXQiOjE3NDExNzI1MDgsImV4cCI6MjA1Njc0ODUwOH0.bDWk072PpG4KdZoyMu_Y8hqMXwG1kfqahZTY65XT3Ok"
        ) {
            install(Postgrest)
        }
    }

    var blessingsList = MutableStateFlow(emptyList<Blessing>())

    private var _loading = mutableStateOf(false)
    var loading: Boolean
        get() = _loading.value
        set(value: Boolean) {
            _loading.value = value
        }

    var page: Long = 1;
    var hasMorePages: Boolean = true;

    fun fetchBlessings() {
        if (_loading.value || !hasMorePages) {
            return;
        }
        val from: Long = (page - 1) * 20
        val to: Long = from + 20 - 1
        println("fetching from $from to $to")
        viewModelScope.launch(Dispatchers.IO) {
            _loading.value = true;
            val blessings: List<Blessing> = try {
                supabase.from("blessings").select(Columns.ALL) {
                    range(from = from, to = to);
                    order(column = "id", order = Order.DESCENDING)
                }.decodeList<Blessing>();
            } catch (e: Exception) {
                e.printStackTrace()
                withContext(Dispatchers.Main) {
                    Toast.makeText(context, "حصل خطأ اثناء التحميل", Toast.LENGTH_LONG).show()
                }
                emptyList<Blessing>()
            }
            _loading.value = false;
            println(blessings)
            if (blessings.size != 0) {
                blessingsList.value += blessings;
                page++;
                println("new count ${blessingsList.value.size}")
            } else {
                hasMorePages = false;
            }
        }

    }

    fun shareBlessing(blessing: Blessing) {

        val sendIntent: Intent = Intent().apply {
            action = Intent.ACTION_SEND
            putExtra(Intent.EXTRA_SUBJECT, "إشراقة")
            putExtra(Intent.EXTRA_TITLE, "تطبيق اترجة")
            putExtra(
                Intent.EXTRA_TEXT,
                "${blessing.text}\n${context.resources.getString(R.string.share_app)}"
            )
            type = "text/plain"
        }

        val shareIntent = Intent.createChooser(sendIntent, "مشاركة الإشراقة")
        shareIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)

        startActivity(context, shareIntent, null)

    }


}