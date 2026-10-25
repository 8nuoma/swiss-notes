package com.swissnotes.app.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * A tag applied to notes (many-to-many via [NoteTagCrossRef]).
 */
@Entity(tableName = "tags")
data class TagEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0L,
    val name: String,
    val color: Int = 0,
)

/**
 * Join table linking notes to tags.
 */
@Entity(
    tableName = "note_tag_cross_ref",
    primaryKeys = ["noteId", "tagId"],
)
data class NoteTagCrossRef(
    val noteId: Long,
    val tagId: Long,
)
