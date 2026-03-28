package com.jeeprep.app.ui.screens.download

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.jeeprep.app.ai.AiEngine
import com.jeeprep.app.ai.ModelDownloadManager
import com.jeeprep.app.ai.ModelDownloadManager.DownloadProgress
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class DownloadUiState(
    val isModelPresent: Boolean = false,
    val isDownloading: Boolean = false,
    val progressPercent: Int = 0,
    val downloadedMB: Long = 0,
    val totalMB: Long = 2580,
    val isComplete: Boolean = false,
    val isInitializing: Boolean = false,
    val isReady: Boolean = false,
    val error: String? = null
)

@HiltViewModel
class DownloadViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    private val aiEngine: AiEngine
) : ViewModel() {

    private val _uiState = MutableStateFlow(DownloadUiState())
    val uiState: StateFlow<DownloadUiState> = _uiState.asStateFlow()

    init {
        checkModel()
    }

    private fun checkModel() {
        val present = ModelDownloadManager.isModelDownloaded(context)
        _uiState.update { it.copy(isModelPresent = present, isReady = present && aiEngine.isReady) }
    }

    fun startDownload() {
        _uiState.update { it.copy(isDownloading = true, error = null) }

        viewModelScope.launch {
            ModelDownloadManager.downloadModel(context).collect { progress ->
                when (progress) {
                    is DownloadProgress.Downloading -> {
                        _uiState.update {
                            it.copy(
                                progressPercent = progress.progressPercent,
                                downloadedMB = progress.downloadedMB,
                                totalMB = progress.totalMB
                            )
                        }
                    }
                    is DownloadProgress.Complete -> {
                        _uiState.update { it.copy(isDownloading = false, isComplete = true, isModelPresent = true) }
                        initializeEngine()
                    }
                    is DownloadProgress.Error -> {
                        _uiState.update { it.copy(isDownloading = false, error = progress.message) }
                    }
                }
            }
        }
    }

    fun initializeEngine() {
        _uiState.update { it.copy(isInitializing = true) }
        viewModelScope.launch {
            try {
                aiEngine.initialize()
                _uiState.update { it.copy(isInitializing = false, isReady = true) }
            } catch (e: Exception) {
                _uiState.update { it.copy(isInitializing = false, error = e.message) }
            }
        }
    }
}
