package com.example.kelvinma.activitytracker

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class ActivityInstrumentedTest {

    @get:Rule
    val composeTestRule = createAndroidComposeRule<MainActivity>()

    @Test
    fun appLaunchesAndTitleIsVisible() {
        composeTestRule.onNodeWithText("Activity List").assertIsDisplayed()
    }

    @Test
    fun navigateToTimerScreen() {
        composeTestRule.onNodeWithText("7 Min Workout").performClick()
        composeTestRule.onNodeWithText("Start Activity").performClick()
        composeTestRule.onNodeWithText("Pause").assertIsDisplayed()
    }
}
