package com.jeeprep.app.data

import android.content.Context
import android.util.Log
import com.jeeprep.app.data.db.dao.QuestionDao
import com.jeeprep.app.data.db.dao.SubjectDao
import com.jeeprep.app.data.db.dao.TopicDao
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Singleton

private const val EXPLANATION_CACHE_VERSION = 1

@Singleton
class DatabaseInitializer @Inject constructor(
    @ApplicationContext private val context: Context,
    private val subjectDao: SubjectDao,
    private val topicDao: TopicDao,
    private val questionDao: QuestionDao
) {
    fun initialize() {
        CoroutineScope(Dispatchers.IO).launch {
            SeedData.seedDatabase(context, subjectDao, topicDao, questionDao)
            clearCorruptedExplanationsIfNeeded()
        }
    }

    private suspend fun clearCorruptedExplanationsIfNeeded() {
        val prefs = context.getSharedPreferences("jeeprep_prefs", Context.MODE_PRIVATE)
        val currentVersion = prefs.getInt("explanation_cache_version", 0)
        if (currentVersion < EXPLANATION_CACHE_VERSION) {
            Log.d("DatabaseInit", "Clearing corrupted cached explanations (v$currentVersion -> v$EXPLANATION_CACHE_VERSION)")
            questionDao.clearAllExplanations()
            prefs.edit().putInt("explanation_cache_version", EXPLANATION_CACHE_VERSION).apply()
        }
    }
}
