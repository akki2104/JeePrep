package com.jeeprep.app.data.repository

import android.app.PendingIntent
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import com.jeeprep.app.ai.AiEngine
import com.jeeprep.app.ai.PromptTemplates
import com.jeeprep.app.data.db.dao.MemoryTrickDao
import com.jeeprep.app.data.db.dao.StudyNoteDao
import com.jeeprep.app.data.db.entity.MemoryTrickEntity
import com.jeeprep.app.data.db.entity.StudyNoteEntity
import android.util.Log
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Singleton

data class NoteGenerationState(
    val topicId: Int = -1,
    val topicName: String = "",
    val content: String = "",
    val isGenerating: Boolean = false,
    val isComplete: Boolean = false,
    val error: String? = null
)

@Singleton
class NotesRepository @Inject constructor(
    private val studyNoteDao: StudyNoteDao,
    private val memoryTrickDao: MemoryTrickDao,
    private val aiEngine: AiEngine,
    @ApplicationContext private val context: Context
) {
    // Singleton scope survives ViewModel destruction
    private val repoScope = CoroutineScope(SupervisorJob() + Dispatchers.Main)

    private val _generationState = MutableStateFlow(NoteGenerationState())
    val generationState: StateFlow<NoteGenerationState> = _generationState.asStateFlow()

    init {
        createNotificationChannel()
    }

    suspend fun getNote(topicId: Int): StudyNoteEntity? {
        val note = studyNoteDao.getNoteForTopic(topicId)
        Log.d("JeePrep", "getNote($topicId) => ${if (note != null) "found (${note.content.length} chars)" else "null"}")
        return note
    }

    fun generateNoteInBackground(topicId: Int, subject: String, topic: String) {
        Log.d("JeePrep", "generateNoteInBackground: topicId=$topicId, subject=$subject, topic=$topic")
        if (_generationState.value.isGenerating) {
            Log.d("JeePrep", "generateNoteInBackground: already generating, skipping")
            return
        }

        _generationState.value = NoteGenerationState(
            topicId = topicId,
            topicName = topic,
            isGenerating = true
        )

        repoScope.launch {
            try {
                val prompt = PromptTemplates.generateNotes(subject, topic)
                val fullNote = StringBuilder()
                aiEngine.generateResponse(prompt).collect { chunk ->
                    fullNote.append(chunk)
                    _generationState.value = _generationState.value.copy(
                        content = fullNote.toString()
                    )
                }
                Log.d("JeePrep", "Generation done: ${fullNote.length} chars")
                if (fullNote.isNotBlank()) {
                    cacheNote(topicId, fullNote.toString())
                    Log.d("JeePrep", "Note cached for topicId=$topicId")
                    _generationState.value = _generationState.value.copy(
                        isGenerating = false,
                        isComplete = true
                    )
                    showCompletionNotification(topicId, topic)
                } else {
                    _generationState.value = _generationState.value.copy(
                        isGenerating = false,
                        error = "Could not generate notes. Make sure the AI model is downloaded."
                    )
                }
            } catch (e: Exception) {
                _generationState.value = _generationState.value.copy(
                    isGenerating = false,
                    error = "Error: ${e.message ?: "Unknown error"}"
                )
            }
        }
    }

    fun clearGenerationState() {
        if (!_generationState.value.isGenerating) {
            _generationState.value = NoteGenerationState()
        }
    }

    suspend fun cacheNote(topicId: Int, content: String) {
        studyNoteDao.deleteForTopic(topicId)
        studyNoteDao.insert(StudyNoteEntity(topicId = topicId, content = content))
    }

    fun getTricksForTopic(topicId: Int): Flow<List<MemoryTrickEntity>> =
        memoryTrickDao.getTricksForTopic(topicId)

    fun getFavoriteTricks(): Flow<List<MemoryTrickEntity>> =
        memoryTrickDao.getFavorites()

    suspend fun generateMemoryTrick(topicId: Int, concept: String, subject: String): Flow<String> {
        val prompt = PromptTemplates.generateMemoryTrick(concept, subject)
        return aiEngine.generateResponse(prompt)
    }

    suspend fun cacheTrick(topicId: Int, concept: String, trick: String) {
        memoryTrickDao.insert(
            MemoryTrickEntity(topicId = topicId, concept = concept, trick = trick)
        )
    }

    suspend fun toggleFavorite(id: Long, isFavorite: Boolean) {
        memoryTrickDao.updateFavorite(id, isFavorite)
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                "notes_generation",
                "Notes Generation",
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = "Notifies when AI note generation is complete"
            }
            val nm = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            nm.createNotificationChannel(channel)
        }
    }

    private fun showCompletionNotification(topicId: Int, topicName: String) {
        val intent = Intent(context, Class.forName("com.jeeprep.app.MainActivity")).apply {
            flags = Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP
            putExtra("navigate_to", "notes/$topicId")
        }
        val pendingIntent = PendingIntent.getActivity(
            context, topicId, intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val nm = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val notification = NotificationCompat.Builder(context, "notes_generation")
            .setSmallIcon(android.R.drawable.ic_menu_edit)
            .setContentTitle("Notes Ready!")
            .setContentText("$topicName revision notes have been generated")
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .build()
        nm.notify(topicName.hashCode(), notification)
    }
}
