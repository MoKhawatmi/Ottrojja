package com.ottrojja.screens.blessingsScreen

import android.app.Application
import android.widget.Toast
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.ottrojja.classes.Blessing
import com.ottrojja.classes.Helpers.reportException
import com.ottrojja.classes.SupabaseProvider
import io.github.jan.supabase.SupabaseClient
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.postgrest.query.Columns
import io.github.jan.supabase.postgrest.query.Order


class BlessingsViewModel(application: Application) : AndroidViewModel(application) {
    val context = application.applicationContext;

    lateinit var supabase: SupabaseClient;

    init {
        supabase = SupabaseProvider.client
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
                reportException(exception = e, file = "BlessingsViewModel")
                withContext(Dispatchers.Main) {
                    Toast.makeText(context, "حصل خطأ اثناء التحميل", Toast.LENGTH_LONG).show()
                }
                emptyList<Blessing>()
            }
            _loading.value = false;
            if (blessings.size != 0) {
                blessingsList.value += blessings;
                page++;
                println("new count ${blessingsList.value.size}")
            } else {
                hasMorePages = false;
            }
        }

    }

}