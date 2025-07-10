package com.example.kelvinma.activitytracker

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.kelvinma.activitytracker.data.Activity
import com.example.kelvinma.activitytracker.data.ActivityRepository
import com.example.kelvinma.activitytracker.ui.activitydetail.ActivityDetailScreen
import com.example.kelvinma.activitytracker.ui.activitylist.ActivityListScreen
import com.example.kelvinma.activitytracker.ui.theme.ActivityTrackerTheme
import com.example.kelvinma.activitytracker.ui.timer.TimerScreen

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            ActivityTrackerTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    val context = LocalContext.current
                    val activityRepository = remember { ActivityRepository(context) }
                    val activities = remember { activityRepository.getActivities() }
                    val navController = rememberNavController()
                    NavHost(navController = navController, startDestination = "activityList") {
                        composable("activityList") {
                            ActivityListScreen(navController, activities)
                        }
                        composable("activityDetail/{activityName}") { backStackEntry ->
                            val activityName = backStackEntry.arguments?.getString("activityName")
                            val activity = activities.find { it.name == activityName }
                            ActivityDetailScreen(navController, activity)
                        }
                        composable("timer/{activityName}") { backStackEntry ->
                            val activityName = backStackEntry.arguments?.getString("activityName")
                            val activity = activities.find { it.name == activityName }
                            if (activity != null) {
                                TimerScreen(activity, {
                                    navController.popBackStack()
                                }, navController)
                            }
                        }
                    }
                }
            }
        }
    }
}