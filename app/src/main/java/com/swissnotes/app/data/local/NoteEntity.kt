package com.swissnotes.app.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * A single note. Models the essentials extracted from the advantages of
 * leading note apps: title, body, folder membership, checklist flag,
 * pin status, color mark and timestamps.
 */
@Entity(tableName = "notes")
data class NoteEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0L,
    val title: String,
    val content: String,
    val folderId: Long? = null,
    val isChecklist: Boolean = false,
    val isPinned: Boolean = false,
    val color: Int = 0,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis(),
)
