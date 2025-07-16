package com.example.kelvinma.activitytracker.ui.timer

import android.view.WindowManager
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.kelvinma.activitytracker.data.Activity
import com.example.kelvinma.activitytracker.data.AppDatabase

@Composable
fun TimerScreen(activity: Activity, onFinish: () -> Unit, navController: NavController) {
    val context = LocalContext.current
    val database = AppDatabase.getDatabase(context)
    val viewModel: TimerViewModel = viewModel(
        factory = TimerViewModelFactory(
            activity,
            database.activitySessionDao(),
            database.categorySessionDao(),
            context
        )
    )

    val timerValue by viewModel.timerValue.collectAsState()
    val isPaused by viewModel.isPaused.collectAsState()
    val currentIntervalIndex by viewModel.currentIntervalIndex.collectAsState()
    val progressPercentage by viewModel.progressPercentage.collectAsState()
    val isRestPeriod by viewModel.isRestPeriod.collectAsState()

    KeepScreenOn()

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Start
            ) {
                IconButton(onClick = { navController.popBackStack() }) {
                    Icon(
                        Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                        tint = MaterialTheme.colorScheme.onSurface
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(32.dp))
            
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                ),
                elevation = CardDefaults.cardElevation(
                    defaultElevation = 4.dp
                )
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = getTimerDisplayText(currentIntervalIndex, isRestPeriod, activity),
                        style = MaterialTheme.typography.headlineMedium,
                        color = getTimerDisplayColor(isRestPeriod),
                        textAlign = TextAlign.Center
                    )
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    Text(
                        text = formatTime(timerValue / 1000),
                        style = MaterialTheme.typography.displayLarge,
                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center
                    )
                    
                    Spacer(modifier = Modifier.height(24.dp))
                    
                    // Progress Section
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "Progress: ${String.format("%.1f", progressPercentage)}%",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onPrimaryContainer,
                            textAlign = TextAlign.Center
                        )
                        
                        Spacer(modifier = Modifier.height(8.dp))
                        
                        LinearProgressIndicator(
                            progress = { progressPercentage / 100f },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(8.dp),
                            color = MaterialTheme.colorScheme.primary,
                            trackColor = MaterialTheme.colorScheme.surfaceVariant,
                        )
                        
                        Spacer(modifier = Modifier.height(8.dp))
                        
                        Text(
                            text = getProgressText(currentIntervalIndex, isRestPeriod, activity),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onPrimaryContainer,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(32.dp))
            
            Row(
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (isPaused) {
                    FilledTonalButton(
                        onClick = { viewModel.resumeTimer() },
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(
                            Icons.Default.PlayArrow,
                            contentDescription = null,
                            modifier = Modifier.padding(end = 8.dp)
                        )
                        Text("Resume")
                    }
                } else {
                    FilledTonalButton(
                        onClick = { viewModel.pauseTimer() },
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(
                            Icons.Default.PlayArrow,
                            contentDescription = null,
                            modifier = Modifier.padding(end = 8.dp)
                        )
                        Text("Pause")
                    }
                }
                
                OutlinedButton(
                    onClick = { viewModel.skipInterval() },
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(
                        Icons.Default.PlayArrow,
                        contentDescription = null,
                        modifier = Modifier.padding(end = 8.dp)
                    )
                    Text(if (isRestPeriod) "Skip Rest" else "Skip")
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Button(
                onClick = { 
                    viewModel.stopActivity()
                    onFinish()
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(
                    Icons.Default.PlayArrow,
                    contentDescription = null,
                    modifier = Modifier.padding(end = 8.dp)
                )
                Text("Finish Activity")
            }
        }
    }
}

@Composable
private fun getTimerDisplayText(
    currentIntervalIndex: Int,
    isRestPeriod: Boolean,
    activity: Activity
): String {
    return when {
        currentIntervalIndex >= activity.intervals.size -> "Activity Complete"
        isRestPeriod -> "Rest Period"
        else -> activity.intervals[currentIntervalIndex].name ?: "Unnamed Interval"
    }
}

@Composable
private fun getProgressText(
    currentIntervalIndex: Int,
    isRestPeriod: Boolean,
    activity: Activity
): String {
    // Calculate the actual interval number, capped at the total number of intervals
    val actualIntervalNumber = minOf(currentIntervalIndex + 1, activity.intervals.size)
    
    return if (isRestPeriod) {
        "Resting after Interval $actualIntervalNumber"
    } else {
        "Interval $actualIntervalNumber of ${activity.intervals.size}"
    }
}

@Composable
private fun getTimerDisplayColor(
    isRestPeriod: Boolean
): androidx.compose.ui.graphics.Color {
    return if (isRestPeriod) {
        MaterialTheme.colorScheme.secondary
    } else {
        MaterialTheme.colorScheme.onPrimaryContainer
    }
}

@Composable
fun formatTime(seconds: Long): String {
    val minutes = seconds / 60
    val remainingSeconds = seconds % 60
    return String.format("%02d:%02d", minutes, remainingSeconds)
}

@Composable
fun KeepScreenOn() {
    val view = LocalView.current
    DisposableEffect(Unit) {
        val window = (view.context as? android.app.Activity)?.window
        window?.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        onDispose {
            window?.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        }
    }
}
