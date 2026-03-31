package com.jeeprep.app.ui.screens.mistakes

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.jeeprep.app.data.db.entity.QuestionEntity
import com.jeeprep.app.data.db.entity.UserAttemptEntity
import com.jeeprep.app.data.repository.PerformanceRepository
import com.jeeprep.app.ai.AiEngine
import com.jeeprep.app.ai.PromptTemplates
import com.jeeprep.app.premium.PremiumManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class MistakeItem(
    val attempt: UserAttemptEntity,
    val question: QuestionEntity
)

data class MistakeJournalState(
    val mistakes: List<MistakeItem> = emptyList(),
    val isLoading: Boolean = true,
    val expandedId: Long? = null,
    val isPremium: Boolean = false,
    val aiOpinion: String = "",
    val isAiOpinionLoading: Boolean = false
)

@HiltViewModel
class MistakeJournalViewModel @Inject constructor(
    private val performanceRepo: PerformanceRepository,
    private val premiumManager: PremiumManager,
    private val aiEngine: AiEngine
) : ViewModel() {

    private val _uiState = MutableStateFlow(MistakeJournalState())
    val uiState: StateFlow<MistakeJournalState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            premiumManager.isPremium.collect { premium ->
                _uiState.update { it.copy(isPremium = premium) }
            }
        }
        viewModelScope.launch {
            performanceRepo.getWrongAttempts(100).collect { attempts ->
                val items = attempts.mapNotNull { attempt ->
                    val question = performanceRepo.getQuestionById(attempt.questionId)
                    question?.let { MistakeItem(attempt = attempt, question = it) }
                }
                // Deduplicate by questionId (keep latest attempt)
                val deduplicated = items
                    .groupBy { it.question.id }
                    .map { (_, list) -> list.maxByOrNull { it.attempt.timestamp }!! }
                    .sortedByDescending { it.attempt.timestamp }

                _uiState.update {
                    it.copy(mistakes = deduplicated, isLoading = false)
                }
            }
        }
    }

    fun toggleExpand(attemptId: Long) {
        _uiState.update {
            it.copy(expandedId = if (it.expandedId == attemptId) null else attemptId)
        }
    }

    fun loadAiOpinion() {
        val state = _uiState.value
        if (state.aiOpinion.isNotBlank() || state.isAiOpinionLoading || state.mistakes.isEmpty()) return

        _uiState.update { it.copy(isAiOpinionLoading = true) }

        viewModelScope.launch {
            try {
                val mistakeSummary = state.mistakes.take(10).joinToString("\n") { mistake ->
                    "Q: ${mistake.question.questionText.take(80)}... | Your: ${mistake.attempt.userAnswer} | Correct: ${mistake.question.correctAnswer} | Difficulty: ${mistake.question.difficulty}"
                }
                val prompt = """You are a JEE exam tutor. Analyze the student's recent mistakes and give a brief, encouraging opinion (3-4 sentences). Identify the main pattern of errors and suggest one specific improvement strategy.

Recent mistakes:
$mistakeSummary

Give your analysis in 3-4 sentences. Be specific about the error pattern and encouraging about improvement."""

                val fullText = StringBuilder()
                aiEngine.generateResponse(prompt).collect { chunk ->
                    fullText.append(chunk)
                    _uiState.update { it.copy(aiOpinion = fullText.toString()) }
                }
                _uiState.update { it.copy(isAiOpinionLoading = false) }
            } catch (e: Exception) {
                _uiState.update { it.copy(aiOpinion = "Could not generate analysis right now.", isAiOpinionLoading = false) }
            }
        }
    }
}
