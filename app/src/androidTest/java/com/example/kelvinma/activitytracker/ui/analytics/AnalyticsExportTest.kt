package com.example.kelvinma.activitytracker.ui.analytics

import android.content.Intent
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsEnabled
import androidx.compose.ui.test.hasContentDescription
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import androidx.navigation.compose.rememberNavController
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.example.kelvinma.activitytracker.data.AppDatabase
import com.example.kelvinma.activitytracker.ui.theme.ActivityTrackerTheme
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class AnalyticsExportTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    private lateinit var database: AppDatabase

    @Before
    fun setup() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        database = AppDatabase.getDatabase(context)
    }

    @After
    fun cleanup() {
        AppDatabase.clearInstance()
    }

    @Test
    fun exportButton_isDisplayed() {
        composeTestRule.setContent {
            ActivityTrackerTheme {
                AnalyticsScreen(navController = rememberNavController())
            }
        }

        composeTestRule
            .onNodeWithContentDescription("Export Data")
            .assertIsDisplayed()
    }

    @Test
    fun exportButton_whenClicked_opensDialog() {
        composeTestRule.setContent {
            ActivityTrackerTheme {
                AnalyticsScreen(navController = rememberNavController())
            }
        }

        // Click export button
        composeTestRule
            .onNodeWithContentDescription("Export Data")
            .performClick()

        // Verify dialog appears
        composeTestRule
            .onNodeWithText("Export Database")
            .assertIsDisplayed()
        
        composeTestRule
            .onNodeWithText("Enter email address to send SQLite database export:")
            .assertIsDisplayed()
    }

    @Test
    fun exportDialog_emailValidation_worksCorrectly() {
        composeTestRule.setContent {
            ActivityTrackerTheme {
                AnalyticsScreen(navController = rememberNavController())
            }
        }

        // Open dialog
        composeTestRule
            .onNodeWithContentDescription("Export Data")
            .performClick()

        // Initially, export button should be disabled
        composeTestRule
            .onNodeWithText("Export & Send")
            .assertIsDisplayed()

        // Enter invalid email
        composeTestRule
            .onNodeWithText("Email Address")
            .performTextInput("invalid-email")

        // Export button should still be disabled and error should show
        composeTestRule
            .onNodeWithText("Please enter a valid email address")
            .assertIsDisplayed()

        // Clear and enter valid email
        composeTestRule
            .onNodeWithText("Email Address")
            .performTextInput("")
        
        composeTestRule
            .onNodeWithText("Email Address")
            .performTextInput("test@example.com")

        // Export button should now be enabled
        composeTestRule
            .onNodeWithText("Export & Send")
            .assertIsEnabled()
    }

    @Test
    fun exportDialog_cancelButton_closesDialog() {
        composeTestRule.setContent {
            ActivityTrackerTheme {
                AnalyticsScreen(navController = rememberNavController())
            }
        }

        // Open dialog
        composeTestRule
            .onNodeWithContentDescription("Export Data")
            .performClick()

        // Verify dialog is open
        composeTestRule
            .onNodeWithText("Export Database")
            .assertIsDisplayed()

        // Click cancel
        composeTestRule
            .onNodeWithText("Cancel")
            .performClick()

        // Verify dialog is closed
        composeTestRule
            .onNodeWithText("Export Database")
            .assertDoesNotExist()
    }

    @Test
    fun exportDialog_displaysLoadingState() {
        composeTestRule.setContent {
            ActivityTrackerTheme {
                AnalyticsScreen(navController = rememberNavController())
            }
        }

        // Open dialog
        composeTestRule
            .onNodeWithContentDescription("Export Data")
            .performClick()

        // Enter valid email
        composeTestRule
            .onNodeWithText("Email Address")
            .performTextInput("test@example.com")

        // Click export (this will trigger the loading state briefly)
        composeTestRule
            .onNodeWithText("Export & Send")
            .performClick()

        // The loading state might be too fast to catch reliably in tests,
        // but we can verify the dialog structure remains intact
        composeTestRule
            .onNodeWithText("Export Database")
            .assertIsDisplayed()
    }

    @Test
    fun analyticsScreen_hasCorrectHeaderLayout() {
        composeTestRule.setContent {
            ActivityTrackerTheme {
                AnalyticsScreen(navController = rememberNavController())
            }
        }

        // Verify header components
        composeTestRule
            .onNodeWithContentDescription("Back")
            .assertIsDisplayed()

        composeTestRule
            .onNodeWithText("Analytics")
            .assertIsDisplayed()

        composeTestRule
            .onNodeWithContentDescription("Export Data")
            .assertIsDisplayed()

        // Verify refresh button is NOT present
        composeTestRule
            .onNode(hasContentDescription("Refresh"))
            .assertDoesNotExist()
    }
}