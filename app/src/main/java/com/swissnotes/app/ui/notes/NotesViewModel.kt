package com.swissnotes.app.ui.notes

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.swissnotes.app.data.NoteRepository
import com.swissnotes.app.data.local.NoteEntity
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * UI state for the notes list screen.
 */
data class NotesUiState(
    val query: String = "",
    val notes: List<NoteEntity> = emptyList(),
)

/**
 * ViewModel for the notes list. Exposes a single [uiState] [StateFlow]
 * and receives user actions as method calls (UDF).
 */
class NotesViewModel(private val repository: NoteRepository) : ViewModel() {

    private val _uiState = MutableStateFlow(NotesUiState())
    val uiState: StateFlow<NotesUiState> = _uiState.asStateFlow()

    val notes: StateFlow<List<NoteEntity>> =
        repository.observeNotes("")
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    fun onQueryChange(query: String) {
        _uiState.update { it.copy(query = query) }
    }

    fun deleteNote(note: NoteEntity) {
        viewModelScope.launch { repository.delete(note) }
    }

    fun togglePin(note: NoteEntity) {
        viewModelScope.launch { repository.togglePin(note) }
    }

    fun createNote() {
        viewModelScope.launch {
            repository.save(
                NoteEntity(
                    title = "",
                    content = "",
                    createdAt = System.currentTimeMillis(),
                    updatedAt = System.currentTimeMillis(),
                ),
            )
        }
    }
}
