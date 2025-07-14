package com.example.kelvinma.activitytracker.data

import org.junit.Assert.*
import org.junit.Test

class DataModelTest {

    @Test
    fun testActivity_creation() {
        val intervals = listOf(
            Interval(
                name = "Test Interval",
                duration = 30,
                duration_unit = "seconds",
                rest_duration = 10,
                rest_duration_unit = "seconds"
            )
        )
        
        val activity = Activity("Test Activity", intervals)
        
        assertEquals("Test Activity", activity.name)
        assertEquals(1, activity.intervals.size)
        assertEquals("Test Interval", activity.intervals[0].name)
    }

    @Test
    fun testInterval_creation() {
        val interval = Interval(
            name = "Test Interval",
            duration = 45,
            duration_unit = "seconds",
            rest_duration = 15,
            rest_duration_unit = "seconds"
        )
        
        assertEquals("Test Interval", interval.name)
        assertEquals(45, interval.duration)
        assertEquals("seconds", interval.duration_unit)
        assertEquals(15, interval.rest_duration)
        assertEquals("seconds", interval.rest_duration_unit)
    }

    @Test
    fun testInterval_withoutRestDuration() {
        val interval = Interval(
            name = "No Rest Interval",
            duration = 60,
            duration_unit = "seconds"
        )
        
        assertEquals("No Rest Interval", interval.name)
        assertEquals(60, interval.duration)
        assertEquals("seconds", interval.duration_unit)
        assertNull(interval.rest_duration)
        assertNull(interval.rest_duration_unit)
    }

    @Test
    fun testActivitySession_creation() {
        val session = ActivitySession(
            activity_name = "Test Activity",
            start_timestamp = 1000L,
            end_timestamp = 2000L,
            total_intervals_in_activity = 3,
            intervals_completed = 2,
            overall_progress_percentage = 66.67f,
            had_pauses = true,
            completion_type = CompletionType.EARLY
        )
        
        assertEquals("Test Activity", session.activity_name)
        assertEquals(1000L, session.start_timestamp)
        assertEquals(2000L, session.end_timestamp)
        assertEquals(3, session.total_intervals_in_activity)
        assertEquals(2, session.intervals_completed)
        assertEquals(66.67f, session.overall_progress_percentage, 0.01f)
        assertTrue(session.had_pauses)
        assertEquals(CompletionType.EARLY, session.completion_type)
    }

    @Test
    fun testCompletionType_values() {
        val naturalCompletion = CompletionType.NATURAL
        val earlyCompletion = CompletionType.EARLY
        
        assertNotNull(naturalCompletion)
        assertNotNull(earlyCompletion)
        assertNotEquals(naturalCompletion, earlyCompletion)
    }

    @Test
    fun testActivity_multipleIntervals() {
        val intervals = listOf(
            Interval("Interval 1", 30, "seconds", 10, "seconds"),
            Interval("Interval 2", 45, "seconds", 15, "seconds"),
            Interval("Interval 3", 60, "seconds")
        )
        
        val activity = Activity("Multi-Interval Activity", intervals)
        
        assertEquals("Multi-Interval Activity", activity.name)
        assertEquals(3, activity.intervals.size)
        assertEquals("Interval 1", activity.intervals[0].name)
        assertEquals("Interval 2", activity.intervals[1].name)
        assertEquals("Interval 3", activity.intervals[2].name)
        
        // Third interval has no rest duration
        assertNull(activity.intervals[2].rest_duration)
        assertNull(activity.intervals[2].rest_duration_unit)
    }

    @Test
    fun testActivitySession_progressCalculation() {
        val session = ActivitySession(
            activity_name = "Progress Test",
            start_timestamp = 1000L,
            end_timestamp = 2000L,
            total_intervals_in_activity = 4,
            intervals_completed = 3,
            overall_progress_percentage = 75f,
            had_pauses = false
        )
        
        assertEquals(75f, session.overall_progress_percentage, 0.01f)
        
        // Calculate expected progress
        val expectedProgress = (3.0f / 4.0f) * 100f
        assertEquals(expectedProgress, session.overall_progress_percentage, 0.01f)
    }
}