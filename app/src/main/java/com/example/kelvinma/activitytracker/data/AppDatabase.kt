package com.example.kelvinma.activitytracker.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.kelvinma.activitytracker.util.Logger

@Database(entities = [ActivitySession::class], version = 2, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {

    abstract fun activitySessionDao(): ActivitySessionDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        private val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {
                try {
                    Logger.i(Logger.TAG_MIGRATION, "Starting migration from version 1 to 2")
                    db.execSQL("ALTER TABLE activity_sessions ADD COLUMN completion_type TEXT")
                    Logger.i(Logger.TAG_MIGRATION, "Successfully completed migration from version 1 to 2")
                } catch (e: Exception) {
                    Logger.e(Logger.TAG_MIGRATION, "Failed to migrate database from version 1 to 2", e)
                    throw e // Re-throw to let Room handle the failure
                }
            }
        }

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                try {
                    Logger.d(Logger.TAG_DATABASE, "Creating database instance")
                    val instance = Room.databaseBuilder(
                        context.applicationContext,
                        AppDatabase::class.java,
                        "activity_tracker_database"
                    )
                    .addMigrations(MIGRATION_1_2)
                    .fallbackToDestructiveMigration() // Add fallback for unhandled migrations
                    .setJournalMode(RoomDatabase.JournalMode.TRUNCATE) // Better for single-user apps
                    .build()
                    
                    INSTANCE = instance
                    Logger.i(Logger.TAG_DATABASE, "Database instance created successfully")
                    instance
                } catch (e: Exception) {
                    Logger.e(Logger.TAG_DATABASE, "Failed to create database instance", e)
                    // Don't cache failed instance
                    INSTANCE = null
                    throw e
                }
            }
        }
        
        /**
         * Clears the database instance. Used for testing.
         */
        fun clearInstance() {
            synchronized(this) {
                INSTANCE?.close()
                INSTANCE = null
                Logger.d(Logger.TAG_DATABASE, "Database instance cleared")
            }
        }
    }
}
