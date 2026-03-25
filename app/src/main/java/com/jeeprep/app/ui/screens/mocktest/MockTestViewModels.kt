package com.jeeprep.app.ui.screens.mocktest

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.jeeprep.app.data.db.dao.MockTestDao
import com.jeeprep.app.data.db.dao.MockTestResultDao
import com.jeeprep.app.data.db.entity.QuestionEntity
import com.jeeprep.app.data.db.entity.MockTestResultEntity
import com.jeeprep.app.data.repository.QuestionRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class MockTestUiState(
    val questions: List<QuestionEntity> = emptyList(),
    val currentIndex: Int = 0,
    val answers: Map<Int, String> = emptyMap(),
    val markedForReview: Set<Int> = emptySet(),
    val remainingSeconds: Int = 0,
    val isLoading: Boolean = true,
    val isPaused: Boolean = false,
    val isSubmitted: Boolean = false,
    val showPalette: Boolean = false
) {
    val currentQuestion: QuestionEntity? get() = questions.getOrNull(currentIndex)
    val attemptedCount: Int get() = answers.size
    val unattemptedCount: Int get() = questions.size - answers.size
    val timerText: String
        get() {
            val hours = remainingSeconds / 3600
            val minutes = (remainingSeconds % 3600) / 60
            val seconds = remainingSeconds % 60
            return "%02d:%02d:%02d".format(hours, minutes, seconds)
        }
}

@HiltViewModel
class MockTestViewModel @Inject constructor(
    private val repo: QuestionRepository,
    private val mockTestDao: MockTestDao,
    private val resultDao: MockTestResultDao
) : ViewModel() {

    private val _uiState = MutableStateFlow(MockTestUiState())
    val uiState: StateFlow<MockTestUiState> = _uiState.asStateFlow()

    private var timerJob: Job? = null

    fun loadTest(testId: Long) {
        viewModelScope.launch {
            val test = mockTestDao.getTestById(testId) ?: return@launch
            val questionIds = test.questionIds.split(",").mapNotNull { it.trim().toLongOrNull() }
            val questions = repo.getQuestionsByIds(questionIds)

            _uiState.update {
                it.copy(
                    questions = questions,
                    remainingSeconds = test.timeLimitMinutes * 60,
                    isLoading = false
                )
            }
            startTimer()
        }
    }

    private fun startTimer() {
        timerJob?.cancel()
        timerJob = viewModelScope.launch {
            while (_uiState.value.remainingSeconds > 0 && !_uiState.value.isSubmitted) {
                delay(1000)
                if (!_uiState.value.isPaused) {
                    _uiState.update { it.copy(remainingSeconds = it.remainingSeconds - 1) }
                }
            }
            if (_uiState.value.remainingSeconds <= 0 && !_uiState.value.isSubmitted) {
                submitTest()
            }
        }
    }

    fun selectAnswer(answer: String) {
        val index = _uiState.value.currentIndex
        _uiState.update {
            it.copy(answers = it.answers + (index to answer))
        }
    }

    fun navigateToQuestion(index: Int) {
        _uiState.update { it.copy(currentIndex = index, showPalette = false) }
    }

    fun nextQuestion() {
        val state = _uiState.value
        if (state.currentIndex < state.questions.size - 1) {
            _uiState.update { it.copy(currentIndex = it.currentIndex + 1) }
        }
    }

    fun previousQuestion() {
        if (_uiState.value.currentIndex > 0) {
            _uiState.update { it.copy(currentIndex = it.currentIndex - 1) }
        }
    }

    fun toggleMarkForReview() {
        val index = _uiState.value.currentIndex
        _uiState.update {
            val newSet = if (index in it.markedForReview) it.markedForReview - index
                         else it.markedForReview + index
            it.copy(markedForReview = newSet)
        }
    }

    fun togglePalette() {
        _uiState.update { it.copy(showPalette = !it.showPalette) }
    }

    fun submitTest() {
        timerJob?.cancel()
        val state = _uiState.value

        var correct = 0
        var incorrect = 0
        var physicsScore = 0
        var chemScore = 0
        var mathScore = 0

        state.questions.forEachIndexed { index, question ->
            val answer = state.answers[index]
            if (answer != null) {
                if (answer == question.correctAnswer) {
                    correct++
                    val score = if (question.questionType == "MCQ") 4 else 4
                    when {
                        question.topicId in 1..100 -> physicsScore += score
                        question.topicId in 101..200 -> chemScore += score
                        else -> mathScore += score
                    }
                } else {
                    incorrect++
                    val penalty = if (question.questionType == "MCQ") -1 else 0
                    when {
                        question.topicId in 1..100 -> physicsScore += penalty
                        question.topicId in 101..200 -> chemScore += penalty
                        else -> mathScore += penalty
                    }
                }
            }
        }

        val totalScore = (correct * 4) - (incorrect * 1)
        val maxScore = state.questions.size * 4

        viewModelScope.launch {
            // Record attempts
            state.questions.forEachIndexed { index, question ->
                val answer = state.answers[index] ?: return@forEachIndexed
                repo.recordAttempt(question.id, answer, answer == question.correctAnswer, 0)
            }
        }

        _uiState.update {
            it.copy(isSubmitted = true)
        }
    }
}

// --- Mock Result ---
data class MockResultState(
    val result: MockTestResultEntity? = null,
    val isLoading: Boolean = true
)

@HiltViewModel
class MockResultViewModel @Inject constructor(
    private val resultDao: MockTestResultDao
) : ViewModel() {
    private val _uiState = MutableStateFlow(MockResultState())
    val uiState: StateFlow<MockResultState> = _uiState.asStateFlow()

    fun loadResult(testId: Long) {
        viewModelScope.launch {
            val result = resultDao.getResultForTest(testId)
            _uiState.update { it.copy(result = result, isLoading = false) }
        }
    }
}
