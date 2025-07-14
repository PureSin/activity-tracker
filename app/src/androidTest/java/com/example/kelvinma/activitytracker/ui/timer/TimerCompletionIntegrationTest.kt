package com.example.kelvinma.activitytracker.ui.timer

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.navigation.compose.rememberNavController
import androidx.room.Room
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.example.kelvinma.activitytracker.data.Activity
import com.example.kelvinma.activitytracker.data.AppDatabase
import com.example.kelvinma.activitytracker.data.CompletionType
import com.example.kelvinma.activitytracker.data.Interval
import com.example.kelvinma.activitytracker.ui.theme.ActivityTrackerTheme
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class TimerCompletionIntegrationTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    private lateinit var database: AppDatabase
    private lateinit var testActivity: Activity

    @Before
    fun setup() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        database = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java)
            .allowMainThreadQueries()
            .build()

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
    }

    @After
    fun teardown() {
        database.close()
    }

    @Test
    fun timerCompletion_fullFlow_savesSessionCorrectly() {
        composeTestRule.setContent {
            ActivityTrackerTheme {
                val navController = rememberNavController()
                TimerScreen(
                    activity = testActivity,
                    onFinish = { },
                    navController = navController
                )
            }
        }

        // Wait for timer to initialize
        composeTestRule.waitForIdle()

        // Verify timer screen is displayed
        composeTestRule.onNodeWithText("Test Quick Activity").assertIsDisplayed()

        // Skip through intervals quickly to complete activity
        composeTestRule.onNodeWithText("Skip").performClick()
        composeTestRule.waitForIdle()
        
        composeTestRule.onNodeWithText("Skip").performClick()
        composeTestRule.waitForIdle()

        // Verify session was saved to database with correct completion status
        runBlocking {
            val sessions = database.activitySessionDao().getAllSessions().first()
            assertEquals("Should have one session", 1, sessions.size)

            val session = sessions[0]
            assertEquals("Activity name should match", testActivity.name, session.activity_name)
            assertEquals("Should have completed all intervals", testActivity.intervals.size, session.intervals_completed)
            assertEquals("Should be natural completion", CompletionType.NATURAL, session.completion_type)
            assertEquals("Progress should be 100%", 100f, session.overall_progress_percentage, 0.1f)
            assertTrue("Session should have valid timestamps", session.start_timestamp > 0 && session.end_timestamp > 0)
        }
    }

    @Test
    fun timerCompletion_verifySessionProgressTracking() {
        composeTestRule.setContent {
            ActivityTrackerTheme {
                val navController = rememberNavController()
                TimerScreen(
                    activity = testActivity,
                    onFinish = { },
                    navController = navController
                )
            }
        }

        composeTestRule.waitForIdle()

        // Skip first interval
        composeTestRule.onNodeWithText("Skip").performClick()
        composeTestRule.waitForIdle()

        // Skip second interval to complete activity
        composeTestRule.onNodeWithText("Skip").performClick()
        composeTestRule.waitForIdle()

        // Verify activity completion state
        composeTestRule.onNodeWithText("Activity Complete!").assertIsDisplayed()

        // Verify final session state in database
        runBlocking {
            val sessions = database.activitySessionDao().getAllSessions().first()
            val session = sessions[0]
            
            assertEquals("All intervals should be completed", testActivity.intervals.size, session.intervals_completed)
            assertEquals("Progress should be 100%", 100f, session.overall_progress_percentage, 0.1f)
            assertEquals("Should be natural completion", CompletionType.NATURAL, session.completion_type)
        }
    }
}