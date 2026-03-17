package com.jeeprep.app.data.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.jeeprep.app.data.db.dao.*
import com.jeeprep.app.data.db.entity.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(
    entities = [
        SubjectEntity::class,
        TopicEntity::class,
        QuestionEntity::class,
        UserAttemptEntity::class,
        StudyNoteEntity::class,
        MemoryTrickEntity::class,
        MockTestEntity::class,
        MockTestResultEntity::class,
        BookmarkEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {

    abstract fun subjectDao(): SubjectDao
    abstract fun topicDao(): TopicDao
    abstract fun questionDao(): QuestionDao
    abstract fun userAttemptDao(): UserAttemptDao
    abstract fun studyNoteDao(): StudyNoteDao
    abstract fun memoryTrickDao(): MemoryTrickDao
    abstract fun mockTestDao(): MockTestDao
    abstract fun mockTestResultDao(): MockTestResultDao
    abstract fun bookmarkDao(): BookmarkDao

    companion object {
        const val DATABASE_NAME = "jeeprep.db"
    }
}
