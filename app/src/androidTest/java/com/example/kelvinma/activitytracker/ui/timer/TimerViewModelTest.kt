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
                    duration_unit = "seconds",
                    rest_duration = 1,
                    rest_duration_unit = "seconds"
                ),
                Interval(
                    name = "Quick Interval 2",
                    duration = 1,
                    duration_unit = "seconds"
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

        // Skip through all intervals to trigger completion
        viewModel.skipInterval() // Complete first interval
        viewModel.skipInterval() // Complete second interval - should finish activity

        // Wait for session to be saved
        kotlinx.coroutines.delay(100)

        // Verify activity is marked as complete
        assertTrue("Activity should be marked as complete", viewModel.isActivityComplete.value)

        // Progress should be 100% when activity is complete
        assertTrue("Progress should be 100% or close", viewModel.progressPercentage.value >= 95f)

        // Verify session was saved with NATURAL completion
        val sessions = dao.getAllSessions().first()
        assertEquals("Should have one session", 1, sessions.size)

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

        val initialIndex = viewModel.currentIntervalIndex.value
        
        // Skip first interval
        viewModel.skipInterval()
        val afterFirstSkip = viewModel.currentIntervalIndex.value
        assertTrue("Should advance after first skip", afterFirstSkip > initialIndex)

        // Skip second interval - should complete activity
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
        assertTrue("Should have completed less than all intervals", savedSession.intervals_completed < testActivity.intervals.size)
    }
}