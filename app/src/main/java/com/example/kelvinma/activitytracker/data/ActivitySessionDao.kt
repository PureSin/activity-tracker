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

    @Query("""
        SELECT SUM(end_timestamp - start_timestamp) 
        FROM activity_sessions 
        WHERE intervals_completed > 0
    """)
    suspend fun getTotalTimeInvested(): Long?

    @Query("""
        SELECT activity_name, COUNT(*) as total_sessions, 
               SUM(CASE WHEN intervals_completed = total_intervals_in_activity OR completion_type = 'EARLY' THEN 1 ELSE 0 END) as completions,
               AVG(overall_progress_percentage) as avg_progress,
               SUM(end_timestamp - start_timestamp) as total_time
        FROM activity_sessions 
        GROUP BY activity_name
    """)
    suspend fun getActivityPerformanceStats(): List<ActivityStatsRaw>

    @Query("""
        SELECT * FROM activity_sessions 
        WHERE date(start_timestamp/1000, 'unixepoch') >= date('now', '-30 days')
        ORDER BY start_timestamp DESC
    """)
    suspend fun getSessionsLast30Days(): List<ActivitySession>

    @Query("""
        SELECT DISTINCT date(start_timestamp/1000, 'unixepoch') as date,
               SUM(CASE WHEN intervals_completed = total_intervals_in_activity OR completion_type = 'EARLY' THEN 1 ELSE 0 END) as completed_sessions
        FROM activity_sessions 
        WHERE start_timestamp >= :startTimestamp
        GROUP BY date(start_timestamp/1000, 'unixepoch')
        ORDER BY date DESC
    """)
    suspend fun getDailyCompletionData(startTimestamp: Long): List<DailyCompletionRaw>
}

data class ActivityStatsRaw(
    val activity_name: String,
    val total_sessions: Int,
    val completions: Int,
    val avg_progress: Float,
    val total_time: Long
)

data class DailyCompletionRaw(
    val date: String,
    val completed_sessions: Int
)
