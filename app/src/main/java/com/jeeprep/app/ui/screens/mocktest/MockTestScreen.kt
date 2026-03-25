package com.jeeprep.app.ui.screens.mocktest

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.Flag
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.jeeprep.app.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MockTestScreen(
    testId: Long,
    navController: NavController,
    viewModel: MockTestViewModel = hiltViewModel()
) {
    LaunchedEffect(testId) { viewModel.loadTest(testId) }
    val uiState by viewModel.uiState.collectAsState()

    if (uiState.isLoading) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
        return
    }

    if (uiState.isSubmitted) {
        MockTestResultScreen(uiState = uiState, onDone = { navController.popBackStack() })
        return
    }

    var showSubmitDialog by remember { mutableStateOf(false) }

    if (showSubmitDialog) {
        AlertDialog(
            onDismissRequest = { showSubmitDialog = false },
            title = { Text("Submit Test?") },
            text = {
                Text("Attempted: ${uiState.attemptedCount}/${uiState.questions.size}\nUnattempted: ${uiState.unattemptedCount}")
            },
            confirmButton = {
                Button(onClick = {
                    showSubmitDialog = false
                    viewModel.submitTest()
                }) { Text("Submit") }
            },
            dismissButton = {
                TextButton(onClick = { showSubmitDialog = false }) { Text("Cancel") }
            }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Icon(Icons.Filled.Timer, contentDescription = null, modifier = Modifier.size(20.dp))
                        Text(uiState.timerText, fontWeight = FontWeight.Bold)
                    }
                },
                actions = {
                    IconButton(onClick = { viewModel.togglePalette() }) {
                        Icon(Icons.Filled.GridView, contentDescription = "Question Palette")
                    }
                    TextButton(onClick = { showSubmitDialog = true }) {
                        Text("Submit", color = MaterialTheme.colorScheme.error)
                    }
                }
            )
        }
    ) { padding ->
        if (uiState.showPalette) {
            QuestionPalette(
                totalQuestions = uiState.questions.size,
                answers = uiState.answers,
                markedForReview = uiState.markedForReview,
                currentIndex = uiState.currentIndex,
                onSelect = { viewModel.navigateToQuestion(it) },
                modifier = Modifier.padding(padding)
            )
        } else {
            val question = uiState.currentQuestion ?: return@Scaffold

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
            ) {
                // Question number bar
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Q ${uiState.currentIndex + 1}/${uiState.questions.size}", fontWeight = FontWeight.Bold)
                    TextButton(onClick = { viewModel.toggleMarkForReview() }) {
                        Icon(
                            if (uiState.currentIndex in uiState.markedForReview) Icons.Filled.Flag else Icons.Outlined.Flag,
                            contentDescription = null,
                            tint = MarkedBlue
                        )
                        Spacer(Modifier.width(4.dp))
                        Text("Mark for Review")
                    }
                }

                // Question content
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .verticalScroll(rememberScrollState())
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text(question.questionText, style = MaterialTheme.typography.bodyLarge)
                    Spacer(Modifier.height(8.dp))

                    val options = listOf("A" to question.optionA, "B" to question.optionB, "C" to question.optionC, "D" to question.optionD)
                    val selectedAnswer = uiState.answers[uiState.currentIndex]

                    options.forEach { (letter, text) ->
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { viewModel.selectAnswer(letter) },
                            colors = CardDefaults.cardColors(
                                containerColor = if (selectedAnswer == letter) MaterialTheme.colorScheme.primaryContainer
                                else MaterialTheme.colorScheme.surface
                            ),
                            border = BorderStroke(
                                1.dp,
                                if (selectedAnswer == letter) MaterialTheme.colorScheme.primary
                                else MaterialTheme.colorScheme.outlineVariant
                            )
                        ) {
                            Row(modifier = Modifier.padding(14.dp), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                                Text("$letter)", fontWeight = FontWeight.Bold)
                                Text(text)
                            }
                        }
                    }
                }

                // Navigation buttons
                Surface(tonalElevation = 3.dp) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        OutlinedButton(
                            onClick = { viewModel.previousQuestion() },
                            enabled = uiState.currentIndex > 0
                        ) {
                            Icon(Icons.Filled.ArrowBack, contentDescription = null)
                            Spacer(Modifier.width(4.dp))
                            Text("Previous")
                        }
                        Button(
                            onClick = { viewModel.nextQuestion() },
                            enabled = uiState.currentIndex < uiState.questions.size - 1
                        ) {
                            Text("Next")
                            Spacer(Modifier.width(4.dp))
                            Icon(Icons.Filled.ArrowForward, contentDescription = null)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun QuestionPalette(
    totalQuestions: Int,
    answers: Map<Int, String>,
    markedForReview: Set<Int>,
    currentIndex: Int,
    onSelect: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier.padding(16.dp)) {
        Text("Question Palette", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(8.dp))

        Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            LegendItem(color = CorrectGreen, label = "Answered")
            LegendItem(color = UnattemptedGray, label = "Not Answered")
            LegendItem(color = MarkedBlue, label = "Marked")
        }

        Spacer(Modifier.height(16.dp))

        LazyVerticalGrid(
            columns = GridCells.Fixed(6),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(totalQuestions) { index ->
                val bgColor = when {
                    index in markedForReview -> MarkedBlue
                    index in answers -> CorrectGreen
                    else -> UnattemptedGray
                }

                Card(
                    modifier = Modifier
                        .aspectRatio(1f)
                        .clickable { onSelect(index) },
                    colors = CardDefaults.cardColors(containerColor = bgColor.copy(alpha = 0.3f)),
                    border = if (index == currentIndex) BorderStroke(2.dp, MaterialTheme.colorScheme.primary) else null
                ) {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text("${index + 1}", fontWeight = FontWeight.Bold, textAlign = TextAlign.Center)
                    }
                }
            }
        }
    }
}

@Composable
private fun LegendItem(color: androidx.compose.ui.graphics.Color, label: String) {
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
        Surface(modifier = Modifier.size(12.dp), color = color.copy(alpha = 0.5f), shape = MaterialTheme.shapes.extraSmall) {}
        Text(label, style = MaterialTheme.typography.bodySmall)
    }
}

@Composable
private fun MockTestResultScreen(
    uiState: MockTestUiState,
    onDone: () -> Unit
) {
    val answers = uiState.answers
    var correct = 0
    var incorrect = 0
    uiState.questions.forEachIndexed { i, q ->
        val a = answers[i]
        if (a != null) {
            if (a == q.correctAnswer) correct++ else incorrect++
        }
    }
    val unattempted = uiState.questions.size - answers.size
    val score = (correct * 4) - (incorrect * 1)
    val maxScore = uiState.questions.size * 4

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(Icons.Filled.Assignment, contentDescription = null, modifier = Modifier.size(64.dp), tint = MaterialTheme.colorScheme.primary)
        Spacer(Modifier.height(16.dp))
        Text("Test Complete!", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(24.dp))

        Text("$score / $maxScore", style = MaterialTheme.typography.displaySmall, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
        Text("Score", style = MaterialTheme.typography.bodyLarge)
        Spacer(Modifier.height(24.dp))

        Row(horizontalArrangement = Arrangement.spacedBy(24.dp)) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text("$correct", color = CorrectGreen, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleLarge)
                Text("Correct", style = MaterialTheme.typography.bodySmall)
            }
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text("$incorrect", color = IncorrectRed, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleLarge)
                Text("Wrong", style = MaterialTheme.typography.bodySmall)
            }
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text("$unattempted", color = UnattemptedGray, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleLarge)
                Text("Skipped", style = MaterialTheme.typography.bodySmall)
            }
        }

        Spacer(Modifier.height(16.dp))
        Text("Marking: +4 correct, -1 wrong (MCQ), +4 correct, 0 wrong (Numerical)", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant, textAlign = TextAlign.Center)

        Spacer(Modifier.height(32.dp))
        Button(onClick = onDone) { Text("Done") }
    }
}
