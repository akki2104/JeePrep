package com.jeeprep.app.data.db.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.jeeprep.app.data.db.entity.UserAttemptEntity
import com.jeeprep.app.data.db.entity.BookmarkEntity
import com.jeeprep.app.data.db.entity.StudyNoteEntity
import com.jeeprep.app.data.db.entity.MemoryTrickEntity
import com.jeeprep.app.data.db.entity.MockTestEntity
import com.jeeprep.app.data.db.entity.MockTestResultEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface UserAttemptDao {
    @Insert
    suspend fun insert(attempt: UserAttemptEntity): Long

    @Insert
    suspend fun insertAll(attempts: List<UserAttemptEntity>)

    @Query("SELECT COUNT(*) FROM user_attempts WHERE isCorrect = 1")
    suspend fun getTotalCorrect(): Int

    @Query("SELECT COUNT(*) FROM user_attempts")
    suspend fun getTotalAttempts(): Int

    @Query("""
        SELECT COUNT(*) FROM user_attempts 
        WHERE isCorrect = 1 
        AND questionId IN (SELECT id FROM questions WHERE topicId = :topicId)
    """)
    suspend fun getCorrectCountForTopic(topicId: Int): Int

    @Query("""
        SELECT COUNT(*) FROM user_attempts 
        WHERE questionId IN (SELECT id FROM questions WHERE topicId = :topicId)
    """)
    suspend fun getAttemptCountForTopic(topicId: Int): Int

    @Query("""
        SELECT CAST(COUNT(CASE WHEN isCorrect = 1 THEN 1 END) AS FLOAT) / COUNT(*) * 100
        FROM user_attempts
        WHERE questionId IN (SELECT id FROM questions WHERE topicId IN 
            (SELECT id FROM topics WHERE subjectId = :subjectId))
    """)
    suspend fun getAccuracyForSubject(subjectId: Int): Float?

    @Query("SELECT COUNT(DISTINCT DATE(timestamp/1000, 'unixepoch')) FROM user_attempts WHERE timestamp > :sinceTimestamp")
    suspend fun getActiveDaysSince(sinceTimestamp: Long): Int

    @Query("SELECT * FROM user_attempts ORDER BY timestamp DESC LIMIT :limit")
    fun getRecentAttempts(limit: Int = 50): Flow<List<UserAttemptEntity>>

    @Query("SELECT COUNT(DISTINCT DATE(timestamp/1000, 'unixepoch')) FROM user_attempts")
    suspend fun getTotalActiveDays(): Int

    @Query("""
        SELECT * FROM user_attempts 
        WHERE isCorrect = 0 
        ORDER BY timestamp DESC 
        LIMIT :limit
    """)
    fun getWrongAttempts(limit: Int = 100): Flow<List<UserAttemptEntity>>

    @Query("SELECT AVG(timeTakenSeconds) FROM user_attempts WHERE timeTakenSeconds > 0")
    suspend fun getAverageTime(): Float?

    @Query("""
        SELECT AVG(timeTakenSeconds) FROM user_attempts 
        WHERE timeTakenSeconds > 0
        AND questionId IN (SELECT id FROM questions WHERE topicId IN 
            (SELECT id FROM topics WHERE subjectId = :subjectId))
    """)
    suspend fun getAverageTimeForSubject(subjectId: Int): Float?

    @Query("SELECT COUNT(*) FROM user_attempts WHERE timestamp > :sinceTimestamp")
    suspend fun getAttemptsSince(sinceTimestamp: Long): Int

    @Query("SELECT COUNT(*) FROM user_attempts WHERE isCorrect = 1 AND timestamp > :sinceTimestamp")
    suspend fun getCorrectSince(sinceTimestamp: Long): Int
}

@Dao
interface StudyNoteDao {
    @Query("SELECT * FROM study_notes WHERE topicId = :topicId ORDER BY createdAt DESC LIMIT 1")
    suspend fun getNoteForTopic(topicId: Int): StudyNoteEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(note: StudyNoteEntity): Long

    @Query("DELETE FROM study_notes WHERE topicId = :topicId")
    suspend fun deleteForTopic(topicId: Int)
}

@Dao
interface MemoryTrickDao {
    @Query("SELECT * FROM memory_tricks WHERE topicId = :topicId ORDER BY createdAt DESC")
    fun getTricksForTopic(topicId: Int): Flow<List<MemoryTrickEntity>>

    @Query("SELECT * FROM memory_tricks WHERE isFavorite = 1 ORDER BY createdAt DESC")
    fun getFavorites(): Flow<List<MemoryTrickEntity>>

    @Insert
    suspend fun insert(trick: MemoryTrickEntity): Long

    @Query("UPDATE memory_tricks SET isFavorite = :isFavorite WHERE id = :id")
    suspend fun updateFavorite(id: Long, isFavorite: Boolean)
}

@Dao
interface MockTestDao {
    @Insert
    suspend fun insert(test: MockTestEntity): Long

    @Query("SELECT * FROM mock_tests ORDER BY createdAt DESC")
    fun getAllTests(): Flow<List<MockTestEntity>>

    @Query("SELECT * FROM mock_tests WHERE id = :testId")
    suspend fun getTestById(testId: Long): MockTestEntity?
}

@Dao
interface MockTestResultDao {
    @Insert
    suspend fun insert(result: MockTestResultEntity): Long

    @Query("SELECT * FROM mock_test_results ORDER BY completedAt DESC")
    fun getAllResults(): Flow<List<MockTestResultEntity>>

    @Query("SELECT * FROM mock_test_results WHERE mockTestId = :testId")
    suspend fun getResultForTest(testId: Long): MockTestResultEntity?

    @Query("SELECT AVG(score * 100.0 / maxScore) FROM mock_test_results")
    suspend fun getAverageScorePercent(): Float?
}

@Dao
interface BookmarkDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(bookmark: BookmarkEntity)

    @Query("DELETE FROM bookmarks WHERE questionId = :questionId")
    suspend fun delete(questionId: Long)

    @Query("SELECT EXISTS(SELECT 1 FROM bookmarks WHERE questionId = :questionId)")
    suspend fun isBookmarked(questionId: Long): Boolean

    @Query("SELECT EXISTS(SELECT 1 FROM bookmarks WHERE questionId = :questionId)")
    fun isBookmarkedFlow(questionId: Long): Flow<Boolean>
}
