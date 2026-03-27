package com.jeeprep.app.ui.screens.progress

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.jeeprep.app.data.repository.PerformanceRepository
import com.jeeprep.app.data.repository.TopicStat
import com.jeeprep.app.premium.PremiumManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class SubjectDetail(
    val accuracy: Float = 0f,
    val avgTime: Float = 0f,
    val topicStats: List<TopicStat> = emptyList(),
    val weakTopics: List<String> = emptyList(),
    val strongTopics: List<String> = emptyList()
)

data class ProgressUiState(
    val totalAttempts: Int = 0,
    val totalCorrect: Int = 0,
    val overallAccuracy: Float = 0f,
    val activeDays: Int = 0,
    val avgMockScore: Float = 0f,
    val avgTime: Float = 0f,
    // Subject details
    val physics: SubjectDetail = SubjectDetail(),
    val chemistry: SubjectDetail = SubjectDetail(),
    val math: SubjectDetail = SubjectDetail(),
    // Recent performance (last 7 days)
    val recentAttempts: Int = 0,
    val recentAccuracy: Float = 0f,
    // Premium
    val isPremium: Boolean = false,
    val aiAnalysis: String = "",
    val isAiAnalysisLoading: Boolean = false,
    // UI
    val isLoading: Boolean = true,
    val expandedSubject: Int? = null // 1=physics, 2=chem, 3=math
)

@HiltViewModel
class ProgressViewModel @Inject constructor(
    private val perfRepo: PerformanceRepository,
    private val premiumManager: PremiumManager
) : ViewModel() {

    private val _uiState = MutableStateFlow(ProgressUiState())
    val uiState: StateFlow<ProgressUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            premiumManager.isPremium.collect { premium ->
                _uiState.update { it.copy(isPremium = premium) }
            }
        }
        loadStats()
    }

    private fun loadStats() {
        viewModelScope.launch {
            val totalAttempts = perfRepo.getTotalAttempts()
            val totalCorrect = perfRepo.getTotalCorrect()
            val overall = perfRepo.getOverallAccuracy()
            val days = perfRepo.getTotalActiveDays()
            val avgMock = perfRepo.getAverageMockScore()
            val avgTime = perfRepo.getAverageTime()

            // Recent 7-day stats
            val sevenDaysAgo = System.currentTimeMillis() - 7 * 24 * 3600 * 1000L
            val recentAttempts = perfRepo.getAttemptsSince(sevenDaysAgo)
            val recentCorrect = perfRepo.getCorrectSince(sevenDaysAgo)
            val recentAccuracy = if (recentAttempts > 0) (recentCorrect.toFloat() / recentAttempts * 100) else 0f

            // Subject details
            val physicsDetail = loadSubjectDetail(1)
            val chemistryDetail = loadSubjectDetail(2)
            val mathDetail = loadSubjectDetail(3)

            _uiState.update {
                ProgressUiState(
                    totalAttempts = totalAttempts,
                    totalCorrect = totalCorrect,
                    overallAccuracy = overall,
                    activeDays = days,
                    avgMockScore = avgMock,
                    avgTime = avgTime,
                    physics = physicsDetail,
                    chemistry = chemistryDetail,
                    math = mathDetail,
                    recentAttempts = recentAttempts,
                    recentAccuracy = recentAccuracy,
                    isPremium = premiumManager.isPremium.value,
                    isLoading = false
                )
            }
        }
    }

    private suspend fun loadSubjectDetail(subjectId: Int): SubjectDetail {
        val accuracy = perfRepo.getAccuracyForSubject(subjectId)
        val avgTime = perfRepo.getAverageTimeForSubject(subjectId)
        val topicStats = perfRepo.getTopicBreakdown(subjectId)
        val attempted = topicStats.filter { it.accuracy >= 0f }
        val weak = attempted.filter { it.accuracy < 50f }.sortedBy { it.accuracy }.map { it.topicName }
        val strong = attempted.filter { it.accuracy >= 70f }.sortedByDescending { it.accuracy }.map { it.topicName }
        return SubjectDetail(accuracy, avgTime, topicStats, weak, strong)
    }

    fun toggleSubjectExpand(subjectId: Int) {
        _uiState.update {
            it.copy(expandedSubject = if (it.expandedSubject == subjectId) null else subjectId)
        }
    }

    fun loadAiAnalysis() {
        if (_uiState.value.aiAnalysis.isNotBlank() || _uiState.value.isAiAnalysisLoading) return
        _uiState.update { it.copy(isAiAnalysisLoading = true) }

        viewModelScope.launch {
            try {
                val flow = perfRepo.getAiWeaknessAnalysis()
                if (flow != null) {
                    val fullText = StringBuilder()
                    flow.collect { chunk ->
                        fullText.append(chunk)
                        _uiState.update { it.copy(aiAnalysis = fullText.toString()) }
                    }
                    _uiState.update { it.copy(isAiAnalysisLoading = false) }
                } else {
                    _uiState.update { it.copy(aiAnalysis = "Not enough data yet. Practice at least 3 questions per topic.", isAiAnalysisLoading = false) }
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(aiAnalysis = "Failed to generate analysis.", isAiAnalysisLoading = false) }
            }
        }
    }
}
