package com.swissnotes.app.ui.notes

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.Folder
import androidx.compose.material.icons.outlined.Label
import androidx.compose.material.icons.outlined.PushPin
import androidx.compose.material.icons.outlined.StickyNote2
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.swissnotes.app.R
import com.swissnotes.app.data.local.FolderEntity
import com.swissnotes.app.data.local.NoteEntity
import com.swissnotes.app.data.local.TagEntity
import com.swissnotes.app.ui.theme.SwissColors
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

private val dateFormat = SimpleDateFormat("MMM d", Locale.getDefault())

/**
 * Notes list screen with a folder/tag sidebar and search. Flat, rounded
 * cards with hairline dividers and a single accent color.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotesScreen(
    viewModel: NotesViewModel,
    onNoteClick: (Long) -> Unit,
    onNewNote: () -> Unit,
    onOpenGraph: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    var showFolderDialog by remember { mutableStateOf(false) }
    var showTagDialog by remember { mutableStateOf(false) }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = SwissColors.White,
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = stringResource(R.string.notes_title),
                            style = MaterialTheme.typography.headlineMedium,
                        )
                        Text(
                            text = stringResource(R.string.notes_count, uiState.notes.size),
                            style = MaterialTheme.typography.labelMedium,
                            color = SwissColors.Grey,
                        )
                    }
                },
                actions = {
                    IconButton(onClick = onOpenGraph) {
                        Icon(
                            imageVector = Icons.Outlined.Label,
                            contentDescription = stringResource(R.string.graph),
                            tint = SwissColors.Black,
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = SwissColors.White),
            )
        },
        floatingActionButton = {
            Box(
                modifier = Modifier
                    .size(56.dp)
                    .clip(MaterialTheme.shapes.extraLarge)
                    .background(SwissColors.Accent)
                    .clickable(onClick = onNewNote),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    imageVector = Icons.Outlined.Add,
                    contentDescription = stringResource(R.string.new_note),
                    tint = SwissColors.White,
                )
            }
        },
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
        ) {
            // Search
            OutlinedTextField(
                value = uiState.query,
                onValueChange = viewModel::onQueryChange,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                placeholder = { Text(stringResource(R.string.search_hint), color = SwissColors.Grey) },
                singleLine = true,
                shape = MaterialTheme.shapes.medium,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = SwissColors.Black,
                    unfocusedBorderColor = SwissColors.LightGrey,
                    focusedTextColor = SwissColors.Black,
                    unfocusedTextColor = SwissColors.Black,
                    cursorColor = SwissColors.Accent,
                ),
            )

            // Folder / tag chips row
            FilterChips(
                uiState = uiState,
                onSelectAll = viewModel::selectAll,
                onSelectFolder = viewModel::selectFolder,
                onSelectTag = viewModel::selectTag,
                onAddFolder = { showFolderDialog = true },
                onAddTag = { showTagDialog = true },
            )

            if (uiState.notes.isEmpty()) {
                EmptyState(modifier = Modifier.fillMaxSize())
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(start = 16.dp, end = 16.dp, bottom = 96.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    items(uiState.notes, key = { it.id }) { note ->
                        NoteCard(
                            note = note,
                            onClick = { onNoteClick(note.id) },
                            onPin = { viewModel.togglePin(note) },
                            onDelete = { viewModel.deleteNote(note) },
                        )
                    }
                }
            }
        }
    }

    if (showFolderDialog) {
        NameDialog(
            title = stringResource(R.string.new_folder),
            label = stringResource(R.string.folder_name),
            onDismiss = { showFolderDialog = false },
            onConfirm = { name ->
                viewModel.createFolder(name)
                showFolderDialog = false
            },
        )
    }
    if (showTagDialog) {
        NameDialog(
            title = stringResource(R.string.new_tag),
            label = stringResource(R.string.tag_name),
            onDismiss = { showTagDialog = false },
            onConfirm = { name ->
                viewModel.createTag(name)
                showTagDialog = false
            },
        )
    }
}

@Composable
private fun FilterChips(
    uiState: NotesUiState,
    onSelectAll: () -> Unit,
    onSelectFolder: (Long?) -> Unit,
    onSelectTag: (Long?) -> Unit,
    onAddFolder: () -> Unit,
    onAddTag: () -> Unit,
) {
    Column(modifier = Modifier.padding(horizontal = 16.dp)) {
        // Folders
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = stringResource(R.string.folders).uppercase(),
                style = MaterialTheme.typography.labelSmall,
                color = SwissColors.Grey,
                modifier = Modifier.weight(1f),
            )
            IconButton(onClick = onAddFolder, modifier = Modifier.size(28.dp)) {
                Icon(Icons.Outlined.Add, contentDescription = stringResource(R.string.new_folder), tint = SwissColors.Grey, modifier = Modifier.size(16.dp))
            }
        }
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Chip(
                text = stringResource(R.string.all_notes),
                selected = uiState.selectedFolderId == null && uiState.selectedTagId == null,
                onClick = onSelectAll,
            )
            uiState.folders.take(6).forEach { folder ->
                Chip(
                    text = folder.name,
                    selected = uiState.selectedFolderId == folder.id,
                    onClick = { onSelectFolder(folder.id) },
                )
            }
        }

        Spacer(Modifier.height(12.dp))

        // Tags
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = stringResource(R.string.tags).uppercase(),
                style = MaterialTheme.typography.labelSmall,
                color = SwissColors.Grey,
                modifier = Modifier.weight(1f),
            )
            IconButton(onClick = onAddTag, modifier = Modifier.size(28.dp)) {
                Icon(Icons.Outlined.Add, contentDescription = stringResource(R.string.new_tag), tint = SwissColors.Grey, modifier = Modifier.size(16.dp))
            }
        }
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            uiState.tags.take(8).forEach { tag ->
                Chip(
                    text = "#${tag.name}",
                    selected = uiState.selectedTagId == tag.id,
                    onClick = { onSelectTag(tag.id) },
                )
            }
        }
        Spacer(Modifier.height(8.dp))
    }
}

@Composable
private fun Chip(
    text: String,
    selected: Boolean,
    onClick: () -> Unit,
) {
    Box(
        modifier = Modifier
            .clip(MaterialTheme.shapes.small)
            .background(if (selected) SwissColors.Black else SwissColors.Surface)
            .clickable(onClick = onClick)
            .padding(horizontal = 14.dp, vertical = 8.dp),
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.labelLarge,
            color = if (selected) SwissColors.White else SwissColors.Grey,
        )
    }
}

@Composable
private fun NoteCard(
    note: NoteEntity,
    onClick: () -> Unit,
    onPin: () -> Unit,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val cardColor = noteColor(note.color)
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = MaterialTheme.shapes.medium,
        color = cardColor,
        shadowElevation = 0.dp,
        border = androidx.compose.foundation.BorderStroke(1.dp, SwissColors.LightGrey),
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                if (note.isPinned) {
                    Icon(
                        imageVector = Icons.Outlined.PushPin,
                        contentDescription = null,
                        tint = SwissColors.Accent,
                        modifier = Modifier.size(16.dp),
                    )
                    Spacer(Modifier.width(6.dp))
                }
                Text(
                    text = note.title.ifBlank { stringResource(R.string.untitled) },
                    style = MaterialTheme.typography.titleMedium,
                    color = SwissColors.Black,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f),
                )
                IconButton(onClick = onPin, modifier = Modifier.size(32.dp)) {
                    Icon(
                        imageVector = Icons.Outlined.PushPin,
                        contentDescription = "Pin",
                        tint = if (note.isPinned) SwissColors.Accent else SwissColors.Grey,
                        modifier = Modifier.size(18.dp),
                    )
                }
                IconButton(onClick = onDelete, modifier = Modifier.size(32.dp)) {
                    Icon(
                        imageVector = Icons.Outlined.Delete,
                        contentDescription = stringResource(R.string.delete),
                        tint = SwissColors.Grey,
                        modifier = Modifier.size(18.dp),
                    )
                }
            }
            if (note.content.isNotBlank()) {
                Spacer(Modifier.height(6.dp))
                Text(
                    text = note.content,
                    style = MaterialTheme.typography.bodyMedium,
                    color = SwissColors.Grey,
                    maxLines = 3,
                    overflow = TextOverflow.Ellipsis,
                )
            }
            Spacer(Modifier.height(10.dp))
            Text(
                text = dateFormat.format(Date(note.updatedAt)),
                style = MaterialTheme.typography.labelSmall,
                color = SwissColors.Grey,
            )
        }
    }
}

@Composable
private fun EmptyState(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier.padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Icon(
            imageVector = Icons.Outlined.StickyNote2,
            contentDescription = null,
            tint = SwissColors.LightGrey,
            modifier = Modifier.size(56.dp),
        )
        Spacer(Modifier.height(16.dp))
        Text(
            text = stringResource(R.string.empty_title),
            style = MaterialTheme.typography.titleLarge,
            color = SwissColors.Black,
        )
        Spacer(Modifier.height(8.dp))
        Text(
            text = stringResource(R.string.empty_hint),
            style = MaterialTheme.typography.bodyMedium,
            color = SwissColors.Grey,
        )
    }
}

@Composable
private fun NameDialog(
    title: String,
    label: String,
    onDismiss: () -> Unit,
    onConfirm: (String) -> Unit,
) {
    var name by remember { mutableStateOf("") }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title) },
        text = {
            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                label = { Text(label) },
                singleLine = true,
            )
        },
        confirmButton = {
            TextButton(onClick = { onConfirm(name) }) {
                Text(stringResource(R.string.create))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.cancel))
            }
        },
    )
}

private fun noteColor(colorIndex: Int): Color = when (colorIndex) {
    1 -> SwissColors.NoteRed
    2 -> SwissColors.NoteYellow
    3 -> SwissColors.NoteGreen
    4 -> SwissColors.NoteBlue
    5 -> SwissColors.NotePurple
    else -> SwissColors.White
}
