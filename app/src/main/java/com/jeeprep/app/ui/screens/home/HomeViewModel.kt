package com.jeeprep.app.ui.screens.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.jeeprep.app.ai.AiEngine
import com.jeeprep.app.ai.ModelDownloadManager
import com.jeeprep.app.data.db.dao.MockTestDao
import com.jeeprep.app.data.db.entity.SubjectEntity
import com.jeeprep.app.data.repository.PerformanceRepository
import com.jeeprep.app.data.repository.QuestionRepository
import com.jeeprep.app.premium.PremiumManager
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import android.content.Context
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class HomeUiState(
    val subjects: List<SubjectEntity> = emptyList(),
    val totalQuestions: Int = 0,
    val totalAttempts: Int = 0,
    val overallAccuracy: Float = 0f,
    val streak: Int = 0,
    val isModelReady: Boolean = false,
    val isPremium: Boolean = false,
    val isLoading: Boolean = true
)

@HiltViewModel
class HomeViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    private val questionRepo: QuestionRepository,
    private val perfRepo: PerformanceRepository,
    private val aiEngine: AiEngine,
    private val mockTestDao: MockTestDao,
    private val premiumManager: PremiumManager
) : ViewModel() {

    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            premiumManager.isPremium.collect { premium ->
                _uiState.update { it.copy(isPremium = premium) }
            }
        }
        loadData()
    }

    fun refreshModelStatus() {
        val isModelReady = ModelDownloadManager.isModelDownloaded(context)
        _uiState.update { it.copy(isModelReady = isModelReady) }
    }

    private fun loadData() {
        viewModelScope.launch {
            val isModelReady = ModelDownloadManager.isModelDownloaded(context)

            questionRepo.getAllSubjects().collect { subjects ->
                _uiState.update { state ->
                    state.copy(
                        subjects = subjects,
                        isModelReady = isModelReady,
                        isLoading = false
                    )
                }
            }
        }

        viewModelScope.launch {
            val totalAttempts = perfRepo.getTotalAttempts()
            val accuracy = perfRepo.getOverallAccuracy()
            val streak = perfRepo.getTotalActiveDays()

            _uiState.update { state ->
                state.copy(
                    totalAttempts = totalAttempts,
                    overallAccuracy = accuracy,
                    streak = streak
                )
            }
        }
    }

    fun createMockTest(onResult: (Long?) -> Unit) {
        viewModelScope.launch {
            val test = questionRepo.generateMockTest()
            if (test != null) {
                val testId = mockTestDao.insert(test)
                onResult(testId)
            } else {
                onResult(null)
            }
        }
    }
}
