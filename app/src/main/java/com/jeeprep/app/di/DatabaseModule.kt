package com.jeeprep.app.di

import android.content.Context
import androidx.room.Room
import com.jeeprep.app.data.db.AppDatabase
import com.jeeprep.app.data.db.dao.*
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): AppDatabase {
        return Room.databaseBuilder(
            context,
            AppDatabase::class.java,
            AppDatabase.DATABASE_NAME
        ).build()
    }

    @Provides fun provideSubjectDao(db: AppDatabase): SubjectDao = db.subjectDao()
    @Provides fun provideTopicDao(db: AppDatabase): TopicDao = db.topicDao()
    @Provides fun provideQuestionDao(db: AppDatabase): QuestionDao = db.questionDao()
    @Provides fun provideUserAttemptDao(db: AppDatabase): UserAttemptDao = db.userAttemptDao()
    @Provides fun provideStudyNoteDao(db: AppDatabase): StudyNoteDao = db.studyNoteDao()
    @Provides fun provideMemoryTrickDao(db: AppDatabase): MemoryTrickDao = db.memoryTrickDao()
    @Provides fun provideMockTestDao(db: AppDatabase): MockTestDao = db.mockTestDao()
    @Provides fun provideMockTestResultDao(db: AppDatabase): MockTestResultDao = db.mockTestResultDao()
    @Provides fun provideBookmarkDao(db: AppDatabase): BookmarkDao = db.bookmarkDao()
}
