package com.example.kelvinma.activitytracker.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface CategorySessionDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(session: CategorySession)

    @Query("SELECT * FROM category_sessions ORDER BY completion_timestamp DESC")
    fun getAllCategorySessions(): Flow<List<CategorySession>>

    @Query("""
        SELECT * FROM category_sessions 
        WHERE category_name = :categoryName 
        AND completion_date = :date
        LIMIT 1
    """)
    suspend fun getCategorySessionForDate(categoryName: String, date: String): CategorySession?

    @Query("""
        SELECT DISTINCT category_name FROM category_sessions 
        WHERE completion_date = :date
    """)
    suspend fun getCompletedCategoriesForDate(date: String): List<String>

    @Query("""
        SELECT * FROM category_sessions 
        WHERE completion_date = :date
        ORDER BY completion_timestamp DESC
    """)
    fun getCategorySessionsForDate(date: String): Flow<List<CategorySession>>

    @Query("""
        SELECT COUNT(DISTINCT category_name) FROM category_sessions 
        WHERE completion_date = :date
    """)
    suspend fun getCompletedCategoryCountForDate(date: String): Int

    @Query("DELETE FROM category_sessions WHERE completion_date < :cutoffDate")
    suspend fun deleteOldSessions(cutoffDate: String)
}