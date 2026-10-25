package com.swissnotes.app.ui.editor

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.swissnotes.app.data.NoteRepository
import com.swissnotes.app.data.local.NoteEntity
import com.swissnotes.app.data.local.TagEntity
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
    val folderId: Long? = null,
    val isChecklist: Boolean = false,
    val color: Int = 0,
    val tags: List<TagEntity> = emptyList(),
    val allTitles: List<String> = emptyList(),
    val isLoaded: Boolean = false,
)

/**
 * ViewModel for the note editor. Loads a note by id and persists edits.
 *
 * The note is always created *before* entering the editor (see
 * [NotesViewModel.createNote]), so [noteId] is never 0 here. This avoids
 * races between tagging/linking and the initial save.
 */
class EditorViewModel(
    private val repository: NoteRepository,
    savedStateHandle: SavedStateHandle,
) : ViewModel() {

    private val noteId: Long = savedStateHandle["noteId"] ?: 0L

    private val _uiState = MutableStateFlow(EditorUiState(noteId = noteId))
    val uiState: StateFlow<EditorUiState> = _uiState.asStateFlow()

    val outgoingLinks = repository.observeOutgoingLinks(noteId)
    val backlinks = repository.observeBacklinks(noteId)

    init {
        viewModelScope.launch {
            if (noteId != 0L) {
                repository.getNote(noteId)?.let { note ->
                    _uiState.update {
                        it.copy(
                            title = note.title,
                            content = note.content,
                            folderId = note.folderId,
                            isChecklist = note.isChecklist,
                            color = note.color,
                            isLoaded = true,
                        )
                    }
                }
            } else {
                _uiState.update { it.copy(isLoaded = true) }
            }
            val titles = repository.getAllTitles()
            _uiState.update { it.copy(allTitles = titles) }
        }

        viewModelScope.launch {
            repository.observeTagsForNote(noteId).collect { tags ->
                _uiState.update { it.copy(tags = tags) }
            }
        }
    }

    fun onTitleChange(value: String) {
        _uiState.update { it.copy(title = value) }
    }

    fun onContentChange(value: String) {
        _uiState.update { it.copy(content = value) }
    }

    fun onChecklistToggle() {
        _uiState.update { it.copy(isChecklist = !it.isChecklist) }
    }

    fun onColorSelect(color: Int) {
        _uiState.update { it.copy(color = color) }
    }

    fun onFolderSelect(folderId: Long?) {
        _uiState.update { it.copy(folderId = folderId) }
    }

    /**
     * Persists the current edit and returns the note id. Idempotent:
     * called by the save button and by [saveAndExit].
     */
    suspend fun save(): Long {
        val state = _uiState.value
        val note = NoteEntity(
            id = state.noteId,
            title = state.title.trim(),
            content = state.content,
            folderId = state.folderId,
            isChecklist = state.isChecklist,
            color = state.color,
        )
        val savedId = repository.save(note)
        _uiState.update { it.copy(noteId = savedId) }
        return savedId
    }

    fun saveAndExit(onDone: () -> Unit) {
        viewModelScope.launch {
            save()
            onDone()
        }
    }

    fun insertWikilink(targetTitle: String) {
        val current = _uiState.value.content
        val separator = if (current.isEmpty() || current.endsWith("\n")) "" else "\n"
        val link = "$separator[[$targetTitle]]"
        _uiState.update { it.copy(content = it.content + link) }
    }

    fun addTag(tagName: String) {
        viewModelScope.launch {
            // Persist first so the note exists, then attach the tag.
            val id = if (_uiState.value.noteId == 0L) save() else _uiState.value.noteId
            repository.addTagToNote(id, tagName)
        }
    }

    fun removeTag(tag: TagEntity) {
        // Remove a single tag association without deleting the tag itself.
        viewModelScope.launch {
            val remaining = _uiState.value.tags.filter { it.id != tag.id }.map { it.name }
            repository.setTagsForNote(noteId, remaining)
        }
    }
}
