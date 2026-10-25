package com.swissnotes.app

import android.app.Application
import com.swissnotes.app.data.NoteRepository
import com.swissnotes.app.data.local.AppDatabase

/**
 * Application-scoped container that constructs the data layer manually.
 */
class SwissNotesApplication : Application() {

    val repository: NoteRepository by lazy {
        val db = AppDatabase.getInstance(this)
        NoteRepository(db.noteDao(), db.folderDao(), db.tagDao(), db.linkDao())
    }
}
