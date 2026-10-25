package com.swissnotes.app.ui.notes

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.swissnotes.app.data.NoteRepository
import com.swissnotes.app.data.local.FolderEntity
import com.swissnotes.app.data.local.NoteEntity
import com.swissnotes.app.data.local.TagEntity
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * UI state for the notes list screen.
 */
data class NotesUiState(
    val query: String = "",
    val selectedFolderId: Long? = null,
    val selectedTagId: Long? = null,
    val notes: List<NoteEntity> = emptyList(),
    val folders: List<FolderEntity> = emptyList(),
    val tags: List<TagEntity> = emptyList(),
)

/**
 * ViewModel for the notes list. Exposes a single [uiState] [StateFlow]
 * and receives user actions as method calls (UDF). One-shot events
 * (e.g. "a note was just created, open it") are exposed via [events].
 */
class NotesViewModel(private val repository: NoteRepository) : ViewModel() {

    private val queryFlow = MutableStateFlow("")
    private val folderFlow = MutableStateFlow<Long?>(null)
    private val tagFlow = MutableStateFlow<Long?>(null)

    private val _uiState = MutableStateFlow(NotesUiState())
    val uiState: StateFlow<NotesUiState> = _uiState.asStateFlow()

    private val _events = MutableSharedFlow<Long>()
    val events: SharedFlow<Long> = _events.asSharedFlow()

    private val notesFlow = combine(queryFlow, folderFlow, tagFlow) { query, folderId, tagId ->
        if (tagId != null) {
            repository.observeNotesByTag(tagId)
        } else {
            repository.observeNotes(query, folderId)
        }
    }.flatMapLatest { it }

    private val foldersFlow = repository.observeFolders()
    private val tagsFlow = repository.observeTags()

    init {
        viewModelScope.launch {
            notesFlow.collect { notes -> _uiState.update { it.copy(notes = notes) } }
        }
        viewModelScope.launch {
            foldersFlow.collect { folders -> _uiState.update { it.copy(folders = folders) } }
        }
        viewModelScope.launch {
            tagsFlow.collect { tags -> _uiState.update { it.copy(tags = tags) } }
        }
        viewModelScope.launch {
            queryFlow.collect { q -> _uiState.update { it.copy(query = q) } }
        }
        viewModelScope.launch {
            folderFlow.collect { f -> _uiState.update { it.copy(selectedFolderId = f) } }
        }
        viewModelScope.launch {
            tagFlow.collect { t -> _uiState.update { it.copy(selectedTagId = t) } }
        }
    }

    fun onQueryChange(query: String) {
        queryFlow.value = query
    }

    fun selectFolder(folderId: Long?) {
        tagFlow.value = null
        folderFlow.value = folderId
    }

    fun selectTag(tagId: Long?) {
        folderFlow.value = null
        tagFlow.value = tagId
    }

    fun selectAll() {
        folderFlow.value = null
        tagFlow.value = null
    }

    /**
     * Creates a blank note and emits its id as an event so the UI can
     * navigate into the editor with a real, persisted id.
     */
    fun createNote() {
        viewModelScope.launch {
            val id = repository.save(NoteEntity(title = "", content = ""))
            _events.emit(id)
        }
    }

    fun deleteNote(note: NoteEntity) {
        viewModelScope.launch { repository.delete(note) }
    }

    fun togglePin(note: NoteEntity) {
        viewModelScope.launch { repository.togglePin(note) }
    }

    fun createFolder(name: String) {
        viewModelScope.launch {
            if (name.isNotBlank()) repository.saveFolder(FolderEntity(name = name.trim()))
        }
    }

    fun renameFolder(folder: FolderEntity, newName: String) {
        viewModelScope.launch {
            if (newName.isNotBlank()) repository.saveFolder(folder.copy(name = newName.trim()))
        }
    }

    fun deleteFolder(folder: FolderEntity) {
        viewModelScope.launch { repository.deleteFolder(folder) }
    }

    fun createTag(name: String) {
        viewModelScope.launch {
            if (name.isNotBlank()) repository.createTag(name)
        }
    }

    fun deleteTag(tag: TagEntity) {
        viewModelScope.launch { repository.deleteTag(tag) }
    }
}
