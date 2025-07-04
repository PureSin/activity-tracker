package com.example.kelvinma.activitytracker.ui.activitydetail

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.tooling.preview.Preview
import androidx.navigation.compose.rememberNavController
import androidx.navigation.NavController
import com.example.kelvinma.activitytracker.data.Activity
import com.example.kelvinma.activitytracker.data.Interval
import com.example.kelvinma.activitytracker.ui.theme.ActivityTrackerTheme

@Composable
fun ActivityDetailScreen(navController: NavController, activity: Activity?) {
    Column(modifier = Modifier.padding(16.dp)) {
        IconButton(onClick = { navController.popBackStack() }) {
            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
        }
        if (activity != null) {
            Text(text = activity.name, style = MaterialTheme.typography.headlineMedium)
            activity.intervals.forEach { interval ->
                Text(text = interval.name ?: "Unnamed Interval")
            }
            Button(onClick = { navController.navigate("timer/${activity.name}") }) {
                Text("Start Activity")
            }
        } else {
            Text(text = "Activity not found")
        }
    }
}

@Preview(showBackground = true)
@Composable
fun ActivityDetailScreenPreview() {
    ActivityTrackerTheme {
        val navController = rememberNavController()
        val activity = Activity(
            name = "Test Activity",
            intervals = listOf(
                Interval("Warm-up", 300, "seconds"),
                Interval("Work", 600, "seconds"),
                Interval("Rest", 300, "seconds")
            )
        )
        ActivityDetailScreen(navController, activity)
    }
}
