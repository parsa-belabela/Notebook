package com.example.ui.viewmodel

import android.app.Application
import android.widget.Toast
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.NexusDatabase
import com.example.data.local.entity.FolderEntity
import com.example.data.local.entity.NoteEntity
import com.example.data.local.entity.NoteLinkEntity
import com.example.data.local.entity.NoteVersionEntity
import com.example.data.local.entity.TagEntity
import com.example.repository.NexusRepository
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class NexusViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: NexusRepository

    val activeNotes: StateFlow<List<NoteEntity>>
    val archivedNotes: StateFlow<List<NoteEntity>>
    val folders: StateFlow<List<FolderEntity>>
    val tags: StateFlow<List<TagEntity>>
    val allNoteLinks: StateFlow<List<NoteLinkEntity>>

    val selectedNote = MutableStateFlow<NoteEntity?>(null)
    val selectedFolderId = MutableStateFlow<Long?>(null)
    val selectedTagId = MutableStateFlow<Long?>(null)
    val showArchived = MutableStateFlow(false)

    val isKnowledgeGraphOpen = MutableStateFlow(false)
    val isCommandPaletteOpen = MutableStateFlow(false)
    val isAiCopilotOpen = MutableStateFlow(false)
    val isVersionHistoryOpen = MutableStateFlow(false)
    val isDarkTheme = MutableStateFlow(true)
    val isSyncing = MutableStateFlow(false)

    val currentNoteVersions = MutableStateFlow<List<NoteVersionEntity>>(emptyList())

    private var autoSaveJob: Job? = null

    init {
        val dao = NexusDatabase.getDatabase(application).noteDao()
        repository = NexusRepository(dao)

        viewModelScope.launch {
            repository.seedDatabaseIfEmpty()
        }

        activeNotes = repository.activeNotes.stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5000),
            emptyList()
        )

        archivedNotes = repository.archivedNotes.stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5000),
            emptyList()
        )

        folders = repository.folders.stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5000),
            emptyList()
        )

        tags = repository.tags.stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5000),
            emptyList()
        )

        allNoteLinks = repository.allNoteLinks.stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5000),
            emptyList()
        )

        // Default open the first pinned/active note once loaded
        viewModelScope.launch {
            activeNotes.collect { notes ->
                if (selectedNote.value == null && notes.isNotEmpty()) {
                    selectedNote.value = notes.first()
                    observeSelectedNoteVersions(notes.first().id)
                }
            }
        }
    }

    // --- Filtered Notes Stream ---
    val filteredNotes: StateFlow<List<NoteEntity>> = combine(
        activeNotes,
        archivedNotes,
        selectedFolderId,
        selectedTagId,
        showArchived
    ) { active, archived, folderId, tagId, isArchived ->
        val source = if (isArchived) archived else active
        source.filter { note ->
            val matchesFolder = folderId == null || note.folderId == folderId
            matchesFolder
        }
    }.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5000),
        emptyList()
    )

    fun selectNote(noteId: Long) {
        viewModelScope.launch {
            repository.getNoteById(noteId).first()?.let { note ->
                selectedNote.value = note
                observeSelectedNoteVersions(note.id)
            }
        }
    }

    fun selectNoteByTitle(title: String) {
        viewModelScope.launch {
            val note = activeNotes.value.find { it.title.equals(title, ignoreCase = true) }
            if (note != null) {
                selectedNote.value = note
                observeSelectedNoteVersions(note.id)
            } else {
                // If linked note doesn't exist yet, auto-create it!
                val newId = repository.createNewNote(title = title)
                repository.getNoteById(newId).first()?.let { newNote ->
                    selectedNote.value = newNote
                    observeSelectedNoteVersions(newNote.id)
                }
            }
        }
    }

    fun createNewNote() {
        viewModelScope.launch {
            val folderId = selectedFolderId.value
            val id = repository.createNewNote(title = "Untitled Note", folderId = folderId)
            repository.getNoteById(id).first()?.let { note ->
                selectedNote.value = note
                observeSelectedNoteVersions(note.id)
            }
        }
    }

    // --- Debounced Live Auto-Save ---
    fun updateNoteTitle(newTitle: String) {
        val current = selectedNote.value ?: return
        val updated = current.copy(title = newTitle)
        selectedNote.value = updated
        triggerDebouncedAutoSave(updated)
    }

    fun updateNoteContent(newContent: String) {
        val current = selectedNote.value ?: return
        val updated = current.copy(content = newContent)
        selectedNote.value = updated
        triggerDebouncedAutoSave(updated)
    }

    private fun triggerDebouncedAutoSave(note: NoteEntity) {
        autoSaveJob?.cancel()
        isSyncing.value = true
        autoSaveJob = viewModelScope.launch {
            delay(600) // 600ms debounce
            repository.saveNote(note)
            isSyncing.value = false
        }
    }

    fun togglePinNote(note: NoteEntity) {
        viewModelScope.launch {
            repository.togglePinNote(note)
        }
    }

    fun toggleFavoriteNote(note: NoteEntity) {
        viewModelScope.launch {
            repository.toggleFavoriteNote(note)
        }
    }

    fun deleteCurrentNote() {
        val current = selectedNote.value ?: return
        viewModelScope.launch {
            repository.deleteNote(current.id)
            selectedNote.value = activeNotes.value.firstOrNull { it.id != current.id }
        }
    }

    fun archiveCurrentNote() {
        val current = selectedNote.value ?: return
        viewModelScope.launch {
            repository.archiveNote(current)
            selectedNote.value = activeNotes.value.firstOrNull { it.id != current.id }
        }
    }

    // --- Folders & Tags ---
    fun createFolder(name: String) {
        viewModelScope.launch {
            repository.createFolder(name)
        }
    }

    fun createTag(name: String) {
        viewModelScope.launch {
            repository.createTag(name)
        }
    }

    fun selectFolderFilter(folderId: Long?) {
        showArchived.value = false
        selectedTagId.value = null
        selectedFolderId.value = folderId
    }

    fun selectTagFilter(tagId: Long?) {
        showArchived.value = false
        selectedFolderId.value = null
        selectedTagId.value = tagId
    }

    fun selectAllNotesFilter() {
        showArchived.value = false
        selectedFolderId.value = null
        selectedTagId.value = null
    }

    // --- Version History ---
    private fun observeSelectedNoteVersions(noteId: Long) {
        viewModelScope.launch {
            repository.getNoteVersions(noteId).collect { versions ->
                currentNoteVersions.value = versions
            }
        }
    }

    fun restoreVersion(version: NoteVersionEntity) {
        val current = selectedNote.value ?: return
        viewModelScope.launch {
            repository.restoreVersion(current.id, version)
            repository.getNoteById(current.id).first()?.let { restored ->
                selectedNote.value = restored
            }
        }
    }

    // --- Export Notes ---
    fun exportAllNotesAsMarkdown(): String {
        val builder = StringBuilder()
        activeNotes.value.forEach { note ->
            builder.append("========================================\n")
            builder.append("TITLE: ").append(note.title).append("\n")
            builder.append("CREATED: ").append(note.createdAt).append("\n")
            builder.append("========================================\n\n")
            builder.append(note.content).append("\n\n\n")
        }
        return builder.toString()
    }
}
