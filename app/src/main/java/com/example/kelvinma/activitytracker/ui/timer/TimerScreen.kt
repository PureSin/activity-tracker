package com.example.kelvinma.activitytracker.ui.timer

import android.view.WindowManager
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.kelvinma.activitytracker.data.Activity
import com.example.kelvinma.activitytracker.data.AppDatabase

@Composable
fun TimerScreen(activity: Activity, onFinish: () -> Unit) {
    val context = LocalContext.current
    val viewModel: TimerViewModel = viewModel(
        factory = TimerViewModelFactory(
            activity,
            AppDatabase.getDatabase(context).activitySessionDao(),
            context
        )
    )

    val timerValue by viewModel.timerValue.collectAsState()
    val isPaused by viewModel.isPaused.collectAsState()
    val currentIntervalIndex by viewModel.currentIntervalIndex.collectAsState()

    KeepScreenOn()

    Column(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = activity.intervals[currentIntervalIndex].name ?: "Unnamed Interval",
            style = MaterialTheme.typography.headlineMedium
        )
        Text(
            text = (timerValue / 1000).toString(),
            style = MaterialTheme.typography.headlineLarge
        )
        Row {
            if (isPaused) {
                Button(onClick = { viewModel.resumeTimer() }) {
                    Text("Resume")
                }
            } else {
                Button(onClick = { viewModel.pauseTimer() }) {
                    Text("Pause")
                }
            }
            Spacer(modifier = Modifier.width(16.dp))
            Button(onClick = { viewModel.skipInterval() }) {
                Text("Skip")
            }
        }
        Button(onClick = onFinish) {
            Text("Finish Activity")
        }
    }
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
