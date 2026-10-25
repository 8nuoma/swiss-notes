package com.swissnotes.app.data

import com.swissnotes.app.data.local.FolderDao
import com.swissnotes.app.data.local.FolderEntity
import com.swissnotes.app.data.local.LinkDao
import com.swissnotes.app.data.local.NoteDao
import com.swissnotes.app.data.local.NoteEntity
import com.swissnotes.app.data.local.NoteLinkEntity
import com.swissnotes.app.data.local.NoteTagCrossRef
import com.swissnotes.app.data.local.TagDao
import com.swissnotes.app.data.local.TagEntity
import kotlinx.coroutines.flow.Flow

/**
 * Repository exposing notes, folders, tags and links to the rest of the
 * app. It is the single entry point to the data layer.
 */
class NoteRepository(
    private val noteDao: NoteDao,
    private val folderDao: FolderDao,
    private val tagDao: TagDao,
    private val linkDao: LinkDao,
) {

    // ---- Notes ----

    fun observeNotes(query: String, folderId: Long?): Flow<List<NoteEntity>> =
        noteDao.observeNotes(query, folderId)

    fun observeNotesByTag(tagId: Long): Flow<List<NoteEntity>> =
        noteDao.observeNotesByTag(tagId)

    fun observeNote(id: Long): Flow<NoteEntity?> = noteDao.observeNote(id)

    suspend fun getNote(id: Long): NoteEntity? = noteDao.getNote(id)

    suspend fun getAllTitles(): List<String> = noteDao.getAllTitles()

    suspend fun save(note: NoteEntity): Long {
        val id = if (note.id == 0L) {
            noteDao.insert(note)
        } else {
            noteDao.update(note)
            note.id
        }
        // Rebuild links after every save so wikilinks stay in sync.
        rebuildLinks(id, note.content)
        return id
    }

    suspend fun delete(note: NoteEntity) {
        noteDao.delete(note)
        linkDao.deleteLinksInvolving(note.id)
        tagDao.deleteTagsForNote(note.id)
    }

    suspend fun togglePin(note: NoteEntity) {
        noteDao.update(note.copy(isPinned = !note.isPinned, updatedAt = System.currentTimeMillis()))
    }

    // ---- Folders ----

    fun observeFolders(): Flow<List<FolderEntity>> = folderDao.observeFolders()

    suspend fun saveFolder(folder: FolderEntity): Long =
        if (folder.id == 0L) folderDao.insert(folder)
        else {
            folderDao.update(folder)
            folder.id
        }

    suspend fun deleteFolder(folder: FolderEntity) = folderDao.delete(folder)

    // ---- Tags ----

    fun observeTags(): Flow<List<TagEntity>> = tagDao.observeTags()

    fun observeTagsForNote(noteId: Long): Flow<List<TagEntity>> = tagDao.observeTagsForNote(noteId)

    suspend fun addTagToNote(noteId: Long, tagName: String) {
        val name = tagName.trim().removePrefix("#")
        if (name.isBlank()) return
        val existing = tagDao.getTagByName(name)
        val tagId = existing?.id ?: tagDao.insert(TagEntity(name = name))
        tagDao.insertCrossRef(NoteTagCrossRef(noteId = noteId, tagId = tagId))
    }

    suspend fun createTag(tagName: String): Long {
        val name = tagName.trim().removePrefix("#")
        require(name.isNotBlank())
        val existing = tagDao.getTagByName(name)
        return existing?.id ?: tagDao.insert(TagEntity(name = name))
    }

    suspend fun setTagsForNote(noteId: Long, tagNames: List<String>) {
        tagDao.deleteTagsForNote(noteId)
        tagNames.forEach { addTagToNote(noteId, it) }
    }

    suspend fun deleteTag(tag: TagEntity) {
        tagDao.deleteCrossRefsForTag(tag.id)
        tagDao.delete(tag)
    }

    // ---- Links ----

    fun observeAllLinks(): Flow<List<NoteLinkEntity>> = linkDao.observeAllLinks()

    fun observeOutgoingLinks(noteId: Long): Flow<List<NoteEntity>> =
        linkDao.observeOutgoingLinks(noteId)

    fun observeBacklinks(noteId: Long): Flow<List<NoteEntity>> =
        linkDao.observeBacklinks(noteId)

    /**
     * Parses Obsidian-style `[[Note Title]]` wikilinks out of [content]
     * and persists the resolved links.
     */
    private suspend fun rebuildLinks(sourceNoteId: Long, content: String) {
        linkDao.deleteLinksFrom(sourceNoteId)
        val titles = parseWikilinks(content)
        titles.forEach { title ->
            noteDao.getNoteByTitle(title)?.let { target ->
                if (target.id != sourceNoteId) {
                    linkDao.insert(NoteLinkEntity(sourceNoteId, target.id))
                }
            }
        }
    }

    private fun parseWikilinks(content: String): List<String> {
        val regex = Regex("""\[\[([^\]|]+)(\|[^\]]*)?\]\]""")
        return regex.findAll(content)
            .map { it.groupValues[1].trim() }
            .distinct()
            .toList()
    }
}
