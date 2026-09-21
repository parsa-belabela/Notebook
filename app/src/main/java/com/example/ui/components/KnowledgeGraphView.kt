package com.example.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.OpenInNew
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.entity.NoteEntity
import com.example.data.local.entity.NoteLinkEntity
import com.example.ui.theme.ElectricViolet
import com.example.ui.theme.NeonCyan
import kotlin.math.cos
import kotlin.math.sin

data class GraphNode(
    val noteId: Long,
    val title: String,
    var position: Offset,
    val isPinned: Boolean,
    val color: Color
)

@Composable
fun KnowledgeGraphView(
    notes: List<NoteEntity>,
    links: List<NoteLinkEntity>,
    onSelectNote: (Long) -> Unit,
    onCloseGraph: () -> Unit
) {
    var scale by remember { mutableStateOf(1f) }
    var offset by remember { mutableStateOf(Offset.Zero) }
    var selectedNode by remember { mutableStateOf<GraphNode?>(null) }
    val textMeasurer = rememberTextMeasurer()

    // Generate node layout positions in a circle/orbital network pattern
    val nodes = remember(notes) {
        val nodeMap = mutableListOf<GraphNode>()
        val count = notes.size.coerceAtLeast(1)
        val center = Offset(500f, 600f)
        val radius = 300f

        notes.forEachIndexed { index, note ->
            val angle = (2 * Math.PI * index / count).toFloat()
            val x = center.x + radius * cos(angle)
            val y = center.y + radius * sin(angle)
            val color = if (note.isPinned) ElectricViolet else if (note.isFavorite) NeonCyan else Color(0xFF10B981)
            nodeMap.add(GraphNode(note.id, note.title, Offset(x, y), note.isPinned, color))
        }
        nodeMap
    }

    // Pulse Animation for selected node
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val pulseRadius by infiniteTransition.animateFloat(
        initialValue = 24f,
        targetValue = 38f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulseRadius"
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF070A10))
            .testTag("knowledge_graph_view")
    ) {
        // --- Canvas Knowledge Graph ---
        Canvas(
            modifier = Modifier
                .fillMaxSize()
                .pointerInput(Unit) {
                    detectTransformGestures { _, pan, zoom, _ ->
                        scale = (scale * zoom).coerceIn(0.5f, 3.5f)
                        offset += pan
                    }
                }
        ) {
            val canvasCenter = center

            // 1. Draw Bi-directional Connected Edge Lines
            links.forEach { link ->
                val sourceNode = nodes.find { it.noteId == link.sourceNoteId }
                val targetNode = nodes.find {
                    it.noteId == link.targetNoteId || it.title.equals(link.targetNoteTitle, ignoreCase = true)
                }

                if (sourceNode != null && targetNode != null) {
                    val start = (sourceNode.position * scale) + offset
                    val end = (targetNode.position * scale) + offset

                    drawLine(
                        brush = Brush.linearGradient(
                            colors = listOf(sourceNode.color.copy(alpha = 0.6f), targetNode.color.copy(alpha = 0.6f))
                        ),
                        start = start,
                        end = end,
                        strokeWidth = 2.5f * scale,
                        pathEffect = PathEffect.dashPathEffect(floatArrayOf(15f, 10f), 0f)
                    )
                }
            }

            // 2. Draw Nodes
            nodes.forEach { node ->
                val nodePos = (node.position * scale) + offset

                // Draw pulse ring if selected
                if (selectedNode?.noteId == node.noteId) {
                    drawCircle(
                        color = node.color.copy(alpha = 0.3f),
                        radius = pulseRadius * scale,
                        center = nodePos
                    )
                }

                // Outer aura glow
                drawCircle(
                    color = node.color.copy(alpha = 0.2f),
                    radius = 22f * scale,
                    center = nodePos
                )

                // Inner Solid Node Circle
                drawCircle(
                    color = node.color,
                    radius = 14f * scale,
                    center = nodePos
                )

                // Node Title Label Below
                val textLayoutResult = textMeasurer.measure(
                    text = node.title,
                    style = TextStyle(
                        fontSize = (12 * scale).sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                )
                drawText(
                    textLayoutResult = textLayoutResult,
                    topLeft = Offset(
                        nodePos.x - textLayoutResult.size.width / 2,
                        nodePos.y + 18f * scale
                    )
                )
            }
        }

        // --- Top Bar Controls ---
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .statusBarsPadding()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                color = Color(0xFF131A26).copy(alpha = 0.9f),
                shape = RoundedCornerShape(12.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0x33FFFFFF))
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Box(modifier = Modifier.size(8.dp).clip(CircleShape).background(NeonCyan))
                    Text(
                        text = "Knowledge Graph (${nodes.size} notes, ${links.size} links)",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }
            }

            IconButton(
                onClick = onCloseGraph,
                modifier = Modifier
                    .clip(CircleShape)
                    .background(Color(0xFF131A26))
            ) {
                Icon(Icons.Default.Close, contentDescription = "Close Graph", tint = Color.White)
            }
        }

        // --- Bottom Selected Node Inspector Card ---
        if (selectedNode != null) {
            val node = selectedNode!!
            Surface(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .navigationBarsPadding()
                    .padding(16.dp)
                    .fillMaxWidth(),
                color = Color(0xFF131A26).copy(alpha = 0.95f),
                shape = RoundedCornerShape(16.dp),
                border = androidx.compose.foundation.BorderStroke(1.5.dp, node.color)
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = node.title,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        Text(
                            text = "Connected note in Knowledge Graph",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color.Gray
                        )
                    }

                    Button(
                        onClick = { onSelectNote(node.noteId) },
                        colors = ButtonDefaults.buttonColors(containerColor = node.color)
                    ) {
                        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                            Icon(Icons.Default.OpenInNew, contentDescription = null, modifier = Modifier.size(16.dp))
                            Text("Open Note")
                        }
                    }
                }
            }
        }
    }
}
