package com.jeeprep.app.ui.screens.pyq

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import com.jeeprep.app.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PYQYearsScreen(
    navController: NavController,
    viewModel: PYQYearsViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Previous Year Questions") },
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
        } else if (uiState.years.isEmpty()) {
            Box(
                Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        Icons.Filled.History,
                        contentDescription = null,
                        modifier = Modifier.size(64.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(Modifier.height(16.dp))
                    Text("No PYQ papers yet", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    Spacer(Modifier.height(8.dp))
                    Text(
                        "Previous year question papers will appear here once added",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(uiState.years) { year ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { navController.navigate("pyq_paper/$year/Mains") }
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(20.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                Icon(Icons.Filled.Description, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                                Column {
                                    Text("JEE Mains $year", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                                    Text("Tap to practice", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                            }
                            Icon(Icons.Filled.ChevronRight, contentDescription = null)
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PYQPaperScreen(
    year: Int,
    examType: String,
    navController: NavController,
    viewModel: PYQPaperViewModel = hiltViewModel()
) {
    LaunchedEffect(year, examType) { viewModel.loadPaper(year, examType) }
    val uiState by viewModel.uiState.collectAsState()

    if (uiState.isSessionComplete) {
        PYQResultScreen(
            correctCount = uiState.correctCount,
            incorrectCount = uiState.incorrectCount,
            totalQuestions = uiState.totalQuestions,
            year = year,
            onBack = { navController.popBackStack() }
        )
        return
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("$examType $year  •  Q ${uiState.currentIndex + 1}/${uiState.totalQuestions}") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Filled.Close, contentDescription = "Exit")
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

        if (uiState.questions.isEmpty()) {
            Box(
                Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentAlignment = Alignment.Center
            ) {
                Text("No questions available for this paper", style = MaterialTheme.typography.bodyLarge)
            }
            return@Scaffold
        }

        val question = uiState.currentQuestion ?: return@Scaffold

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
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
                // Year & difficulty badge
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    AssistChip(onClick = {}, label = { Text("$year") })
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
                }

                // Question text
                MathText(text = question.questionText, modifier = Modifier.fillMaxWidth())

                Spacer(Modifier.height(8.dp))

                // Options
                val options = listOf(
                    "A" to question.optionA,
                    "B" to question.optionB,
                    "C" to question.optionC,
                    "D" to question.optionD
                )

                options.forEach { (letter, text) ->
                    PYQOptionCard(
                        letter = letter,
                        text = text,
                        isSelected = uiState.selectedAnswer == letter,
                        isSubmitted = uiState.isAnswerSubmitted,
                        isCorrect = letter == question.correctAnswer,
                        onClick = { viewModel.selectAnswer(letter) }
                    )
                }

                // Show answer explanation after submit
                if (uiState.isAnswerSubmitted && !question.explanation.isNullOrBlank()) {
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
                                Text("Explanation", fontWeight = FontWeight.Bold)
                            }
                            Spacer(Modifier.height(8.dp))
                            MathText(text = question.explanation!!)
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
private fun PYQOptionCard(
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
        label = "pyqOptionBg"
    )
    val borderColor by animateColorAsState(
        targetValue = when {
            isSubmitted && isCorrect -> CorrectGreen
            isSubmitted && isSelected && !isCorrect -> IncorrectRed
            isSelected -> MaterialTheme.colorScheme.primary
            else -> MaterialTheme.colorScheme.outlineVariant
        },
        label = "pyqOptionBorder"
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
            Text("$letter)", fontWeight = FontWeight.Bold, color = borderColor)
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
private fun PYQResultScreen(
    correctCount: Int,
    incorrectCount: Int,
    totalQuestions: Int,
    year: Int,
    onBack: () -> Unit
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

        Text("PYQ $year Complete!", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
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

        Button(onClick = onBack) {
            Text("Back to PYQ Papers")
        }
    }
}
