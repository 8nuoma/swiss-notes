package com.swissnotes.app.ui.editor

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.outlined.Check
import androidx.compose.material.icons.outlined.CheckBox
import androidx.compose.material.icons.outlined.CheckBoxOutlineBlank
import androidx.compose.material.icons.outlined.Image
import androidx.compose.material.icons.outlined.Link
import androidx.compose.material.icons.outlined.Movie
import androidx.compose.material.icons.outlined.Tag
import androidx.compose.material.icons.outlined.Visibility
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.swissnotes.app.R
import com.swissnotes.app.data.AttachmentManager
import com.swissnotes.app.data.local.NoteEntity
import com.swissnotes.app.ui.components.MarkdownView
import com.swissnotes.app.ui.theme.SwissColors
import kotlinx.coroutines.launch

/**
 * Note editor. Title and body use local [rememberSaveable] state so
 * keystrokes never recompose the whole screen (fixes input lag). Supports
 * image/video insertion, a Markdown toolbar, edit/preview toggle, tags,
 * colors and wikilinks.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditorScreen(
    viewModel: EditorViewModel,
    onBack: () -> Unit,
    onOpenNote: (Long) -> Unit,
    modifier: Modifier = Modifier,
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    // Local state for fast typing (no recomposition of the whole screen).
    var title by rememberSaveable { mutableStateOf("") }
    var content by rememberSaveable { mutableStateOf("") }
    var isPreview by rememberSaveable { mutableStateOf(false) }
    var initialized by rememberSaveable { mutableStateOf(false) }

    // Sync from ViewModel once when the note is first loaded.
    LaunchedEffect(uiState.isLoaded) {
        if (uiState.isLoaded && !initialized) {
            title = uiState.title
            content = uiState.content
            initialized = true
        }
    }

    var showLinkDialog by remember { mutableStateOf(false) }
    var showTagDialog by remember { mutableStateOf(false) }

    // Image picker (Photo Picker).
    val imagePicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia(),
    ) { uri ->
        uri?.let { u ->
            scope.launch {
                val path = AttachmentManager.copyToStorage(context, u, AttachmentManager.Kind.Image)
                if (path != null) {
                    content += "\n![image]($path)\n"
                }
            }
        }
    }

    // Video picker.
    val videoPicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent(),
    ) { uri ->
        uri?.let { u ->
            scope.launch {
                val path = AttachmentManager.copyToStorage(context, u, AttachmentManager.Kind.Video)
                if (path != null) {
                    content += "\n![video]($path)\n"
                }
            }
        }
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = SwissColors.White,
        topBar = {
            TopAppBar(
                navigationIcon = {
                    IconButton(onClick = { viewModel.saveAndExit(title, content, onBack) }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Outlined.ArrowBack,
                            contentDescription = stringResource(R.string.back),
                            tint = SwissColors.Black,
                        )
                    }
                },
                title = { Text(stringResource(R.string.app_name), style = MaterialTheme.typography.titleMedium) },
                actions = {
                    IconButton(onClick = viewModel::onChecklistToggle) {
                        Icon(
                            imageVector = if (uiState.isChecklist) Icons.Outlined.CheckBox else Icons.Outlined.CheckBoxOutlineBlank,
                            contentDescription = stringResource(R.string.checklist),
                            tint = if (uiState.isChecklist) SwissColors.Accent else SwissColors.Black,
                        )
                    }
                    IconButton(onClick = { showLinkDialog = true }) {
                        Icon(
                            imageVector = Icons.Outlined.Link,
                            contentDescription = stringResource(R.string.insert_link),
                            tint = SwissColors.Black,
                        )
                    }
                    IconButton(onClick = { showTagDialog = true }) {
                        Icon(
                            imageVector = Icons.Outlined.Tag,
                            contentDescription = stringResource(R.string.add_tag),
                            tint = SwissColors.Black,
                        )
                    }
                    IconButton(onClick = { isPreview = !isPreview }) {
                        Icon(
                            imageVector = Icons.Outlined.Visibility,
                            contentDescription = "Preview",
                            tint = if (isPreview) SwissColors.Accent else SwissColors.Black,
                        )
                    }
                    IconButton(onClick = { viewModel.saveAndExit(title, content, onBack) }) {
                        Icon(
                            imageVector = Icons.Outlined.Check,
                            contentDescription = stringResource(R.string.save),
                            tint = SwissColors.Accent,
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = SwissColors.White),
            )
        },
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
        ) {
            if (isPreview) {
                // Preview mode
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                        .padding(horizontal = 20.dp, vertical = 16.dp),
                ) {
                    Text(
                        text = title.ifBlank { stringResource(R.string.untitled) },
                        style = MaterialTheme.typography.headlineLarge,
                        color = SwissColors.Black,
                    )
                    Spacer(Modifier.height(12.dp))
                    MarkdownView(text = content, onWikilinkClick = { title ->
                        // Resolve wikilink title to note id and open it.
                        scope.launch {
                            val note = viewModel.resolveByTitle(title)
                            if (note != null) onOpenNote(note.id)
                        }
                    })
                }
            } else {
                // Edit mode
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    placeholder = { Text(stringResource(R.string.title_hint), color = SwissColors.Grey) },
                    textStyle = MaterialTheme.typography.headlineMedium,
                    shape = RoundedCornerShape(0.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Color.Transparent,
                        unfocusedBorderColor = Color.Transparent,
                        focusedTextColor = SwissColors.Black,
                        unfocusedTextColor = SwissColors.Black,
                        cursorColor = SwissColors.Accent,
                    ),
                )

                // Markdown + media toolbar
                MarkdownToolbar(
                    onInsert = { md -> content += md },
                    onImage = {
                        imagePicker.launch(
                            PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly),
                        )
                    },
                    onVideo = { videoPicker.launch("video/*") },
                )

                // Tags
                if (uiState.tags.isNotEmpty()) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState())
                            .padding(horizontal = 16.dp, vertical = 4.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        uiState.tags.forEach { tag ->
                            Box(
                                modifier = Modifier
                                    .clip(MaterialTheme.shapes.small)
                                    .background(SwissColors.Surface)
                                    .padding(horizontal = 10.dp, vertical = 4.dp),
                            ) {
                                Text("#${tag.name}", style = MaterialTheme.typography.labelMedium, color = SwissColors.Grey)
                            }
                        }
                    }
                }

                // Content
                OutlinedTextField(
                    value = content,
                    onValueChange = { content = it },
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    placeholder = { Text(stringResource(R.string.content_hint), color = SwissColors.Grey) },
                    textStyle = MaterialTheme.typography.bodyLarge,
                    shape = RoundedCornerShape(0.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Color.Transparent,
                        unfocusedBorderColor = Color.Transparent,
                        focusedTextColor = SwissColors.Black,
                        unfocusedTextColor = SwissColors.Black,
                        cursorColor = SwissColors.Accent,
                    ),
                )

                HorizontalDivider(color = SwissColors.LightGrey)

                ColorSelector(
                    selected = uiState.color,
                    onSelect = viewModel::onColorSelect,
                )

                LinkPanel(
                    noteId = uiState.noteId,
                    viewModel = viewModel,
                    onOpenNote = onOpenNote,
                )
            }
        }
    }

    if (showLinkDialog) {
        LinkDialog(
            titles = uiState.allTitles,
            currentTitle = title.trim(),
            onDismiss = { showLinkDialog = false },
            onSelect = { target ->
                val sep = if (content.isEmpty() || content.endsWith("\n")) "" else "\n"
                content += "$sep[[$target]]"
                showLinkDialog = false
            },
        )
    }
    if (showTagDialog) {
        TagDialog(
            onDismiss = { showTagDialog = false },
            onConfirm = { name ->
                viewModel.addTag(name)
                showTagDialog = false
            },
        )
    }
}

@Composable
private fun MarkdownToolbar(
    onInsert: (String) -> Unit,
    onImage: () -> Unit,
    onVideo: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState())
            .padding(horizontal = 16.dp, vertical = 4.dp),
        horizontalArrangement = Arrangement.spacedBy(4.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        ToolButton("H1") { onInsert("# ") }
        ToolButton("H2") { onInsert("## ") }
        ToolButton("B") { onInsert("**") }
        ToolButton("I") { onInsert("*") }
        ToolButton("`") { onInsert("`") }
        ToolButton("•") { onInsert("- ") }
        ToolButton("1.") { onInsert("1. ") }
        ToolButton("☐") { onInsert("- [ ] ") }
        ToolButton("❝") { onInsert("> ") }
        ToolButton("```") { onInsert("```\n\n```") }
        ToolButton("—") { onInsert("\n---\n") }
        ToolButton("🔗") { onInsert("[[") }
        IconButton(onClick = onImage, modifier = Modifier.size(36.dp)) {
            Icon(Icons.Outlined.Image, contentDescription = "Image", tint = SwissColors.Black, modifier = Modifier.size(20.dp))
        }
        IconButton(onClick = onVideo, modifier = Modifier.size(36.dp)) {
            Icon(Icons.Outlined.Movie, contentDescription = "Video", tint = SwissColors.Black, modifier = Modifier.size(20.dp))
        }
    }
}

@Composable
private fun ToolButton(label: String, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .clip(MaterialTheme.shapes.small)
            .background(SwissColors.Surface)
            .clickable(onClick = onClick)
            .padding(horizontal = 10.dp, vertical = 8.dp),
    ) {
        Text(label, style = MaterialTheme.typography.labelLarge, color = SwissColors.Black)
    }
}

@Composable
private fun ColorSelector(
    selected: Int,
    onSelect: (Int) -> Unit,
) {
    val colors = listOf(
        0 to SwissColors.White,
        1 to SwissColors.NoteRed,
        2 to SwissColors.NoteYellow,
        3 to SwissColors.NoteGreen,
        4 to SwissColors.NoteBlue,
        5 to SwissColors.NotePurple,
    )
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(stringResource(R.string.select_color), style = MaterialTheme.typography.labelMedium, color = SwissColors.Grey)
        colors.forEach { (index, color) ->
            Box(
                modifier = Modifier
                    .size(28.dp)
                    .clip(CircleShape)
                    .background(color)
                    .then(
                        if (index == selected) {
                            Modifier
                                .padding(3.dp)
                                .clip(CircleShape)
                                .background(SwissColors.Accent)
                                .padding(2.dp)
                        } else {
                            Modifier
                        },
                    )
                    .clickable { onSelect(index) },
            )
        }
    }
}

@Composable
private fun LinkPanel(
    noteId: Long,
    viewModel: EditorViewModel,
    onOpenNote: (Long) -> Unit,
) {
    if (noteId == 0L) return
    val outgoing by viewModel.outgoingLinks.collectAsStateWithLifecycle(initialValue = emptyList())
    val backlinks by viewModel.backlinks.collectAsStateWithLifecycle(initialValue = emptyList())

    Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)) {
        if (backlinks.isNotEmpty() || outgoing.isNotEmpty()) {
            Text(stringResource(R.string.backlinks).uppercase(), style = MaterialTheme.typography.labelSmall, color = SwissColors.Grey)
            backlinks.forEach { note ->
                LinkRow(note, onClick = { onOpenNote(note.id) })
            }
            Spacer(Modifier.height(8.dp))
            Text(stringResource(R.string.links).uppercase(), style = MaterialTheme.typography.labelSmall, color = SwissColors.Grey)
            outgoing.forEach { note ->
                LinkRow(note, onClick = { onOpenNote(note.id) })
            }
        }
    }
}

@Composable
private fun LinkRow(note: NoteEntity, onClick: () -> Unit) {
    Text(
        text = "↳ ${note.title.ifBlank { "Untitled" }}",
        style = MaterialTheme.typography.bodyMedium,
        color = SwissColors.Accent,
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = 4.dp),
    )
}

@Composable
private fun LinkDialog(
    titles: List<String>,
    currentTitle: String,
    onDismiss: () -> Unit,
    onSelect: (String) -> Unit,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.insert_wikilink)) },
        text = {
            Column(
                modifier = Modifier
                    .height(280.dp)
                    .verticalScroll(rememberScrollState()),
            ) {
                titles.filter { it.isNotBlank() && it != currentTitle }.forEach { t ->
                    Text(
                        text = t,
                        style = MaterialTheme.typography.bodyLarge,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onSelect(t) }
                            .padding(vertical = 12.dp),
                    )
                    HorizontalDivider(color = SwissColors.LightGrey)
                }
            }
        },
        confirmButton = {},
        dismissButton = {
            TextButton(onClick = onDismiss) { Text(stringResource(R.string.cancel)) }
        },
    )
}

@Composable
private fun TagDialog(
    onDismiss: () -> Unit,
    onConfirm: (String) -> Unit,
) {
    var name by remember { mutableStateOf("") }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.add_tag)) },
        text = {
            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                label = { Text(stringResource(R.string.tag_name)) },
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
