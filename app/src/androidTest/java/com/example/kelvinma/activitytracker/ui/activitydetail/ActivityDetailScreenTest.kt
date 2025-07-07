package com.example.kelvinma.activitytracker.ui.activitydetail

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.navigation.compose.rememberNavController
import com.example.kelvinma.activitytracker.data.Activity
import com.example.kelvinma.activitytracker.data.Interval
import org.junit.Rule
import org.junit.Test

class ActivityDetailScreenTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun activityDetailScreen_displaysActivityDetails() {
        val activity = Activity(
            "Test Activity",
            listOf(
                Interval("Warm-up", 300, "seconds"),
                Interval("Work", 600, "seconds")
            )
        )
        composeTestRule.setContent {
            val navController = rememberNavController()
            com.example.kelvinma.activitytracker.ui.theme.ActivityTrackerTheme {
                ActivityDetailScreen(navController = navController, activity = activity)
            }
        }

        composeTestRule.onNodeWithText("Test Activity").assertIsDisplayed()
        composeTestRule.onNodeWithText("Warm-up").assertIsDisplayed()
        composeTestRule.onNodeWithText("Work").assertIsDisplayed()
    }
}
