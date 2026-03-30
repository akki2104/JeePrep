package com.jeeprep.app.ui.screens.chat

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.jeeprep.app.ui.components.MathText
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatScreen(
    questionId: Long,
    navController: NavController,
    viewModel: ChatViewModel = hiltViewModel()
) {
    LaunchedEffect(questionId) {
        viewModel.loadQuestion(questionId)
    }

    val uiState by viewModel.uiState.collectAsState()
    var inputText by remember { mutableStateOf("") }
    val listState = rememberLazyListState()
    val coroutineScope = rememberCoroutineScope()

    // Auto-scroll to bottom when new messages arrive
    LaunchedEffect(uiState.messages.size) {
        if (uiState.messages.isNotEmpty()) {
            listState.animateScrollToItem(uiState.messages.size - 1)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Discuss with AI") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            // Question preview card
            if (uiState.questionText.isNotBlank()) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant
                    )
                ) {
                    Text(
                        text = uiState.questionText,
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.padding(12.dp),
                        maxLines = 3
                    )
                }
            }

            // Messages
            LazyColumn(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                state = listState,
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(uiState.messages.filter { !it.isLoading || it.text.isNotBlank() }) { message ->
                    if (message.isLoading && message.text.isBlank()) return@items
                    ChatBubble(message = message)
                }

                if (uiState.isLoading) {
                    item {
                        Row(
                            modifier = Modifier.padding(start = 8.dp, top = 4.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            CircularProgressIndicator(modifier = Modifier.size(16.dp))
                            Text("Thinking...", style = MaterialTheme.typography.bodySmall)
                        }
                    }
                }
            }

            // Input bar
            Surface(tonalElevation = 3.dp) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = inputText,
                        onValueChange = { inputText = it },
                        modifier = Modifier.weight(1f),
                        placeholder = { Text("Ask about this question...") },
                        maxLines = 3,
                        shape = RoundedCornerShape(24.dp)
                    )
                    FilledIconButton(
                        onClick = {
                            if (inputText.isNotBlank()) {
                                viewModel.sendMessage(inputText)
                                inputText = ""
                                coroutineScope.launch {
                                    listState.animateScrollToItem(uiState.messages.size)
                                }
                            }
                        },
                        enabled = inputText.isNotBlank() && !uiState.isLoading
                    ) {
                        Icon(Icons.Filled.Send, contentDescription = "Send")
                    }
                }
            }
        }
    }
}

@Composable
private fun ChatBubble(message: ChatMessage) {
    val alignment = if (message.isUser) Alignment.End else Alignment.Start
    val bgColor = if (message.isUser)
        MaterialTheme.colorScheme.primaryContainer
    else
        MaterialTheme.colorScheme.surfaceVariant
    val shape = if (message.isUser)
        RoundedCornerShape(16.dp, 16.dp, 4.dp, 16.dp)
    else
        RoundedCornerShape(16.dp, 16.dp, 16.dp, 4.dp)

    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = alignment
    ) {
        if (!message.isUser) {
            Text(
                "AI Tutor",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(start = 8.dp, bottom = 2.dp)
            )
        }
        Box(
            modifier = Modifier
                .widthIn(max = 300.dp)
                .clip(shape)
                .background(bgColor)
                .padding(12.dp)
        ) {
            if (message.isUser) {
                Text(
                    text = message.text,
                    style = MaterialTheme.typography.bodyMedium
                )
            } else {
                MathText(text = message.text)
            }
        }
    }
}
