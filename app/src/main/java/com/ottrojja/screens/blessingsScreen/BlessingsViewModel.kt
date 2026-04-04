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
import kotlinx.coroutines.flow.update


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

    private var _isRefreshing = mutableStateOf(false)
    var isRefreshing: Boolean
        get() = _isRefreshing.value
        set(value: Boolean) {
            _isRefreshing.value = value
        }


    var page: Long = 1;
    var hasMorePages: Boolean = true;

    fun fetchBlessings(refresh: Boolean = false) {
        if (_loading.value || !hasMorePages) {
            return;
        }
        val from: Long = (page - 1) * 20
        val to: Long = from + 20 - 1
        println("fetching from $from to $to")
        viewModelScope.launch(Dispatchers.IO) {
            if (refresh) _isRefreshing.value = true else _loading.value = true
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

            if (refresh) {
                blessingsList.value = blessings
                page = 2
                hasMorePages = blessings.isNotEmpty()
            } else {
                if (blessings.isNotEmpty()) {
                    blessingsList.value += blessings
                    page++
                } else {
                    hasMorePages = false
                }
            }

            withContext(Dispatchers.Main) {
                _loading.value = false;
                _isRefreshing.value = false
            }
        }
    }

    fun refresh() {
        page = 1
        hasMorePages = true
        fetchBlessings(refresh = true)
    }

}