package com.swissnotes.app.ui.editor

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.swissnotes.app.data.NoteRepository
import com.swissnotes.app.data.local.NoteEntity
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * UI state for the note editor.
 */
data class EditorUiState(
    val noteId: Long = 0L,
    val title: String = "",
    val content: String = "",
    val isChecklist: Boolean = false,
    val isLoaded: Boolean = false,
    val isSaved: Boolean = false,
)

/**
 * ViewModel for the note editor. Loads a note by id (when editing)
 * and persists edits.
 */
class EditorViewModel(
    private val repository: NoteRepository,
    savedStateHandle: SavedStateHandle,
) : ViewModel() {

    private val noteId: Long = savedStateHandle["noteId"] ?: 0L

    private val _uiState = MutableStateFlow(EditorUiState(noteId = noteId))
    val uiState: StateFlow<EditorUiState> = _uiState.asStateFlow()

    init {
        if (noteId != 0L) {
            viewModelScope.launch {
                repository.getNote(noteId)?.let { note ->
                    _uiState.update {
                        it.copy(
                            title = note.title,
                            content = note.content,
                            isChecklist = note.isChecklist,
                            isLoaded = true,
                        )
                    }
                }
            }
        } else {
            _uiState.update { it.copy(isLoaded = true) }
        }
    }

    fun onTitleChange(value: String) {
        _uiState.update { it.copy(title = value, isSaved = false) }
    }

    fun onContentChange(value: String) {
        _uiState.update { it.copy(content = value, isSaved = false) }
    }

    fun onChecklistToggle() {
        _uiState.update { it.copy(isChecklist = !it.isChecklist, isSaved = false) }
    }

    fun save() {
        val state = _uiState.value
        viewModelScope.launch {
            repository.save(
                NoteEntity(
                    id = state.noteId,
                    title = state.title.trim(),
                    content = state.content,
                    isChecklist = state.isChecklist,
                    isPinned = false,
                    createdAt = System.currentTimeMillis(),
                    updatedAt = System.currentTimeMillis(),
                ),
            )
            _uiState.update { it.copy(isSaved = true) }
        }
    }
}
