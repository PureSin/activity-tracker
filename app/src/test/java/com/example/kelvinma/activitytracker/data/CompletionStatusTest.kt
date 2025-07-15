package com.example.kelvinma.activitytracker.data

import org.junit.Assert.*
import org.junit.Test

class CompletionStatusTest {

    @Test
    fun testNoActivityStarted() {
        val session = ActivitySession(
            activity_name = "Test",
            start_timestamp = 1000L,
            end_timestamp = 2000L,
            total_intervals_in_activity = 5,
            intervals_completed = 0,
            overall_progress_percentage = 0f,
            had_pauses = false,
            completion_type = null
        )

        val status = session.getCompletionStatus()
        assertEquals(CompletionStatus.PARTIAL_COMPLETION, status)
    }

    @Test
    fun testCompletedEarly() {
        val session = ActivitySession(
            activity_name = "Test",
            start_timestamp = 1000L,
            end_timestamp = 2000L,
            total_intervals_in_activity = 5,
            intervals_completed = 3,
            overall_progress_percentage = 60f,
            had_pauses = false,
            completion_type = CompletionType.EARLY
        )

        val status = session.getCompletionStatus()
        assertEquals(CompletionStatus.COMPLETED_EARLY, status)
    }

    @Test
    fun testCompletedFull() {
        val session = ActivitySession(
            activity_name = "Test",
            start_timestamp = 1000L,
            end_timestamp = 2000L,
            total_intervals_in_activity = 5,
            intervals_completed = 5,
            overall_progress_percentage = 100f,
            had_pauses = false,
            completion_type = CompletionType.NATURAL
        )

        val status = session.getCompletionStatus()
        assertEquals(CompletionStatus.COMPLETED_FULL, status)
    }

    @Test
    fun testCompletedFullWithPause() {
        val session = ActivitySession(
            activity_name = "Test",
            start_timestamp = 1000L,
            end_timestamp = 2000L,
            total_intervals_in_activity = 5,
            intervals_completed = 5,
            overall_progress_percentage = 100f,
            had_pauses = true,
            completion_type = CompletionType.NATURAL
        )

        val status = session.getCompletionStatus()
        assertEquals(CompletionStatus.COMPLETED_FULL_WITH_PAUSE, status)
    }

    @Test
    fun testPartialCompletion() {
        val session = ActivitySession(
            activity_name = "Test",
            start_timestamp = 1000L,
            end_timestamp = 2000L,
            total_intervals_in_activity = 5,
            intervals_completed = 3,
            overall_progress_percentage = 60f,
            had_pauses = false,
            completion_type = CompletionType.NATURAL
        )

        val status = session.getCompletionStatus()
        assertEquals(CompletionStatus.PARTIAL_COMPLETION, status)
    }

    @Test
    fun testPartialCompletionWithPause() {
        val session = ActivitySession(
            activity_name = "Test",
            start_timestamp = 1000L,
            end_timestamp = 2000L,
            total_intervals_in_activity = 5,
            intervals_completed = 3,
            overall_progress_percentage = 60f,
            had_pauses = true,
            completion_type = CompletionType.NATURAL
        )

        val status = session.getCompletionStatus()
        assertEquals(CompletionStatus.PARTIAL_COMPLETION, status)
    }

    @Test
    fun testEarlyCompletionTakesPrecedence() {
        // Even if intervals_completed = total_intervals, early completion should take precedence
        val session = ActivitySession(
            activity_name = "Test",
            start_timestamp = 1000L,
            end_timestamp = 2000L,
            total_intervals_in_activity = 5,
            intervals_completed = 5,
            overall_progress_percentage = 100f,
            had_pauses = false,
            completion_type = CompletionType.EARLY
        )

        val status = session.getCompletionStatus()
        assertEquals(CompletionStatus.COMPLETED_EARLY, status)
    }

    @Test
    fun testEarlyCompletionWithPartialProgress() {
        val session = ActivitySession(
            activity_name = "Test",
            start_timestamp = 1000L,
            end_timestamp = 2000L,
            total_intervals_in_activity = 10,
            intervals_completed = 3,
            overall_progress_percentage = 30f,
            had_pauses = true,
            completion_type = CompletionType.EARLY
        )

        val status = session.getCompletionStatus()
        assertEquals(CompletionStatus.COMPLETED_EARLY, status)
    }

    @Test
    fun testEarlyCompletionRequiresProgress() {
        // Updated: If completion_type is EARLY, it should return COMPLETED_EARLY regardless of progress
        val session = ActivitySession(
            activity_name = "Test",
            start_timestamp = 1000L,
            end_timestamp = 2000L,
            total_intervals_in_activity = 5,
            intervals_completed = 0,
            overall_progress_percentage = 0f,
            had_pauses = false,
            completion_type = CompletionType.EARLY
        )

        val status = session.getCompletionStatus()
        assertEquals(CompletionStatus.COMPLETED_EARLY, status)
    }

    @Test
    fun testBoundaryCase_oneInterval() {
        val session = ActivitySession(
            activity_name = "Test",
            start_timestamp = 1000L,
            end_timestamp = 2000L,
            total_intervals_in_activity = 1,
            intervals_completed = 1,
            overall_progress_percentage = 100f,
            had_pauses = false,
            completion_type = CompletionType.NATURAL
        )

        val status = session.getCompletionStatus()
        assertEquals(CompletionStatus.COMPLETED_FULL, status)
    }

    @Test
    fun testBoundaryCase_zeroIntervals() {
        val session = ActivitySession(
            activity_name = "Test",
            start_timestamp = 1000L,
            end_timestamp = 2000L,
            total_intervals_in_activity = 0,
            intervals_completed = 0,
            overall_progress_percentage = 0f,
            had_pauses = false,
            completion_type = null
        )

        val status = session.getCompletionStatus()
        assertEquals(CompletionStatus.PARTIAL_COMPLETION, status)
    }

    @Test
    fun testEarlyCompletionOnFirstInterval() {
        // User finishes during the first interval - should still return COMPLETED_EARLY
        val session = ActivitySession(
            activity_name = "Test",
            start_timestamp = 1000L,
            end_timestamp = 2000L,
            total_intervals_in_activity = 5,
            intervals_completed = 1,
            overall_progress_percentage = 20f,
            had_pauses = false,
            completion_type = CompletionType.EARLY
        )

        val status = session.getCompletionStatus()
        assertEquals(CompletionStatus.COMPLETED_EARLY, status)
    }

    @Test
    fun testEarlyCompletionOnFirstIntervalWithSingleInterval() {
        // Edge case: Single interval activity completed early
        val session = ActivitySession(
            activity_name = "Test",
            start_timestamp = 1000L,
            end_timestamp = 2000L,
            total_intervals_in_activity = 1,
            intervals_completed = 1,
            overall_progress_percentage = 100f,
            had_pauses = false,
            completion_type = CompletionType.EARLY
        )

        val status = session.getCompletionStatus()
        assertEquals(CompletionStatus.COMPLETED_EARLY, status)
    }

    @Test
    fun testEarlyCompletionBeforeFirstIntervalComplete() {
        // User clicks finish activity at the start of first interval (0 intervals completed)
        val session = ActivitySession(
            activity_name = "Test",
            start_timestamp = 1000L,
            end_timestamp = 2000L,
            total_intervals_in_activity = 5,
            intervals_completed = 0,
            overall_progress_percentage = 0f,
            had_pauses = false,
            completion_type = CompletionType.EARLY
        )

        val status = session.getCompletionStatus()
        assertEquals(CompletionStatus.COMPLETED_EARLY, status)
    }
}