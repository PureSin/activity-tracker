package com.example.kelvinma.activitytracker.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface ActivitySessionDao {

    @Insert
    suspend fun insert(session: ActivitySession)

    @Update
    suspend fun update(session: ActivitySession)

    @Query("SELECT * FROM activity_sessions ORDER BY start_timestamp DESC")
    fun getAllSessions(): Flow<List<ActivitySession>>

    @Query("SELECT * FROM activity_sessions WHERE activity_name = :activityName ORDER BY start_timestamp DESC")
    fun getSessionsForActivity(activityName: String): Flow<List<ActivitySession>>

    @Query("""
        SELECT * FROM activity_sessions 
        WHERE start_timestamp >= :startOfDay AND start_timestamp < :endOfDay
        ORDER BY start_timestamp DESC
    """)
    fun getSessionsForDay(startOfDay: Long, endOfDay: Long): Flow<List<ActivitySession>>

    @Query("""
        SELECT * FROM activity_sessions 
        WHERE activity_name = :activityName 
        AND start_timestamp >= :startOfDay AND start_timestamp < :endOfDay
        ORDER BY start_timestamp DESC
    """)
    fun getSessionsForActivityAndDay(activityName: String, startOfDay: Long, endOfDay: Long): Flow<List<ActivitySession>>

    @Query("""
        SELECT COUNT(*) FROM activity_sessions 
        WHERE intervals_completed > 0 
        OR completion_type = 'EARLY'
    """)
    suspend fun getTotalCompletedSessions(): Int

    @Query("""
        SELECT COUNT(*) FROM activity_sessions 
        WHERE intervals_completed = total_intervals_in_activity 
        AND had_pauses = 0
    """)
    suspend fun getFullCompletionCount(): Int

    @Query("""
        SELECT COUNT(*) FROM activity_sessions 
        WHERE intervals_completed = total_intervals_in_activity 
        AND had_pauses = 1
    """)
    suspend fun getFullCompletionWithPauseCount(): Int

    @Query("""
        SELECT COUNT(*) FROM activity_sessions 
        WHERE intervals_completed > 0 
        AND intervals_completed < total_intervals_in_activity
    """)
    suspend fun getPartialCompletionCount(): Int

    @Query("""
        SELECT COUNT(*) FROM activity_sessions 
        WHERE completion_type = 'EARLY'
    """)
    suspend fun getEarlyCompletionCount(): Int
}
