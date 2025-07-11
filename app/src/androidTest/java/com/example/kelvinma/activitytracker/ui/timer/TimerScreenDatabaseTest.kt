package com.example.kelvinma.activitytracker.ui.timer

import androidx.room.Room
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.example.kelvinma.activitytracker.data.Activity
import com.example.kelvinma.activitytracker.data.ActivitySession
import com.example.kelvinma.activitytracker.data.AppDatabase
import com.example.kelvinma.activitytracker.data.Interval
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class TimerScreenDatabaseTest {

    private lateinit var database: AppDatabase
    private lateinit var testActivity: Activity

    @Before
    fun setup() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        database = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java)
            .allowMainThreadQueries()
            .build()

        testActivity = Activity(
            name = "Test Activity",
            intervals = listOf(
                Interval(
                    name = "Interval 1",
                    duration = 1,
                    duration_unit = "seconds",
                    rest_duration = 0,
                    rest_duration_unit = "seconds"
                ),
                Interval(
                    name = "Interval 2", 
                    duration = 1,
                    duration_unit = "seconds",
                    rest_duration = 0,
                    rest_duration_unit = "seconds"
                )
            )
        )
    }

    @After
    fun teardown() {
        database.close()
    }

    @Test
    fun activitySessionDao_insertSession_savesCorrectly() = runBlocking {
        // Given: A test activity session
        val currentTime = System.currentTimeMillis()
        val testSession = ActivitySession(
            activity_name = "Test Activity",
            start_timestamp = currentTime,
            end_timestamp = currentTime + 1000,
            total_intervals_in_activity = 2,
            intervals_completed = 0,
            overall_progress_percentage = 0f,
            had_pauses = false
        )

        // When: Session is inserted into database
        database.activitySessionDao().insert(testSession)

        // Then: Session should be retrievable from database
        val sessions = database.activitySessionDao().getAllSessions().first()
        assertEquals("One session should be saved", 1, sessions.size)

        val savedSession = sessions[0]
        assertEquals("Test Activity", savedSession.activity_name)
        assertEquals(2, savedSession.total_intervals_in_activity)
        assertEquals(0, savedSession.intervals_completed)
        assertEquals(0f, savedSession.overall_progress_percentage, 0.1f)
        assertEquals(false, savedSession.had_pauses)
        assertTrue("Session should have start timestamp", savedSession.start_timestamp > 0)
        assertTrue("Session should have end timestamp", savedSession.end_timestamp > 0)
    }

    @Test
    fun activitySessionDao_insertSessionWithPauses_recordsPauseCorrectly() = runBlocking {
        // Given: A test activity session with pauses
        val currentTime = System.currentTimeMillis()
        val testSession = ActivitySession(
            activity_name = "Test Activity",
            start_timestamp = currentTime,
            end_timestamp = currentTime + 2000,
            total_intervals_in_activity = 2,
            intervals_completed = 1,
            overall_progress_percentage = 50f,
            had_pauses = true
        )

        // When: Session is inserted into database
        database.activitySessionDao().insert(testSession)

        // Then: Session should record that there were pauses
        val sessions = database.activitySessionDao().getAllSessions().first()
        assertEquals(1, sessions.size)
        
        val savedSession = sessions[0]
        assertTrue("Session should record pauses", savedSession.had_pauses)
        assertEquals(1, savedSession.intervals_completed)
        assertEquals(50f, savedSession.overall_progress_percentage, 0.1f)
    }

    @Test
    fun activitySessionDao_insertPartialSession_recordsCorrectProgress() = runBlocking {
        // Given: A partially completed session
        val currentTime = System.currentTimeMillis()
        val testSession = ActivitySession(
            activity_name = "Test Activity",
            start_timestamp = currentTime,
            end_timestamp = currentTime + 500,
            total_intervals_in_activity = 2,
            intervals_completed = 0,
            overall_progress_percentage = 0f,
            had_pauses = false
        )

        // When: Session is inserted
        database.activitySessionDao().insert(testSession)

        // Then: Session should record correct progress for early termination
        val sessions = database.activitySessionDao().getAllSessions().first()
        assertEquals(1, sessions.size)
        
        val savedSession = sessions[0]
        assertEquals(0, savedSession.intervals_completed)
        assertEquals(0f, savedSession.overall_progress_percentage, 0.1f)
        assertEquals(false, savedSession.had_pauses)
    }
}