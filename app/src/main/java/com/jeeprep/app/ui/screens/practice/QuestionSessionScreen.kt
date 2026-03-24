package com.jeeprep.app.ui.screens.practice

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
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
import com.jeeprep.app.ui.components.MathText
import com.jeeprep.app.ui.components.PremiumLockedButton
import com.jeeprep.app.ui.components.PremiumBanner
import com.jeeprep.app.ui.navigation.SubScreen
import com.jeeprep.app.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QuestionSessionScreen(
    subjectId: Int,
    topicId: Int,
    navController: NavController,
    viewModel: QuestionSessionViewModel = hiltViewModel()
) {
    LaunchedEffect(topicId) {
        viewModel.loadQuestions(topicId)
    }

    val uiState by viewModel.uiState.collectAsState()

    if (uiState.isSessionComplete) {
        SessionCompleteScreen(
            correctCount = uiState.correctCount,
            incorrectCount = uiState.incorrectCount,
            totalQuestions = uiState.totalQuestions,
            onBackToTopics = { navController.popBackStack() }
        )
        return
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Q ${uiState.currentIndex + 1}/${uiState.totalQuestions}") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Filled.Close, contentDescription = "Exit")
                    }
                },
                actions = {
                    IconButton(onClick = { viewModel.toggleBookmark() }) {
                        Icon(
                            if (uiState.isBookmarked) Icons.Filled.Bookmark else Icons.Filled.BookmarkBorder,
                            contentDescription = "Bookmark",
                            tint = if (uiState.isBookmarked) MaterialTheme.colorScheme.primary
                            else MaterialTheme.colorScheme.onSurfaceVariant
                        )
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

        val question = uiState.currentQuestion ?: return@Scaffold

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            // Progress bar
            LinearProgressIndicator(
                progress = { uiState.progress },
                modifier = Modifier.fillMaxWidth(),
            )

            Column(
                modifier = Modifier
                    .weight(1f)
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Difficulty badge
                AssistChip(
                    onClick = {},
                    label = { Text(question.difficulty) },
                    leadingIcon = {
                        val color = when (question.difficulty) {
                            "Easy" -> EasyColor
                            "Medium" -> MediumColor
                            "Hard" -> HardColor
                            else -> MaterialTheme.colorScheme.primary
                        }
                        Icon(Icons.Filled.Circle, contentDescription = null, tint = color, modifier = Modifier.size(8.dp))
                    }
                )

                // Question text
                MathText(
                    text = question.questionText,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(Modifier.height(8.dp))

                // Options
                val options = listOf(
                    "A" to question.optionA,
                    "B" to question.optionB,
                    "C" to question.optionC,
                    "D" to question.optionD
                )

                options.forEach { (letter, text) ->
                    OptionCard(
                        letter = letter,
                        text = text,
                        isSelected = uiState.selectedAnswer == letter,
                        isSubmitted = uiState.isAnswerSubmitted,
                        isCorrect = letter == question.correctAnswer,
                        onClick = { viewModel.selectAnswer(letter) }
                    )
                }

                // Explanation (after submit)
                if (uiState.isAnswerSubmitted) {
                    Spacer(Modifier.height(8.dp))

                    // Quick explanation card
                    Card(
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant
                        )
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Icon(Icons.Filled.Lightbulb, contentDescription = null, tint = Amber40)
                                Text("Quick Explanation", fontWeight = FontWeight.Bold)
                            }
                            Spacer(Modifier.height(8.dp))

                            if (uiState.isQuickLoading || uiState.isExplanationLoading) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    CircularProgressIndicator(modifier = Modifier.size(16.dp))
                                    Text(
                                        uiState.aiBusyMessage ?: "AI is thinking...",
                                        style = MaterialTheme.typography.bodySmall
                                    )
                                }
                            }

                            if (uiState.quickExplanation.isNotBlank()) {
                                MathText(text = uiState.quickExplanation)
                            } else if (uiState.explanation.isNotBlank()) {
                                MathText(text = uiState.explanation)
                            }
                        }
                    }

                    // Detailed explanation (expandable)
                    AnimatedVisibility(visible = uiState.showDetailed) {
                        Card(
                            modifier = Modifier.padding(top = 8.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.secondaryContainer
                            )
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Icon(Icons.Filled.MenuBook, contentDescription = null, tint = MaterialTheme.colorScheme.secondary)
                                    Text("Detailed Explanation", fontWeight = FontWeight.Bold)
                                }
                                Spacer(Modifier.height(8.dp))

                                if (uiState.isDetailedLoading && uiState.detailedExplanation.isBlank()) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        CircularProgressIndicator(modifier = Modifier.size(16.dp))
                                        Text("Generating detailed explanation...", style = MaterialTheme.typography.bodySmall)
                                    }
                                }

                                if (uiState.detailedExplanation.isNotBlank()) {
                                    MathText(text = uiState.detailedExplanation)
                                }
                            }
                        }
                    }

                    // Trick/shortcut (expandable)
                    AnimatedVisibility(visible = uiState.showTrick) {
                        Card(
                            modifier = Modifier.padding(top = 8.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.tertiaryContainer
                            )
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Icon(Icons.Filled.FlashOn, contentDescription = null, tint = Amber40)
                                    Text("Trick / Shortcut", fontWeight = FontWeight.Bold)
                                }
                                Spacer(Modifier.height(8.dp))

                                if (uiState.isTrickLoading && uiState.trick.isBlank()) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        CircularProgressIndicator(modifier = Modifier.size(16.dp))
                                        Text("Finding shortcuts...", style = MaterialTheme.typography.bodySmall)
                                    }
                                }

                                if (uiState.trick.isNotBlank()) {
                                    MathText(text = uiState.trick)
                                }
                            }
                        }
                    }

                    // Action buttons row (below all explanation cards)
                    if (uiState.quickExplanation.isNotBlank() || uiState.explanation.isNotBlank()) {
                        Spacer(Modifier.height(8.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            PremiumLockedButton(
                                text = "Detailed",
                                onClick = { viewModel.loadDetailedExplanation() },
                                onPremiumClick = { navController.navigate(SubScreen.PREMIUM) },
                                isPremium = uiState.isPremium,
                                modifier = Modifier.weight(1f),
                                icon = { Icon(Icons.Filled.MenuBook, contentDescription = null, modifier = Modifier.size(16.dp)) }
                            )
                            PremiumLockedButton(
                                text = "Trick",
                                onClick = { viewModel.loadTrick() },
                                onPremiumClick = { navController.navigate(SubScreen.PREMIUM) },
                                isPremium = uiState.isPremium,
                                modifier = Modifier.weight(1f),
                                icon = { Icon(Icons.Filled.FlashOn, contentDescription = null, modifier = Modifier.size(16.dp)) }
                            )
                            PremiumLockedButton(
                                text = "Ask AI",
                                onClick = {
                                    val q = uiState.currentQuestion ?: return@PremiumLockedButton
                                    navController.navigate("chat/${q.id}")
                                },
                                onPremiumClick = { navController.navigate(SubScreen.PREMIUM) },
                                isPremium = uiState.isPremium,
                                modifier = Modifier.weight(1f),
                                icon = { Icon(Icons.Filled.Chat, contentDescription = null, modifier = Modifier.size(16.dp)) }
                            )
                        }

                        // Premium upsell banner for free users
                        if (!uiState.isPremium) {
                            Spacer(Modifier.height(8.dp))
                            PremiumBanner(onPremiumClick = { navController.navigate(SubScreen.PREMIUM) })
                        }
                    }
                }
            }

            // Bottom action bar
            Surface(
                tonalElevation = 3.dp,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    if (!uiState.isAnswerSubmitted) {
                        Button(
                            onClick = { viewModel.submitAnswer() },
                            modifier = Modifier.fillMaxWidth(),
                            enabled = uiState.selectedAnswer != null
                        ) {
                            Text("Submit Answer")
                        }
                    } else {
                        Button(
                            onClick = { viewModel.nextQuestion() },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                if (uiState.currentIndex + 1 >= uiState.totalQuestions) "View Results"
                                else "Next Question"
                            )
                            Spacer(Modifier.width(8.dp))
                            Icon(Icons.Filled.ArrowForward, contentDescription = null)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun OptionCard(
    letter: String,
    text: String,
    isSelected: Boolean,
    isSubmitted: Boolean,
    isCorrect: Boolean,
    onClick: () -> Unit
) {
    val backgroundColor by animateColorAsState(
        targetValue = when {
            isSubmitted && isCorrect -> CorrectGreen.copy(alpha = 0.15f)
            isSubmitted && isSelected && !isCorrect -> IncorrectRed.copy(alpha = 0.15f)
            isSelected -> MaterialTheme.colorScheme.primaryContainer
            else -> MaterialTheme.colorScheme.surface
        },
        label = "optionBg"
    )

    val borderColor by animateColorAsState(
        targetValue = when {
            isSubmitted && isCorrect -> CorrectGreen
            isSubmitted && isSelected && !isCorrect -> IncorrectRed
            isSelected -> MaterialTheme.colorScheme.primary
            else -> MaterialTheme.colorScheme.outlineVariant
        },
        label = "optionBorder"
    )

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(enabled = !isSubmitted) { onClick() },
        colors = CardDefaults.cardColors(containerColor = backgroundColor),
        border = BorderStroke(1.dp, borderColor)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                "$letter)",
                fontWeight = FontWeight.Bold,
                color = borderColor
            )
            MathText(text = text, modifier = Modifier.weight(1f))
            if (isSubmitted) {
                if (isCorrect) {
                    Icon(Icons.Filled.CheckCircle, contentDescription = null, tint = CorrectGreen)
                } else if (isSelected) {
                    Icon(Icons.Filled.Cancel, contentDescription = null, tint = IncorrectRed)
                }
            }
        }
    }
}

@Composable
private fun SessionCompleteScreen(
    correctCount: Int,
    incorrectCount: Int,
    totalQuestions: Int,
    onBackToTopics: () -> Unit
) {
    val accuracy = if (totalQuestions > 0) (correctCount * 100) / totalQuestions else 0

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = if (accuracy >= 70) Icons.Filled.EmojiEvents else Icons.Filled.TrendingUp,
            contentDescription = null,
            modifier = Modifier.size(80.dp),
            tint = if (accuracy >= 70) Amber40 else MaterialTheme.colorScheme.primary
        )
        Spacer(Modifier.height(24.dp))

        Text("Session Complete!", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(16.dp))

        Text("$accuracy%", style = MaterialTheme.typography.displayMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
        Text("Accuracy", style = MaterialTheme.typography.bodyLarge)
        Spacer(Modifier.height(24.dp))

        Row(horizontalArrangement = Arrangement.spacedBy(32.dp)) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text("$correctCount", style = MaterialTheme.typography.titleLarge, color = CorrectGreen, fontWeight = FontWeight.Bold)
                Text("Correct", style = MaterialTheme.typography.bodySmall)
            }
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text("$incorrectCount", style = MaterialTheme.typography.titleLarge, color = IncorrectRed, fontWeight = FontWeight.Bold)
                Text("Incorrect", style = MaterialTheme.typography.bodySmall)
            }
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text("$totalQuestions", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                Text("Total", style = MaterialTheme.typography.bodySmall)
            }
        }

        Spacer(Modifier.height(32.dp))

        Button(onClick = onBackToTopics) {
            Text("Back to Topics")
        }
    }
}
