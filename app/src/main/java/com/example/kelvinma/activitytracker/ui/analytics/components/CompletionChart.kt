package com.example.kelvinma.activitytracker.ui.analytics.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.kelvinma.activitytracker.ui.analytics.CompletionBreakdown
import kotlin.math.cos
import kotlin.math.sin

@Composable
fun CompletionChart(
    completionBreakdown: CompletionBreakdown,
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
                text = "Completion Breakdown",
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontWeight = FontWeight.Bold
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            if (completionBreakdown.totalSessions > 0) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Pie Chart
                    Box(
                        modifier = Modifier.size(120.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        PieChart(completionBreakdown = completionBreakdown)
                        
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = "${String.format("%.0f", completionBreakdown.completionRate)}%",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = "Complete",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                            )
                        }
                    }
                    
                    // Legend
                    Column(
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        LegendItem(
                            color = Color(0xFF4CAF50),
                            label = "Full",
                            count = completionBreakdown.full
                        )
                        LegendItem(
                            color = Color(0xFF8BC34A),
                            label = "With Pauses",
                            count = completionBreakdown.fullWithPause
                        )
                        LegendItem(
                            color = Color(0xFFFF9800),
                            label = "Early",
                            count = completionBreakdown.early
                        )
                        LegendItem(
                            color = Color(0xFFFFC107),
                            label = "Partial",
                            count = completionBreakdown.partial
                        )
                        if (completionBreakdown.incomplete > 0) {
                            LegendItem(
                                color = Color(0xFFE0E0E0),
                                label = "Incomplete",
                                count = completionBreakdown.incomplete
                            )
                        }
                    }
                }
            } else {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(120.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "No session data available",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                        textAlign = TextAlign.Center
                    )
                }
            }
        }
    }
}

@Composable
private fun PieChart(completionBreakdown: CompletionBreakdown) {
    val total = completionBreakdown.totalSessions.toFloat()
    if (total == 0f) return
    
    val colors = listOf(
        Color(0xFF4CAF50), // Full
        Color(0xFF8BC34A), // Full with pauses
        Color(0xFFFF9800), // Early
        Color(0xFFFFC107), // Partial
        Color(0xFFE0E0E0)  // Incomplete
    )
    
    val values = listOf(
        completionBreakdown.full,
        completionBreakdown.fullWithPause,
        completionBreakdown.early,
        completionBreakdown.partial,
        completionBreakdown.incomplete
    )
    
    Canvas(modifier = Modifier.size(120.dp)) {
        val strokeWidth = 20.dp.toPx()
        val radius = (size.minDimension - strokeWidth) / 2
        val center = androidx.compose.ui.geometry.Offset(size.width / 2, size.height / 2)
        
        var currentAngle = -90f // Start from top
        
        values.forEachIndexed { index, value ->
            if (value > 0) {
                val sweepAngle = (value / total) * 360f
                drawArc(
                    color = colors[index],
                    startAngle = currentAngle,
                    sweepAngle = sweepAngle,
                    useCenter = false,
                    style = Stroke(width = strokeWidth),
                    topLeft = androidx.compose.ui.geometry.Offset(
                        center.x - radius,
                        center.y - radius
                    ),
                    size = androidx.compose.ui.geometry.Size(radius * 2, radius * 2)
                )
                currentAngle += sweepAngle
            }
        }
    }
}

@Composable
private fun LegendItem(
    color: Color,
    label: String,
    count: Int
) {
    Row(
        verticalAlignment = Alignment.CenterVertically
    ) {
        Surface(
            modifier = Modifier.size(12.dp),
            shape = CircleShape,
            color = color
        ) {}
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = "$label ($count)",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}