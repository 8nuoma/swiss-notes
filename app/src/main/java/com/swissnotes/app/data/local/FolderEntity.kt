package com.swissnotes.app.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * A folder used to group notes. Supports nesting via [parentId].
 */
@Entity(tableName = "folders")
data class FolderEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0L,
    val name: String,
    val parentId: Long? = null,
    val createdAt: Long = System.currentTimeMillis(),
)
