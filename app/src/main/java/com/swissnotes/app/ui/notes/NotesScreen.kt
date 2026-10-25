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
import androidx.compose.material.icons.automirrored.outlined.Menu
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.outlined.Folder
import androidx.compose.material.icons.outlined.Hub
import androidx.compose.material.icons.outlined.Label
import androidx.compose.material.icons.outlined.PushPin
import androidx.compose.material.icons.outlined.StickyNote2
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.NavigationDrawerItem
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
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
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

private val dateFormat = SimpleDateFormat("MMM d, yyyy", Locale.getDefault())

/**
 * Notes list screen with a navigation drawer for folders and tags.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotesScreen(
    viewModel: NotesViewModel,
    onNoteClick: (Long) -> Unit,
    onOpenGraph: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val drawerState = rememberDrawerState(DrawerValue.Closed)
    val scope = rememberCoroutineScope()

    var showFolderDialog by remember { mutableStateOf(false) }
    var showTagDialog by remember { mutableStateOf(false) }
    var noteToDelete by remember { mutableStateOf<NoteEntity?>(null) }
    var folderToRename by remember { mutableStateOf<FolderEntity?>(null) }
    var folderToDelete by remember { mutableStateOf<FolderEntity?>(null) }
    var tagToDelete by remember { mutableStateOf<TagEntity?>(null) }

    // Observe one-shot "open note" events emitted after creation.
    LaunchedEffect(Unit) {
        viewModel.events.collect { id -> onNoteClick(id) }
    }

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            ModalDrawerSheet(
                drawerContainerColor = SwissColors.White,
            ) {
                DrawerContent(
                    uiState = uiState,
                    onSelectAll = {
                        viewModel.selectAll()
                        scope.launch { drawerState.close() }
                    },
                    onSelectFolder = { id ->
                        viewModel.selectFolder(id)
                        scope.launch { drawerState.close() }
                    },
                    onSelectTag = { id ->
                        viewModel.selectTag(id)
                        scope.launch { drawerState.close() }
                    },
                    onAddFolder = {
                        showFolderDialog = true
                        scope.launch { drawerState.close() }
                    },
                    onAddTag = {
                        showTagDialog = true
                        scope.launch { drawerState.close() }
                    },
                    onRenameFolder = { folderToRename = it },
                    onDeleteFolder = { folderToDelete = it },
                    onDeleteTag = { tagToDelete = it },
                )
            }
        },
    ) {
        Scaffold(
            modifier = modifier.fillMaxSize(),
            containerColor = SwissColors.White,
            topBar = {
                TopAppBar(
                    navigationIcon = {
                        IconButton(onClick = { scope.launch { drawerState.open() } }) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Outlined.Menu,
                                contentDescription = "Menu",
                                tint = SwissColors.Black,
                            )
                        }
                    },
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
                                imageVector = Icons.Outlined.Hub,
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
                        .clickable(onClick = viewModel::createNote),
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
                    placeholder = {
                        Text(stringResource(R.string.search_hint), color = SwissColors.Grey)
                    },
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
                                onDelete = { noteToDelete = note },
                            )
                        }
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
    noteToDelete?.let { note ->
        AlertDialog(
            onDismissRequest = { noteToDelete = null },
            title = { Text(stringResource(R.string.delete)) },
            text = { Text(stringResource(R.string.delete_note_confirm)) },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.deleteNote(note)
                    noteToDelete = null
                }) {
                    Text(stringResource(R.string.delete), color = SwissColors.Accent)
                }
            },
            dismissButton = {
                TextButton(onClick = { noteToDelete = null }) {
                    Text(stringResource(R.string.cancel))
                }
            },
        )
    }
    folderToRename?.let { folder ->
        RenameFolderDialog(
            folder = folder,
            onDismiss = { folderToRename = null },
            onConfirm = { name ->
                viewModel.renameFolder(folder, name)
                folderToRename = null
            },
        )
    }
    folderToDelete?.let { folder ->
        AlertDialog(
            onDismissRequest = { folderToDelete = null },
            title = { Text(stringResource(R.string.delete)) },
            text = { Text(stringResource(R.string.delete_folder_confirm, folder.name)) },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.deleteFolder(folder)
                    folderToDelete = null
                }) {
                    Text(stringResource(R.string.delete), color = SwissColors.Accent)
                }
            },
            dismissButton = {
                TextButton(onClick = { folderToDelete = null }) {
                    Text(stringResource(R.string.cancel))
                }
            },
        )
    }
    tagToDelete?.let { tag ->
        AlertDialog(
            onDismissRequest = { tagToDelete = null },
            title = { Text(stringResource(R.string.delete)) },
            text = { Text(stringResource(R.string.delete_tag_confirm, tag.name)) },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.deleteTag(tag)
                    tagToDelete = null
                }) {
                    Text(stringResource(R.string.delete), color = SwissColors.Accent)
                }
            },
            dismissButton = {
                TextButton(onClick = { tagToDelete = null }) {
                    Text(stringResource(R.string.cancel))
                }
            },
        )
    }
}

@Composable
private fun DrawerContent(
    uiState: NotesUiState,
    onSelectAll: () -> Unit,
    onSelectFolder: (Long) -> Unit,
    onSelectTag: (Long) -> Unit,
    onAddFolder: () -> Unit,
    onAddTag: () -> Unit,
    onRenameFolder: (FolderEntity) -> Unit,
    onDeleteFolder: (FolderEntity) -> Unit,
    onDeleteTag: (TagEntity) -> Unit,
) {
    LazyColumn(modifier = Modifier.fillMaxSize()) {
        item {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = stringResource(R.string.app_name),
                    style = MaterialTheme.typography.titleLarge,
                    color = SwissColors.Black,
                )
                Spacer(Modifier.height(16.dp))
            }
        }

        item {
            NavigationDrawerItem(
                label = { Text(stringResource(R.string.all_notes)) },
                selected = uiState.selectedFolderId == null && uiState.selectedTagId == null,
                onClick = onSelectAll,
                icon = { Icon(Icons.Outlined.StickyNote2, contentDescription = null) },
            )
        }

        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
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
        }

        items(uiState.folders, key = { "folder-${it.id}" }) { folder ->
            DrawerItemRow(
                icon = { Icon(Icons.Outlined.Folder, contentDescription = null, modifier = Modifier.size(20.dp)) },
                label = folder.name,
                selected = uiState.selectedFolderId == folder.id,
                onClick = { onSelectFolder(folder.id) },
                onDelete = { onDeleteFolder(folder) },
                onRename = { onRenameFolder(folder) },
            )
        }

        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
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
        }

        items(uiState.tags, key = { "tag-${it.id}" }) { tag ->
            DrawerItemRow(
                icon = { Icon(Icons.Outlined.Label, contentDescription = null, modifier = Modifier.size(20.dp)) },
                label = "#${tag.name}",
                selected = uiState.selectedTagId == tag.id,
                onClick = { onSelectTag(tag.id) },
                onDelete = { onDeleteTag(tag) },
            )
        }

        item { Spacer(Modifier.height(24.dp)) }
    }
}

@Composable
private fun DrawerItemRow(
    icon: @Composable () -> Unit,
    label: String,
    selected: Boolean,
    onClick: () -> Unit,
    onDelete: () -> Unit,
    onRename: (() -> Unit)? = null,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(if (selected) SwissColors.AccentSoft else Color.Transparent)
            .clickable(onClick = onClick)
            .padding(start = 16.dp, end = 8.dp, top = 4.dp, bottom = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        icon()
        Spacer(Modifier.width(12.dp))
        Text(
            text = label,
            style = MaterialTheme.typography.bodyLarge,
            color = if (selected) SwissColors.Accent else SwissColors.Black,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.weight(1f),
        )
        if (onRename != null) {
            IconButton(onClick = onRename, modifier = Modifier.size(32.dp)) {
                Icon(
                    imageVector = Icons.Outlined.Edit,
                    contentDescription = stringResource(R.string.rename),
                    tint = SwissColors.Grey,
                    modifier = Modifier.size(16.dp),
                )
            }
        }
        IconButton(onClick = onDelete, modifier = Modifier.size(32.dp)) {
            Icon(
                imageVector = Icons.Outlined.Delete,
                contentDescription = stringResource(R.string.delete),
                tint = SwissColors.Grey,
                modifier = Modifier.size(16.dp),
            )
        }
    }
}

@Composable
private fun RenameFolderDialog(
    folder: FolderEntity,
    onDismiss: () -> Unit,
    onConfirm: (String) -> Unit,
) {
    var name by remember { mutableStateOf(folder.name) }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.rename)) },
        text = {
            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                label = { Text(stringResource(R.string.folder_name)) },
                singleLine = true,
            )
        },
        confirmButton = {
            TextButton(onClick = { onConfirm(name) }) { Text(stringResource(R.string.create)) }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text(stringResource(R.string.cancel)) }
        },
    )
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
            TextButton(onClick = { onConfirm(name) }) { Text(stringResource(R.string.create)) }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text(stringResource(R.string.cancel)) }
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
