package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.entity.FolderEntity
import com.example.data.local.entity.NoteEntity
import com.example.data.local.entity.TagEntity
import com.example.ui.theme.ElectricViolet
import com.example.ui.theme.NeonCyan

@Composable
fun FolderTreeDrawerContent(
    folders: List<FolderEntity>,
    tags: List<TagEntity>,
    pinnedNotes: List<NoteEntity>,
    selectedFolderId: Long?,
    selectedTagId: Long?,
    showArchived: Boolean,
    onSelectAllNotes: () -> Unit,
    onSelectFolder: (Long?) -> Unit,
    onSelectTag: (Long?) -> Unit,
    onSelectNote: (Long) -> Unit,
    onToggleArchivedView: (Boolean) -> Unit,
    onCreateFolder: (String) -> Unit,
    onCreateTag: (String) -> Unit,
    onCloseDrawer: () -> Unit
) {
    var showNewFolderDialog by remember { mutableStateOf(false) }
    var showNewTagDialog by remember { mutableStateOf(false) }
    var folderNameInput by remember { mutableStateOf("") }
    var tagNameInput by remember { mutableStateOf("") }

    ModalDrawerSheet(
        drawerContainerColor = MaterialTheme.colorScheme.surface,
        drawerContentColor = MaterialTheme.colorScheme.onSurface,
        modifier = Modifier
            .width(300.dp)
            .testTag("folder_tree_drawer")
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            // Header Logo
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(ElectricViolet),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Default.Description, contentDescription = null, tint = Color.White)
                }
                Column {
                    Text(
                        text = "NexusNote",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "Knowledge Workspace",
                        style = MaterialTheme.typography.labelSmall,
                        color = NeonCyan
                    )
                }
            }

            Divider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))

            LazyColumn(
                modifier = Modifier
                    .weight(1f)
                    .padding(vertical = 12.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                // Navigation item: All Notes
                item {
                    NavigationDrawerItem(
                        icon = { Icon(Icons.Default.Notes, contentDescription = null, tint = ElectricViolet) },
                        label = { Text("All Notes", fontWeight = FontWeight.SemiBold) },
                        selected = selectedFolderId == null && selectedTagId == null && !showArchived,
                        onClick = {
                            onSelectAllNotes()
                            onCloseDrawer()
                        },
                        colors = NavigationDrawerItemDefaults.colors(
                            selectedContainerColor = ElectricViolet.copy(alpha = 0.15f),
                            selectedIconColor = ElectricViolet,
                            selectedTextColor = ElectricViolet
                        )
                    )
                }

                // Pinned Notes Quick Hero Section
                if (pinnedNotes.isNotEmpty()) {
                    item {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "PINNED NOTES",
                            style = MaterialTheme.typography.labelSmall,
                            color = ElectricViolet,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp)
                        )
                    }

                    items(pinnedNotes) { note ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                                .clickable {
                                    onSelectNote(note.id)
                                    onCloseDrawer()
                                }
                                .padding(horizontal = 16.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(Icons.Default.PushPin, contentDescription = null, tint = ElectricViolet, modifier = Modifier.size(14.dp))
                            Text(
                                text = note.title,
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurface,
                                maxLines = 1
                            )
                        }
                    }
                }

                // Folders Section
                item {
                    Spacer(modifier = Modifier.height(12.dp))
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 12.dp, vertical = 4.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "FOLDERS",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontWeight = FontWeight.Bold
                        )
                        IconButton(
                            onClick = { showNewFolderDialog = true },
                            modifier = Modifier.size(20.dp)
                        ) {
                            Icon(Icons.Default.Add, contentDescription = "Add Folder", tint = ElectricViolet)
                        }
                    }
                }

                items(folders) { folder ->
                    val isSelected = selectedFolderId == folder.id && !showArchived
                    NavigationDrawerItem(
                        icon = { Icon(Icons.Default.Folder, contentDescription = null, tint = NeonCyan) },
                        label = { Text(folder.name, fontSize = 14.sp) },
                        selected = isSelected,
                        onClick = {
                            onSelectFolder(folder.id)
                            onCloseDrawer()
                        },
                        colors = NavigationDrawerItemDefaults.colors(
                            selectedContainerColor = NeonCyan.copy(alpha = 0.15f),
                            selectedIconColor = NeonCyan,
                            selectedTextColor = NeonCyan
                        )
                    )
                }

                // Tags Section
                item {
                    Spacer(modifier = Modifier.height(12.dp))
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 12.dp, vertical = 4.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "TAGS",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontWeight = FontWeight.Bold
                        )
                        IconButton(
                            onClick = { showNewTagDialog = true },
                            modifier = Modifier.size(20.dp)
                        ) {
                            Icon(Icons.Default.Add, contentDescription = "Add Tag", tint = NeonCyan)
                        }
                    }
                }

                items(tags) { tag ->
                    val isSelected = selectedTagId == tag.id && !showArchived
                    NavigationDrawerItem(
                        icon = { Icon(Icons.Default.Label, contentDescription = null, tint = Color(0xFF10B981)) },
                        label = { Text("#${tag.name}", fontSize = 14.sp) },
                        selected = isSelected,
                        onClick = {
                            onSelectTag(tag.id)
                            onCloseDrawer()
                        },
                        colors = NavigationDrawerItemDefaults.colors(
                            selectedContainerColor = Color(0xFF10B981).copy(alpha = 0.15f),
                            selectedIconColor = Color(0xFF10B981),
                            selectedTextColor = Color(0xFF10B981)
                        )
                    )
                }

                // Archive Section
                item {
                    Spacer(modifier = Modifier.height(12.dp))
                    NavigationDrawerItem(
                        icon = { Icon(Icons.Default.Archive, contentDescription = null, tint = Color.Gray) },
                        label = { Text("Archive", fontSize = 14.sp) },
                        selected = showArchived,
                        onClick = {
                            onToggleArchivedView(true)
                            onCloseDrawer()
                        }
                    )
                }
            }
        }
    }

    // New Folder Dialog
    if (showNewFolderDialog) {
        AlertDialog(
            onDismissRequest = { showNewFolderDialog = false },
            title = { Text("Create Folder") },
            text = {
                OutlinedTextField(
                    value = folderNameInput,
                    onValueChange = { folderNameInput = it },
                    label = { Text("Folder Name") }
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (folderNameInput.isNotBlank()) {
                            onCreateFolder(folderNameInput)
                            folderNameInput = ""
                            showNewFolderDialog = false
                        }
                    }
                ) { Text("Create") }
            },
            dismissButton = {
                TextButton(onClick = { showNewFolderDialog = false }) { Text("Cancel") }
            }
        )
    }

    // New Tag Dialog
    if (showNewTagDialog) {
        AlertDialog(
            onDismissRequest = { showNewTagDialog = false },
            title = { Text("Create Tag") },
            text = {
                OutlinedTextField(
                    value = tagNameInput,
                    onValueChange = { tagNameInput = it },
                    label = { Text("Tag Name") }
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (tagNameInput.isNotBlank()) {
                            onCreateTag(tagNameInput)
                            tagNameInput = ""
                            showNewTagDialog = false
                        }
                    }
                ) { Text("Create") }
            },
            dismissButton = {
                TextButton(onClick = { showNewTagDialog = false }) { Text("Cancel") }
            }
        )
    }
}
