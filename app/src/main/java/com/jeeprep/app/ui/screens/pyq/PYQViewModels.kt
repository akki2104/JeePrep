package com.jeeprep.app.ui.screens.pyq

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.jeeprep.app.data.db.entity.QuestionEntity
import com.jeeprep.app.data.repository.QuestionRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

// --- PYQ Year List ---
data class PYQYearsUiState(
    val years: List<Int> = emptyList(),
    val isLoading: Boolean = true
)

@HiltViewModel
class PYQYearsViewModel @Inject constructor(
    private val repo: QuestionRepository
) : ViewModel() {
    private val _uiState = MutableStateFlow(PYQYearsUiState())
    val uiState: StateFlow<PYQYearsUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            repo.getAvailableYears().collect { years ->
                _uiState.update { it.copy(years = years, isLoading = false) }
            }
        }
    }
}

// --- PYQ Paper (Interactive Practice Mode) ---
data class PYQPaperUiState(
    val questions: List<QuestionEntity> = emptyList(),
    val currentIndex: Int = 0,
    val selectedAnswer: String? = null,
    val isAnswerSubmitted: Boolean = false,
    val correctCount: Int = 0,
    val incorrectCount: Int = 0,
    val isSessionComplete: Boolean = false,
    val isLoading: Boolean = true
) {
    val currentQuestion: QuestionEntity? get() = questions.getOrNull(currentIndex)
    val totalQuestions: Int get() = questions.size
    val progress: Float get() = if (totalQuestions > 0) (currentIndex + 1).toFloat() / totalQuestions else 0f
}

@HiltViewModel
class PYQPaperViewModel @Inject constructor(
    private val repo: QuestionRepository
) : ViewModel() {
    private val _uiState = MutableStateFlow(PYQPaperUiState())
    val uiState: StateFlow<PYQPaperUiState> = _uiState.asStateFlow()

    fun loadPaper(year: Int, examType: String) {
        viewModelScope.launch {
            val questions = repo.getQuestionsByYear(year, examType)
            _uiState.update { it.copy(questions = questions, isLoading = false) }
        }
    }

    fun selectAnswer(answer: String) {
        if (_uiState.value.isAnswerSubmitted) return
        _uiState.update { it.copy(selectedAnswer = answer) }
    }

    fun submitAnswer() {
        val state = _uiState.value
        val question = state.currentQuestion ?: return
        val answer = state.selectedAnswer ?: return
        if (state.isAnswerSubmitted) return

        val isCorrect = answer == question.correctAnswer
        val timeTaken = 0 // PYQ mode doesn't track time per question

        viewModelScope.launch {
            repo.recordAttempt(question.id, answer, isCorrect, timeTaken)
        }

        _uiState.update {
            it.copy(
                isAnswerSubmitted = true,
                correctCount = if (isCorrect) it.correctCount + 1 else it.correctCount,
                incorrectCount = if (!isCorrect) it.incorrectCount + 1 else it.incorrectCount
            )
        }
    }

    fun nextQuestion() {
        val state = _uiState.value
        if (state.currentIndex + 1 >= state.totalQuestions) {
            _uiState.update { it.copy(isSessionComplete = true) }
            return
        }
        _uiState.update {
            it.copy(
                currentIndex = it.currentIndex + 1,
                selectedAnswer = null,
                isAnswerSubmitted = false
            )
        }
    }
}
