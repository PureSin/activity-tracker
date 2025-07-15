package com.example.kelvinma.activitytracker.ui.activitylist

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.captureToImage
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.onRoot
import androidx.compose.ui.graphics.asAndroidBitmap
import androidx.navigation.compose.rememberNavController
import androidx.room.Room
import androidx.test.platform.app.InstrumentationRegistry
import com.example.kelvinma.activitytracker.data.Activity
import com.example.kelvinma.activitytracker.data.AppDatabase
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import java.io.File
import java.io.FileOutputStream

class ActivityListScreenTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    private lateinit var database: AppDatabase

    @Before
    fun setup() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        database = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java)
            .allowMainThreadQueries()
            .build()
    }

    @After
    fun teardown() {
        database.close()
    }

    @Test
    fun activityListScreen_displaysActivities() {
        val activities = listOf(
            Activity("Activity 1", emptyList()),
            Activity("Activity 2", emptyList())
        )
        composeTestRule.setContent {
            val navController = rememberNavController()
            com.example.kelvinma.activitytracker.ui.theme.ActivityTrackerTheme {
                ActivityListScreen(
                    navController = navController, 
                    activities = activities, 
                    activitySessionDao = database.activitySessionDao()
                )
            }
        }

        composeTestRule.onNodeWithText("Activity 1").assertIsDisplayed()
        composeTestRule.onNodeWithText("Activity 2").assertIsDisplayed()
    }

    @Test
    fun activityListScreen_screenshotTest() {
        val activities = listOf(
            Activity("Workout A", emptyList()),
            Activity("Study Session", emptyList()),
            Activity("Meditation", emptyList())
        )
        
        composeTestRule.setContent {
            val navController = rememberNavController()
            com.example.kelvinma.activitytracker.ui.theme.ActivityTrackerTheme {
                ActivityListScreen(
                    navController = navController, 
                    activities = activities,
                    activitySessionDao = database.activitySessionDao()
                )
            }
        }
        
        // Capture screenshot
        val screenshot = composeTestRule.onRoot().captureToImage()
        
        // Save screenshot to device storage for comparison
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        val screenshotDir = File(context.filesDir, "screenshots")
        if (!screenshotDir.exists()) {
            screenshotDir.mkdirs()
        }
        
        val screenshotFile = File(screenshotDir, "activity_list_screen_test.png")
        
        // Compare with existing screenshot if it exists
        if (screenshotFile.exists()) {
            // Load existing screenshot and compare
            val existingScreenshot = screenshotFile.readBytes()
            val currentScreenshot = screenshot.asAndroidBitmap()
            
            // Simple pixel comparison (in real scenario, you'd use a more sophisticated comparison)
            // This is a basic implementation - for production use, consider using a library like Shot or Paparazzi
            val currentBytes = currentScreenshot.toByteArray()
            
            // If screenshots don't match, save the new one with a timestamp
            if (!existingScreenshot.contentEquals(currentBytes)) {
                val timestamp = System.currentTimeMillis()
                val newScreenshotFile = File(screenshotDir, "activity_list_screen_test_$timestamp.png")
                FileOutputStream(newScreenshotFile).use { out ->
                    currentScreenshot.compress(android.graphics.Bitmap.CompressFormat.PNG, 100, out)
                }
                
                throw AssertionError("Screenshot comparison failed. New screenshot saved as: ${newScreenshotFile.name}")
            }
        } else {
            // First run - save the baseline screenshot
            FileOutputStream(screenshotFile).use { out ->
                screenshot.asAndroidBitmap().compress(android.graphics.Bitmap.CompressFormat.PNG, 100, out)
            }
        }
    }
    
    private fun android.graphics.Bitmap.toByteArray(): ByteArray {
        val stream = java.io.ByteArrayOutputStream()
        this.compress(android.graphics.Bitmap.CompressFormat.PNG, 100, stream)
        return stream.toByteArray()
    }
}
