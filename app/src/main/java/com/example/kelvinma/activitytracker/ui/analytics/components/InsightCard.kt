package com.example.kelvinma.activitytracker.ui.analytics.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.kelvinma.activitytracker.ui.analytics.Insight
import com.example.kelvinma.activitytracker.ui.analytics.InsightType

@Composable
fun InsightCards(
    insights: List<Insight>,
    modifier: Modifier = Modifier
) {
    if (insights.isNotEmpty()) {
        Column(modifier = modifier) {
            Text(
                text = "Insights & Recommendations",
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.onSurface,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(horizontal = 16.dp)
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Column(
                verticalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.padding(horizontal = 16.dp)
            ) {
                insights.forEach { insight ->
                    InsightCard(insight = insight)
                }
            }
        }
    }
}

@Composable
fun InsightCard(
    insight: Insight,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = when (insight.type) {
                InsightType.STREAK -> MaterialTheme.colorScheme.tertiaryContainer
                InsightType.PERFORMANCE -> MaterialTheme.colorScheme.primaryContainer
                InsightType.PATTERN -> MaterialTheme.colorScheme.secondaryContainer
                InsightType.MOTIVATION -> MaterialTheme.colorScheme.errorContainer
                InsightType.RECOMMENDATION -> MaterialTheme.colorScheme.surfaceVariant
            }
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 2.dp
        )
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = getInsightIcon(insight.type),
                contentDescription = null,
                tint = getInsightIconColor(insight.type),
                modifier = Modifier.size(24.dp)
            )
            
            Spacer(modifier = Modifier.width(12.dp))
            
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = insight.title,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = when (insight.type) {
                        InsightType.STREAK -> MaterialTheme.colorScheme.onTertiaryContainer
                        InsightType.PERFORMANCE -> MaterialTheme.colorScheme.onPrimaryContainer
                        InsightType.PATTERN -> MaterialTheme.colorScheme.onSecondaryContainer
                        InsightType.MOTIVATION -> MaterialTheme.colorScheme.onErrorContainer
                        InsightType.RECOMMENDATION -> MaterialTheme.colorScheme.onSurfaceVariant
                    }
                )
                
                Spacer(modifier = Modifier.height(4.dp))
                
                Text(
                    text = insight.message,
                    style = MaterialTheme.typography.bodyMedium,
                    color = when (insight.type) {
                        InsightType.STREAK -> MaterialTheme.colorScheme.onTertiaryContainer
                        InsightType.PERFORMANCE -> MaterialTheme.colorScheme.onPrimaryContainer
                        InsightType.PATTERN -> MaterialTheme.colorScheme.onSecondaryContainer
                        InsightType.MOTIVATION -> MaterialTheme.colorScheme.onErrorContainer
                        InsightType.RECOMMENDATION -> MaterialTheme.colorScheme.onSurfaceVariant
                    }.copy(alpha = 0.8f)
                )
                
                if (insight.isActionable) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "💡 Actionable",
                        style = MaterialTheme.typography.bodySmall,
                        color = when (insight.type) {
                            InsightType.STREAK -> MaterialTheme.colorScheme.onTertiaryContainer
                            InsightType.PERFORMANCE -> MaterialTheme.colorScheme.onPrimaryContainer
                            InsightType.PATTERN -> MaterialTheme.colorScheme.onSecondaryContainer
                            InsightType.MOTIVATION -> MaterialTheme.colorScheme.onErrorContainer
                            InsightType.RECOMMENDATION -> MaterialTheme.colorScheme.onSurfaceVariant
                        }.copy(alpha = 0.6f)
                    )
                }
            }
        }
    }
}

private fun getInsightIcon(type: InsightType): ImageVector {
    return when (type) {
        InsightType.STREAK -> Icons.Default.Favorite
        InsightType.PERFORMANCE -> Icons.Default.Person
        InsightType.PATTERN -> Icons.Default.Info
        InsightType.MOTIVATION -> Icons.Default.Star
        InsightType.RECOMMENDATION -> Icons.Default.Build
    }
}

private fun getInsightIconColor(type: InsightType): Color {
    return when (type) {
        InsightType.STREAK -> Color(0xFFFF5722)
        InsightType.PERFORMANCE -> Color(0xFF4CAF50)
        InsightType.PATTERN -> Color(0xFF2196F3)
        InsightType.MOTIVATION -> Color(0xFFFFC107)
        InsightType.RECOMMENDATION -> Color(0xFF9C27B0)
    }
}