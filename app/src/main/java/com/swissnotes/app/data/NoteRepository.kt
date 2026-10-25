package com.swissnotes.app.data

import com.swissnotes.app.data.local.NoteDao
import com.swissnotes.app.data.local.NoteEntity
import kotlinx.coroutines.flow.Flow

/**
 * Repository that exposes notes to the rest of the app. It is the
 * single entry point to the data layer, hiding the database behind
 * a clean interface (per the Android architecture guidance).
 */
class NoteRepository(private val noteDao: NoteDao) {

    fun observeNotes(query: String): Flow<List<NoteEntity>> =
        noteDao.observeNotes(query)

    fun observeNote(id: Long): Flow<NoteEntity?> =
        noteDao.observeNote(id)

    suspend fun getNote(id: Long): NoteEntity? =
        noteDao.getNote(id)

    suspend fun save(note: NoteEntity): Long =
        if (note.id == 0L) {
            noteDao.insert(note)
        } else {
            noteDao.update(note)
            note.id
        }

    suspend fun delete(note: NoteEntity) = noteDao.delete(note)

    suspend fun togglePin(note: NoteEntity) {
        noteDao.update(note.copy(isPinned = !note.isPinned, updatedAt = System.currentTimeMillis()))
    }
}
