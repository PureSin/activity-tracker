package com.example.kelvinma.activitytracker.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(entities = [ActivitySession::class], version = 1, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {

    abstract fun activitySessionDao(): ActivitySessionDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "activity_tracker_database"
                ).build()
                INSTANCE = instance
                instance
            }
        }
    }
}
