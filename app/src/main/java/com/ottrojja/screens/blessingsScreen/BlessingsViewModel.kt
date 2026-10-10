package com.ottrojja.screens.blessingsScreen

import android.app.Application
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.mikepenz.markdown.model.parseMarkdown
import com.ottrojja.classes.Helpers.reportException
import com.ottrojja.classes.SupabaseProvider
import com.ottrojja.composables.ToastManager.classes.AppToastManager
import com.ottrojja.composables.ToastManager.classes.AppToastType
import com.ottrojja.screens.blessingsScreen.classes.Blessing
import com.ottrojja.screens.blessingsScreen.classes.BlessingCategory
import com.ottrojja.screens.blessingsScreen.classes.BlessingUiItem
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.postgrest.query.Columns
import io.github.jan.supabase.postgrest.query.Order
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.drop
import kotlinx.coroutines.launch


class BlessingsViewModel(application: Application) : AndroidViewModel(application) {
    val context = application.applicationContext

    private val supabase: SupabaseClient = SupabaseProvider.client

    val blessingsList = MutableStateFlow(emptyList<BlessingUiItem>())

    private val _searchQuery = MutableStateFlow("")
    val searchQuery = _searchQuery

    private val _filterCategory = MutableStateFlow(BlessingCategory.ALL)
    val filterCategory = _filterCategory

    private var _loading = mutableStateOf(false)
    var loading: Boolean
        get() = _loading.value
        set(value) {
            _loading.value = value
        }

    private var _isRefreshing = mutableStateOf(false)
    var isRefreshing: Boolean
        get() = _isRefreshing.value
        set(value) {
            _isRefreshing.value = value
        }

    private var page: Long = 1
    private var hasMorePages: Boolean = true
    private var fetchJob: Job? = null

    @Volatile
    private var requestId = 0

    init {
        observeSearch()
        fetchBlessings() // initial load (the screen no longer triggers it)
    }

    private fun observeSearch() {
        viewModelScope.launch {
            _searchQuery
                .drop(1) // skip the initial "" so we don't fetch twice on start
                .debounce(500)
                .distinctUntilChanged()
                .collectLatest { refresh() }
        }
    }

    fun updateSearch(query: String) {
        _searchQuery.value = query
    }

    fun updateCategory(category: BlessingCategory) {
        _filterCategory.value = category
        refresh()
    }

    fun fetchBlessings(refresh: Boolean = false) {
        // A refresh always wins and cancels whatever is in flight;
        // pagination is ignored while something is already loading.
        if (!refresh && (_loading.value || _isRefreshing.value || !hasMorePages)) return

        fetchJob?.cancel()
        val myRequest = ++requestId
        val requestedPage = if (refresh) 1L else page
        val from = (requestedPage - 1) * PAGE_SIZE
        val to = from + PAGE_SIZE - 1

        if (refresh) {
            _isRefreshing.value = true
            _loading.value = false
        } else {
            _loading.value = true
        }

        fetchJob = viewModelScope.launch(Dispatchers.IO) {
            try {
                val query = _searchQuery.value
                val category = _filterCategory.value

                val items = supabase.from("blessings").select(Columns.ALL) {
                    range(from = from, to = to)
                    order(column = "id", order = Order.DESCENDING)
                    if (query.isNotBlank()) {
                        filter { ilike(column = "text", pattern = "%$query%") }
                    }
                    if (category != BlessingCategory.ALL) {
                        filter { eq(column = "category", value = category) }
                    }
                }.decodeList<Blessing>().map { blessing ->
                    // The full markdown string arrives at once: parse it once, off the main thread.
                    BlessingUiItem(blessing, parseMarkdown(blessing.text))
                }

                if (myRequest != requestId) return@launch // superseded by a newer request

                if (refresh) {
                    blessingsList.value = items
                    page = 2
                } else {
                    blessingsList.value += items
                    page++
                }
                hasMorePages = items.size.toLong() == PAGE_SIZE
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                e.printStackTrace()
                reportException(exception = e, file = "BlessingsViewModel")
                AppToastManager.show(
                    message = "حصل خطأ اثناء التحميل",
                    type = AppToastType.ERROR
                )
            } finally {
                if (myRequest == requestId) {
                    _loading.value = false
                    _isRefreshing.value = false
                }
            }
        }
    }

    fun refresh() {
        page = 1
        hasMorePages = true
        fetchBlessings(refresh = true)
    }

    private companion object {
        const val PAGE_SIZE = 20L
    }
}