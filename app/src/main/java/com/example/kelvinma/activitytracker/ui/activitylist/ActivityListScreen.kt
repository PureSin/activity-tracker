package com.example.kelvinma.activitytracker.ui.activitylist

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.kelvinma.activitytracker.data.Activity

@Composable
fun ActivityListScreen(navController: NavController, activities: List<Activity>) {
    Column(modifier = Modifier.padding(16.dp)) {
        Text(text = "Activity List", style = MaterialTheme.typography.headlineMedium)
        activities.forEach { activity ->
            Text(
                text = activity.name,
                modifier = Modifier.clickable { navController.navigate("activityDetail/${activity.name}") }
            )
        }
    }
}
