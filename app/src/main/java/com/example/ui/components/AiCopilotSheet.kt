package com.example.ui.components

import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.remote.GeminiClient
import com.example.ui.theme.ElectricViolet
import com.example.ui.theme.NeonCyan
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AiCopilotSheet(
    noteTitle: String,
    noteContent: String,
    onAppendResult: (String) -> Unit,
    onReplaceContent: (String) -> Unit,
    onDismiss: () -> Unit
) {
    val coroutineScope = rememberCoroutineScope()
    var isLoading by remember { mutableStateOf(false) }
    var aiResultText by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var customPrompt by remember { mutableStateOf("") }
    val clipboardManager = LocalClipboardManager.current
    val context = LocalContext.current

    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    fun runCopilotAction(systemPrompt: String, userText: String) {
        isLoading = true
        errorMessage = null
        aiResultText = ""

        coroutineScope.launch {
            val result = GeminiClient.promptCopilot(systemPrompt, userText)
            isLoading = false
            result.onSuccess { response ->
                aiResultText = response
            }.onFailure { err ->
                errorMessage = err.message ?: "An unknown AI error occurred."
            }
        }
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = MaterialTheme.colorScheme.surface,
        contentColor = MaterialTheme.colorScheme.onSurface,
        shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 12.dp)
                .verticalScroll(rememberScrollState())
                .testTag("ai_copilot_sheet")
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        Icons.Default.AutoAwesome,
                        contentDescription = "Gemini AI",
                        tint = ElectricViolet,
                        modifier = Modifier.size(28.dp)
                    )
                    Column {
                        Text(
                            text = "Nexus Gemini AI Copilot",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "Powered by Gemini 3.5 Flash",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                IconButton(onClick = onDismiss) {
                    Icon(Icons.Default.Close, contentDescription = "Close", tint = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Preset Action Chips
            Text(
                text = "QUICK ACTIONS",
                style = MaterialTheme.typography.labelSmall,
                color = ElectricViolet,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(
                    onClick = {
                        runCopilotAction(
                            systemPrompt = "You are an executive assistant. Provide a clean, bulleted summary of the following note content in markdown.",
                            userText = "Title: $noteTitle\n\nContent:\n$noteContent"
                        )
                    },
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(containerColor = ElectricViolet.copy(alpha = 0.2f), contentColor = ElectricViolet)
                ) {
                    Text("Summarize", fontSize = 12.sp)
                }

                Button(
                    onClick = {
                        runCopilotAction(
                            systemPrompt = "You are a professional editor. Rewrite and polish the following note text for maximum clarity and clean tone in markdown.",
                            userText = noteContent
                        )
                    },
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(containerColor = NeonCyan.copy(alpha = 0.2f), contentColor = NeonCyan)
                ) {
                    Text("Fix & Polish", fontSize = 12.sp)
                }

                Button(
                    onClick = {
                        runCopilotAction(
                            systemPrompt = "You are a creative brainstorming partner. Generate 3 key insights or next action steps for this note in markdown.",
                            userText = "Title: $noteTitle\n\nContent:\n$noteContent"
                        )
                    },
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF10B981).copy(alpha = 0.2f), contentColor = Color(0xFF10B981))
                ) {
                    Text("Expand Ideas", fontSize = 12.sp)
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Custom Prompt Input
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant)
                    .padding(horizontal = 12.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                BasicTextField(
                    value = customPrompt,
                    onValueChange = { customPrompt = it },
                    textStyle = TextStyle(
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.onSurface
                    ),
                    cursorBrush = SolidColor(ElectricViolet),
                    modifier = Modifier
                        .weight(1f)
                        .testTag("ai_copilot_prompt_input"),
                    decorationBox = { innerTextField ->
                        Box {
                            if (customPrompt.isEmpty()) {
                                Text(
                                    "Ask Copilot (e.g., 'Translate to French' or 'Create checklist')...",
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    fontSize = 13.sp
                                )
                            }
                            innerTextField()
                        }
                    }
                )

                IconButton(
                    onClick = {
                        if (customPrompt.isNotBlank()) {
                            runCopilotAction(
                                systemPrompt = "You are Nexus AI Assistant. Answer the user request precisely based on the note context.",
                                userText = "User Request: $customPrompt\n\nNote Title: $noteTitle\nNote Content:\n$noteContent"
                            )
                        }
                    },
                    enabled = customPrompt.isNotBlank() && !isLoading
                ) {
                    Icon(
                        Icons.Default.Send,
                        contentDescription = "Send Prompt",
                        tint = if (customPrompt.isNotBlank()) ElectricViolet else Color.Gray
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Loading state
            if (isLoading) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    CircularProgressIndicator(color = ElectricViolet, modifier = Modifier.size(24.dp))
                    Spacer(modifier = Modifier.width(12.dp))
                    Text("Gemini AI is thinking...", color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }

            // Error Display
            if (errorMessage != null) {
                Surface(
                    color = Color(0xFFEF4444).copy(alpha = 0.15f),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.ErrorOutline, contentDescription = null, tint = Color(0xFFEF4444))
                        Text(text = errorMessage!!, color = Color(0xFFEF4444), fontSize = 13.sp)
                    }
                }
            }

            // AI Result Output Area
            if (aiResultText.isNotEmpty()) {
                Surface(
                    color = MaterialTheme.colorScheme.surfaceVariant,
                    shape = RoundedCornerShape(14.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, ElectricViolet.copy(alpha = 0.4f)),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp)
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "AI GENERATED RESPONSE",
                                style = MaterialTheme.typography.labelSmall,
                                color = ElectricViolet,
                                fontWeight = FontWeight.Bold
                            )

                            Row {
                                IconButton(
                                    onClick = {
                                        clipboardManager.setText(AnnotatedString(aiResultText))
                                        Toast.makeText(context, "Copied to clipboard", Toast.LENGTH_SHORT).show()
                                    },
                                    modifier = Modifier.size(24.dp)
                                ) {
                                    Icon(Icons.Default.ContentCopy, contentDescription = "Copy", tint = NeonCyan, modifier = Modifier.size(16.dp))
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        Text(
                            text = aiResultText,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurface,
                            lineHeight = 20.sp
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            OutlinedButton(
                                onClick = {
                                    onAppendResult("\n\n### ⚡ AI Response\n$aiResultText")
                                    onDismiss()
                                },
                                modifier = Modifier.weight(1f)
                            ) {
                                Text("Append to Note", fontSize = 12.sp)
                            }

                            Button(
                                onClick = {
                                    onReplaceContent(aiResultText)
                                    onDismiss()
                                },
                                modifier = Modifier.weight(1f),
                                colors = ButtonDefaults.buttonColors(containerColor = ElectricViolet)
                            ) {
                                Text("Replace Content", fontSize = 12.sp)
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}
