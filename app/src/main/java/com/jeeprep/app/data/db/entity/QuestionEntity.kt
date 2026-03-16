package com.jeeprep.app.data.db.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "questions",
    foreignKeys = [
        ForeignKey(
            entity = TopicEntity::class,
            parentColumns = ["id"],
            childColumns = ["topicId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("topicId")]
)
data class QuestionEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val topicId: Int,
    val questionText: String,
    val optionA: String,
    val optionB: String,
    val optionC: String,
    val optionD: String,
    val correctAnswer: String,         // "A", "B", "C", "D" or numeric value
    val explanation: String? = null,    // AI-generated, cached
    val difficulty: String = "Medium",  // Easy, Medium, Hard
    val questionType: String = "MCQ",   // MCQ, Numerical
    val year: Int? = null,              // PYQ year (null for AI-generated)
    val session: String? = null,        // "Jan", "Feb", etc.
    val examType: String = "Mains",    // Mains, Advanced
    val isAiGenerated: Boolean = false,
    val imageResName: String? = null    // optional diagram resource name
)
