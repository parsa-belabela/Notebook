package com.example.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "notes",
    foreignKeys = [
        ForeignKey(
            entity = FolderEntity::class,
            parentColumns = ["id"],
            childColumns = ["folderId"],
            onDelete = ForeignKey.SET_NULL
        )
    ],
    indices = [Index(value = ["folderId"])]
)
data class NoteEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String,
    val content: String,
    val folderId: Long? = null,
    val isPinned: Boolean = false,
    val isFavorite: Boolean = false,
    val isArchived: Boolean = false,
    val colorHex: String? = null,
    val priority: String = "NORMAL", // "HIGH", "MEDIUM", "LOW", "NORMAL"
    val status: String = "ACTIVE",   // "ACTIVE", "DRAFT", "ARCHIVED"
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "folders")
data class FolderEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val parentId: Long? = null,
    val iconName: String = "folder",
    val colorHex: String = "#7C3AED",
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "tags")
data class TagEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val colorHex: String = "#06B6D4"
)

@Entity(
    tableName = "note_tag_cross_ref",
    primaryKeys = ["noteId", "tagId"],
    indices = [Index(value = ["tagId"])]
)
data class NoteTagCrossRef(
    val noteId: Long,
    val tagId: Long
)

@Entity(
    tableName = "note_links",
    indices = [Index(value = ["sourceNoteId"]), Index(value = ["targetNoteId"])]
)
data class NoteLinkEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val sourceNoteId: Long,
    val targetNoteTitle: String,
    val targetNoteId: Long? = null,
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(
    tableName = "note_versions",
    indices = [Index(value = ["noteId"])]
)
data class NoteVersionEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val noteId: Long,
    val titleSnapshot: String,
    val contentSnapshot: String,
    val timestamp: Long = System.currentTimeMillis(),
    val changeSummary: String = "Auto-save snapshot"
)
