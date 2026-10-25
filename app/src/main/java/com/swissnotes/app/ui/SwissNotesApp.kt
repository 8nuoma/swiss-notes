package com.swissnotes.app.ui

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.swissnotes.app.AppViewModelProvider
import com.swissnotes.app.ui.editor.EditorScreen
import com.swissnotes.app.ui.editor.EditorViewModel
import com.swissnotes.app.ui.graph.GraphScreen
import com.swissnotes.app.ui.graph.GraphViewModel
import com.swissnotes.app.ui.notes.NotesScreen
import com.swissnotes.app.ui.notes.NotesViewModel

object NotesDestination {
    const val ROUTE = "notes"
}

object EditorDestination {
    const val ROUTE = "editor"
    const val NOTE_ID_ARG = "noteId"
    const val routeWithArgs = "editor?$NOTE_ID_ARG={$NOTE_ID_ARG}"
}

object GraphDestination {
    const val ROUTE = "graph"
}

/**
 * Top-level navigation graph. Single-activity app with three
 * destinations: notes list, note editor, and the knowledge graph.
 */
@Composable
fun SwissNotesApp(modifier: Modifier = Modifier) {
    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = NotesDestination.ROUTE,
        modifier = modifier,
    ) {
        composable(route = NotesDestination.ROUTE) {
            val viewModel: NotesViewModel = viewModel(factory = AppViewModelProvider.Factory)
            NotesScreen(
                viewModel = viewModel,
                onNoteClick = { id -> navController.navigate("editor?noteId=$id") },
                onOpenGraph = { navController.navigate(GraphDestination.ROUTE) },
            )
        }
        composable(
            route = EditorDestination.routeWithArgs,
            arguments = listOf(
                navArgument(EditorDestination.NOTE_ID_ARG) {
                    type = NavType.LongType
                    defaultValue = 0L
                },
            ),
        ) {
            val viewModel: EditorViewModel = viewModel(factory = AppViewModelProvider.Factory)
            EditorScreen(
                viewModel = viewModel,
                onBack = { navController.popBackStack() },
                onOpenNote = { id -> navController.navigate("editor?noteId=$id") },
            )
        }
        composable(route = GraphDestination.ROUTE) {
            val viewModel: GraphViewModel = viewModel(factory = AppViewModelProvider.Factory)
            GraphScreen(
                viewModel = viewModel,
                onBack = { navController.popBackStack() },
                onOpenNote = { id -> navController.navigate("editor?noteId=$id") },
            )
        }
    }
}
