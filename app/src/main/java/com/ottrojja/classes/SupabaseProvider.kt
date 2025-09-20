package com.ottrojja.classes

import com.ottrojja.BuildConfig
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.createSupabaseClient
import io.github.jan.supabase.postgrest.Postgrest


object SupabaseProvider {

    @Volatile
    private var _client: SupabaseClient? = null

    val client: SupabaseClient
        get() = _client ?: synchronized(this) {
            _client ?: createSupabaseClient(
                supabaseUrl = BuildConfig.SUPABASE_URL,
                supabaseKey = BuildConfig.SUPABASE_PUBLIC_KEY
            ) {
                install(Postgrest)
            }.also { _client = it }
        }
}
