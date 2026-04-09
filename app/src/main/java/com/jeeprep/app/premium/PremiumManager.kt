package com.jeeprep.app.premium

import android.content.Context
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PremiumManager @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val prefs = context.getSharedPreferences("jeeprep_premium", Context.MODE_PRIVATE)

    private val _isPremium = MutableStateFlow(prefs.getBoolean("is_premium", false))
    val isPremium: StateFlow<Boolean> = _isPremium.asStateFlow()

    // Track total free question downloads (global limit, not per-topic)
    fun getTotalDownloadCount(): Int =
        prefs.getInt("total_downloads", 0)

    fun incrementDownloadCount() {
        val current = getTotalDownloadCount()
        prefs.edit().putInt("total_downloads", current + 1).apply()
    }

    fun canDownloadFree(): Boolean =
        getTotalDownloadCount() < MAX_FREE_DOWNLOADS

    fun getRemainingFreeDownloads(): Int =
        (MAX_FREE_DOWNLOADS - getTotalDownloadCount()).coerceAtLeast(0)

    fun unlockPremium() {
        prefs.edit().putBoolean("is_premium", true).apply()
        _isPremium.value = true
    }

    // For testing
    fun resetPremium() {
        prefs.edit().putBoolean("is_premium", false).apply()
        _isPremium.value = false
    }

    companion object {
        const val MAX_FREE_DOWNLOADS = 10
        const val PREMIUM_PRICE = "₹50"
        const val PREMIUM_FEATURES = """
• Detailed step-by-step explanations
• Trick & shortcut for every question
• Discuss with AI tutor (unlimited)
• AI weakness analysis & study plan
• PYQ college/IIT attribution
• Download unlimited extra questions (free users: 10 max)
• Advanced progress insights
"""
    }
}
