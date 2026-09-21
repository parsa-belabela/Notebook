package com.example.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import com.example.data.local.entity.FolderEntity
import com.example.data.local.entity.NoteEntity
import com.example.data.local.entity.NoteLinkEntity
import com.example.data.local.entity.NoteTagCrossRef
import com.example.data.local.entity.NoteVersionEntity
import com.example.data.local.entity.TagEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface NexusNoteDao {

    // --- Notes ---
    @Query("SELECT * FROM notes WHERE isArchived = 0 ORDER BY isPinned DESC, updatedAt DESC")
    fun getAllActiveNotes(): Flow<List<NoteEntity>>

    @Query("SELECT * FROM notes WHERE isArchived = 1 ORDER BY updatedAt DESC")
    fun getArchivedNotes(): Flow<List<NoteEntity>>

    @Query("SELECT * FROM notes WHERE id = :id")
    fun getNoteById(id: Long): Flow<NoteEntity?>

    @Query("SELECT * FROM notes WHERE id = :id")
    suspend fun getNoteByIdDirect(id: Long): NoteEntity?

    @Query("SELECT * FROM notes WHERE LOWER(title) = LOWER(:title) LIMIT 1")
    suspend fun getNoteByTitle(title: String): NoteEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertNote(note: NoteEntity): Long

    @Update
    suspend fun updateNote(note: NoteEntity)

    @Delete
    suspend fun deleteNote(note: NoteEntity)

    @Query("DELETE FROM notes WHERE id = :id")
    suspend fun deleteNoteById(id: Long)

    // --- Folders ---
    @Query("SELECT * FROM folders ORDER BY name ASC")
    fun getAllFolders(): Flow<List<FolderEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFolder(folder: FolderEntity): Long

    @Query("DELETE FROM folders WHERE id = :folderId")
    suspend fun deleteFolder(folderId: Long)

    // --- Tags ---
    @Query("SELECT * FROM tags ORDER BY name ASC")
    fun getAllTags(): Flow<List<TagEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTag(tag: TagEntity): Long

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertNoteTagCrossRef(crossRef: NoteTagCrossRef)

    @Query("DELETE FROM note_tag_cross_ref WHERE noteId = :noteId AND tagId = :tagId")
    suspend fun deleteNoteTagCrossRef(noteId: Long, tagId: Long)

    @Query("DELETE FROM note_tag_cross_ref WHERE noteId = :noteId")
    suspend fun clearTagsForNote(noteId: Long)

    @Query("""
        SELECT tags.* FROM tags 
        INNER JOIN note_tag_cross_ref ON tags.id = note_tag_cross_ref.tagId 
        WHERE note_tag_cross_ref.noteId = :noteId
    """)
    fun getTagsForNote(noteId: Long): Flow<List<TagEntity>>

    // --- Bi-directional Links ---
    @Query("SELECT * FROM note_links")
    fun getAllNoteLinks(): Flow<List<NoteLinkEntity>>

    @Query("SELECT * FROM note_links WHERE sourceNoteId = :sourceId")
    fun getLinksFromNote(sourceId: Long): Flow<List<NoteLinkEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertNoteLink(link: NoteLinkEntity): Long

    @Query("DELETE FROM note_links WHERE sourceNoteId = :sourceId")
    suspend fun deleteNoteLinksFromSource(sourceId: Long)

    // --- Version History ---
    @Query("SELECT * FROM note_versions WHERE noteId = :noteId ORDER BY timestamp DESC")
    fun getNoteVersions(noteId: Long): Flow<List<NoteVersionEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertNoteVersion(version: NoteVersionEntity): Long

    @Query("DELETE FROM note_versions WHERE noteId = :noteId")
    suspend fun clearVersionsForNote(noteId: Long)
}
