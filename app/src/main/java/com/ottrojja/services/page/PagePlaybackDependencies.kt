package com.ottrojja.services.page

import android.content.Context
import com.ottrojja.room.repositories.QuranRepository

/**
 * How PagePlaybackService gets a QuranRepository (it builds playlists and fetches the next page
 * itself, so continuous playback works with the app in the background).
 *
 * Call init() once in Application.onCreate (or wherever the manual DI container is built).
 */
object PagePlaybackDependencies {
    @Volatile
    private var provider: ((Context) -> QuranRepository)? = null

    fun init(provider: (Context) -> QuranRepository) {
        this.provider = provider
    }

    fun repository(context: Context): QuranRepository =
        provider?.invoke(context.applicationContext)
            ?: error("PagePlaybackDependencies.init { ... } must be called in Application.onCreate")
}
