package com.example.kelvinma.activitytracker.data

enum class CompletionStatus {
    COMPLETED_FULL,
    COMPLETED_FULL_WITH_PAUSE,
    COMPLETED_EARLY,
    PARTIAL_COMPLETION
}

fun ActivitySession.getCompletionStatus(): CompletionStatus {
    return when {
        completion_type == CompletionType.EARLY -> CompletionStatus.COMPLETED_EARLY
        total_intervals_in_activity == 0 -> CompletionStatus.PARTIAL_COMPLETION
        intervals_completed == total_intervals_in_activity && !had_pauses -> CompletionStatus.COMPLETED_FULL
        intervals_completed == total_intervals_in_activity && had_pauses -> CompletionStatus.COMPLETED_FULL_WITH_PAUSE
        else -> CompletionStatus.PARTIAL_COMPLETION
    }
}