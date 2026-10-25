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

/**
 * Top-level navigation graph. A single-activity app with two
 * destinations: the notes list and the note editor.
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
            val viewModel: NotesViewModel =
                viewModel(factory = AppViewModelProvider.Factory)
            NotesScreen(
                viewModel = viewModel,
                onNoteClick = { id ->
                    navController.navigate("editor?noteId=$id")
                },
                onNewNote = {
                    navController.navigate(EditorDestination.ROUTE)
                },
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
            val viewModel: EditorViewModel =
                viewModel(factory = AppViewModelProvider.Factory)
            EditorScreen(
                viewModel = viewModel,
                onBack = { navController.popBackStack() },
            )
        }
    }
}
