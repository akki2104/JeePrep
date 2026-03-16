package com.jeeprep.app.data.db.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.jeeprep.app.data.db.entity.QuestionEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface QuestionDao {

    @Query("SELECT * FROM questions WHERE topicId = :topicId AND questionType = :type ORDER BY RANDOM() LIMIT :limit")
    suspend fun getQuestionsByTopic(topicId: Int, type: String = "MCQ", limit: Int = 10): List<QuestionEntity>

    @Query("SELECT * FROM questions WHERE topicId IN (SELECT id FROM topics WHERE subjectId = :subjectId) ORDER BY RANDOM() LIMIT :limit")
    suspend fun getQuestionsBySubject(subjectId: Int, limit: Int = 30): List<QuestionEntity>

    @Query("SELECT * FROM questions WHERE year = :year AND examType = :examType ORDER BY id")
    suspend fun getQuestionsByYear(year: Int, examType: String = "Mains"): List<QuestionEntity>

    @Query("SELECT * FROM questions WHERE difficulty = :difficulty AND topicId = :topicId ORDER BY RANDOM() LIMIT :limit")
    suspend fun getQuestionsByDifficulty(topicId: Int, difficulty: String, limit: Int = 10): List<QuestionEntity>

    @Query("SELECT * FROM questions WHERE id = :questionId")
    suspend fun getQuestionById(questionId: Long): QuestionEntity?

    @Query("SELECT * FROM questions WHERE id IN (:ids)")
    suspend fun getQuestionsByIds(ids: List<Long>): List<QuestionEntity>

    // For mock test generation — get balanced questions across subjects
    @Query("""
        SELECT * FROM questions 
        WHERE topicId IN (SELECT id FROM topics WHERE subjectId = :subjectId) 
        AND isAiGenerated = 0
        ORDER BY RANDOM() 
        LIMIT :count
    """)
    suspend fun getRandomQuestionsForSubject(subjectId: Int, count: Int): List<QuestionEntity>

    @Query("SELECT DISTINCT year FROM questions WHERE year IS NOT NULL ORDER BY year DESC")
    fun getAvailableYears(): Flow<List<Int>>

    @Query("SELECT COUNT(*) FROM questions WHERE topicId = :topicId")
    suspend fun getQuestionCountForTopic(topicId: Int): Int

    @Query("SELECT COUNT(*) FROM questions")
    suspend fun getTotalQuestionCount(): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(question: QuestionEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(questions: List<QuestionEntity>)

    @Query("DELETE FROM questions")
    suspend fun deleteAll()

    @Update
    suspend fun update(question: QuestionEntity)

    @Query("UPDATE questions SET explanation = :explanation WHERE id = :questionId")
    suspend fun updateExplanation(questionId: Long, explanation: String)

    @Query("UPDATE questions SET explanation = NULL")
    suspend fun clearAllExplanations()

    // Bookmarked questions
    @Query("SELECT q.* FROM questions q INNER JOIN bookmarks b ON q.id = b.questionId ORDER BY b.createdAt DESC")
    fun getBookmarkedQuestions(): Flow<List<QuestionEntity>>
}
