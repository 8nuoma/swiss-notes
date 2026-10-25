package com.swissnotes.app.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

/**
 * Data access object for note links (wikilinks).
 */
@Dao
interface LinkDao {

    @Query("SELECT * FROM note_links")
    fun observeAllLinks(): Flow<List<NoteLinkEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(link: NoteLinkEntity)

    @Query("DELETE FROM note_links WHERE sourceNoteId = :sourceNoteId")
    suspend fun deleteLinksFrom(sourceNoteId: Long)

    @Query("DELETE FROM note_links WHERE sourceNoteId = :noteId OR targetNoteId = :noteId")
    suspend fun deleteLinksInvolving(noteId: Long)

    /** Outgoing links: notes that the given note links to. */
    @Query(
        "SELECT notes.* FROM notes INNER JOIN note_links ON notes.id = note_links.targetNoteId " +
            "WHERE note_links.sourceNoteId = :noteId",
    )
    fun observeOutgoingLinks(noteId: Long): Flow<List<NoteEntity>>

    /** Backlinks: notes that link to the given note. */
    @Query(
        "SELECT notes.* FROM notes INNER JOIN note_links ON notes.id = note_links.sourceNoteId " +
            "WHERE note_links.targetNoteId = :noteId",
    )
    fun observeBacklinks(noteId: Long): Flow<List<NoteEntity>>
}
