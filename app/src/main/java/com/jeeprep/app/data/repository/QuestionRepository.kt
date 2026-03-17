package com.jeeprep.app.data.repository

import android.util.Log
import com.jeeprep.app.ai.AiEngine
import com.jeeprep.app.ai.PromptTemplates
import com.jeeprep.app.data.db.dao.*
import com.jeeprep.app.data.db.entity.*
import kotlinx.coroutines.flow.Flow
import org.json.JSONObject
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class QuestionRepository @Inject constructor(
    private val questionDao: QuestionDao,
    private val subjectDao: SubjectDao,
    private val topicDao: TopicDao,
    private val userAttemptDao: UserAttemptDao,
    private val bookmarkDao: BookmarkDao,
    private val aiEngine: AiEngine
) {
    fun getAllSubjects(): Flow<List<SubjectEntity>> = subjectDao.getAllSubjects()

    suspend fun getSubjectById(subjectId: Int): SubjectEntity? = subjectDao.getSubjectById(subjectId)

    suspend fun getTopicById(topicId: Int): TopicEntity? = topicDao.getTopicById(topicId)

    fun getTopicsBySubject(subjectId: Int): Flow<List<TopicEntity>> =
        topicDao.getTopicsBySubject(subjectId)

    suspend fun getQuestionsByTopic(topicId: Int, limit: Int = 10): List<QuestionEntity> =
        questionDao.getQuestionsByTopic(topicId, limit = limit)

    suspend fun insertQuestion(question: QuestionEntity): Long =
        questionDao.insert(question)

    suspend fun getQuestionsBySubject(subjectId: Int, limit: Int = 30): List<QuestionEntity> =
        questionDao.getQuestionsBySubject(subjectId, limit)

    suspend fun getQuestionsByYear(year: Int, examType: String = "Mains"): List<QuestionEntity> =
        questionDao.getQuestionsByYear(year, examType)

    fun getAvailableYears(): Flow<List<Int>> = questionDao.getAvailableYears()

    suspend fun getQuestionById(id: Long): QuestionEntity? = questionDao.getQuestionById(id)

    suspend fun getQuestionsByIds(ids: List<Long>): List<QuestionEntity> = questionDao.getQuestionsByIds(ids)

    suspend fun recordAttempt(
        questionId: Long,
        userAnswer: String,
        isCorrect: Boolean,
        timeTakenSeconds: Int
    ) {
        userAttemptDao.insert(
            UserAttemptEntity(
                questionId = questionId,
                userAnswer = userAnswer,
                isCorrect = isCorrect,
                timeTakenSeconds = timeTakenSeconds
            )
        )
    }

    suspend fun generateExplanation(question: QuestionEntity): Flow<String> {
        // Check if cached
        question.explanation?.let { cached ->
            if (cached.isNotBlank()) {
                return kotlinx.coroutines.flow.flowOf(cached)
            }
        }

        val prompt = PromptTemplates.explainAnswer(
            question = question.questionText,
            options = listOf(question.optionA, question.optionB, question.optionC, question.optionD),
            correctAnswer = question.correctAnswer
        )

        return aiEngine.generateResponse(prompt)
    }

    suspend fun cacheExplanation(questionId: Long, explanation: String) {
        questionDao.updateExplanation(questionId, explanation)
    }

    suspend fun generateAiQuestion(subject: String, topic: String, difficulty: String = "Medium"): QuestionEntity? {
        val prompt = PromptTemplates.generateMCQ(subject, topic, difficulty)
        val response = aiEngine.generateResponseSync(prompt, temperature = 0.8)

        Log.d("JeePrep", "AI MCQ raw response (${response.length} chars): ${response.take(500)}")

        // Try to extract JSON from the response (AI might wrap it in markdown code blocks)
        val jsonStr = extractJson(response)
        if (jsonStr == null) {
            Log.e("JeePrep", "No JSON found in AI response")
            return null
        }

        return try {
            val json = JSONObject(jsonStr)
            QuestionEntity(
                topicId = 0, // will be set by caller
                questionText = json.getString("question"),
                optionA = json.getString("optionA"),
                optionB = json.getString("optionB"),
                optionC = json.getString("optionC"),
                optionD = json.getString("optionD"),
                correctAnswer = json.getString("correctAnswer"),
                explanation = json.optString("explanation", null),
                difficulty = difficulty,
                isAiGenerated = true
            )
        } catch (e: Exception) {
            Log.e("JeePrep", "JSON parse error: ${e.message}")
            null
        }
    }

    /** Extract JSON object from AI response — handles markdown code blocks, leading text, etc. */
    private fun extractJson(response: String): String? {
        val trimmed = response.trim()
        // Direct JSON
        if (trimmed.startsWith("{")) return trimmed
        // Strip markdown code fence
        val codeBlockRegex = Regex("```(?:json)?\\s*\\n?(\\{[\\s\\S]*?\\})\\s*```")
        codeBlockRegex.find(trimmed)?.let { return it.groupValues[1] }
        // Find first { ... } block
        val start = trimmed.indexOf('{')
        val end = trimmed.lastIndexOf('}')
        if (start >= 0 && end > start) return trimmed.substring(start, end + 1)
        return null
    }

    // Bookmarks
    suspend fun toggleBookmark(questionId: Long) {
        if (bookmarkDao.isBookmarked(questionId)) {
            bookmarkDao.delete(questionId)
        } else {
            bookmarkDao.insert(BookmarkEntity(questionId = questionId))
        }
    }

    fun isBookmarked(questionId: Long): Flow<Boolean> = bookmarkDao.isBookmarkedFlow(questionId)
    fun getBookmarkedQuestions(): Flow<List<QuestionEntity>> = questionDao.getBookmarkedQuestions()

    // Mock test generation
    suspend fun generateMockTest(examType: String = "Mains"): MockTestEntity? {
        val physicsQs = questionDao.getRandomQuestionsForSubject(1, 30)
        val chemistryQs = questionDao.getRandomQuestionsForSubject(2, 30)
        val mathQs = questionDao.getRandomQuestionsForSubject(3, 30)

        val allQuestions = physicsQs + chemistryQs + mathQs
        if (allQuestions.size < 30) return null

        return MockTestEntity(
            title = "$examType Mock Test",
            questionIds = allQuestions.map { it.id }.joinToString(","),
            timeLimitMinutes = if (examType == "Mains") 180 else 180,
            totalQuestions = allQuestions.size,
            examType = examType
        )
    }
}
