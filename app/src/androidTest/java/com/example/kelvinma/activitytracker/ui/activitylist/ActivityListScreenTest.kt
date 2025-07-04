package com.example.kelvinma.activitytracker.ui.activitylist

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.navigation.compose.rememberNavController
import com.example.kelvinma.activitytracker.data.Activity
import org.junit.Rule
import org.junit.Test

class ActivityListScreenTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun activityListScreen_displaysActivities() {
        val activities = listOf(
            Activity("Activity 1", emptyList()),
            Activity("Activity 2", emptyList())
        )
        composeTestRule.setContent {
            val navController = rememberNavController()
            ActivityListScreen(navController = navController, activities = activities)
        }

        composeTestRule.onNodeWithText("Activity 1").assertIsDisplayed()
        composeTestRule.onNodeWithText("Activity 2").assertIsDisplayed()
    }
}
