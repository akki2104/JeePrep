package com.jeeprep.app.ui.screens.premium

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.jeeprep.app.premium.PremiumManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class PremiumUiState(
    val showConfirmDialog: Boolean = false,
    val isProcessing: Boolean = false,
    val purchaseSuccess: Boolean = false
)

@HiltViewModel
class PremiumViewModel @Inject constructor(
    private val premiumManager: PremiumManager
) : ViewModel() {

    val isPremium: StateFlow<Boolean> = premiumManager.isPremium

    private val _uiState = MutableStateFlow(PremiumUiState())
    val uiState: StateFlow<PremiumUiState> = _uiState.asStateFlow()

    fun showConfirmDialog() {
        _uiState.update { it.copy(showConfirmDialog = true) }
    }

    fun dismissConfirmDialog() {
        _uiState.update { it.copy(showConfirmDialog = false) }
    }

    fun confirmPurchase() {
        _uiState.update { it.copy(showConfirmDialog = false, isProcessing = true) }
        viewModelScope.launch {
            // TODO: Replace with Google Play Billing Library integration
            // For now: simulate a brief processing delay
            delay(1500)
            premiumManager.unlockPremium()
            _uiState.update { it.copy(isProcessing = false, purchaseSuccess = true) }
        }
    }

    /** Debug only: reset premium for testing */
    fun resetPremium() {
        premiumManager.resetPremium()
        _uiState.update { PremiumUiState() }
    }
}
