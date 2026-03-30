package com.jeeprep.app.ui.screens.chat

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.jeeprep.app.ai.AiEngine
import com.jeeprep.app.ai.PromptTemplates
import com.jeeprep.app.data.repository.QuestionRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class ChatMessage(
    val text: String,
    val isUser: Boolean,
    val isLoading: Boolean = false
)

data class ChatUiState(
    val messages: List<ChatMessage> = emptyList(),
    val questionText: String = "",
    val isLoading: Boolean = false,
    val isReady: Boolean = false
)

@HiltViewModel
class ChatViewModel @Inject constructor(
    private val aiEngine: AiEngine,
    private val repo: QuestionRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(ChatUiState())
    val uiState: StateFlow<ChatUiState> = _uiState.asStateFlow()

    private var systemPrompt = ""

    fun loadQuestion(questionId: Long) {
        viewModelScope.launch {
            val question = repo.getQuestionById(questionId) ?: return@launch
            systemPrompt = PromptTemplates.chatSystemPrompt(
                questionText = question.questionText,
                correctAnswer = "${question.correctAnswer}: ${
                    when (question.correctAnswer) {
                        "A" -> question.optionA
                        "B" -> question.optionB
                        "C" -> question.optionC
                        "D" -> question.optionD
                        else -> question.correctAnswer
                    }
                }",
                explanation = question.explanation ?: ""
            )
            _uiState.update {
                it.copy(
                    questionText = question.questionText,
                    isReady = true,
                    isLoading = true
                )
            }

            // Auto-generate an intro response about the question
            val introPrompt = """$systemPrompt

The student just opened the chat to discuss this question. Give a brief (2-3 sentence) welcome that:
- Mentions the key concept tested
- Hints at what makes this question tricky
- Invites them to ask follow-up questions
Use markdown and LaTeX (${'$'}...${'$'}) for math if needed."""

            val fullText = StringBuilder()
            aiEngine.generateResponse(introPrompt).collect { chunk ->
                fullText.append(chunk)
                _uiState.update { state ->
                    state.copy(
                        messages = listOf(ChatMessage(text = fullText.toString(), isUser = false)),
                        isLoading = false
                    )
                }
            }

            if (fullText.isEmpty()) {
                _uiState.update { state ->
                    state.copy(
                        messages = listOf(
                            ChatMessage(
                                text = "Hi! Ask me anything about this question — I can explain concepts, show alternative methods, or help you understand why other options are wrong.",
                                isUser = false
                            )
                        ),
                        isLoading = false
                    )
                }
            }
        }
    }

    fun sendMessage(userMessage: String) {
        if (userMessage.isBlank() || _uiState.value.isLoading) return

        if (aiEngine.isBusy) {
            _uiState.update { state ->
                state.copy(
                    messages = state.messages + ChatMessage(
                        text = "AI is currently generating notes in the background. Your message will be processed once it's done — please wait a moment.",
                        isUser = false
                    )
                )
            }
            return
        }

        val userMsg = ChatMessage(text = userMessage, isUser = true)
        val placeholder = ChatMessage(text = "", isUser = false, isLoading = true)
        _uiState.update {
            it.copy(
                messages = it.messages + userMsg + placeholder,
                isLoading = true
            )
        }

        viewModelScope.launch {
            // Build conversation context
            val conversationHistory = _uiState.value.messages
                .filter { !it.isLoading }
                .joinToString("\n") { msg ->
                    if (msg.isUser) "Student: ${msg.text}" else "Tutor: ${msg.text}"
                }

            val fullPrompt = """$systemPrompt

Previous conversation:
$conversationHistory

Student: $userMessage

Respond as the tutor. Be concise (under 100 words). Use markdown and LaTeX ($...$) for math."""

            val fullText = StringBuilder()
            aiEngine.generateResponse(fullPrompt).collect { chunk ->
                fullText.append(chunk)
                _uiState.update { state ->
                    val msgs = state.messages.toMutableList()
                    // Replace the last (placeholder/streaming) AI message
                    val lastIdx = msgs.indexOfLast { !it.isUser }
                    if (lastIdx >= 0) {
                        msgs[lastIdx] = ChatMessage(text = fullText.toString(), isUser = false)
                    }
                    state.copy(messages = msgs)
                }
            }

            // Streaming done
            if (fullText.isEmpty()) {
                _uiState.update { state ->
                    val msgs = state.messages.toMutableList()
                    val lastIdx = msgs.indexOfLast { !it.isUser }
                    if (lastIdx >= 0) {
                        msgs[lastIdx] = ChatMessage(
                            text = "Sorry, I couldn't generate a response. Make sure the AI model is downloaded.",
                            isUser = false
                        )
                    }
                    state.copy(messages = msgs, isLoading = false)
                }
            } else {
                _uiState.update { it.copy(isLoading = false) }
            }
        }
    }
}
