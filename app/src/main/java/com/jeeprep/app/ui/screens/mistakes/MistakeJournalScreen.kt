package com.jeeprep.app.ui.screens.mistakes

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.jeeprep.app.ui.components.PremiumLockedButton
import com.jeeprep.app.ui.components.PremiumBanner
import com.jeeprep.app.ui.navigation.SubScreen
import com.jeeprep.app.ui.theme.*
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MistakeJournalScreen(
    navController: NavController,
    viewModel: MistakeJournalViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Mistake Journal") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        if (uiState.isLoading) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
            return@Scaffold
        }

        if (uiState.mistakes.isEmpty()) {
            Box(
                Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        Icons.Filled.CheckCircle,
                        contentDescription = null,
                        modifier = Modifier.size(64.dp),
                        tint = CorrectGreen
                    )
                    Spacer(Modifier.height(16.dp))
                    Text("No mistakes yet!", style = MaterialTheme.typography.titleMedium)
                    Text(
                        "Practice some questions to see your mistakes here",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            return@Scaffold
        }

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // AI Opinion card (premium)
            if (uiState.isPremium && uiState.mistakes.isNotEmpty()) {
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = GoldLightColor.copy(alpha = 0.3f))
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Icon(Icons.Filled.Psychology, contentDescription = null, tint = GoldColor)
                                Text("AI Analysis", fontWeight = FontWeight.Bold)
                                Badge(containerColor = GoldColor) {
                                    Text("PRO", style = MaterialTheme.typography.labelSmall, color = androidx.compose.ui.graphics.Color.White)
                                }
                            }
                            Spacer(Modifier.height(8.dp))

                            if (uiState.aiOpinion.isNotBlank()) {
                                Text(uiState.aiOpinion, style = MaterialTheme.typography.bodyMedium)
                            } else if (uiState.isAiOpinionLoading) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    CircularProgressIndicator(modifier = Modifier.size(16.dp))
                                    Text("Analyzing your mistakes...", style = MaterialTheme.typography.bodySmall)
                                }
                            } else {
                                OutlinedButton(onClick = { viewModel.loadAiOpinion() }) {
                                    Icon(Icons.Filled.AutoAwesome, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Spacer(Modifier.width(8.dp))
                                    Text("Get AI Opinion")
                                }
                            }
                        }
                    }
                }
            }

            item {
                Text(
                    "${uiState.mistakes.size} unique mistakes",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(Modifier.height(4.dp))
            }

            items(uiState.mistakes, key = { it.attempt.id }) { mistake ->
                val isExpanded = uiState.expandedId == mistake.attempt.id
                val dateFormat = remember { SimpleDateFormat("dd MMM, HH:mm", Locale.getDefault()) }

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    onClick = { viewModel.toggleExpand(mistake.attempt.id) }
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.Top
                        ) {
                            Text(
                                text = mistake.question.questionText,
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Medium,
                                modifier = Modifier.weight(1f),
                                maxLines = if (isExpanded) Int.MAX_VALUE else 2
                            )
                            Icon(
                                if (isExpanded) Icons.Filled.ExpandLess else Icons.Filled.ExpandMore,
                                contentDescription = null,
                                modifier = Modifier.padding(start = 8.dp)
                            )
                        }

                        Spacer(Modifier.height(8.dp))

                        Row(
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            AssistChip(
                                onClick = {},
                                label = { Text(mistake.question.difficulty) },
                                leadingIcon = {
                                    val color = when (mistake.question.difficulty) {
                                        "Easy" -> EasyColor
                                        "Medium" -> MediumColor
                                        "Hard" -> HardColor
                                        else -> MaterialTheme.colorScheme.primary
                                    }
                                    Icon(Icons.Filled.Circle, contentDescription = null, tint = color, modifier = Modifier.size(8.dp))
                                }
                            )
                            Text(
                                text = dateFormat.format(Date(mistake.attempt.timestamp)),
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.align(Alignment.CenterVertically)
                            )
                        }

                        AnimatedVisibility(visible = isExpanded) {
                            Column(modifier = Modifier.padding(top = 12.dp)) {
                                HorizontalDivider()
                                Spacer(Modifier.height(12.dp))

                                // Your answer vs correct answer
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            "Your Answer",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = IncorrectRed
                                        )
                                        val yourAnswer = when (mistake.attempt.userAnswer) {
                                            "A" -> mistake.question.optionA
                                            "B" -> mistake.question.optionB
                                            "C" -> mistake.question.optionC
                                            "D" -> mistake.question.optionD
                                            else -> mistake.attempt.userAnswer
                                        }
                                        Text(
                                            "${mistake.attempt.userAnswer}) $yourAnswer",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = IncorrectRed
                                        )
                                    }
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            "Correct Answer",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = CorrectGreen
                                        )
                                        val correctAnswer = when (mistake.question.correctAnswer) {
                                            "A" -> mistake.question.optionA
                                            "B" -> mistake.question.optionB
                                            "C" -> mistake.question.optionC
                                            "D" -> mistake.question.optionD
                                            else -> mistake.question.correctAnswer
                                        }
                                        Text(
                                            "${mistake.question.correctAnswer}) $correctAnswer",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = CorrectGreen
                                        )
                                    }
                                }

                                // Show explanation if cached
                                if (!mistake.question.explanation.isNullOrBlank()) {
                                    Spacer(Modifier.height(12.dp))
                                    Card(
                                        colors = CardDefaults.cardColors(
                                            containerColor = MaterialTheme.colorScheme.surfaceVariant
                                        )
                                    ) {
                                        Column(modifier = Modifier.padding(12.dp)) {
                                            Text(
                                                "Explanation",
                                                style = MaterialTheme.typography.labelSmall,
                                                fontWeight = FontWeight.Bold
                                            )
                                            Spacer(Modifier.height(4.dp))
                                            Text(
                                                text = mistake.question.explanation!!,
                                                style = MaterialTheme.typography.bodySmall
                                            )
                                        }
                                    }
                                }

                                // Discuss with AI button
                                Spacer(Modifier.height(8.dp))
                                PremiumLockedButton(
                                    text = "Discuss with AI",
                                    onClick = {
                                        navController.navigate("chat/${mistake.question.id}")
                                    },
                                    onPremiumClick = { navController.navigate(SubScreen.PREMIUM) },
                                    isPremium = uiState.isPremium,
                                    modifier = Modifier.fillMaxWidth(),
                                    icon = { Icon(Icons.Filled.Chat, contentDescription = null, modifier = Modifier.size(16.dp)) }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
