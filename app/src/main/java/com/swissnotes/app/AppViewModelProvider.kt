package com.swissnotes.app

import android.app.Application
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.CreationExtras
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.swissnotes.app.ui.editor.EditorViewModel
import com.swissnotes.app.ui.notes.NotesViewModel

/**
 * Provides ViewModels using a factory that resolves the application
 * repository without a DI framework.
 */
object AppViewModelProvider {

    val Factory = viewModelFactory {
        initializer {
            NotesViewModel(swissNotesApplication().repository)
        }
        initializer {
            EditorViewModel(swissNotesApplication().repository)
        }
    }
}

fun CreationExtras.swissNotesApplication(): SwissNotesApplication =
    (this[ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY] as SwissNotesApplication)
