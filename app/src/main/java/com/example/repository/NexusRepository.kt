package com.example.repository

import com.example.data.local.dao.NexusNoteDao
import com.example.data.local.entity.FolderEntity
import com.example.data.local.entity.NoteEntity
import com.example.data.local.entity.NoteLinkEntity
import com.example.data.local.entity.NoteTagCrossRef
import com.example.data.local.entity.NoteVersionEntity
import com.example.data.local.entity.TagEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext
import java.util.regex.Pattern

class NexusRepository(private val dao: NexusNoteDao) {

    val activeNotes: Flow<List<NoteEntity>> = dao.getAllActiveNotes()
    val archivedNotes: Flow<List<NoteEntity>> = dao.getArchivedNotes()
    val folders: Flow<List<FolderEntity>> = dao.getAllFolders()
    val tags: Flow<List<TagEntity>> = dao.getAllTags()
    val allNoteLinks: Flow<List<NoteLinkEntity>> = dao.getAllNoteLinks()

    fun getNoteById(id: Long): Flow<NoteEntity?> = dao.getNoteById(id)
    fun getTagsForNote(noteId: Long): Flow<List<TagEntity>> = dao.getTagsForNote(noteId)
    fun getNoteVersions(noteId: Long): Flow<List<NoteVersionEntity>> = dao.getNoteVersions(noteId)

    suspend fun saveNote(note: NoteEntity): Long = withContext(Dispatchers.IO) {
        val updatedNote = note.copy(updatedAt = System.currentTimeMillis())
        val noteId = if (note.id == 0L) {
            dao.insertNote(updatedNote)
        } else {
            dao.updateNote(updatedNote)
            note.id
        }

        // Auto-extract bi-directional links [[Note Title]]
        extractAndSaveBiDirectionalLinks(noteId, updatedNote.content)

        // Save automatic snapshot for version history if content changed significantly
        saveVersionSnapshot(noteId, updatedNote.title, updatedNote.content, "Auto-saved edit")

        noteId
    }

    suspend fun createNewNote(title: String = "Untitled Note", folderId: Long? = null): Long = withContext(Dispatchers.IO) {
        val newNote = NoteEntity(
            title = title,
            content = "# $title\n\nStart writing here or type `/` for slash commands...",
            folderId = folderId,
            createdAt = System.currentTimeMillis(),
            updatedAt = System.currentTimeMillis()
        )
        val id = dao.insertNote(newNote)
        saveVersionSnapshot(id, title, newNote.content, "Created note")
        id
    }

    suspend fun deleteNote(id: Long) = withContext(Dispatchers.IO) {
        dao.deleteNoteById(id)
        dao.clearTagsForNote(id)
        dao.deleteNoteLinksFromSource(id)
        dao.clearVersionsForNote(id)
    }

    suspend fun archiveNote(note: NoteEntity) = withContext(Dispatchers.IO) {
        dao.updateNote(note.copy(isArchived = true, updatedAt = System.currentTimeMillis()))
    }

    suspend fun unarchiveNote(note: NoteEntity) = withContext(Dispatchers.IO) {
        dao.updateNote(note.copy(isArchived = false, updatedAt = System.currentTimeMillis()))
    }

    suspend fun togglePinNote(note: NoteEntity) = withContext(Dispatchers.IO) {
        dao.updateNote(note.copy(isPinned = !note.isPinned, updatedAt = System.currentTimeMillis()))
    }

    suspend fun toggleFavoriteNote(note: NoteEntity) = withContext(Dispatchers.IO) {
        dao.updateNote(note.copy(isFavorite = !note.isFavorite, updatedAt = System.currentTimeMillis()))
    }

    // --- Folders ---
    suspend fun createFolder(name: String, iconName: String = "folder", colorHex: String = "#7C3AED"): Long = withContext(Dispatchers.IO) {
        dao.insertFolder(FolderEntity(name = name, iconName = iconName, colorHex = colorHex))
    }

    suspend fun deleteFolder(id: Long) = withContext(Dispatchers.IO) {
        dao.deleteFolder(id)
    }

    // --- Tags ---
    suspend fun createTag(name: String, colorHex: String = "#06B6D4"): Long = withContext(Dispatchers.IO) {
        dao.insertTag(TagEntity(name = name.removePrefix("#"), colorHex = colorHex))
    }

    suspend fun addTagToNote(noteId: Long, tagId: Long) = withContext(Dispatchers.IO) {
        dao.insertNoteTagCrossRef(NoteTagCrossRef(noteId, tagId))
    }

    suspend fun removeTagFromNote(noteId: Long, tagId: Long) = withContext(Dispatchers.IO) {
        dao.deleteNoteTagCrossRef(noteId, tagId)
    }

    // --- Version History ---
    suspend fun saveVersionSnapshot(noteId: Long, title: String, content: String, summary: String) = withContext(Dispatchers.IO) {
        val versions = dao.getNoteVersions(noteId).first()
        val lastVersion = versions.firstOrNull()

        // Avoid duplicating identical content snapshots
        if (lastVersion == null || lastVersion.contentSnapshot != content || lastVersion.titleSnapshot != title) {
            dao.insertNoteVersion(
                NoteVersionEntity(
                    noteId = noteId,
                    titleSnapshot = title,
                    contentSnapshot = content,
                    timestamp = System.currentTimeMillis(),
                    changeSummary = summary
                )
            )
        }
    }

    suspend fun restoreVersion(noteId: Long, version: NoteVersionEntity) = withContext(Dispatchers.IO) {
        val currentNote = dao.getNoteByIdDirect(noteId) ?: return@withContext
        val restoredNote = currentNote.copy(
            title = version.titleSnapshot,
            content = version.contentSnapshot,
            updatedAt = System.currentTimeMillis()
        )
        dao.updateNote(restoredNote)
        extractAndSaveBiDirectionalLinks(noteId, version.contentSnapshot)
    }

    // --- Bi-Directional Link Extractor ---
    private suspend fun extractAndSaveBiDirectionalLinks(sourceNoteId: Long, content: String) {
        dao.deleteNoteLinksFromSource(sourceNoteId)
        val linkPattern = Pattern.compile("\\[\\[(.*?)\\]\\]")
        val matcher = linkPattern.matcher(content)

        val targetTitles = mutableSetOf<String>()
        while (matcher.find()) {
            val title = matcher.group(1)?.trim()
            if (!title.isNullOrEmpty()) {
                targetTitles.add(title)
            }
        }

        for (title in targetTitles) {
            val targetNote = dao.getNoteByTitle(title)
            dao.insertNoteLink(
                NoteLinkEntity(
                    sourceNoteId = sourceNoteId,
                    targetNoteTitle = title,
                    targetNoteId = targetNote?.id
                )
            )
        }
    }

    // --- Seed Pre-Populated Initial Data ---
    suspend fun seedDatabaseIfEmpty() = withContext(Dispatchers.IO) {
        val existingNotes = dao.getAllActiveNotes().first()
        if (existingNotes.isNotEmpty()) return@withContext

        // Seed Folders
        val workFolderId = dao.insertFolder(FolderEntity(name = "🚀 Projects & Work", iconName = "briefcase", colorHex = "#7C3AED"))
        val ideasFolderId = dao.insertFolder(FolderEntity(name = "💡 Brainstorming", iconName = "sparkles", colorHex = "#06B6D4"))
        dao.insertFolder(FolderEntity(name = "📚 Personal Knowledge Wiki", iconName = "book", colorHex = "#10B981"))

        // Seed Tags
        val tagArch = dao.insertTag(TagEntity(name = "architecture", colorHex = "#7C3AED"))
        val tagAi = dao.insertTag(TagEntity(name = "ai", colorHex = "#06B6D4"))
        val tagRoadmap = dao.insertTag(TagEntity(name = "roadmap", colorHex = "#10B981"))

        // Seed Note 1
        val note1Id = dao.insertNote(
            NoteEntity(
                title = "Welcome to NexusNote 🪐",
                content = """
                    # Welcome to NexusNote
                    Your hyper-modern **block-based note-taking workspace** featuring live markdown, Notion-style slash commands, bi-directional linking, and an interactive Knowledge Graph.

                    ### 🚀 Key Features
                    - Type `/` anywhere in the editor to open the **Slash Command Menu** (headers, code blocks, checklists, callouts).
                    - Type `[[Note Title]]` to create a **Bi-Directional Link** between notes.
                    - Tap **Cmd + K** (or the search bar) to invoke the **Raycast/Linear Command Palette**.
                    - Tap **Knowledge Graph** in the top bar to visualize your graph connections!

                    ### 📋 Quick Setup
                    - [x] Install NexusNote
                    - [ ] Create my first linked note
                    - [ ] Try Gemini AI Copilot (`Cmd + J` or Copilot button)

                    Check out [[System Architecture]] and [[AI Copilot Features]] for deeper details.
                """.trimIndent(),
                folderId = workFolderId,
                isPinned = true,
                isFavorite = true,
                createdAt = System.currentTimeMillis() - 86400000,
                updatedAt = System.currentTimeMillis()
            )
        )
        dao.insertNoteTagCrossRef(NoteTagCrossRef(note1Id, tagArch))
        dao.insertNoteTagCrossRef(NoteTagCrossRef(note1Id, tagAi))

        // Seed Note 2
        val note2Id = dao.insertNote(
            NoteEntity(
                title = "System Architecture",
                content = """
                    # System Architecture

                    NexusNote is engineered as an offline-first high-performance Android application.

                    > - **UI Layer**: Jetpack Compose with Material 3 Glassmorphism
                    > - **Persistence**: Room SQLite with reactive Kotlin Flow streams
                    > - **AI Engine**: Gemini 3.5 Flash REST API
                    > - **Knowledge Graph**: Custom Compose Canvas force-directed node graph

                    Connected to [[Welcome to NexusNote 🪐]] and [[AI Copilot Features]].
                """.trimIndent(),
                folderId = workFolderId,
                isPinned = false,
                isFavorite = true,
                createdAt = System.currentTimeMillis() - 43200000,
                updatedAt = System.currentTimeMillis() - 3600000
            )
        )
        dao.insertNoteTagCrossRef(NoteTagCrossRef(note2Id, tagArch))
        dao.insertNoteTagCrossRef(NoteTagCrossRef(note2Id, tagRoadmap))

        // Seed Note 3
        val note3Id = dao.insertNote(
            NoteEntity(
                title = "AI Copilot Features",
                content = """
                    # AI Copilot Features

                    NexusNote integrates Gemini 3.5 Flash directly into your text editor.

                    ### ⚡ Copilot Capabilities
                    - **Executive Summary**: Synthesize long notes into actionable points.
                    - **Grammar & Tone Polish**: Rephrase text cleanly without losing intent.
                    - **Brainstorming Assistant**: Generate outline points and task checklists.
                    - **Smart Auto-Tagging**: Recommend multi-color tags based on note context.

                    Back to [[Welcome to NexusNote 🪐]].
                """.trimIndent(),
                folderId = ideasFolderId,
                isPinned = false,
                isFavorite = false,
                createdAt = System.currentTimeMillis() - 21600000,
                updatedAt = System.currentTimeMillis() - 1800000
            )
        )
        dao.insertNoteTagCrossRef(NoteTagCrossRef(note3Id, tagAi))

        // Extract initial links
        extractAndSaveBiDirectionalLinks(note1Id, dao.getNoteByIdDirect(note1Id)?.content ?: "")
        extractAndSaveBiDirectionalLinks(note2Id, dao.getNoteByIdDirect(note2Id)?.content ?: "")
        extractAndSaveBiDirectionalLinks(note3Id, dao.getNoteByIdDirect(note3Id)?.content ?: "")
    }
}
