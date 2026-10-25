package com.swissnotes.app.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * A single note. The entity models the essentials extracted from the
 * advantages of leading note apps: a title, body, checklist flag,
 * pin status and timestamps for sorting and search.
 */
@Entity(tableName = "notes")
data class NoteEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0L,
    val title: String,
    val content: String,
    val isChecklist: Boolean = false,
    val isPinned: Boolean = false,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis(),
)
