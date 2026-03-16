package com.jeeprep.app.data.db.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "study_notes")
data class StudyNoteEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val topicId: Int,
    val content: String,
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "memory_tricks")
data class MemoryTrickEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val topicId: Int,
    val concept: String,
    val trick: String,
    val isFavorite: Boolean = false,
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "mock_tests")
data class MockTestEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String,
    val questionIds: String,          // comma-separated question IDs
    val timeLimitMinutes: Int,
    val totalQuestions: Int,
    val examType: String = "Mains",   // Mains, Advanced
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "mock_test_results")
data class MockTestResultEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val mockTestId: Long,
    val score: Int,
    val maxScore: Int,
    val correctCount: Int,
    val incorrectCount: Int,
    val unattemptedCount: Int,
    val timeTakenSeconds: Int,
    val physicsScore: Int = 0,
    val chemistryScore: Int = 0,
    val mathScore: Int = 0,
    val completedAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "bookmarks")
data class BookmarkEntity(
    @PrimaryKey val questionId: Long,
    val createdAt: Long = System.currentTimeMillis()
)
