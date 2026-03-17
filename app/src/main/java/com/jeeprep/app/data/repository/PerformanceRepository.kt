package com.jeeprep.app.data.repository

import com.jeeprep.app.ai.AiEngine
import com.jeeprep.app.ai.PromptTemplates
import com.jeeprep.app.data.db.dao.*
import com.jeeprep.app.data.db.entity.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import javax.inject.Inject
import javax.inject.Singleton

data class TopicStat(
    val topicName: String,
    val totalAttempts: Int,
    val correct: Int,
    val accuracy: Float // -1f means no data
)

@Singleton
class PerformanceRepository @Inject constructor(
    private val userAttemptDao: UserAttemptDao,
    private val questionDao: QuestionDao,
    private val topicDao: TopicDao,
    private val mockTestResultDao: MockTestResultDao,
    private val aiEngine: AiEngine
) {
    suspend fun getTotalAttempts(): Int = userAttemptDao.getTotalAttempts()
    suspend fun getTotalCorrect(): Int = userAttemptDao.getTotalCorrect()

    suspend fun getOverallAccuracy(): Float {
        val total = userAttemptDao.getTotalAttempts()
        if (total == 0) return 0f
        val correct = userAttemptDao.getTotalCorrect()
        return (correct.toFloat() / total) * 100f
    }

    suspend fun getAccuracyForSubject(subjectId: Int): Float =
        userAttemptDao.getAccuracyForSubject(subjectId) ?: 0f

    suspend fun getTopicAccuracy(topicId: Int): Float {
        val total = userAttemptDao.getAttemptCountForTopic(topicId)
        if (total == 0) return -1f // no data
        val correct = userAttemptDao.getCorrectCountForTopic(topicId)
        return (correct.toFloat() / total) * 100f
    }

    suspend fun getWeakTopics(subjectId: Int): List<Pair<TopicEntity, Float>> {
        val topics = topicDao.getTopicsBySubjectOnce(subjectId)
        return topics.mapNotNull { topic ->
            val accuracy = getTopicAccuracy(topic.id)
            if (accuracy >= 0f && accuracy < 50f) topic to accuracy else null
        }.sortedBy { it.second }
    }

    suspend fun getTotalActiveDays(): Int = userAttemptDao.getTotalActiveDays()

    fun getRecentAttempts(limit: Int = 50): Flow<List<UserAttemptEntity>> =
        userAttemptDao.getRecentAttempts(limit)

    fun getWrongAttempts(limit: Int = 100): Flow<List<UserAttemptEntity>> =
        userAttemptDao.getWrongAttempts(limit)

    suspend fun getQuestionById(questionId: Long): QuestionEntity? =
        questionDao.getQuestionById(questionId)

    fun getMockTestResults(): Flow<List<MockTestResultEntity>> =
        mockTestResultDao.getAllResults()

    suspend fun getAverageMockScore(): Float =
        mockTestResultDao.getAverageScorePercent() ?: 0f

    suspend fun getAverageTime(): Float = userAttemptDao.getAverageTime() ?: 0f

    suspend fun getAverageTimeForSubject(subjectId: Int): Float =
        userAttemptDao.getAverageTimeForSubject(subjectId) ?: 0f

    suspend fun getAttemptsSince(sinceTimestamp: Long): Int =
        userAttemptDao.getAttemptsSince(sinceTimestamp)

    suspend fun getCorrectSince(sinceTimestamp: Long): Int =
        userAttemptDao.getCorrectSince(sinceTimestamp)

    suspend fun getTopicBreakdown(subjectId: Int): List<TopicStat> {
        val topics = topicDao.getTopicsBySubjectOnce(subjectId)
        return topics.map { topic ->
            val total = userAttemptDao.getAttemptCountForTopic(topic.id)
            val correct = if (total > 0) userAttemptDao.getCorrectCountForTopic(topic.id) else 0
            val accuracy = if (total > 0) (correct.toFloat() / total * 100) else -1f
            TopicStat(topic.name, total, correct, accuracy)
        }
    }

    suspend fun getAiWeaknessAnalysis(): Flow<String>? {
        val topics = topicDao.getAllTopicsOnce()
        val statsLines = topics.mapNotNull { topic ->
            val total = userAttemptDao.getAttemptCountForTopic(topic.id)
            if (total < 3) return@mapNotNull null
            val correct = userAttemptDao.getCorrectCountForTopic(topic.id)
            val accuracy = (correct.toFloat() / total * 100).toInt()
            "${topic.name}: $accuracy% ($correct/$total)"
        }

        if (statsLines.isEmpty()) return null

        val prompt = PromptTemplates.analyzeWeakness(statsLines.joinToString("\n"))
        return aiEngine.generateResponse(prompt)
    }

    // Helper to get topics without Flow
    private suspend fun TopicDao.getTopicsBySubjectOnce(subjectId: Int): List<TopicEntity> {
        return getTopicsBySubject(subjectId).first()
    }

    private suspend fun TopicDao.getAllTopicsOnce(): List<TopicEntity> {
        return getAllTopics().first()
    }
}
