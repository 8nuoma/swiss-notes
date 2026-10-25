package com.swissnotes.app

import android.app.Application
import com.swissnotes.app.data.NoteRepository
import com.swissnotes.app.data.local.AppDatabase

/**
 * Application-scoped container that constructs the data layer manually,
 * keeping dependencies minimal while retaining a clean architecture.
 */
class SwissNotesApplication : Application() {

    val repository: NoteRepository by lazy {
        NoteRepository(AppDatabase.getInstance(this).noteDao())
    }
}
