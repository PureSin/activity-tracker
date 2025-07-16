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
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.kelvinma.activitytracker.data.Activity
import com.example.kelvinma.activitytracker.data.ActivitySessionDao
import com.example.kelvinma.activitytracker.data.Category
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
fun ActivityListScreen(navController: NavController, categories: List<Category>, activitySessionDao: ActivitySessionDao) {
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
                text = "Activity Categories",
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
            items(categories) { category ->
                CategoryCard(
                    category = category,
                    todaySessions = todaySessions,
                    navController = navController
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CategoryCard(
    category: Category,
    todaySessions: List<com.example.kelvinma.activitytracker.data.ActivitySession>,
    navController: NavController
) {
    var isExpanded by remember { mutableStateOf(true) }
    
    // Check if any activity in this category is completed today
    val isCategoryCompletedToday = todaySessions.any { session ->
        category.activities.any { activity ->
            session.activity_name == activity.name && 
            (session.getCompletionStatus() == CompletionStatus.COMPLETED_FULL ||
             session.getCompletionStatus() == CompletionStatus.COMPLETED_FULL_WITH_PAUSE ||
             session.getCompletionStatus() == CompletionStatus.COMPLETED_EARLY)
        }
    }
    
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = if (isCategoryCompletedToday) 
                MaterialTheme.colorScheme.primaryContainer 
            else 
                MaterialTheme.colorScheme.surfaceVariant
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 4.dp
        )
    ) {
        Column {
            // Category Header
            Card(
                onClick = { isExpanded = !isExpanded },
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = Color.Transparent
                )
            ) {
                Row(
                    modifier = Modifier
                        .padding(16.dp)
                        .fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = category.name,
                            style = MaterialTheme.typography.headlineSmall,
                            color = if (isCategoryCompletedToday) 
                                MaterialTheme.colorScheme.onPrimaryContainer
                            else 
                                MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        
                        if (isCategoryCompletedToday) {
                            Spacer(modifier = Modifier.width(8.dp))
                            Icon(
                                imageVector = Icons.Default.CheckCircle,
                                contentDescription = "Category completed today",
                                tint = Color(0xFF4CAF50)
                            )
                        }
                    }
                    
                    Icon(
                        imageVector = if (isExpanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                        contentDescription = if (isExpanded) "Collapse" else "Expand",
                        tint = if (isCategoryCompletedToday) 
                            MaterialTheme.colorScheme.onPrimaryContainer
                        else 
                            MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            
            // Activities List (shown when expanded)
            if (isExpanded) {
                Column(
                    modifier = Modifier.padding(start = 16.dp, end = 16.dp, bottom = 16.dp)
                ) {
                    category.activities.forEach { activity ->
                        val isActivityCompletedToday = todaySessions.any { session ->
                            session.activity_name == activity.name && 
                            (session.getCompletionStatus() == CompletionStatus.COMPLETED_FULL ||
                             session.getCompletionStatus() == CompletionStatus.COMPLETED_FULL_WITH_PAUSE ||
                             session.getCompletionStatus() == CompletionStatus.COMPLETED_EARLY)
                        }
                        
                        ActivityCard(
                            activity = activity,
                            isCompletedToday = isActivityCompletedToday,
                            navController = navController
                        )
                        
                        if (activity != category.activities.last()) {
                            Spacer(modifier = Modifier.height(8.dp))
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ActivityCard(
    activity: Activity,
    isCompletedToday: Boolean,
    navController: NavController
) {
    Card(
        onClick = { navController.navigate("activityDetail/${activity.name}") },
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 1.dp
        )
    ) {
        Row(
            modifier = Modifier
                .padding(12.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.Top
        ) {
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = activity.name,
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )
                
                Spacer(modifier = Modifier.height(4.dp))
                
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.PlayArrow,
                        contentDescription = "Duration",
                        tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                        modifier = Modifier.padding(end = 4.dp)
                    )
                    Text(
                        text = calculateTotalDuration(activity),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                    )
                }
            }
            
            if (isCompletedToday) {
                Icon(
                    imageVector = Icons.Default.CheckCircle,
                    contentDescription = "Completed today",
                    tint = Color(0xFF4CAF50),
                    modifier = Modifier.padding(start = 8.dp)
                )
            }
        }
    }
}
