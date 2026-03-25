package com.jeeprep.app.ui.screens.mocktest

import androidx.compose.foundation.layout.*
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MockResultScreen(
    testId: Long,
    navController: NavController,
    viewModel: MockResultViewModel = hiltViewModel()
) {
    LaunchedEffect(testId) { viewModel.loadResult(testId) }
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Test Result") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        val result = uiState.result
        if (uiState.isLoading || result == null) {
            Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                if (uiState.isLoading) CircularProgressIndicator()
                else Text("Result not found")
            }
            return@Scaffold
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text("${result.score} / ${result.maxScore}", style = MaterialTheme.typography.displaySmall, fontWeight = FontWeight.Bold)
            Text("Overall Score", style = MaterialTheme.typography.bodyLarge)

            HorizontalDivider()

            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("Physics", fontWeight = FontWeight.Bold)
                    Text("${result.physicsScore}")
                }
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("Chemistry", fontWeight = FontWeight.Bold)
                    Text("${result.chemistryScore}")
                }
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("Maths", fontWeight = FontWeight.Bold)
                    Text("${result.mathScore}")
                }
            }

            HorizontalDivider()

            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("${result.correctCount}", fontWeight = FontWeight.Bold)
                    Text("Correct", style = MaterialTheme.typography.bodySmall)
                }
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("${result.incorrectCount}", fontWeight = FontWeight.Bold)
                    Text("Wrong", style = MaterialTheme.typography.bodySmall)
                }
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("${result.unattemptedCount}", fontWeight = FontWeight.Bold)
                    Text("Skipped", style = MaterialTheme.typography.bodySmall)
                }
            }

            Spacer(Modifier.weight(1f))
            Button(onClick = { navController.popBackStack() }) { Text("Done") }
        }
    }
}
