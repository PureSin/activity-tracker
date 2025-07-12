package com.example.kelvinma.activitytracker

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.kelvinma.activitytracker.R
import androidx.navigation.NavController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.kelvinma.activitytracker.data.Activity
import com.example.kelvinma.activitytracker.data.ActivityRepository
import com.example.kelvinma.activitytracker.data.AppDatabase
import com.example.kelvinma.activitytracker.ui.activitydetail.ActivityDetailScreen
import com.example.kelvinma.activitytracker.ui.activitylist.ActivityListScreen
import com.example.kelvinma.activitytracker.ui.analytics.AnalyticsScreen
import com.example.kelvinma.activitytracker.ui.theme.ActivityTrackerTheme
import com.example.kelvinma.activitytracker.ui.timer.TimerScreen
import com.example.kelvinma.activitytracker.util.Logger

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Logger.i(Logger.TAG_NAVIGATION, "MainActivity onCreate")
        
        setContent {
            ActivityTrackerTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    AppContent()
                }
            }
        }
    }
}

@Composable
private fun AppContent() {
    val context = LocalContext.current
    var activities by remember { mutableStateOf<List<Activity>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var database by remember { mutableStateOf<AppDatabase?>(null) }
    
    // Load activities and database asynchronously
    LaunchedEffect(Unit) {
        try {
            Logger.d(Logger.TAG_NAVIGATION, "Loading activities and database")
            
            // Initialize database
            val db = try {
                AppDatabase.getDatabase(context)
            } catch (e: Exception) {
                Logger.e(Logger.TAG_DATABASE, "Failed to initialize database", e)
                throw e
            }
            
            // Load activities
            val activityRepository = ActivityRepository(context)
            val loadedActivities = try {
                activityRepository.getActivities()
            } catch (e: Exception) {
                Logger.e(Logger.TAG_REPOSITORY, "Failed to load activities", e)
                emptyList<Activity>()
            }
            
            if (loadedActivities.isEmpty()) {
                Logger.w(Logger.TAG_NAVIGATION, "No activities loaded - app may not function properly")
                errorMessage = context.getString(R.string.error_no_activities_found)
            } else {
                Logger.i(Logger.TAG_NAVIGATION, "Successfully loaded ${loadedActivities.size} activities")
            }
            
            activities = loadedActivities
            database = db
            isLoading = false
            
        } catch (e: Exception) {
            Logger.e(Logger.TAG_NAVIGATION, "Critical error during app initialization", e)
            errorMessage = context.getString(R.string.error_app_init_failed, e.message ?: "Unknown error")
            isLoading = false
        }
    }
    
    when {
        isLoading -> {
            LoadingScreen()
        }
        errorMessage != null && database == null -> {
            ErrorScreen(errorMessage!!) {
                // Retry initialization
                isLoading = true
                errorMessage = null
            }
        }
        database != null -> {
            MainNavigation(
                activities = activities,
                database = database!!,
                hasActivitiesError = errorMessage != null
            )
        }
    }
}

@Composable
private fun LoadingScreen() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            CircularProgressIndicator()
            Text(
                text = stringResource(R.string.error_loading_title),
                style = MaterialTheme.typography.bodyLarge
            )
        }
    }
}

@Composable
private fun ErrorScreen(errorMessage: String, onRetry: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = stringResource(R.string.error_generic_title),
                style = MaterialTheme.typography.headlineSmall,
                color = MaterialTheme.colorScheme.error
            )
            Text(
                text = errorMessage,
                style = MaterialTheme.typography.bodyMedium,
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onSurface
            )
            Button(onClick = onRetry) {
                Text(stringResource(R.string.error_retry_button))
            }
        }
    }
}

@Composable
private fun MainNavigation(
    activities: List<Activity>,
    database: AppDatabase,
    hasActivitiesError: Boolean
) {
    val navController = rememberNavController()
    
    NavHost(navController = navController, startDestination = "activityList") {
        composable("activityList") {
            ActivityListScreen(navController, activities, database.activitySessionDao())
        }
        
        composable("activityDetail/{activityName}") { backStackEntry ->
            val activityName = backStackEntry.arguments?.getString("activityName")
            Logger.d(Logger.TAG_NAVIGATION, "Navigating to activity detail: $activityName")
            
            if (activityName.isNullOrBlank()) {
                Logger.w(Logger.TAG_NAVIGATION, "Empty activity name provided to activityDetail")
                val context = LocalContext.current
                ErrorScreen(context.getString(R.string.error_invalid_activity_name)) {
                    navController.popBackStack()
                }
                return@composable
            }
            
            val activity = activities.find { it.name == activityName }
            if (activity == null) {
                Logger.w(Logger.TAG_NAVIGATION, "Activity not found: $activityName")
                val context = LocalContext.current
                ErrorScreen(context.getString(R.string.error_activity_not_found, activityName)) {
                    navController.popBackStack()
                }
                return@composable
            }
            
            Logger.logNavigation("activityDetail/$activityName", true)
            ActivityDetailScreen(navController, activity)
        }
        
        composable("timer/{activityName}") { backStackEntry ->
            val activityName = backStackEntry.arguments?.getString("activityName")
            Logger.d(Logger.TAG_NAVIGATION, "Navigating to timer: $activityName")
            
            if (activityName.isNullOrBlank()) {
                Logger.w(Logger.TAG_NAVIGATION, "Empty activity name provided to timer")
                val context = LocalContext.current
                ErrorScreen(context.getString(R.string.error_invalid_activity_name)) {
                    navController.popBackStack()
                }
                return@composable
            }
            
            val activity = activities.find { it.name == activityName }
            if (activity == null) {
                Logger.w(Logger.TAG_NAVIGATION, "Activity not found for timer: $activityName")
                val context = LocalContext.current
                ErrorScreen(context.getString(R.string.error_activity_not_found, activityName)) {
                    navController.popBackStack()
                }
                return@composable
            }
            
            Logger.logNavigation("timer/$activityName", true)
            TimerScreen(activity, {
                Logger.logTimerEvent("Timer finished", "activity: $activityName")
                navController.popBackStack()
            }, navController)
        }
        
        composable("analytics") {
            Logger.logNavigation("analytics", true)
            AnalyticsScreen(navController)
        }
    }
}