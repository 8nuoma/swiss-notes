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
 * observes a single source of truth, in line with unidirectional
 * data flow (UDF).
 */
@Dao
interface NoteDao {

    @Query(
        "SELECT * FROM notes " +
            "WHERE (:query = '' OR title LIKE '%' || :query || '%' " +
            "OR content LIKE '%' || :query || '%') " +
            "ORDER BY isPinned DESC, updatedAt DESC",
    )
    fun observeNotes(query: String): Flow<List<NoteEntity>>

    @Query("SELECT * FROM notes WHERE id = :id")
    fun observeNote(id: Long): Flow<NoteEntity?>

    @Query("SELECT * FROM notes WHERE id = :id")
    suspend fun getNote(id: Long): NoteEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(note: NoteEntity): Long

    @Update
    suspend fun update(note: NoteEntity)

    @Delete
    suspend fun delete(note: NoteEntity)
}
