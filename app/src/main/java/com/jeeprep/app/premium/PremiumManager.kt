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

    // Track free question downloads per topic
    fun getDownloadCountForTopic(topicId: Int): Int =
        prefs.getInt("downloads_topic_$topicId", 0)

    fun incrementDownloadCount(topicId: Int) {
        val current = getDownloadCountForTopic(topicId)
        prefs.edit().putInt("downloads_topic_$topicId", current + 1).apply()
    }

    fun canDownloadFree(topicId: Int): Boolean =
        getDownloadCountForTopic(topicId) < FREE_DOWNLOADS_PER_TOPIC

    fun getRemainingFreeDownloads(topicId: Int): Int =
        (FREE_DOWNLOADS_PER_TOPIC - getDownloadCountForTopic(topicId)).coerceAtLeast(0)

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
        const val FREE_DOWNLOADS_PER_TOPIC = 5
        const val PREMIUM_PRICE = "₹50"
        const val PREMIUM_FEATURES = """
• Detailed step-by-step explanations
• Trick & shortcut for every question
• Discuss with AI tutor (unlimited)
• AI weakness analysis & study plan
• PYQ college/IIT attribution
• Download unlimited extra questions
• Advanced progress insights
"""
    }
}
