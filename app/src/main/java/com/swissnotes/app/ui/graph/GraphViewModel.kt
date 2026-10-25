package com.swissnotes.app.ui.graph

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.swissnotes.app.data.NoteRepository
import com.swissnotes.app.data.local.NoteEntity
import com.swissnotes.app.data.local.NoteLinkEntity
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn

/**
 * UI state for the graph view.
 */
data class GraphUiState(
    val notes: List<NoteEntity> = emptyList(),
    val links: List<NoteLinkEntity> = emptyList(),
)

/**
 * ViewModel for the knowledge-graph screen. Combines notes and their
 * wikilinks into a single graph state.
 */
class GraphViewModel(repository: NoteRepository) : ViewModel() {

    private val notesFlow = repository.observeNotes("", null)
    private val linksFlow = repository.observeAllLinks()

    private val _uiState = MutableStateFlow(GraphUiState())
    val uiState: StateFlow<GraphUiState> = _uiState.asStateFlow()

    val graphState: StateFlow<GraphUiState> =
        combine(notesFlow, linksFlow) { notes, links ->
            GraphUiState(notes = notes, links = links)
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), GraphUiState())
}
