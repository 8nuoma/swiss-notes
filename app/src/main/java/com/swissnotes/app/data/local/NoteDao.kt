package com.swissnotes.app.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

/**
 * Data access object for notes. Exposes reactive [Flow]s so the UI
 * observes a single source of truth (UDF).
 */
@Dao
interface NoteDao {

    @Query(
        "SELECT * FROM notes " +
            "WHERE (:query = '' OR title LIKE '%' || :query || '%' " +
            "OR content LIKE '%' || :query || '%') " +
            "AND (:folderId IS NULL OR folderId = :folderId) " +
            "ORDER BY isPinned DESC, updatedAt DESC",
    )
    fun observeNotes(query: String, folderId: Long?): Flow<List<NoteEntity>>

    @Query(
        "SELECT notes.* FROM notes " +
            "INNER JOIN note_tag_cross_ref ON notes.id = note_tag_cross_ref.noteId " +
            "WHERE note_tag_cross_ref.tagId = :tagId " +
            "ORDER BY isPinned DESC, updatedAt DESC",
    )
    fun observeNotesByTag(tagId: Long): Flow<List<NoteEntity>>

    @Query("SELECT * FROM notes WHERE id = :id")
    fun observeNote(id: Long): Flow<NoteEntity?>

    @Query("SELECT * FROM notes WHERE id = :id")
    suspend fun getNote(id: Long): NoteEntity?

    @Query("SELECT * FROM notes WHERE title = :title LIMIT 1")
    suspend fun getNoteByTitle(title: String): NoteEntity?

    @Query("SELECT title FROM notes")
    suspend fun getAllTitles(): List<String>

    @Query("SELECT * FROM notes")
    suspend fun getAllNotes(): List<NoteEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(note: NoteEntity): Long

    @Update
    suspend fun update(note: NoteEntity)

    @Delete
    suspend fun delete(note: NoteEntity)

    /** Detach all notes from a folder before the folder is deleted. */
    @Query("UPDATE notes SET folderId = NULL WHERE folderId = :folderId")
    suspend fun clearFolder(folderId: Long)
}
