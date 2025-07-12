package com.example.kelvinma.activitytracker.ui.analytics

import kotlin.time.Duration

data class AnalyticsData(
    val totalSessions: Int = 0,
    val completionRate: Float = 0f,
    val currentStreak: Int = 0,
    val longestStreak: Int = 0,
    val timeInvested: Long = 0, // in milliseconds
    val completionBreakdown: CompletionBreakdown = CompletionBreakdown(),
    val activityPerformance: List<ActivityPerformance> = emptyList(),
    val insights: List<Insight> = emptyList()
)

data class CompletionBreakdown(
    val full: Int = 0,
    val fullWithPause: Int = 0,
    val early: Int = 0,
    val partial: Int = 0,
    val incomplete: Int = 0
) {
    val totalCompletions: Int get() = full + fullWithPause + early
    val completionRate: Float get() = if (totalSessions > 0) (totalCompletions.toFloat() / totalSessions) * 100f else 0f
    val totalSessions: Int get() = full + fullWithPause + early + partial + incomplete
}

data class ActivityPerformance(
    val activityName: String,
    val totalSessions: Int,
    val completions: Int,
    val completionRate: Float,
    val averageProgress: Float,
    val totalTimeSpent: Long // in milliseconds
)

data class Insight(
    val type: InsightType,
    val title: String,
    val message: String,
    val isActionable: Boolean = false
)

enum class InsightType {
    STREAK,
    PERFORMANCE,
    PATTERN,
    MOTIVATION,
    RECOMMENDATION
}

data class WeeklyStats(
    val weekStartTimestamp: Long,
    val sessionsCompleted: Int,
    val totalSessions: Int,
    val completionRate: Float
)

data class DailyCompletionStatus(
    val date: Long, // timestamp for the day
    val hasCompletedActivity: Boolean,
    val sessionsCount: Int = 0
)