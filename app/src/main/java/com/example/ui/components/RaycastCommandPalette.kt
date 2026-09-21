package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.data.local.entity.NoteEntity
import com.example.ui.theme.ElectricViolet
import com.example.ui.theme.NeonCyan

data class PaletteAction(
    val title: String,
    val subtitle: String,
    val icon: @Composable () -> Unit,
    val onExecute: () -> Unit
)

@Composable
fun RaycastCommandPalette(
    notes: List<NoteEntity>,
    onSelectNote: (Long) -> Unit,
    onCreateNewNote: () -> Unit,
    onOpenGraph: () -> Unit,
    onOpenAiCopilot: () -> Unit,
    onExportNotes: () -> Unit,
    onToggleTheme: () -> Unit,
    onDismiss: () -> Unit
) {
    var searchQuery by remember { mutableStateOf("") }

    val systemActions = remember {
        listOf(
            PaletteAction(
                title = "Create New Note",
                subtitle = "Start a fresh block-based document",
                icon = { Icon(Icons.Default.AddCircle, contentDescription = null, tint = ElectricViolet) },
                onExecute = { onCreateNewNote(); onDismiss() }
            ),
            PaletteAction(
                title = "Open Knowledge Graph",
                subtitle = "Visualize bi-directional note connections",
                icon = { Icon(Icons.Default.Hub, contentDescription = null, tint = NeonCyan) },
                onExecute = { onOpenGraph(); onDismiss() }
            ),
            PaletteAction(
                title = "Run Gemini AI Copilot",
                subtitle = "Summarize, polish, or ask questions",
                icon = { Icon(Icons.Default.AutoAwesome, contentDescription = null, tint = ElectricViolet) },
                onExecute = { onOpenAiCopilot(); onDismiss() }
            ),
            PaletteAction(
                title = "Export All Notes (Markdown / JSON)",
                subtitle = "Download notes to file storage",
                icon = { Icon(Icons.Default.Download, contentDescription = null, tint = Color(0xFF10B981)) },
                onExecute = { onExportNotes(); onDismiss() }
            ),
            PaletteAction(
                title = "Toggle Dark / Light Theme",
                subtitle = "Switch visual aesthetic",
                icon = { Icon(Icons.Default.DarkMode, contentDescription = null, tint = Color(0xFFF59E0B)) },
                onExecute = { onToggleTheme(); onDismiss() }
            )
        )
    }

    val filteredNotes = remember(searchQuery, notes) {
        if (searchQuery.isBlank()) notes else {
            notes.filter {
                it.title.contains(searchQuery, ignoreCase = true) ||
                it.content.contains(searchQuery, ignoreCase = true)
            }
        }
    }

    val filteredActions = remember(searchQuery) {
        if (searchQuery.isBlank()) systemActions else {
            systemActions.filter {
                it.title.contains(searchQuery, ignoreCase = true) ||
                it.subtitle.contains(searchQuery, ignoreCase = true)
            }
        }
    }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 16.dp)
                .testTag("raycast_command_palette"),
            shape = RoundedCornerShape(20.dp),
            color = MaterialTheme.colorScheme.surface,
            border = androidx.compose.foundation.BorderStroke(1.5.dp, ElectricViolet.copy(alpha = 0.6f)),
            tonalElevation = 12.dp
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                // Command Search Input Box
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant)
                        .padding(horizontal = 12.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(Icons.Default.Search, contentDescription = "Search", tint = ElectricViolet)
                    BasicTextField(
                        value = searchQuery,
                        onValueChange = { searchQuery = it },
                        textStyle = TextStyle(
                            fontSize = 16.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurface
                        ),
                        cursorBrush = SolidColor(ElectricViolet),
                        modifier = Modifier
                            .weight(1f)
                            .testTag("command_palette_input"),
                        decorationBox = { innerTextField ->
                            Box {
                                if (searchQuery.isEmpty()) {
                                    Text(
                                        "Type a command or search notes (Cmd + K)...",
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        fontSize = 15.sp
                                    )
                                }
                                innerTextField()
                            }
                        }
                    )
                    if (searchQuery.isNotEmpty()) {
                        IconButton(
                            onClick = { searchQuery = "" },
                            modifier = Modifier.size(20.dp)
                        ) {
                            Icon(Icons.Default.Close, contentDescription = "Clear", tint = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                LazyColumn(
                    modifier = Modifier.heightIn(max = 380.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    // --- System Actions Section ---
                    if (filteredActions.isNotEmpty()) {
                        item {
                            Text(
                                text = "SYSTEM ACTIONS",
                                style = MaterialTheme.typography.labelSmall,
                                color = ElectricViolet,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                        items(filteredActions) { action ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(10.dp))
                                    .clickable { action.onExecute() }
                                    .padding(horizontal = 12.dp, vertical = 10.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                Box(modifier = Modifier.size(24.dp), contentAlignment = Alignment.Center) {
                                    action.icon()
                                }
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = action.title,
                                        style = MaterialTheme.typography.bodyMedium,
                                        fontWeight = FontWeight.SemiBold,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                    Text(
                                        text = action.subtitle,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                                Text("↵", color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }
                    }

                    // --- Notes Matching Search ---
                    if (filteredNotes.isNotEmpty()) {
                        item {
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "NOTES (${filteredNotes.size})",
                                style = MaterialTheme.typography.labelSmall,
                                color = NeonCyan,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                        items(filteredNotes) { note ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(10.dp))
                                    .clickable {
                                        onSelectNote(note.id)
                                        onDismiss()
                                    }
                                    .padding(horizontal = 12.dp, vertical = 10.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                Icon(Icons.Default.Description, contentDescription = null, tint = NeonCyan)
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = note.title,
                                        style = MaterialTheme.typography.bodyMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                    Text(
                                        text = note.content.take(60).replace("\n", " ") + "...",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
