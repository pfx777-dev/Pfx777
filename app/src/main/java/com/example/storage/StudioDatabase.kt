package com.example.storage

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(
    entities = [CreationEntity::class, DownloadEntity::class],
    version = 1,
    exportSchema = false
)
abstract class StudioDatabase : RoomDatabase() {
    abstract fun creationDao(): CreationDao

    companion object {
        @Volatile
        private var INSTANCE: StudioDatabase? = null

        fun getInstance(context: Context): StudioDatabase {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: Room.databaseBuilder(
                    context.applicationContext,
                    StudioDatabase::class.java,
                    "vardaan_creation_studio.db"
                ).build().also { INSTANCE = it }
            }
        }
    }
}
