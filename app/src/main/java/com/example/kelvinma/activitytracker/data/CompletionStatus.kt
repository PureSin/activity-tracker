package com.example.kelvinma.activitytracker.data

enum class CompletionStatus {
    COMPLETED_FULL,
    COMPLETED_FULL_WITH_PAUSE,
    COMPLETED_EARLY,
    PARTIAL_COMPLETION,
    NO_ACTIVITY_STARTED
}

fun ActivitySession.getCompletionStatus(): CompletionStatus {
    return when {
        intervals_completed == 0 -> CompletionStatus.NO_ACTIVITY_STARTED
        completion_type == CompletionType.EARLY && intervals_completed > 0 -> CompletionStatus.COMPLETED_EARLY
        intervals_completed == total_intervals_in_activity && !had_pauses -> CompletionStatus.COMPLETED_FULL
        intervals_completed == total_intervals_in_activity && had_pauses -> CompletionStatus.COMPLETED_FULL_WITH_PAUSE
        intervals_completed > 0 && intervals_completed < total_intervals_in_activity -> CompletionStatus.PARTIAL_COMPLETION
        else -> CompletionStatus.NO_ACTIVITY_STARTED
    }
}