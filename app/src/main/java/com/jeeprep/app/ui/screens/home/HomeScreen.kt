package com.jeeprep.app.ui.screens.home

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.navigation.NavController
import com.jeeprep.app.ui.components.PremiumBanner
import com.jeeprep.app.ui.navigation.SubScreen
import com.jeeprep.app.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    navController: NavController,
    viewModel: HomeViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    // Re-check model status every time HomeScreen becomes visible
    val lifecycleOwner = androidx.lifecycle.compose.LocalLifecycleOwner.current
    androidx.compose.runtime.DisposableEffect(lifecycleOwner) {
        val observer = androidx.lifecycle.LifecycleEventObserver { _, event ->
            if (event == androidx.lifecycle.Lifecycle.Event.ON_RESUME) {
                viewModel.refreshModelStatus()
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Header
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "JEE Prep",
                        style = MaterialTheme.typography.headlineLarge,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Offline AI-Powered Exam Preparation",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                // Pro badge — always visible, navigates to premium screen
                FilledTonalButton(
                    onClick = { navController.navigate(SubScreen.PREMIUM) },
                    colors = ButtonDefaults.filledTonalButtonColors(
                        containerColor = if (uiState.isPremium) CorrectGreen.copy(alpha = 0.15f) else GoldColor.copy(alpha = 0.15f),
                        contentColor = if (uiState.isPremium) CorrectGreen else GoldColor
                    )
                ) {
                    Icon(
                        if (uiState.isPremium) Icons.Filled.Verified else Icons.Filled.WorkspacePremium,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(Modifier.width(4.dp))
                    Text(if (uiState.isPremium) "Pro" else "Upgrade")
                }
            }
        }

        // Model status banner
        if (!uiState.isModelReady) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.errorContainer
                    )
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { navController.navigate(SubScreen.MODEL_DOWNLOAD) }
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Icon(Icons.Filled.Download, contentDescription = null)
                        Column(modifier = Modifier.weight(1f)) {
                            Text("AI Model Required", fontWeight = FontWeight.Bold)
                            Text(
                                "Download Gemma 4 E2B (~2.5 GB) for AI-powered features",
                                style = MaterialTheme.typography.bodySmall
                            )
                        }
                        Icon(Icons.Filled.ArrowForward, contentDescription = null)
                    }
                }
            }
        }

        // Quick stats
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                StatCard(
                    modifier = Modifier.weight(1f),
                    title = "Practiced",
                    value = "${uiState.totalAttempts}",
                    icon = Icons.Filled.CheckCircle
                )
                StatCard(
                    modifier = Modifier.weight(1f),
                    title = "Accuracy",
                    value = "${uiState.overallAccuracy.toInt()}%",
                    icon = Icons.Filled.TrendingUp
                )
                StatCard(
                    modifier = Modifier.weight(1f),
                    title = "Streak",
                    value = "${uiState.streak} days",
                    icon = Icons.Filled.LocalFireDepartment
                )
            }
        }

        // Quick actions
        item {
            Text(
                text = "Quick Start",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
        }

        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                QuickActionCard(
                    modifier = Modifier.weight(1f),
                    title = "Practice",
                    subtitle = "Topic-wise MCQs",
                    icon = Icons.Filled.Quiz,
                    onClick = { navController.navigate("practice") }
                )
                QuickActionCard(
                    modifier = Modifier.weight(1f),
                    title = "Mock Test",
                    subtitle = "Full-length test",
                    icon = Icons.Filled.Timer,
                    onClick = {
                        viewModel.createMockTest { testId ->
                            if (testId != null) {
                                navController.navigate("mock_test/$testId")
                            }
                        }
                    }
                )
            }
        }

        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                QuickActionCard(
                    modifier = Modifier.weight(1f),
                    title = "Notes",
                    subtitle = "AI revision notes",
                    icon = Icons.Filled.MenuBook,
                    onClick = { navController.navigate("notes") }
                )
                QuickActionCard(
                    modifier = Modifier.weight(1f),
                    title = "PYQ Papers",
                    subtitle = "Previous years",
                    icon = Icons.Filled.History,
                    onClick = { navController.navigate(SubScreen.PYQ_YEARS) }
                )
            }
        }

        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                QuickActionCard(
                    modifier = Modifier.weight(1f),
                    title = "Mistakes",
                    subtitle = "Review wrong answers",
                    icon = Icons.Filled.ErrorOutline,
                    onClick = { navController.navigate(SubScreen.MISTAKE_JOURNAL) }
                )
                Spacer(modifier = Modifier.weight(1f))
            }
        }

        // Premium upsell banner
        if (!uiState.isPremium) {
            item {
                PremiumBanner(onPremiumClick = { navController.navigate(SubScreen.PREMIUM) })
            }
        }

        // Subject cards
        item {
            Text(
                text = "Subjects",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(top = 8.dp)
            )
        }

        items(uiState.subjects) { subject ->
            SubjectCard(
                subject = subject,
                onClick = { navController.navigate("practice/${subject.id}") }
            )
        }
    }
}

@Composable
private fun StatCard(
    modifier: Modifier = Modifier,
    title: String,
    value: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f),
                            MaterialTheme.colorScheme.surface
                        )
                    )
                )
                .padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(24.dp)
            )
            Spacer(Modifier.height(4.dp))
            Text(value, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
            Text(title, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@Composable
private fun QuickActionCard(
    modifier: Modifier = Modifier,
    title: String,
    subtitle: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    onClick: () -> Unit
) {
    Card(
        modifier = modifier.clickable(onClick = onClick),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(MaterialTheme.colorScheme.primaryContainer),
                contentAlignment = Alignment.Center
            ) {
                Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(20.dp))
            }
            Column {
                Text(title, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyLarge)
                Text(subtitle, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    }
}

@Composable
private fun SubjectCard(
    subject: com.jeeprep.app.data.db.entity.SubjectEntity,
    onClick: () -> Unit
) {
    val color = when (subject.id) {
        1 -> PhysicsColor
        2 -> ChemistryColor
        3 -> MathColor
        else -> MaterialTheme.colorScheme.primary
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(14.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    Brush.horizontalGradient(
                        colors = listOf(
                            color.copy(alpha = 0.12f),
                            color.copy(alpha = 0.03f)
                        )
                    )
                )
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                val icon = when (subject.iconName) {
                    "physics" -> Icons.Filled.Science
                    "chemistry" -> Icons.Filled.Biotech
                    "math" -> Icons.Filled.Functions
                    else -> Icons.Filled.School
                }
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(color.copy(alpha = 0.15f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(icon, contentDescription = null, tint = color, modifier = Modifier.size(24.dp))
                }
                Text(
                    subject.name,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }
            Icon(Icons.Filled.ChevronRight, contentDescription = null, tint = color)
        }
    }
}
