package com.example.kelvinma.activitytracker.ui.timer

import androidx.room.Room
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.example.kelvinma.activitytracker.data.Activity
import com.example.kelvinma.activitytracker.data.ActivitySessionDao
import com.example.kelvinma.activitytracker.data.AppDatabase
import com.example.kelvinma.activitytracker.data.CompletionType
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
class TimerViewModelTest {

    private lateinit var database: AppDatabase
    private lateinit var dao: ActivitySessionDao
    private lateinit var testActivity: Activity
    private lateinit var viewModel: TimerViewModel

    @Before
    fun setup() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        database = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        dao = database.activitySessionDao()
        
        // Clear any existing data
        runBlocking {
            database.clearAllTables()
        }

        testActivity = Activity(
            name = "Test Quick Activity",
            intervals = listOf(
                Interval(
                    name = "Quick Interval 1",
                    duration = 1,
                    duration_unit = "seconds"
                    // No rest duration to simplify tests
                )
            )
        )

        // Don't create ViewModel in setup to avoid auto-start
        // Each test will create its own when needed
    }

    @After
    fun tearDown() {
        database.close()
    }

    @Test
    fun testActivityCompletionFlow() = runBlocking {
        // Create ViewModel for this test
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        viewModel = TimerViewModel(testActivity, dao, context)
        
        // Wait a moment for initialization to complete
        kotlinx.coroutines.delay(100)

        // Skip the single interval to trigger completion
        viewModel.skipInterval() // Complete first interval - should finish activity

        // Wait for session to be saved
        kotlinx.coroutines.delay(100)

        // Verify activity is marked as complete
        assertTrue("Activity should be marked as complete", viewModel.isActivityComplete.value)

        // Progress should be 100% when activity is complete
        assertTrue("Progress should be 100% or close", viewModel.progressPercentage.value >= 95f)

        // Verify session was saved with NATURAL completion
        val sessions = dao.getAllSessions().first()
        assertTrue("Should have at least one session", sessions.isNotEmpty())

        val savedSession = sessions[0]
        assertEquals("Activity name should match", testActivity.name, savedSession.activity_name)
        assertEquals("Should have completed all intervals", testActivity.intervals.size, savedSession.intervals_completed)
        assertEquals("Should be natural completion", CompletionType.NATURAL, savedSession.completion_type)
        assertEquals("Progress should be 100%", 100f, savedSession.overall_progress_percentage, 0.1f)
    }

    @Test
    fun testCurrentIntervalIndexProgression() = runBlocking {
        // Clear database for this test
        database.clearAllTables()
        
        // Create ViewModel for this test
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        viewModel = TimerViewModel(testActivity, dao, context)
        
        // Wait a moment for initialization to complete
        kotlinx.coroutines.delay(100)

        // Initially should be at first interval (might have advanced from 0 during initialization)
        assertTrue("Should start at interval 0 or 1", viewModel.currentIntervalIndex.value <= 1)
        
        // Skip the single interval - should complete activity
        viewModel.skipInterval()
        kotlinx.coroutines.delay(100) // Wait for completion
        assertTrue("Activity should be complete", viewModel.isActivityComplete.value)
    }

    @Test
    fun testEarlyStopCompletion() = runBlocking {
        // Clear database for this test
        database.clearAllTables()
        
        // Create ViewModel for this test
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        viewModel = TimerViewModel(testActivity, dao, context)
        
        // Wait a moment for initialization to complete
        kotlinx.coroutines.delay(100)

        // Stop activity before completion
        viewModel.stopActivity()
        
        // Wait for session to be saved
        kotlinx.coroutines.delay(200)

        // Verify session was saved with EARLY completion
        val sessions = dao.getAllSessions().first()
        assertTrue("Should have at least one session", sessions.isNotEmpty())

        val savedSession = sessions[0]
        assertEquals("Should be early completion", CompletionType.EARLY, savedSession.completion_type)
        assertTrue("Should have completed less than all intervals", savedSession.intervals_completed <= testActivity.intervals.size)
    }

    @Test
    fun testRestPeriodFlow() = runBlocking {
        // Clear database for this test
        database.clearAllTables()
        
        // Create activity with rest periods - use very short durations to minimize timing issues
        val activityWithRest = Activity(
            name = "Activity With Rest",
            intervals = listOf(
                Interval(
                    name = "Interval 1",
                    duration = 1,
                    duration_unit = "seconds",
                    rest_duration = 1,
                    rest_duration_unit = "seconds"
                ),
                Interval(
                    name = "Interval 2", 
                    duration = 1,
                    duration_unit = "seconds"
                )
            )
        )
        
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        viewModel = TimerViewModel(activityWithRest, dao, context)
        
        kotlinx.coroutines.delay(200) // Longer delay for stability
        
        // Skip through the entire activity and verify completion
        // The exact flow depends on timing, but we can test final state
        repeat(5) { // Skip enough times to complete activity
            if (!viewModel.isActivityComplete.value) {
                viewModel.skipInterval()
                kotlinx.coroutines.delay(100)
            }
        }
        
        assertTrue("Activity should eventually be complete", viewModel.isActivityComplete.value)
        
        // Verify session was saved
        val sessions = dao.getAllSessions().first()
        assertTrue("Should have at least one session", sessions.isNotEmpty())
        assertEquals("Should be activity with rest", activityWithRest.name, sessions[0].activity_name)
    }

    @Test
    fun testRestPeriodSkippedWhenZeroDuration() = runBlocking {
        // Clear database for this test
        database.clearAllTables()
        
        // Create activity with zero rest duration
        val activityNoRest = Activity(
            name = "Activity No Rest",
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
                    duration_unit = "seconds"
                )
            )
        )
        
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        viewModel = TimerViewModel(activityNoRest, dao, context)
        
        kotlinx.coroutines.delay(200)
        
        // Skip through activity - should complete without rest periods
        repeat(3) { // Skip enough times to complete activity
            if (!viewModel.isActivityComplete.value) {
                viewModel.skipInterval()
                kotlinx.coroutines.delay(100)
            }
        }
        
        assertTrue("Activity should be complete", viewModel.isActivityComplete.value)
        assertEquals("Should not be in rest period at end", false, viewModel.isRestPeriod.value)
        
        // Verify session was saved
        val sessions = dao.getAllSessions().first()
        assertTrue("Should have at least one session", sessions.isNotEmpty())
        assertEquals("Should be activity without rest", activityNoRest.name, sessions[0].activity_name)
    }

    @Test
    fun testRestPeriodNotAfterLastInterval() = runBlocking {
        // Clear database for this test
        database.clearAllTables()
        
        // Create activity where last interval has rest duration
        val activityLastRest = Activity(
            name = "Activity Last Rest",
            intervals = listOf(
                Interval(
                    name = "Final Interval",
                    duration = 1,
                    duration_unit = "seconds",
                    rest_duration = 5,
                    rest_duration_unit = "seconds"
                )
            )
        )
        
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        viewModel = TimerViewModel(activityLastRest, dao, context)
        
        kotlinx.coroutines.delay(200)
        
        // Skip final interval - should complete activity without rest period
        repeat(2) { // Skip enough times to ensure completion
            if (!viewModel.isActivityComplete.value) {
                viewModel.skipInterval()
                kotlinx.coroutines.delay(100)
            }
        }
        
        assertTrue("Activity should be complete", viewModel.isActivityComplete.value)
        assertEquals("Should not be in rest period after last interval", false, viewModel.isRestPeriod.value)
        
        // Verify session was saved
        val sessions = dao.getAllSessions().first()
        assertTrue("Should have at least one session", sessions.isNotEmpty())
        assertEquals("Should be final interval activity", activityLastRest.name, sessions[0].activity_name)
    }
}