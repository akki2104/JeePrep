package com.jeeprep.app.ui.screens.progress

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.jeeprep.app.ui.components.MathText
import com.jeeprep.app.ui.components.PremiumBanner
import com.jeeprep.app.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProgressScreen(
    onNavigatePremium: () -> Unit = {},
    viewModel: ProgressViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = { TopAppBar(title = { Text("Your Progress") }) }
    ) { padding ->
        if (uiState.isLoading) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { CircularProgressIndicator() }
            return@Scaffold
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // --- Hero stats card with gradient ---
            Card(
                Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            Brush.linearGradient(
                                colors = listOf(
                                    MaterialTheme.colorScheme.primaryContainer,
                                    MaterialTheme.colorScheme.secondaryContainer
                                )
                            )
                        )
                        .padding(20.dp)
                ) {
                    Column {
                        Text("Overall Performance", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                        Spacer(Modifier.height(16.dp))
                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                            HeroStat(value = "${uiState.overallAccuracy.toInt()}%", label = "Accuracy", icon = Icons.Filled.TrendingUp)
                            HeroStat(value = "${uiState.totalAttempts}", label = "Questions", icon = Icons.Filled.Quiz)
                            HeroStat(value = "${uiState.activeDays}", label = "Days", icon = Icons.Filled.CalendarMonth)
                            HeroStat(value = "${uiState.avgTime.toInt()}s", label = "Avg Time", icon = Icons.Filled.Timer)
                        }
                    }
                }
            }

            // --- Recent 7-day trend ---
            if (uiState.recentAttempts > 0) {
                Card(Modifier.fillMaxWidth()) {
                    Column(Modifier.padding(16.dp)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(Icons.Filled.Insights, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                            Text("Last 7 Days", fontWeight = FontWeight.Bold)
                        }
                        Spacer(Modifier.height(12.dp))
                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                            StatItem(value = "${uiState.recentAttempts}", label = "Practiced")
                            StatItem(value = "${uiState.recentAccuracy.toInt()}%", label = "Accuracy")
                            val trend = uiState.recentAccuracy - uiState.overallAccuracy
                            StatItem(
                                value = "${if (trend >= 0) "+" else ""}${trend.toInt()}%",
                                label = "vs Overall",
                                color = if (trend >= 0) CorrectGreen else IncorrectRed
                            )
                        }
                    }
                }
            }

            // --- Subject-wise detailed cards ---
            Text("Subject Breakdown", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)

            SubjectDetailCard(
                subject = "Physics",
                subjectId = 1,
                detail = uiState.physics,
                color = PhysicsColor,
                isExpanded = uiState.expandedSubject == 1,
                onToggle = { viewModel.toggleSubjectExpand(1) }
            )
            SubjectDetailCard(
                subject = "Chemistry",
                subjectId = 2,
                detail = uiState.chemistry,
                color = ChemistryColor,
                isExpanded = uiState.expandedSubject == 2,
                onToggle = { viewModel.toggleSubjectExpand(2) }
            )
            SubjectDetailCard(
                subject = "Mathematics",
                subjectId = 3,
                detail = uiState.math,
                color = MathColor,
                isExpanded = uiState.expandedSubject == 3,
                onToggle = { viewModel.toggleSubjectExpand(3) }
            )

            // --- Mock test average ---
            if (uiState.avgMockScore > 0) {
                Card(Modifier.fillMaxWidth()) {
                    Column(Modifier.padding(20.dp)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(Icons.Filled.Assignment, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                            Text("Mock Tests", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                        }
                        Spacer(Modifier.height(8.dp))
                        Text(
                            "Average Score: ${uiState.avgMockScore.toInt()}%",
                            style = MaterialTheme.typography.bodyLarge,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }

            // --- AI Weakness Analysis (Premium) ---
            if (uiState.isPremium) {
                Card(
                    Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = GoldLightColor.copy(alpha = 0.3f))
                ) {
                    Column(Modifier.padding(16.dp)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(Icons.Filled.Psychology, contentDescription = null, tint = GoldColor)
                            Text("AI Analysis", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                            Badge(containerColor = GoldColor) {
                                Text("PRO", style = MaterialTheme.typography.labelSmall, color = androidx.compose.ui.graphics.Color.White)
                            }
                        }
                        Spacer(Modifier.height(8.dp))

                        if (uiState.aiAnalysis.isNotBlank()) {
                            MathText(text = uiState.aiAnalysis)
                        } else if (uiState.isAiAnalysisLoading) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                CircularProgressIndicator(modifier = Modifier.size(16.dp))
                                Text("Analyzing your performance...", style = MaterialTheme.typography.bodySmall)
                            }
                        } else {
                            Button(onClick = { viewModel.loadAiAnalysis() }) {
                                Icon(Icons.Filled.AutoAwesome, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(Modifier.width(8.dp))
                                Text("Generate AI Analysis")
                            }
                        }
                    }
                }
            } else {
                PremiumBanner(onPremiumClick = onNavigatePremium)
            }

            // Empty state
            if (uiState.totalAttempts == 0) {
                Card(Modifier.fillMaxWidth()) {
                    Column(
                        Modifier
                            .fillMaxWidth()
                            .padding(32.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            Icons.Filled.BarChart,
                            contentDescription = null,
                            modifier = Modifier.size(48.dp),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(Modifier.height(16.dp))
                        Text("No data yet", fontWeight = FontWeight.Bold)
                        Text(
                            "Start practicing to see your progress here",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun HeroStat(
    value: String,
    label: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Icon(icon, contentDescription = null, modifier = Modifier.size(20.dp), tint = MaterialTheme.colorScheme.primary)
        Spacer(Modifier.height(4.dp))
        Text(value, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
        Text(label, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

@Composable
private fun StatItem(
    value: String,
    label: String,
    color: androidx.compose.ui.graphics.Color = MaterialTheme.colorScheme.primary
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(value, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = color)
        Text(label, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

@Composable
private fun SubjectDetailCard(
    subject: String,
    subjectId: Int,
    detail: SubjectDetail,
    color: androidx.compose.ui.graphics.Color,
    isExpanded: Boolean,
    onToggle: () -> Unit
) {
    Card(
        Modifier
            .fillMaxWidth()
            .clickable(onClick = onToggle),
        colors = CardDefaults.cardColors(containerColor = color.copy(alpha = 0.05f))
    ) {
        Column(Modifier.padding(16.dp)) {
            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .clip(CircleShape)
                            .background(color)
                    )
                    Text(subject, fontWeight = FontWeight.Bold)
                }
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        "${detail.accuracy.toInt()}%",
                        fontWeight = FontWeight.Bold,
                        color = when {
                            detail.accuracy >= 70 -> CorrectGreen
                            detail.accuracy >= 40 -> MediumColor
                            detail.accuracy > 0 -> IncorrectRed
                            else -> UnattemptedGray
                        }
                    )
                    Icon(
                        if (isExpanded) Icons.Filled.ExpandLess else Icons.Filled.ExpandMore,
                        contentDescription = null
                    )
                }
            }
            Spacer(Modifier.height(8.dp))
            LinearProgressIndicator(
                progress = { (detail.accuracy / 100f).coerceIn(0f, 1f) },
                modifier = Modifier.fillMaxWidth(),
                color = color,
                trackColor = color.copy(alpha = 0.15f)
            )

            if (detail.avgTime > 0) {
                Spacer(Modifier.height(4.dp))
                Text(
                    "Avg time: ${detail.avgTime.toInt()}s per question",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            AnimatedVisibility(visible = isExpanded) {
                Column(modifier = Modifier.padding(top = 12.dp)) {
                    HorizontalDivider()
                    Spacer(Modifier.height(12.dp))

                    // Weak topics
                    if (detail.weakTopics.isNotEmpty()) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Icon(Icons.Filled.Warning, contentDescription = null, tint = IncorrectRed, modifier = Modifier.size(16.dp))
                            Text("Weak Topics", style = MaterialTheme.typography.labelMedium, color = IncorrectRed)
                        }
                        detail.weakTopics.forEach { topic ->
                            Text("  • $topic", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        Spacer(Modifier.height(8.dp))
                    }

                    // Strong topics
                    if (detail.strongTopics.isNotEmpty()) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Icon(Icons.Filled.Star, contentDescription = null, tint = CorrectGreen, modifier = Modifier.size(16.dp))
                            Text("Strong Topics", style = MaterialTheme.typography.labelMedium, color = CorrectGreen)
                        }
                        detail.strongTopics.forEach { topic ->
                            Text("  • $topic", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        Spacer(Modifier.height(8.dp))
                    }

                    // Topic-wise breakdown
                    Text("All Topics", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
                    Spacer(Modifier.height(4.dp))
                    detail.topicStats.forEach { stat ->
                        Row(
                            Modifier
                                .fillMaxWidth()
                                .padding(vertical = 2.dp),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(stat.topicName, style = MaterialTheme.typography.bodySmall, modifier = Modifier.weight(1f))
                            if (stat.accuracy >= 0f) {
                                Text(
                                    "${stat.accuracy.toInt()}% (${stat.correct}/${stat.totalAttempts})",
                                    style = MaterialTheme.typography.bodySmall,
                                    fontWeight = FontWeight.Medium,
                                    color = when {
                                        stat.accuracy >= 70 -> CorrectGreen
                                        stat.accuracy >= 40 -> MediumColor
                                        else -> IncorrectRed
                                    }
                                )
                            } else {
                                Text("—", style = MaterialTheme.typography.bodySmall, color = UnattemptedGray)
                            }
                        }
                    }
                }
            }
        }
    }
}
