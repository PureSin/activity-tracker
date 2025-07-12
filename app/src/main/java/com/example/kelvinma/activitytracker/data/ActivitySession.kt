package com.example.kelvinma.activitytracker.data

import androidx.room.Entity
import androidx.room.PrimaryKey

enum class CompletionType {
    NATURAL,    // Activity completed all intervals naturally
    EARLY       // User manually finished activity early
}

@Entity(tableName = "activity_sessions")
data class ActivitySession(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val activity_name: String,
    val start_timestamp: Long,
    val end_timestamp: Long,
    val total_intervals_in_activity: Int,
    val intervals_completed: Int,
    val overall_progress_percentage: Float,
    val had_pauses: Boolean,
    val completion_type: CompletionType? = null
)
