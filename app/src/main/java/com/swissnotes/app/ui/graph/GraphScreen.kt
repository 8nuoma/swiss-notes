package com.swissnotes.app.ui.graph

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.swissnotes.app.R
import com.swissnotes.app.data.local.NoteEntity
import com.swissnotes.app.ui.theme.SwissColors
import kotlin.math.cos
import kotlin.math.sin

/**
 * Knowledge-graph view: notes as nodes, wikilinks as edges, laid out
 * on a circle. A calm, minimal visualization of the note network.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GraphScreen(
    viewModel: GraphViewModel,
    onBack: () -> Unit,
    onOpenNote: (Long) -> Unit,
    modifier: Modifier = Modifier,
) {
    val state by viewModel.graphState.collectAsStateWithLifecycle()

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = SwissColors.White,
        topBar = {
            TopAppBar(
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Outlined.ArrowBack,
                            contentDescription = stringResource(R.string.back),
                            tint = SwissColors.Black,
                        )
                    }
                },
                title = { Text(stringResource(R.string.graph), style = MaterialTheme.typography.titleMedium) },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = SwissColors.White),
            )
        },
    ) { innerPadding ->
        if (state.notes.isEmpty()) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .padding(24.dp),
                verticalArrangement = androidx.compose.foundation.layout.Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Text(
                    text = stringResource(R.string.graph_empty),
                    style = MaterialTheme.typography.bodyMedium,
                    color = SwissColors.Grey,
                )
            }
        } else {
            GraphCanvas(
                notes = state.notes,
                links = state.links,
                onOpenNote = onOpenNote,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
            )
        }
    }
}

@Composable
private fun GraphCanvas(
    notes: List<NoteEntity>,
    links: List<com.swissnotes.app.data.local.NoteLinkEntity>,
    onOpenNote: (Long) -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .background(SwissColors.OffWhite)
            .pointerInput(notes, links) {
                detectTapGestures { offset ->
                    // Find the closest node and open it.
                    val pos = layout(notes, size.width.toFloat(), size.height.toFloat())
                    val hit = notes.firstOrNull { note ->
                        val p = pos[note.id] ?: return@firstOrNull false
                        (p - offset).getDistance() < 40f
                    }
                    hit?.let { onOpenNote(it.id) }
                }
            },
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val positions = layout(notes, size.width, size.height)

            // Edges
            links.forEach { link ->
                val from = positions[link.sourceNoteId] ?: return@forEach
                val to = positions[link.targetNoteId] ?: return@forEach
                drawLine(
                    color = SwissColors.LightGrey,
                    start = from,
                    end = to,
                    strokeWidth = 1.5f,
                )
            }

            // Nodes
            notes.forEach { note ->
                val center = positions[note.id] ?: return@forEach
                val color = noteColor(note.color)
                drawCircle(
                    color = color,
                    radius = 22f,
                    center = center,
                )
                drawCircle(
                    color = SwissColors.Black,
                    radius = 22f,
                    center = center,
                    style = androidx.compose.ui.graphics.drawscope.Stroke(width = 1.5f),
                )
            }
        }
    }
}

private fun layout(
    notes: List<NoteEntity>,
    width: Float,
    height: Float,
): Map<Long, Offset> {
    if (notes.isEmpty()) return emptyMap()
    val center = Offset(width / 2f, height / 2f)
    val radius = (minOf(width, height) / 2f) - 60f
    return notes.mapIndexed { index, note ->
        val angle = (2.0 * Math.PI * index) / notes.size
        val x = center.x + (radius * cos(angle)).toFloat()
        val y = center.y + (radius * sin(angle)).toFloat()
        note.id to Offset(x, y)
    }.toMap()
}

private fun noteColor(colorIndex: Int): Color = when (colorIndex) {
    1 -> SwissColors.NoteRed
    2 -> SwissColors.NoteYellow
    3 -> SwissColors.NoteGreen
    4 -> SwissColors.NoteBlue
    5 -> SwissColors.NotePurple
    else -> SwissColors.White
}
