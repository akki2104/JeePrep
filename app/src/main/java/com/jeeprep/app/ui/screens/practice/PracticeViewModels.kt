package com.jeeprep.app.ui.screens.practice

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.jeeprep.app.ai.AiEngine
import com.jeeprep.app.ai.PromptTemplates
import com.jeeprep.app.data.db.entity.SubjectEntity
import com.jeeprep.app.data.db.entity.TopicEntity
import com.jeeprep.app.data.db.entity.QuestionEntity
import com.jeeprep.app.data.repository.QuestionRepository
import com.jeeprep.app.premium.PremiumManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

// --- Practice Subject Selection ---
data class PracticeUiState(
    val subjects: List<SubjectEntity> = emptyList(),
    val isLoading: Boolean = true
)

@HiltViewModel
class PracticeViewModel @Inject constructor(
    private val repo: QuestionRepository
) : ViewModel() {
    private val _uiState = MutableStateFlow(PracticeUiState())
    val uiState: StateFlow<PracticeUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            repo.getAllSubjects().collect { subjects ->
                _uiState.update { it.copy(subjects = subjects, isLoading = false) }
            }
        }
    }
}

// --- Topic List for a Subject ---
data class TopicListUiState(
    val topics: List<TopicEntity> = emptyList(),
    val isLoading: Boolean = true
)

@HiltViewModel
class TopicListViewModel @Inject constructor(
    private val repo: QuestionRepository
) : ViewModel() {
    private val _uiState = MutableStateFlow(TopicListUiState())
    val uiState: StateFlow<TopicListUiState> = _uiState.asStateFlow()

    fun loadTopics(subjectId: Int) {
        viewModelScope.launch {
            repo.getTopicsBySubject(subjectId).collect { topics ->
                _uiState.update { it.copy(topics = topics, isLoading = false) }
            }
        }
    }
}

// --- Question Session ---
data class QuestionSessionState(
    val questions: List<QuestionEntity> = emptyList(),
    val currentIndex: Int = 0,
    val selectedAnswer: String? = null,
    val isAnswerSubmitted: Boolean = false,
    // Stepped explanation
    val quickExplanation: String = "",
    val detailedExplanation: String = "",
    val trick: String = "",
    val isQuickLoading: Boolean = false,
    val isDetailedLoading: Boolean = false,
    val isTrickLoading: Boolean = false,
    val isQuickComplete: Boolean = false,
    val isDetailedComplete: Boolean = false,
    val isTrickComplete: Boolean = false,
    val showDetailed: Boolean = false,
    val showTrick: Boolean = false,
    // Legacy (maps to quickExplanation)
    val explanation: String = "",
    val isExplanationLoading: Boolean = false,
    // Scores
    val correctCount: Int = 0,
    val incorrectCount: Int = 0,
    val isSessionComplete: Boolean = false,
    val isLoading: Boolean = true,
    val isBookmarked: Boolean = false,
    val aiBusyMessage: String? = null,
    val isPremium: Boolean = false,
    // Topic context for AI generation
    val topicId: Int = 0,
    val subjectName: String = "",
    val topicName: String = ""
) {
    val currentQuestion: QuestionEntity?
        get() = questions.getOrNull(currentIndex)

    val totalQuestions: Int get() = questions.size
    val progress: Float get() = if (totalQuestions > 0) (currentIndex + 1).toFloat() / totalQuestions else 0f
}

@HiltViewModel
class QuestionSessionViewModel @Inject constructor(
    private val repo: QuestionRepository,
    private val aiEngine: AiEngine,
    private val premiumManager: PremiumManager
) : ViewModel() {
    private val _uiState = MutableStateFlow(QuestionSessionState())
    val uiState: StateFlow<QuestionSessionState> = _uiState.asStateFlow()

    private var sessionStartTime = 0L
    private var questionStartTime = 0L

    init {
        viewModelScope.launch {
            premiumManager.isPremium.collect { premium ->
                _uiState.update { it.copy(isPremium = premium) }
            }
        }
    }

    fun loadQuestions(topicId: Int) {
        viewModelScope.launch {
            val questions = repo.getQuestionsByTopic(topicId, limit = 50)
            val topic = repo.getTopicById(topicId)
            val subject = topic?.let { repo.getSubjectById(it.subjectId) }
            sessionStartTime = System.currentTimeMillis()
            questionStartTime = System.currentTimeMillis()
            _uiState.update {
                it.copy(
                    questions = questions,
                    isLoading = false,
                    topicId = topicId,
                    subjectName = subject?.name ?: "",
                    topicName = topic?.name ?: ""
                )
            }
            questions.firstOrNull()?.let { q -> checkBookmark(q.id) }
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
        val timeTaken = ((System.currentTimeMillis() - questionStartTime) / 1000).toInt()

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

        loadQuickExplanation(question)
    }

    private fun sanitizeAiText(raw: String): String = raw
        .replace(Regex("[\u0001-\u0008\u000B\u000C\u000E-\u001F]"), "")
        .replace(Regex("[\u2500-\u259F\u2E80-\u9FFF\uAC00-\uD7AF\uF900-\uFAFF]"), "")

    private fun loadQuickExplanation(question: QuestionEntity) {
        // If cached explanation exists, use it (already complete)
        question.explanation?.let { cached ->
            if (cached.isNotBlank()) {
                _uiState.update { it.copy(quickExplanation = cached, explanation = cached, isQuickComplete = true) }
                return
            }
        }

        if (aiEngine.isBusy) {
            _uiState.update {
                it.copy(
                    aiBusyMessage = "AI is currently generating notes in the background. Explanation will load once it's done.",
                    isQuickLoading = true, isExplanationLoading = true
                )
            }
        } else {
            _uiState.update { it.copy(isQuickLoading = true, isExplanationLoading = true, aiBusyMessage = null) }
        }

        viewModelScope.launch {
            try {
                val options = listOf(question.optionA, question.optionB, question.optionC, question.optionD)
                val prompt = PromptTemplates.quickExplain(question.questionText, options, question.correctAnswer)
                val fullText = StringBuilder()
                aiEngine.generateResponse(prompt).collect { chunk ->
                    fullText.append(chunk)
                    _uiState.update {
                        it.copy(
                            quickExplanation = fullText.toString(),
                            explanation = fullText.toString(),
                            isQuickLoading = false, isExplanationLoading = false,
                            aiBusyMessage = null
                        )
                    }
                }
                // Generation complete — sanitize and mark done
                val sanitized = sanitizeAiText(fullText.toString())
                _uiState.update { it.copy(quickExplanation = sanitized, explanation = sanitized, isQuickComplete = true) }
                repo.cacheExplanation(question.id, sanitized)
            } catch (e: Exception) {
                Log.e("QuickExplain", "Error", e)
                _uiState.update {
                    it.copy(quickExplanation = "Failed to generate explanation.",
                        isQuickLoading = false, isExplanationLoading = false,
                        isQuickComplete = true, aiBusyMessage = null)
                }
            }
        }
    }

    fun loadDetailedExplanation() {
        val question = _uiState.value.currentQuestion ?: return
        if (_uiState.value.detailedExplanation.isNotBlank()) {
            _uiState.update { it.copy(showDetailed = true) }
            return
        }
        if (aiEngine.isBusy) {
            _uiState.update { it.copy(showDetailed = true, aiBusyMessage = "AI is busy. This will start once it's done.", isDetailedLoading = true) }
        } else {
            _uiState.update { it.copy(showDetailed = true, isDetailedLoading = true, aiBusyMessage = null) }
        }

        viewModelScope.launch {
            try {
                val options = listOf(question.optionA, question.optionB, question.optionC, question.optionD)
                val prompt = PromptTemplates.detailedExplain(question.questionText, options, question.correctAnswer)
                val fullText = StringBuilder()
                aiEngine.generateResponse(prompt).collect { chunk ->
                    fullText.append(chunk)
                    _uiState.update { it.copy(detailedExplanation = fullText.toString(), isDetailedLoading = false, aiBusyMessage = null) }
                }
                val sanitized = sanitizeAiText(fullText.toString())
                _uiState.update { it.copy(detailedExplanation = sanitized, isDetailedComplete = true) }
            } catch (e: Exception) {
                Log.e("QuickExplain", "Error detailed", e)
                _uiState.update { it.copy(detailedExplanation = "Failed to generate.", isDetailedLoading = false, isDetailedComplete = true, aiBusyMessage = null) }
            }
        }
    }

    fun loadTrick() {
        val question = _uiState.value.currentQuestion ?: return
        if (_uiState.value.trick.isNotBlank()) {
            _uiState.update { it.copy(showTrick = true) }
            return
        }
        if (aiEngine.isBusy) {
            _uiState.update { it.copy(showTrick = true, aiBusyMessage = "AI is busy. This will start once it's done.", isTrickLoading = true) }
        } else {
            _uiState.update { it.copy(showTrick = true, isTrickLoading = true, aiBusyMessage = null) }
        }

        viewModelScope.launch {
            try {
                val options = listOf(question.optionA, question.optionB, question.optionC, question.optionD)
                val prompt = PromptTemplates.trickForQuestion(question.questionText, options, question.correctAnswer)
                val fullText = StringBuilder()
                aiEngine.generateResponse(prompt).collect { chunk ->
                    fullText.append(chunk)
                    _uiState.update { it.copy(trick = fullText.toString(), isTrickLoading = false, showTrick = true, aiBusyMessage = null) }
                }
                val sanitized = sanitizeAiText(fullText.toString())
                _uiState.update { it.copy(trick = sanitized, isTrickComplete = true) }
            } catch (e: Exception) {
                Log.e("QuickExplain", "Error trick", e)
                _uiState.update { it.copy(trick = "Failed to generate.", isTrickLoading = false, isTrickComplete = true, aiBusyMessage = null) }
            }
        }
    }

    fun nextQuestion() {
        val state = _uiState.value
        if (state.currentIndex + 1 >= state.totalQuestions) {
            _uiState.update { it.copy(isSessionComplete = true) }
            return
        }

        questionStartTime = System.currentTimeMillis()
        val nextIndex = state.currentIndex + 1
        _uiState.update {
            it.copy(
                currentIndex = nextIndex,
                selectedAnswer = null,
                isAnswerSubmitted = false,
                quickExplanation = "",
                detailedExplanation = "",
                trick = "",
                explanation = "",
                isQuickLoading = false,
                isDetailedLoading = false,
                isTrickLoading = false,
                isExplanationLoading = false,
                isQuickComplete = false,
                isDetailedComplete = false,
                isTrickComplete = false,
                showDetailed = false,
                showTrick = false,
                isBookmarked = false
            )
        }

        state.questions.getOrNull(nextIndex)?.let { q -> checkBookmark(q.id) }
    }

    fun toggleBookmark() {
        val questionId = _uiState.value.currentQuestion?.id ?: return
        viewModelScope.launch {
            repo.toggleBookmark(questionId)
            checkBookmark(questionId)
        }
    }

    private fun checkBookmark(questionId: Long) {
        viewModelScope.launch {
            repo.isBookmarked(questionId).collect { isBookmarked ->
                _uiState.update { it.copy(isBookmarked = isBookmarked) }
            }
        }
    }
}
