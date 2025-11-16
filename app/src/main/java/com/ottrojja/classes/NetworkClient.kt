package com.ottrojja.classes

import okhttp3.OkHttpClient
import java.util.concurrent.TimeUnit

object NetworkClient {
    val ottrojjaClient: OkHttpClient by lazy {
        OkHttpClient.Builder()
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(60, TimeUnit.SECONDS)
            .writeTimeout(60, TimeUnit.SECONDS)
            .callTimeout(0, TimeUnit.MILLISECONDS)
            .build()
    }
}