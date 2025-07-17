package com.example.kelvinma.activitytracker.ui.analytics

import android.content.Intent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import java.util.Locale
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.kelvinma.activitytracker.data.AppDatabase
import com.example.kelvinma.activitytracker.ui.analytics.components.CompletionChart
import com.example.kelvinma.activitytracker.ui.analytics.components.ExportDialog
import com.example.kelvinma.activitytracker.ui.analytics.components.InsightCard
import com.example.kelvinma.activitytracker.ui.analytics.components.MetricsCard
import com.example.kelvinma.activitytracker.ui.analytics.components.StreakDisplay

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AnalyticsScreen(navController: NavController) {
    val context = LocalContext.current
    val viewModel: AnalyticsViewModel = viewModel(
        factory = AnalyticsViewModelFactory(
            AppDatabase.getDatabase(context).activitySessionDao(),
            context
        )
    )
    
    val analyticsData by viewModel.analyticsData.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val isExporting by viewModel.isExporting.collectAsState()
    val exportEvent by viewModel.exportEvent.collectAsState()
    val exportResult by viewModel.exportResult.collectAsState()
    
    var showExportDialog by remember { mutableStateOf(false) }
    
    // Handle export intent
    LaunchedEffect(exportEvent) {
        exportEvent?.let { intent ->
            context.startActivity(Intent.createChooser(intent, "Send Database Export"))
            viewModel.clearExportEvent()
            showExportDialog = false
        }
    }
    
    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = { navController.popBackStack() }) {
                    Icon(
                        Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                        tint = MaterialTheme.colorScheme.onSurface
                    )
                }
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Analytics",
                    style = MaterialTheme.typography.headlineMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.weight(1f)
                )
                IconButton(onClick = { showExportDialog = true }) {
                    Icon(
                        Icons.Default.Share,
                        contentDescription = "Export Data",
                        tint = MaterialTheme.colorScheme.onSurface
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            if (isLoading) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Overview Metrics
                    item {
                        Text(
                            text = "Overview",
                            style = MaterialTheme.typography.titleLarge,
                            color = MaterialTheme.colorScheme.onSurface,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            MetricsCard(
                                title = "Sessions",
                                value = "${analyticsData.totalSessions}",
                                icon = Icons.Default.List,
                                modifier = Modifier.weight(1f)
                            )
                            MetricsCard(
                                title = "Completion",
                                value = "${String.format(Locale.getDefault(), "%.0f", analyticsData.completionRate)}%",
                                icon = Icons.Default.CheckCircle,
                                modifier = Modifier.weight(1f)
                            )
                        }
                        
                        Spacer(modifier = Modifier.height(8.dp))
                        
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            MetricsCard(
                                title = "Current Streak",
                                value = "${analyticsData.currentStreak} days",
                                icon = Icons.Default.Star,
                                modifier = Modifier.weight(1f)
                            )
                            MetricsCard(
                                title = "Time Invested",
                                value = formatDuration(analyticsData.timeInvested),
                                icon = Icons.Default.DateRange,
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                    
                    // Streak Analysis
                    item {
                        StreakDisplay(
                            currentStreak = analyticsData.currentStreak,
                            longestStreak = analyticsData.longestStreak,
                            weeklyCompletionRate = analyticsData.completionRate
                        )
                    }
                    
                    // Completion Breakdown
                    item {
                        CompletionChart(
                            completionBreakdown = analyticsData.completionBreakdown
                        )
                    }
                    
                    // Activity Performance
                    if (analyticsData.activityPerformance.isNotEmpty()) {
                        item {
                            Text(
                                text = "Activity Performance",
                                style = MaterialTheme.typography.titleLarge,
                                color = MaterialTheme.colorScheme.onSurface,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        
                        items(analyticsData.activityPerformance) { performance ->
                            ActivityPerformanceCard(performance = performance)
                        }
                    }
                    
                    // Insights
                    if (analyticsData.insights.isNotEmpty()) {
                        item {
                            Text(
                                text = "Insights & Recommendations",
                                style = MaterialTheme.typography.titleLarge,
                                color = MaterialTheme.colorScheme.onSurface,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 16.dp)
                            )
                        }
                        
                        items(analyticsData.insights) { insight ->
                            InsightCard(
                                insight = insight,
                                modifier = Modifier.padding(horizontal = 16.dp)
                            )
                        }
                    }
                }
            }
        }
    }
    
    // Export Dialog
    ExportDialog(
        isVisible = showExportDialog,
        isExporting = isExporting,
        exportResult = exportResult,
        onDismiss = { showExportDialog = false },
        onExport = { email -> viewModel.exportDatabase(email) }
    )
}

@Composable
fun ActivityPerformanceCard(
    performance: ActivityPerformance,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 2.dp
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = performance.activityName,
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontWeight = FontWeight.Bold
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(
                        text = "Completion Rate",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                    )
                    Text(
                        text = "${String.format(Locale.getDefault(), "%.0f", performance.completionRate)}%",
                        style = MaterialTheme.typography.titleSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontWeight = FontWeight.Bold
                    )
                }
                
                Column {
                    Text(
                        text = "Sessions",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                    )
                    Text(
                        text = "${performance.completions}/${performance.totalSessions}",
                        style = MaterialTheme.typography.titleSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontWeight = FontWeight.Bold
                    )
                }
                
                Column {
                    Text(
                        text = "Avg Progress",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                    )
                    Text(
                        text = "${String.format(Locale.getDefault(), "%.0f", performance.averageProgress)}%",
                        style = MaterialTheme.typography.titleSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontWeight = FontWeight.Bold
                    )
                }
                
                Column {
                    Text(
                        text = "Time Spent",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                    )
                    Text(
                        text = formatDuration(performance.totalTimeSpent),
                        style = MaterialTheme.typography.titleSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

private fun formatDuration(milliseconds: Long): String {
    val totalMinutes = milliseconds / (1000 * 60)
    val hours = totalMinutes / 60
    val minutes = totalMinutes % 60
    
    return when {
        hours > 0 -> "${hours}h ${minutes}m"
        minutes > 0 -> "${minutes}m"
        else -> "< 1m"
    }
}