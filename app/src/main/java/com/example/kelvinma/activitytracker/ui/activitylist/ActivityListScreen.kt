package com.example.kelvinma.activitytracker.ui.activitylist

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.kelvinma.activitytracker.data.Activity
import com.example.kelvinma.activitytracker.data.ActivitySessionDao
import com.example.kelvinma.activitytracker.data.CompletionStatus
import com.example.kelvinma.activitytracker.data.getCompletionStatus
import java.util.Calendar

fun getTodayTimestamps(): Pair<Long, Long> {
    val calendar = Calendar.getInstance()
    calendar.set(Calendar.HOUR_OF_DAY, 0)
    calendar.set(Calendar.MINUTE, 0)
    calendar.set(Calendar.SECOND, 0)
    calendar.set(Calendar.MILLISECOND, 0)
    val startOfDay = calendar.timeInMillis
    
    calendar.add(Calendar.DAY_OF_MONTH, 1)
    val endOfDay = calendar.timeInMillis
    
    return Pair(startOfDay, endOfDay)
}

fun calculateTotalDuration(activity: Activity): String {
    var totalSeconds = 0
    
    for (interval in activity.intervals) {
        // Convert interval duration to seconds
        val intervalSeconds = when (interval.duration_unit.lowercase()) {
            "minutes" -> interval.duration * 60
            "hours" -> interval.duration * 3600
            else -> interval.duration // assume seconds
        }
        totalSeconds += intervalSeconds
        
        // Add rest duration if present
        interval.rest_duration?.let { restDuration ->
            val restSeconds = when (interval.rest_duration_unit?.lowercase()) {
                "minutes" -> restDuration * 60
                "hours" -> restDuration * 3600
                else -> restDuration // assume seconds
            }
            totalSeconds += restSeconds
        }
    }
    
    // Format duration
    return when {
        totalSeconds >= 3600 -> {
            val hours = totalSeconds / 3600
            val minutes = (totalSeconds % 3600) / 60
            if (minutes > 0) "${hours}h ${minutes}m" else "${hours}h"
        }
        totalSeconds >= 60 -> {
            val minutes = totalSeconds / 60
            val seconds = totalSeconds % 60
            if (seconds > 0) "${minutes}m ${seconds}s" else "${minutes}m"
        }
        else -> "${totalSeconds}s"
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ActivityListScreen(navController: NavController, activities: List<Activity>, activitySessionDao: ActivitySessionDao) {
    val (startOfDay, endOfDay) = remember { getTodayTimestamps() }
    val todaySessions by activitySessionDao.getSessionsForDay(startOfDay, endOfDay).collectAsState(initial = emptyList())
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Activity List",
                style = MaterialTheme.typography.headlineMedium,
                color = MaterialTheme.colorScheme.onSurface
            )
            IconButton(onClick = { navController.navigate("analytics") }) {
                Icon(
                    Icons.Default.DateRange,
                    contentDescription = "Analytics",
                    tint = MaterialTheme.colorScheme.onSurface
                )
            }
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(activities) { activity ->
                val isCompletedToday = todaySessions.any { session ->
                    session.activity_name == activity.name && 
                    (session.getCompletionStatus() == CompletionStatus.COMPLETED_FULL ||
                     session.getCompletionStatus() == CompletionStatus.COMPLETED_FULL_WITH_PAUSE ||
                     session.getCompletionStatus() == CompletionStatus.COMPLETED_EARLY)
                }
                Card(
                    onClick = { navController.navigate("activityDetail/${activity.name}") },
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant
                    ),
                    elevation = CardDefaults.cardElevation(
                        defaultElevation = 2.dp
                    )
                ) {
                    Row(
                        modifier = Modifier
                            .padding(16.dp)
                            .fillMaxWidth(),
                        verticalAlignment = Alignment.Top
                    ) {
                        Column(
                            modifier = Modifier.weight(1f)
                        ) {
                            Text(
                                text = activity.name,
                                style = MaterialTheme.typography.headlineSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            
                            Spacer(modifier = Modifier.height(8.dp))
                            
                            Row(
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.PlayArrow,
                                    contentDescription = "Duration",
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                                    modifier = Modifier.padding(end = 4.dp)
                                )
                                Text(
                                    text = calculateTotalDuration(activity),
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                                )
                            }
                        }
                        
                        if (isCompletedToday) {
                            Icon(
                                imageVector = Icons.Default.CheckCircle,
                                contentDescription = "Completed today",
                                tint = Color(0xFF4CAF50), // Green color
                                modifier = Modifier.padding(start = 8.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}
