package com.ottrojja.room.database

import android.content.Context
import androidx.room.Room

object DatabaseProvider {

    @Volatile
    private var INSTANCE: QuranDatabase? = null

    fun getDatabase(context: Context): QuranDatabase {
        return INSTANCE ?: synchronized(this) {
            Room.databaseBuilder(
                context.applicationContext,
                QuranDatabase::class.java,
                "QuranDB"
            ).addMigrations(MIGRATION_1_2, MIGRATION_2_3, MIGRATION_3_4, MIGRATION_4_5, MIGRATION_5_6,
                MIGRATION_6_7, MIGRATION_7_8
            )
                .fallbackToDestructiveMigration(false)
                .build().also {
                    INSTANCE = it
                }
        }
    }
}