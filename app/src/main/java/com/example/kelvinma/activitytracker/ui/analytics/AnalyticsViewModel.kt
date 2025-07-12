package com.example.kelvinma.activitytracker.ui.analytics

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.kelvinma.activitytracker.data.ActivitySession
import com.example.kelvinma.activitytracker.data.ActivitySessionDao
import com.example.kelvinma.activitytracker.data.ActivityStatsRaw
import com.example.kelvinma.activitytracker.data.CompletionStatus
import com.example.kelvinma.activitytracker.data.CompletionType
import com.example.kelvinma.activitytracker.data.getCompletionStatus
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import java.util.*
import kotlin.math.roundToInt

class AnalyticsViewModel(
    private val activitySessionDao: ActivitySessionDao
) : ViewModel() {

    private val _analyticsData = MutableStateFlow(AnalyticsData())
    val analyticsData: StateFlow<AnalyticsData> = _analyticsData

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    init {
        loadAnalyticsData()
    }

    private fun loadAnalyticsData() {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val sessions = activitySessionDao.getSessionsLast30Days()
                val activityStats = activitySessionDao.getActivityPerformanceStats()
                val totalTimeInvested = activitySessionDao.getTotalTimeInvested() ?: 0L

                val analyticsData = calculateAnalytics(sessions, activityStats, totalTimeInvested)
                _analyticsData.value = analyticsData
            } catch (e: Exception) {
                // Handle error - could emit error state or use default data
                _analyticsData.value = AnalyticsData()
            } finally {
                _isLoading.value = false
            }
        }
    }

    private fun calculateAnalytics(
        sessions: List<ActivitySession>,
        activityStats: List<ActivityStatsRaw>,
        totalTimeInvested: Long
    ): AnalyticsData {
        val completionBreakdown = calculateCompletionBreakdown(sessions)
        val activityPerformance = calculateActivityPerformance(activityStats)
        val streakData = calculateStreakData(sessions)
        val insights = generateInsights(sessions, completionBreakdown, streakData)

        return AnalyticsData(
            totalSessions = sessions.size,
            completionRate = completionBreakdown.completionRate,
            currentStreak = streakData.first,
            longestStreak = streakData.second,
            timeInvested = totalTimeInvested,
            completionBreakdown = completionBreakdown,
            activityPerformance = activityPerformance,
            insights = insights
        )
    }

    private fun calculateCompletionBreakdown(sessions: List<ActivitySession>): CompletionBreakdown {
        var full = 0
        var fullWithPause = 0
        var early = 0
        var partial = 0
        var incomplete = 0

        sessions.forEach { session ->
            when (session.getCompletionStatus()) {
                CompletionStatus.COMPLETED_FULL -> full++
                CompletionStatus.COMPLETED_FULL_WITH_PAUSE -> fullWithPause++
                CompletionStatus.COMPLETED_EARLY -> early++
                CompletionStatus.PARTIAL_COMPLETION -> partial++
                CompletionStatus.NO_ACTIVITY_STARTED -> incomplete++
            }
        }

        return CompletionBreakdown(full, fullWithPause, early, partial, incomplete)
    }

    private fun calculateActivityPerformance(activityStats: List<ActivityStatsRaw>): List<ActivityPerformance> {
        return activityStats.map { stats ->
            ActivityPerformance(
                activityName = stats.activity_name,
                totalSessions = stats.total_sessions,
                completions = stats.completions,
                completionRate = if (stats.total_sessions > 0) 
                    (stats.completions.toFloat() / stats.total_sessions) * 100f else 0f,
                averageProgress = stats.avg_progress,
                totalTimeSpent = stats.total_time
            )
        }
    }

    private fun calculateStreakData(sessions: List<ActivitySession>): Pair<Int, Int> {
        if (sessions.isEmpty()) return Pair(0, 0)

        // Sort sessions by date (newest first)
        val sortedSessions = sessions.sortedByDescending { it.start_timestamp }
        
        // Group sessions by day and check if any day has completions
        val dailyCompletions = mutableMapOf<String, Boolean>()
        val calendar = Calendar.getInstance()
        
        sortedSessions.forEach { session ->
            calendar.timeInMillis = session.start_timestamp
            val dateKey = "${calendar.get(Calendar.YEAR)}-${calendar.get(Calendar.DAY_OF_YEAR)}"
            
            val isCompleted = session.getCompletionStatus() in listOf(
                CompletionStatus.COMPLETED_FULL,
                CompletionStatus.COMPLETED_FULL_WITH_PAUSE,
                CompletionStatus.COMPLETED_EARLY
            )
            
            dailyCompletions[dateKey] = dailyCompletions[dateKey] ?: false || isCompleted
        }

        // Calculate current streak
        var currentStreak = 0
        var longestStreak = 0
        var tempStreak = 0
        
        val today = Calendar.getInstance()
        val sortedDates = dailyCompletions.keys.sorted().reversed()
        
        // Check current streak from today backwards
        for (i in 0 until 30) { // Check last 30 days
            today.timeInMillis = System.currentTimeMillis()
            today.add(Calendar.DAY_OF_YEAR, -i)
            val dateKey = "${today.get(Calendar.YEAR)}-${today.get(Calendar.DAY_OF_YEAR)}"
            
            if (dailyCompletions[dateKey] == true) {
                if (i == 0 || currentStreak > 0) currentStreak++
                tempStreak++
                longestStreak = maxOf(longestStreak, tempStreak)
            } else {
                if (i == 0) currentStreak = 0 // If today has no completion, streak is 0
                tempStreak = 0
            }
        }

        return Pair(currentStreak, longestStreak)
    }

    private fun generateInsights(
        sessions: List<ActivitySession>,
        breakdown: CompletionBreakdown,
        streakData: Pair<Int, Int>
    ): List<Insight> {
        val insights = mutableListOf<Insight>()

        // Streak insights
        if (streakData.first > 0) {
            insights.add(
                Insight(
                    type = InsightType.STREAK,
                    title = "Great Streak!",
                    message = "You're on a ${streakData.first}-day streak! Keep it up!",
                    isActionable = false
                )
            )
        }

        // Completion rate insights
        if (breakdown.completionRate >= 80f) {
            insights.add(
                Insight(
                    type = InsightType.PERFORMANCE,
                    title = "Excellent Performance",
                    message = "Your completion rate of ${breakdown.completionRate.roundToInt()}% is outstanding!",
                    isActionable = false
                )
            )
        } else if (breakdown.completionRate < 50f && sessions.isNotEmpty()) {
            insights.add(
                Insight(
                    type = InsightType.RECOMMENDATION,
                    title = "Room for Improvement",
                    message = "Consider starting with shorter sessions to build consistency.",
                    isActionable = true
                )
            )
        }

        // Early completion insights
        if (breakdown.early > breakdown.full) {
            insights.add(
                Insight(
                    type = InsightType.PATTERN,
                    title = "Early Completion Pattern",
                    message = "You tend to finish activities early. Consider adjusting activity duration.",
                    isActionable = true
                )
            )
        }

        // Motivation insights
        if (streakData.first == 0 && sessions.isNotEmpty()) {
            insights.add(
                Insight(
                    type = InsightType.MOTIVATION,
                    title = "Start Fresh",
                    message = "Every expert was once a beginner. Start your new streak today!",
                    isActionable = true
                )
            )
        }

        return insights
    }

    fun refreshData() {
        loadAnalyticsData()
    }
}