package com.example.ui

import android.widget.Toast
import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.ui.components.*
import com.example.ui.theme.ElectricViolet
import com.example.ui.theme.NeonCyan
import com.example.ui.theme.NexusNoteTheme
import com.example.ui.viewmodel.NexusViewModel
import kotlinx.coroutines.launch

enum class AppTab { BOARD, EDITOR, GRAPH }

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NexusApp(viewModel: NexusViewModel) {
    val activeNotes by viewModel.filteredNotes.collectAsStateWithLifecycle()
    val allActiveNotes by viewModel.activeNotes.collectAsStateWithLifecycle()
    val folders by viewModel.folders.collectAsStateWithLifecycle()
    val tags by viewModel.tags.collectAsStateWithLifecycle()
    val links by viewModel.allNoteLinks.collectAsStateWithLifecycle()

    val selectedNote by viewModel.selectedNote.collectAsStateWithLifecycle()
    val selectedFolderId by viewModel.selectedFolderId.collectAsStateWithLifecycle()
    val selectedTagId by viewModel.selectedTagId.collectAsStateWithLifecycle()
    val showArchived by viewModel.showArchived.collectAsStateWithLifecycle()

    val isGraphOpen by viewModel.isKnowledgeGraphOpen.collectAsStateWithLifecycle()
    val isCommandPaletteOpen by viewModel.isCommandPaletteOpen.collectAsStateWithLifecycle()
    val isAiCopilotOpen by viewModel.isAiCopilotOpen.collectAsStateWithLifecycle()
    val isVersionHistoryOpen by viewModel.isVersionHistoryOpen.collectAsStateWithLifecycle()
    val isDarkTheme by viewModel.isDarkTheme.collectAsStateWithLifecycle()
    val isSyncing by viewModel.isSyncing.collectAsStateWithLifecycle()
    val noteVersions by viewModel.currentNoteVersions.collectAsStateWithLifecycle()

    var activeTab by remember { mutableStateOf(AppTab.BOARD) }

    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val coroutineScope = rememberCoroutineScope()
    val clipboardManager = LocalClipboardManager.current
    val context = LocalContext.current

    val pinnedNotes = remember(allActiveNotes) { allActiveNotes.filter { it.isPinned } }

    NexusNoteTheme(darkTheme = isDarkTheme) {
        ModalNavigationDrawer(
            drawerState = drawerState,
            drawerContent = {
                FolderTreeDrawerContent(
                    folders = folders,
                    tags = tags,
                    pinnedNotes = pinnedNotes,
                    selectedFolderId = selectedFolderId,
                    selectedTagId = selectedTagId,
                    showArchived = showArchived,
                    onSelectAllNotes = { viewModel.selectAllNotesFilter() },
                    onSelectFolder = { viewModel.selectFolderFilter(it) },
                    onSelectTag = { viewModel.selectTagFilter(it) },
                    onSelectNote = { noteId ->
                        viewModel.selectNote(noteId)
                        activeTab = AppTab.EDITOR
                    },
                    onToggleArchivedView = { viewModel.showArchived.value = it },
                    onCreateFolder = { viewModel.createFolder(it) },
                    onCreateTag = { viewModel.createTag(it) },
                    onCloseDrawer = { coroutineScope.launch { drawerState.close() } }
                )
            }
        ) {
            Scaffold(
                topBar = {
                    TopAppBar(
                        title = {
                            Column {
                                Text(
                                    text = "ENTERPRISE WORKSPACE",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = ElectricViolet,
                                    letterSpacing = 1.sp
                                )
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Text(
                                        text = "NexusNote",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 18.sp
                                    )
                                    Surface(
                                        color = ElectricViolet.copy(alpha = 0.2f),
                                        shape = RoundedCornerShape(6.dp)
                                    ) {
                                        Text(
                                            text = if (showArchived) "ARCHIVE" else "PRO",
                                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                            fontSize = 9.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = ElectricViolet
                                        )
                                    }
                                }
                            }
                        },
                        navigationIcon = {
                            IconButton(
                                onClick = { coroutineScope.launch { drawerState.open() } },
                                modifier = Modifier.testTag("open_drawer_button")
                            ) {
                                Icon(Icons.Default.Menu, contentDescription = "Open Drawer")
                            }
                        },
                        actions = {
                            // Command Palette Button (Cmd + K)
                            IconButton(
                                onClick = { viewModel.isCommandPaletteOpen.value = true },
                                modifier = Modifier.testTag("top_search_button")
                            ) {
                                Icon(Icons.Default.Search, contentDescription = "Command Palette", tint = ElectricViolet)
                            }

                            // Knowledge Graph View Button
                            IconButton(
                                onClick = { activeTab = if (activeTab == AppTab.GRAPH) AppTab.BOARD else AppTab.GRAPH },
                                modifier = Modifier.testTag("top_graph_button")
                            ) {
                                Icon(
                                    Icons.Default.Hub,
                                    contentDescription = "Knowledge Graph",
                                    tint = if (activeTab == AppTab.GRAPH) NeonCyan else MaterialTheme.colorScheme.onSurface
                                )
                            }

                            // Pin Note
                            selectedNote?.let { note ->
                                IconButton(onClick = { viewModel.togglePinNote(note) }) {
                                    Icon(
                                        imageVector = Icons.Default.PushPin,
                                        contentDescription = "Pin Note",
                                        tint = if (note.isPinned) ElectricViolet else MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }

                            // Toggle Dark Mode
                            IconButton(onClick = { viewModel.isDarkTheme.value = !isDarkTheme }) {
                                Icon(
                                    imageVector = if (isDarkTheme) Icons.Default.LightMode else Icons.Default.DarkMode,
                                    contentDescription = "Toggle Theme"
                                )
                            }
                        },
                        colors = TopAppBarDefaults.topAppBarColors(
                            containerColor = MaterialTheme.colorScheme.background,
                            titleContentColor = MaterialTheme.colorScheme.onBackground
                        )
                    )
                },
                bottomBar = {
                    NavigationBar(
                        containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.95f),
                        tonalElevation = 8.dp
                    ) {
                        NavigationBarItem(
                            selected = activeTab == AppTab.BOARD,
                            onClick = { activeTab = AppTab.BOARD },
                            icon = { Icon(Icons.Default.GridView, contentDescription = "Board") },
                            label = { Text("Board", fontSize = 11.sp) },
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = ElectricViolet,
                                selectedTextColor = ElectricViolet,
                                indicatorColor = ElectricViolet.copy(alpha = 0.2f)
                            )
                        )
                        NavigationBarItem(
                            selected = activeTab == AppTab.EDITOR,
                            onClick = { activeTab = AppTab.EDITOR },
                            icon = { Icon(Icons.Default.Description, contentDescription = "Editor") },
                            label = { Text("Editor", fontSize = 11.sp) },
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = ElectricViolet,
                                selectedTextColor = ElectricViolet,
                                indicatorColor = ElectricViolet.copy(alpha = 0.2f)
                            )
                        )
                        NavigationBarItem(
                            selected = activeTab == AppTab.GRAPH,
                            onClick = { activeTab = AppTab.GRAPH },
                            icon = { Icon(Icons.Default.AccountTree, contentDescription = "Graph") },
                            label = { Text("Graph", fontSize = 11.sp) },
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = NeonCyan,
                                selectedTextColor = NeonCyan,
                                indicatorColor = NeonCyan.copy(alpha = 0.2f)
                            )
                        )
                    }
                },
                floatingActionButton = {
                    FloatingActionButton(
                        onClick = {
                            viewModel.createNewNote()
                            activeTab = AppTab.EDITOR
                        },
                        containerColor = ElectricViolet,
                        contentColor = Color.White,
                        modifier = Modifier.testTag("create_note_fab")
                    ) {
                        Icon(Icons.Default.Add, contentDescription = "New Note")
                    }
                }
            ) { paddingValues ->
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues)
                        .background(MaterialTheme.colorScheme.background)
                ) {
                    when (activeTab) {
                        AppTab.BOARD -> {
                            BentoDashboardView(
                                activeNote = selectedNote,
                                allNotesCount = allActiveNotes.size,
                                tags = tags,
                                isSyncing = isSyncing,
                                onOpenNote = { noteId ->
                                    viewModel.selectNote(noteId)
                                    activeTab = AppTab.EDITOR
                                },
                                onOpenGraph = { activeTab = AppTab.GRAPH },
                                onOpenAiCopilot = { viewModel.isAiCopilotOpen.value = true },
                                onOpenCommandPalette = { viewModel.isCommandPaletteOpen.value = true },
                                onSelectTag = { tagId ->
                                    viewModel.selectTagFilter(tagId)
                                    activeTab = AppTab.EDITOR
                                },
                                onCreateNewNote = {
                                    viewModel.createNewNote()
                                    activeTab = AppTab.EDITOR
                                }
                            )
                        }

                        AppTab.GRAPH -> {
                            // --- Knowledge Graph Canvas Screen ---
                            KnowledgeGraphView(
                                notes = allActiveNotes,
                                links = links,
                                onSelectNote = { noteId ->
                                    viewModel.selectNote(noteId)
                                    activeTab = AppTab.EDITOR
                                },
                                onCloseGraph = { activeTab = AppTab.BOARD }
                            )
                        }

                        AppTab.EDITOR -> {
                            Column(modifier = Modifier.fillMaxSize()) {
                                // --- Active Note Tabs Bar ---
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .horizontalScroll(rememberScrollState())
                                        .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                                        .padding(horizontal = 8.dp, vertical = 6.dp),
                                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    activeNotes.take(6).forEach { note ->
                                        val isSelected = selectedNote?.id == note.id
                                        Surface(
                                            color = if (isSelected) ElectricViolet.copy(alpha = 0.25f) else MaterialTheme.colorScheme.surface,
                                            shape = RoundedCornerShape(8.dp),
                                            border = androidx.compose.foundation.BorderStroke(
                                                1.dp,
                                                if (isSelected) ElectricViolet else Color.Transparent
                                            ),
                                            modifier = Modifier.clickable { viewModel.selectNote(note.id) }
                                        ) {
                                            Row(
                                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                                verticalAlignment = Alignment.CenterVertically,
                                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                                            ) {
                                                Icon(
                                                    Icons.Default.Description,
                                                    contentDescription = null,
                                                    tint = if (isSelected) ElectricViolet else Color.Gray,
                                                    modifier = Modifier.size(14.dp)
                                                )
                                                Text(
                                                    text = note.title.ifEmpty { "Untitled" },
                                                    fontSize = 12.sp,
                                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                                    color = if (isSelected) ElectricViolet else MaterialTheme.colorScheme.onSurface
                                                )
                                            }
                                        }
                                    }

                                    // New Tab Button
                                    IconButton(
                                        onClick = { viewModel.createNewNote() },
                                        modifier = Modifier.size(28.dp)
                                    ) {
                                        Icon(Icons.Default.Add, contentDescription = "New Tab", tint = ElectricViolet)
                                    }
                                }

                                // --- Editor Workspace ---
                                if (selectedNote != null) {
                                    val currentNote = selectedNote!!
                                    SlashBlockEditor(
                                        title = currentNote.title,
                                        content = currentNote.content,
                                        isSyncing = isSyncing,
                                        onTitleChange = { viewModel.updateNoteTitle(it) },
                                        onContentChange = { viewModel.updateNoteContent(it) },
                                        onLinkClick = { linkTitle -> viewModel.selectNoteByTitle(linkTitle) },
                                        onOpenAiCopilot = { viewModel.isAiCopilotOpen.value = true },
                                        onOpenVersionHistory = { viewModel.isVersionHistoryOpen.value = true }
                                    )
                                } else {
                                    EmptyNotePlaceholder(onCreateNewNote = { viewModel.createNewNote() })
                                }
                            }
                        }
                    }
                }
            }
        }

        // --- Modals & Sheets ---
        if (isCommandPaletteOpen) {
            RaycastCommandPalette(
                notes = allActiveNotes,
                onSelectNote = { viewModel.selectNote(it) },
                onCreateNewNote = { viewModel.createNewNote() },
                onOpenGraph = { viewModel.isKnowledgeGraphOpen.value = true },
                onOpenAiCopilot = { viewModel.isAiCopilotOpen.value = true },
                onExportNotes = {
                    val exportText = viewModel.exportAllNotesAsMarkdown()
                    clipboardManager.setText(AnnotatedString(exportText))
                    Toast.makeText(context, "Exported all notes to clipboard!", Toast.LENGTH_LONG).show()
                },
                onToggleTheme = { viewModel.isDarkTheme.value = !isDarkTheme },
                onDismiss = { viewModel.isCommandPaletteOpen.value = false }
            )
        }

        if (isAiCopilotOpen && selectedNote != null) {
            val note = selectedNote!!
            AiCopilotSheet(
                noteTitle = note.title,
                noteContent = note.content,
                onAppendResult = { appendedText -> viewModel.updateNoteContent(note.content + appendedText) },
                onReplaceContent = { newContent -> viewModel.updateNoteContent(newContent) },
                onDismiss = { viewModel.isAiCopilotOpen.value = false }
            )
        }

        if (isVersionHistoryOpen) {
            VersionHistoryDialog(
                versions = noteVersions,
                onRestoreVersion = { version -> viewModel.restoreVersion(version) },
                onDismiss = { viewModel.isVersionHistoryOpen.value = false }
            )
        }
    }
}

@Composable
fun EmptyNotePlaceholder(onCreateNewNote: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Icon(
                Icons.Default.NoteAdd,
                contentDescription = null,
                tint = ElectricViolet,
                modifier = Modifier.size(64.dp)
            )
            Text(
                text = "No Note Selected",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground
            )
            Text(
                text = "Create a new block-based note or select an existing one from the sidebar drawer.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Button(
                onClick = onCreateNewNote,
                colors = ButtonDefaults.buttonColors(containerColor = ElectricViolet)
            ) {
                Text("Create New Note")
            }
        }
    }
}
