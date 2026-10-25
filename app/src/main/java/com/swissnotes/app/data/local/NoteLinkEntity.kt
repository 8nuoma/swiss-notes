package com.swissnotes.app.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * A bidirectional link between two notes, representing an Obsidian-style
 * `[[wikilink]]` connection. Links are stored as source→target pairs so
 * backlinks (incoming links) can be queried directly.
 */
@Entity(
    tableName = "note_links",
    primaryKeys = ["sourceNoteId", "targetNoteId"],
)
data class NoteLinkEntity(
    val sourceNoteId: Long,
    val targetNoteId: Long,
)
