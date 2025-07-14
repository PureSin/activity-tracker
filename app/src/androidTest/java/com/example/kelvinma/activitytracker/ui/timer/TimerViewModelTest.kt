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

        viewModel = TimerViewModel(testActivity, dao, context)
    }

    @After
    fun tearDown() {
        database.close()
    }

    @Test
    fun testActivityCompletionFlow() = runBlocking {

        // Skip through all intervals to trigger completion
        viewModel.skipInterval() // Complete first interval
        viewModel.skipInterval() // Complete second interval - should finish activity

        // Verify activity is marked as complete
        assertTrue("Activity should be marked as complete", viewModel.isActivityComplete.value)

        // Verify progress is 100%
        assertEquals("Progress should be 100%", 100f, viewModel.progressPercentage.value, 0.1f)

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

        // Initially should be at first interval
        assertEquals("Should start at interval 0", 0, viewModel.currentIntervalIndex.value)

        // Skip first interval
        viewModel.skipInterval()
        assertEquals("Should be at interval 1", 1, viewModel.currentIntervalIndex.value)

        // Skip second interval - should complete activity
        viewModel.skipInterval()
        assertEquals("Should be at interval 2 (past last interval)", 2, viewModel.currentIntervalIndex.value)
        assertTrue("Activity should be complete", viewModel.isActivityComplete.value)
    }

    @Test
    fun testEarlyStopCompletion() = runBlocking {

        // Stop activity before completion
        viewModel.stopActivity()

        // Verify session was saved with EARLY completion
        val sessions = dao.getAllSessions().first()
        assertEquals("Should have one session", 1, sessions.size)

        val savedSession = sessions[0]
        assertEquals("Should be early completion", CompletionType.EARLY, savedSession.completion_type)
        assertTrue("Should have completed less than all intervals", savedSession.intervals_completed < testActivity.intervals.size)
    }
}