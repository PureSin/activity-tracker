package com.example.kelvinma.activitytracker.ui.activitydetail

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.kelvinma.activitytracker.data.ActivitySessionDao
import com.example.kelvinma.activitytracker.data.CompletionStatus
import com.example.kelvinma.activitytracker.data.getCompletionStatus
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch

data class ActivityStats(
    val totalSessions: Int = 0,
    val fullCompletions: Int = 0,
    val fullCompletionsWithPause: Int = 0,
    val earlyCompletions: Int = 0,
    val partialCompletions: Int = 0,
    val averageProgress: Float = 0f
) {
    fun totalCompletions(): Int = fullCompletions + fullCompletionsWithPause + earlyCompletions
}

class ActivityDetailViewModel(
    private val activitySessionDao: ActivitySessionDao,
    private val activityName: String
) : ViewModel() {

    private val _stats = MutableStateFlow(ActivityStats())
    val stats: StateFlow<ActivityStats> = _stats

    init {
        loadActivityStats()
    }

    private fun loadActivityStats() {
        viewModelScope.launch {
            activitySessionDao.getSessionsForActivity(activityName).collect { sessions ->
                if (sessions.isNotEmpty()) {
                    val fullCompletions = sessions.count { 
                        it.getCompletionStatus() == CompletionStatus.COMPLETED_FULL 
                    }
                    val fullCompletionsWithPause = sessions.count { 
                        it.getCompletionStatus() == CompletionStatus.COMPLETED_FULL_WITH_PAUSE 
                    }
                    val earlyCompletions = sessions.count { 
                        it.getCompletionStatus() == CompletionStatus.COMPLETED_EARLY 
                    }
                    val partialCompletions = sessions.count { 
                        it.getCompletionStatus() == CompletionStatus.PARTIAL_COMPLETION 
                    }
                    val averageProgress = sessions.map { it.overall_progress_percentage }.average().toFloat()

                    _stats.value = ActivityStats(
                        totalSessions = sessions.size,
                        fullCompletions = fullCompletions,
                        fullCompletionsWithPause = fullCompletionsWithPause,
                        earlyCompletions = earlyCompletions,
                        partialCompletions = partialCompletions,
                        averageProgress = averageProgress
                    )
                } else {
                    _stats.value = ActivityStats()
                }
            }
        }
    }
}