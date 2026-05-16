package com.ottrojja.composables.adviceDisplay

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.ottrojja.classes.DataStore.DataStoreRepository
import com.ottrojja.classes.Helpers.reportException
import com.ottrojja.classes.SupabaseProvider
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.postgrest.postgrest
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class AdviceDisplayViewModel(application: Application) : AndroidViewModel(application) {
    val context = application.applicationContext

    var supabase: SupabaseClient = SupabaseProvider.client;


    private var _loading = MutableStateFlow<Boolean>(false)
    var loading: StateFlow<Boolean> = _loading;

    private var _advice = MutableStateFlow<Advice?>(null)
    var advice: StateFlow<Advice?> = _advice;


    fun fetchAdvice() {
        viewModelScope.launch(Dispatchers.IO) {
            _loading.value = true;

            val fetchedAdvice = try {
                val result = withContext(Dispatchers.IO) {
                    supabase.postgrest
                        .rpc("get_random_row_advice")
                        .decodeList<Advice>()
                }

                result.firstOrNull()
            } catch (e: Exception) {
                reportException(e, "AdviceDisplayViewModel")
                null
            }

            val finalResult = fetchedAdvice
                ?: DataStoreRepository.mainScreenAdviceFlow.first()

            DataStoreRepository.setMainScreenAdvice(finalResult)
            _advice.value = finalResult;
            _loading.value = false;
        }
    }
}