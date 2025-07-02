package com.example.kelvinma.activitytracker.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface ActivitySessionDao {

    @Insert
    suspend fun insert(session: ActivitySession)

    @Query("SELECT * FROM activity_sessions ORDER BY start_timestamp DESC")
    fun getAllSessions(): Flow<List<ActivitySession>>
}
