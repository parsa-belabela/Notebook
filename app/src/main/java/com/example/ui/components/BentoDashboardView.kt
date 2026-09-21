package com.example.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.entity.NoteEntity
import com.example.data.local.entity.TagEntity
import com.example.ui.theme.ElectricViolet
import com.example.ui.theme.NeonCyan

@Composable
fun BentoDashboardView(
    activeNote: NoteEntity?,
    allNotesCount: Int,
    tags: List<TagEntity>,
    isSyncing: Boolean,
    onOpenNote: (Long) -> Unit,
    onOpenGraph: () -> Unit,
    onOpenAiCopilot: () -> Unit,
    onOpenCommandPalette: () -> Unit,
    onSelectTag: (Long) -> Unit,
    onCreateNewNote: () -> Unit
) {
    val scrollState = rememberScrollState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF0B0F17))
            .verticalScroll(scrollState)
            .padding(16.dp)
            .testTag("bento_dashboard_view"),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // --- Quick Command Search Shortcut Box ---
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(20.dp))
                .clickable { onOpenCommandPalette() }
                .testTag("bento_search_bar"),
            color = Color(0xFF131A26),
            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0x1AFFFFFF))
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 14.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Icon(
                        Icons.Default.ElectricBolt,
                        contentDescription = null,
                        tint = NeonCyan,
                        modifier = Modifier.size(18.dp)
                    )
                    Text(
                        text = "Quick Search...",
                        color = Color(0xFF9CA3AF),
                        fontSize = 14.sp
                    )
                }

                Surface(
                    color = Color(0x1AFFFFFF),
                    shape = RoundedCornerShape(6.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0x1AFFFFFF))
                ) {
                    Text(
                        text = "Cmd + K",
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }
            }
        }

        // --- Bento Grid 2x2 Layout ---
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Left Column: Primary Featured Active Note Card (Spans 2 rows visually)
            Surface(
                modifier = Modifier
                    .weight(1.1f)
                    .height(260.dp)
                    .clip(RoundedCornerShape(28.dp))
                    .clickable {
                        activeNote?.let { onOpenNote(it.id) } ?: onCreateNewNote()
                    }
                    .testTag("bento_primary_note_card"),
                color = Color(0xFF1A1F2B),
                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0x26FFFFFF))
            ) {
                Box(modifier = Modifier.fillMaxSize().padding(16.dp)) {
                    // Subtle ambient gradient background glow
                    Box(
                        modifier = Modifier
                            .size(100.dp)
                            .align(Alignment.TopEnd)
                            .offset(x = 20.dp, y = (-20).dp)
                            .clip(CircleShape)
                            .background(ElectricViolet.copy(alpha = 0.15f))
                    )

                    Column(
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Icon(
                                    Icons.Default.Description,
                                    contentDescription = null,
                                    tint = ElectricViolet,
                                    modifier = Modifier.size(16.dp)
                                )
                                Text(
                                    text = "ACTIVE NOTE",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = ElectricViolet,
                                    letterSpacing = 1.sp
                                )
                            }

                            Text(
                                text = activeNote?.title ?: "Create Your First Note",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = Color.White,
                                maxLines = 2
                            )

                            Text(
                                text = activeNote?.content?.take(120)?.replace("\n", " ")
                                    ?: "Click here to create a new block-based Markdown document.",
                                style = MaterialTheme.typography.bodySmall,
                                color = Color(0xFF9CA3AF),
                                fontSize = 12.sp,
                                lineHeight = 16.sp,
                                maxLines = 4
                            )
                        }

                        // Avatar badge indicators at bottom
                        Row(
                            horizontalArrangement = Arrangement.spacedBy((-6).dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(24.dp)
                                    .clip(CircleShape)
                                    .background(NeonCyan)
                                    .border(2.dp, Color(0xFF0B0F17), CircleShape)
                            )
                            Box(
                                modifier = Modifier
                                    .size(24.dp)
                                    .clip(CircleShape)
                                    .background(ElectricViolet)
                                    .border(2.dp, Color(0xFF0B0F17), CircleShape)
                            )
                            Box(
                                modifier = Modifier
                                    .size(24.dp)
                                    .clip(CircleShape)
                                    .background(Color(0xFF10B981))
                                    .border(2.dp, Color(0xFF0B0F17), CircleShape)
                            )
                        }
                    }
                }
            }

            // Right Column: Split Tiles (Knowledge Graph & Metrics)
            Column(
                modifier = Modifier
                    .weight(0.9f)
                    .height(260.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Knowledge Graph Preview Bento Card
                Surface(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(28.dp))
                        .clickable { onOpenGraph() }
                        .testTag("bento_graph_preview_card"),
                    color = Color(0x0FFFFFFF),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0x1AFFFFFF))
                ) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        // Decorative Canvas Nodes
                        Canvas(modifier = Modifier.fillMaxSize()) {
                            drawCircle(color = ElectricViolet.copy(alpha = 0.5f), radius = 6f, center = Offset(size.width * 0.5f, size.height * 0.5f))
                            drawCircle(color = NeonCyan.copy(alpha = 0.5f), radius = 5f, center = Offset(size.width * 0.3f, size.height * 0.35f))
                            drawCircle(color = NeonCyan.copy(alpha = 0.5f), radius = 5f, center = Offset(size.width * 0.7f, size.height * 0.35f))
                            drawLine(
                                color = ElectricViolet.copy(alpha = 0.3f),
                                start = Offset(size.width * 0.5f, size.height * 0.5f),
                                end = Offset(size.width * 0.3f, size.height * 0.35f),
                                strokeWidth = 2f
                            )
                            drawLine(
                                color = ElectricViolet.copy(alpha = 0.3f),
                                start = Offset(size.width * 0.5f, size.height * 0.5f),
                                end = Offset(size.width * 0.7f, size.height * 0.35f),
                                strokeWidth = 2f
                            )
                        }

                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Icon(
                                Icons.Default.Hub,
                                contentDescription = "Knowledge Graph",
                                tint = NeonCyan,
                                modifier = Modifier.size(28.dp)
                            )
                            Text(
                                text = "Graph View",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        }
                    }
                }

                // Total Notes Status Bento Card
                Surface(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(28.dp)),
                    color = NeonCyan.copy(alpha = 0.12f),
                    border = androidx.compose.foundation.BorderStroke(1.dp, NeonCyan.copy(alpha = 0.3f))
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(14.dp),
                        verticalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                Icons.Default.CloudDone,
                                contentDescription = null,
                                tint = NeonCyan,
                                modifier = Modifier.size(18.dp)
                            )
                            Text(
                                text = if (isSyncing) "SYNCING" else "SYNCED",
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold,
                                color = NeonCyan
                            )
                        }

                        Column {
                            Text(
                                text = "$allNotesCount",
                                fontSize = 24.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                            Text(
                                text = "Total Notes",
                                fontSize = 10.sp,
                                color = NeonCyan
                            )
                        }
                    }
                }
            }
        }

        // --- Popular Tags Bento Card (Spans full width) ---
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(28.dp))
                .testTag("bento_popular_tags_card"),
            color = Color(0x0FFFFFFF),
            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0x1AFFFFFF))
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "POPULAR TAGS",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF9CA3AF),
                        letterSpacing = 1.sp
                    )
                    Icon(
                        Icons.Default.MoreHoriz,
                        contentDescription = null,
                        tint = Color(0xFF9CA3AF),
                        modifier = Modifier.size(18.dp)
                    )
                }

                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    val defaultTags = listOf("Strategy", "Design", "Q4-Launch", "Personal")
                    val displayTags = if (tags.isNotEmpty()) tags.map { it.name } else defaultTags

                    displayTags.take(4).forEachIndexed { idx, tagName ->
                        val (chipBg, chipBorder, chipText) = when (idx % 3) {
                            0 -> Triple(ElectricViolet.copy(alpha = 0.2f), ElectricViolet.copy(alpha = 0.4f), ElectricViolet)
                            1 -> Triple(NeonCyan.copy(alpha = 0.2f), NeonCyan.copy(alpha = 0.4f), NeonCyan)
                            else -> Triple(Color(0xFF10B981).copy(alpha = 0.2f), Color(0xFF10B981).copy(alpha = 0.4f), Color(0xFF10B981))
                        }

                        Surface(
                            color = chipBg,
                            shape = RoundedCornerShape(12.dp),
                            border = androidx.compose.foundation.BorderStroke(1.dp, chipBorder),
                            modifier = Modifier.clickable {
                                val tagObj = tags.find { it.name == tagName }
                                tagObj?.let { onSelectTag(it.id) }
                            }
                        ) {
                            Text(
                                text = "#$tagName",
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = chipText
                            )
                        }
                    }
                }
            }
        }

        // --- Quick AI Draft Prompt Banner ---
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(20.dp))
                .background(
                    brush = Brush.horizontalGradient(
                        colors = listOf(ElectricViolet.copy(alpha = 0.25f), Color(0xFF131A26))
                    )
                )
                .border(
                    width = 1.dp,
                    brush = Brush.horizontalGradient(listOf(ElectricViolet, Color.Transparent)),
                    shape = RoundedCornerShape(20.dp)
                )
                .clickable { onOpenAiCopilot() }
                .padding(14.dp)
                .testTag("bento_ai_draft_banner")
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Icon(
                    Icons.Default.AutoAwesome,
                    contentDescription = "Gemini AI",
                    tint = ElectricViolet,
                    modifier = Modifier.size(24.dp)
                )
                Text(
                    text = "Draft or summarize notes with Gemini AI Copilot",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium,
                    color = Color.White,
                    modifier = Modifier.weight(1f)
                )
                Icon(
                    Icons.Default.ChevronRight,
                    contentDescription = null,
                    tint = ElectricViolet
                )
            }
        }
    }
}
