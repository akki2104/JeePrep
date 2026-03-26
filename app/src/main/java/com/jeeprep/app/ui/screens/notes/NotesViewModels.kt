package com.jeeprep.app.ui.screens.notes

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.jeeprep.app.data.db.entity.SubjectEntity
import com.jeeprep.app.data.db.entity.TopicEntity
import com.jeeprep.app.data.repository.NotesRepository
import com.jeeprep.app.data.repository.QuestionRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import android.util.Log
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

// --- Notes Subject List ---
data class NotesUiState(
    val subjects: List<SubjectEntity> = emptyList(),
    val isLoading: Boolean = true
)

@HiltViewModel
class NotesViewModel @Inject constructor(
    private val repo: QuestionRepository
) : ViewModel() {
    private val _uiState = MutableStateFlow(NotesUiState())
    val uiState: StateFlow<NotesUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            repo.getAllSubjects().collect { subjects ->
                _uiState.update { it.copy(subjects = subjects, isLoading = false) }
            }
        }
    }
}

// --- Subject Notes (topic list for notes) ---
data class SubjectNotesUiState(
    val subject: SubjectEntity? = null,
    val topics: List<TopicEntity> = emptyList(),
    val isLoading: Boolean = true
)

@HiltViewModel
class SubjectNotesViewModel @Inject constructor(
    private val repo: QuestionRepository
) : ViewModel() {
    private val _uiState = MutableStateFlow(SubjectNotesUiState())
    val uiState: StateFlow<SubjectNotesUiState> = _uiState.asStateFlow()

    fun loadSubject(subjectId: Int) {
        viewModelScope.launch {
            val subject = repo.getSubjectById(subjectId)
            _uiState.update { it.copy(subject = subject) }

            repo.getTopicsBySubject(subjectId).collect { topics ->
                _uiState.update { it.copy(topics = topics, isLoading = false) }
            }
        }
    }
}

// --- Topic Notes ---
data class TopicNotesUiState(
    val topic: TopicEntity? = null,
    val subjectName: String = "",
    val cachedNote: String? = null,
    val streamedNote: String = "",
    val isGenerating: Boolean = false,
    val isLoading: Boolean = true
)

@HiltViewModel
class TopicNotesViewModel @Inject constructor(
    private val notesRepo: NotesRepository,
    private val questionRepo: QuestionRepository
) : ViewModel() {
    private val _uiState = MutableStateFlow(TopicNotesUiState())
    val uiState: StateFlow<TopicNotesUiState> = _uiState.asStateFlow()

    // Observe the singleton repository's generation state
    val generationState = notesRepo.generationState

    fun loadTopic(topicId: Int) {
        viewModelScope.launch {
            val topic = questionRepo.getTopicById(topicId)
            val subjectName = topic?.let { questionRepo.getSubjectById(it.subjectId)?.name } ?: ""
            val cached = notesRepo.getNote(topicId)

            // Check if there's an ongoing generation for this topic
            val genState = notesRepo.generationState.value
            val isCurrentlyGenerating = genState.isGenerating && genState.topicId == topicId

            Log.d("JeePrep", "loadTopic($topicId): cached=${cached?.content?.length ?: "null"}, isCurrentlyGenerating=$isCurrentlyGenerating")
            _uiState.update {
                it.copy(
                    topic = topic,
                    subjectName = subjectName,
                    cachedNote = cached?.content,
                    streamedNote = if (isCurrentlyGenerating) genState.content else "",
                    isGenerating = isCurrentlyGenerating,
                    isLoading = false
                )
            }

            // If generation is in progress for this topic, keep observing
            if (isCurrentlyGenerating) {
                observeGeneration(topicId)
            }
        }
    }

    fun generateNotes() {
        val state = _uiState.value
        val topic = state.topic ?: return
        _uiState.update { it.copy(isGenerating = true, streamedNote = "", cachedNote = null) }

        notesRepo.generateNoteInBackground(topic.id, state.subjectName, topic.name)
        observeGeneration(topic.id)
    }

    private fun observeGeneration(topicId: Int) {
        viewModelScope.launch {
            notesRepo.generationState.collect { genState ->
                if (genState.topicId == topicId) {
                    _uiState.update {
                        it.copy(
                            streamedNote = genState.content,
                            isGenerating = genState.isGenerating,
                            cachedNote = if (genState.isComplete) genState.content else it.cachedNote
                        )
                    }
                    if (genState.error != null) {
                        _uiState.update {
                            it.copy(
                                streamedNote = genState.error,
                                isGenerating = false
                            )
                        }
                    }
                    // Stop observing when done
                    if (!genState.isGenerating) return@collect
                }
            }
        }
    }

    fun generateMemoryTrick(concept: String) {
        val state = _uiState.value
        val topic = state.topic ?: return

        viewModelScope.launch {
            val fullTrick = StringBuilder()
            notesRepo.generateMemoryTrick(topic.id, concept, state.subjectName).collect { chunk ->
                fullTrick.append(chunk)
            }
            notesRepo.cacheTrick(topic.id, concept, fullTrick.toString())
        }
    }
}
