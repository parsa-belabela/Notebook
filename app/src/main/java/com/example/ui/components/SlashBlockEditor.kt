package com.example.ui.components

import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.FormatListBulleted
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
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.ElectricViolet
import com.example.ui.theme.NeonCyan
import java.util.regex.Pattern

data class SlashCommand(
    val title: String,
    val description: String,
    val prefix: String,
    val icon: @Composable () -> Unit
)

@Composable
fun SlashBlockEditor(
    title: String,
    content: String,
    isSyncing: Boolean,
    onTitleChange: (String) -> Unit,
    onContentChange: (String) -> Unit,
    onLinkClick: (String) -> Unit,
    onOpenAiCopilot: () -> Unit,
    onOpenVersionHistory: () -> Unit
) {
    var isPreviewMode by remember { mutableStateOf(false) }
    var showSlashMenu by remember { mutableStateOf(false) }
    val scrollState = rememberScrollState()
    val clipboardManager = LocalClipboardManager.current
    val context = LocalContext.current

    val slashCommands = remember {
        listOf(
            SlashCommand("Heading 1", "Large section heading", "# ") {
                Text("H1", fontWeight = FontWeight.Bold, color = ElectricViolet)
            },
            SlashCommand("Heading 2", "Medium subsection heading", "## ") {
                Text("H2", fontWeight = FontWeight.Bold, color = ElectricViolet)
            },
            SlashCommand("Heading 3", "Small section title", "### ") {
                Text("H3", fontWeight = FontWeight.Bold, color = ElectricViolet)
            },
            SlashCommand("Checklist Item", "Task item with checkbox", "- [ ] ") {
                Icon(Icons.Default.CheckBox, contentDescription = "Todo", tint = NeonCyan)
            },
            SlashCommand("Bullet List", "Unordered bullet point", "- ") {
                Icon(Icons.AutoMirrored.Filled.FormatListBulleted, contentDescription = "Bullet", tint = ElectricViolet)
            },
            SlashCommand("Callout Quote", "Highlighted quote block", "> ") {
                Icon(Icons.Default.FormatQuote, contentDescription = "Quote", tint = Color(0xFFF59E0B))
            },
            SlashCommand("Code Block", "Syntax highlighted code snippet", "```kotlin\n// Code snippet\n```") {
                Icon(Icons.Default.Code, contentDescription = "Code", tint = NeonCyan)
            },
            SlashCommand("Divider Line", "Horizontal separator rule", "\n---\n") {
                Icon(Icons.Default.HorizontalRule, contentDescription = "Divider", tint = Color.Gray)
            },
            SlashCommand("Table", "Markdown table grid", "| Column 1 | Column 2 |\n| --- | --- |\n| Data 1 | Data 2 |\n") {
                Icon(Icons.Default.TableChart, contentDescription = "Table", tint = ElectricViolet)
            },
            SlashCommand("Bi-Directional Link", "Link to another note [[Title]]", "[[") {
                Icon(Icons.Default.Link, contentDescription = "Link", tint = NeonCyan)
            }
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // --- Editor Top Status Bar ---
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            // Auto-save Sync Status Indicator
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .clip(CircleShape)
                        .background(if (isSyncing) Color(0xFFF59E0B) else Color(0xFF10B981))
                )
                Text(
                    text = if (isSyncing) "Syncing..." else "Saved",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            // Formatting / Action Controls
            Row(
                horizontalArrangement = Arrangement.spacedBy(4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // AI Copilot Trigger
                IconButton(
                    onClick = onOpenAiCopilot,
                    modifier = Modifier.testTag("editor_ai_button")
                ) {
                    Icon(
                        Icons.Default.AutoAwesome,
                        contentDescription = "AI Copilot",
                        tint = ElectricViolet
                    )
                }

                // Version History
                IconButton(
                    onClick = onOpenVersionHistory,
                    modifier = Modifier.testTag("editor_history_button")
                ) {
                    Icon(
                        Icons.Default.History,
                        contentDescription = "Version History",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                // Live Preview Toggle
                IconButton(
                    onClick = { isPreviewMode = !isPreviewMode },
                    modifier = Modifier.testTag("editor_preview_toggle")
                ) {
                    Icon(
                        imageVector = if (isPreviewMode) Icons.Default.Edit else Icons.Default.Visibility,
                        contentDescription = "Toggle Preview Mode",
                        tint = if (isPreviewMode) NeonCyan else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        // --- Toolbar Row for Quick Insertion ---
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 4.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            AssistChip(
                onClick = { showSlashMenu = !showSlashMenu },
                label = { Text("/ Commands", fontSize = 12.sp) },
                leadingIcon = { Icon(Icons.Default.Terminal, contentDescription = null, modifier = Modifier.size(16.dp)) },
                colors = AssistChipDefaults.assistChipColors(
                    containerColor = ElectricViolet.copy(alpha = 0.15f),
                    labelColor = ElectricViolet
                )
            )

            AssistChip(
                onClick = { onContentChange(content + "\n# ") },
                label = { Text("H1", fontSize = 12.sp, fontWeight = FontWeight.Bold) }
            )

            AssistChip(
                onClick = { onContentChange(content + "\n## ") },
                label = { Text("H2", fontSize = 12.sp, fontWeight = FontWeight.Bold) }
            )

            AssistChip(
                onClick = { onContentChange(content + "\n- [ ] ") },
                label = { Text("Todo", fontSize = 12.sp) },
                leadingIcon = { Icon(Icons.Default.CheckBox, contentDescription = null, modifier = Modifier.size(16.dp)) }
            )

            AssistChip(
                onClick = { onContentChange(content + "\n- ") },
                label = { Text("Bullet", fontSize = 12.sp) },
                leadingIcon = { Icon(Icons.AutoMirrored.Filled.FormatListBulleted, contentDescription = null, modifier = Modifier.size(16.dp)) }
            )

            AssistChip(
                onClick = { onContentChange(content + "\n> ") },
                label = { Text("Quote", fontSize = 12.sp) },
                leadingIcon = { Icon(Icons.Default.FormatQuote, contentDescription = null, modifier = Modifier.size(16.dp)) }
            )

            AssistChip(
                onClick = { onContentChange(content + "\n```kotlin\n\n```") },
                label = { Text("Code", fontSize = 12.sp) },
                leadingIcon = { Icon(Icons.Default.Code, contentDescription = null, modifier = Modifier.size(16.dp)) }
            )

            AssistChip(
                onClick = { onContentChange(content + " [[") },
                label = { Text("Link", fontSize = 12.sp) },
                leadingIcon = { Icon(Icons.Default.Link, contentDescription = null, modifier = Modifier.size(16.dp)) }
            )
        }

        // --- Slash Command Popup Dropdown ---
        AnimatedVisibility(visible = showSlashMenu) {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 4.dp),
                shape = RoundedCornerShape(12.dp),
                color = MaterialTheme.colorScheme.surfaceVariant,
                tonalElevation = 8.dp
            ) {
                Column(
                    modifier = Modifier
                        .heightIn(max = 240.dp)
                        .verticalScroll(rememberScrollState())
                        .padding(8.dp)
                ) {
                    Text(
                        text = "INSERT BLOCK COMMAND",
                        style = MaterialTheme.typography.labelSmall,
                        color = ElectricViolet,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                    slashCommands.forEach { cmd ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                                .clickable {
                                    onContentChange(content + cmd.prefix)
                                    showSlashMenu = false
                                }
                                .padding(horizontal = 12.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Box(modifier = Modifier.size(24.dp), contentAlignment = Alignment.Center) {
                                cmd.icon()
                            }
                            Column {
                                Text(
                                    text = cmd.title,
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.SemiBold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = cmd.description,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
            }
        }

        Divider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f), thickness = 1.dp)

        // --- Main Note Title & Content Editor / Live Renderer Area ---
        Column(
            modifier = Modifier
                .fillMaxSize()
                .weight(1f)
                .verticalScroll(scrollState)
                .padding(20.dp)
        ) {
            // Title Input
            BasicTextField(
                value = title,
                onValueChange = onTitleChange,
                textStyle = TextStyle(
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground
                ),
                cursorBrush = SolidColor(ElectricViolet),
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("note_title_input"),
                decorationBox = { innerTextField ->
                    Box {
                        if (title.isEmpty()) {
                            Text(
                                "Untitled Note...",
                                fontSize = 28.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                            )
                        }
                        innerTextField()
                    }
                }
            )

            Spacer(modifier = Modifier.height(16.dp))

            if (!isPreviewMode) {
                // --- Raw Markdown Editor ---
                BasicTextField(
                    value = content,
                    onValueChange = { newText ->
                        onContentChange(newText)
                        // Trigger slash popup if ends with '/' at line start
                        if (newText.endsWith("\n/") || newText == "/") {
                            showSlashMenu = true
                        }
                    },
                    textStyle = TextStyle(
                        fontSize = 16.sp,
                        lineHeight = 24.sp,
                        color = MaterialTheme.colorScheme.onBackground,
                        fontFamily = FontFamily.Default
                    ),
                    cursorBrush = SolidColor(NeonCyan),
                    modifier = Modifier
                        .fillMaxWidth()
                        .defaultMinSize(minHeight = 350.dp)
                        .testTag("note_content_input"),
                    decorationBox = { innerTextField ->
                        Box {
                            if (content.isEmpty()) {
                                Text(
                                    "Type '/' for slash commands or start writing...",
                                    fontSize = 16.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                                )
                            }
                            innerTextField()
                        }
                    }
                )
            } else {
                // --- Live Rendered Markdown View ---
                RenderedMarkdownView(
                    content = content,
                    onContentChange = onContentChange,
                    onLinkClick = onLinkClick
                )
            }
        }
    }
}

@Composable
fun RenderedMarkdownView(
    content: String,
    onContentChange: (String) -> Unit,
    onLinkClick: (String) -> Unit
) {
    val lines = remember(content) { content.lines() }
    val clipboardManager = LocalClipboardManager.current
    val context = LocalContext.current

    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        var inCodeBlock = false
        var codeBlockContent = StringBuilder()
        var codeLanguage = ""

        lines.forEachIndexed { index, line ->
            val trimmed = line.trim()

            if (trimmed.startsWith("```")) {
                if (!inCodeBlock) {
                    inCodeBlock = true
                    codeLanguage = trimmed.removePrefix("```").trim()
                    codeBlockContent = StringBuilder()
                } else {
                    // Close code block
                    inCodeBlock = false
                    val codeStr = codeBlockContent.toString()
                    CodeBlockCard(
                        code = codeStr,
                        language = if (codeLanguage.isEmpty()) "code" else codeLanguage,
                        onCopy = {
                            clipboardManager.setText(AnnotatedString(codeStr))
                            Toast.makeText(context, "Code copied to clipboard!", Toast.LENGTH_SHORT).show()
                        }
                    )
                }
                return@forEachIndexed
            }

            if (inCodeBlock) {
                codeBlockContent.append(line).append("\n")
                return@forEachIndexed
            }

            // Heading 1
            if (trimmed.startsWith("# ")) {
                Text(
                    text = trimmed.removePrefix("# "),
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = ElectricViolet,
                    modifier = Modifier.padding(top = 8.dp, bottom = 4.dp)
                )
            }
            // Heading 2
            else if (trimmed.startsWith("## ")) {
                Text(
                    text = trimmed.removePrefix("## "),
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = NeonCyan,
                    modifier = Modifier.padding(top = 6.dp, bottom = 2.dp)
                )
            }
            // Heading 3
            else if (trimmed.startsWith("### ")) {
                Text(
                    text = trimmed.removePrefix("### "),
                    fontSize = 17.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onBackground,
                    modifier = Modifier.padding(top = 4.dp)
                )
            }
            // Checklist task item
            else if (trimmed.startsWith("- [ ] ") || trimmed.startsWith("- [x] ")) {
                val isChecked = trimmed.startsWith("- [x] ")
                val taskText = trimmed.substring(6)

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.clickable {
                        // Toggle task state
                        val newLines = lines.toMutableList()
                        val updatedLine = if (isChecked) "- [ ] $taskText" else "- [x] $taskText"
                        newLines[index] = updatedLine
                        onContentChange(newLines.joinToString("\n"))
                    }
                ) {
                    Checkbox(
                        checked = isChecked,
                        onCheckedChange = { checked ->
                            val newLines = lines.toMutableList()
                            val updatedLine = if (checked) "- [x] $taskText" else "- [ ] $taskText"
                            newLines[index] = updatedLine
                            onContentChange(newLines.joinToString("\n"))
                        },
                        colors = CheckboxDefaults.colors(
                            checkedColor = NeonCyan,
                            uncheckedColor = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    )
                    Text(
                        text = taskText,
                        style = MaterialTheme.typography.bodyMedium,
                        color = if (isChecked) MaterialTheme.colorScheme.onSurfaceVariant else MaterialTheme.colorScheme.onBackground
                    )
                }
            }
            // Bullet List
            else if (trimmed.startsWith("- ")) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.Top,
                    modifier = Modifier.padding(start = 8.dp)
                ) {
                    Text("•", color = ElectricViolet, fontWeight = FontWeight.Bold)
                    RichTextWithLinks(trimmed.removePrefix("- "), onLinkClick)
                }
            }
            // Callout quote
            else if (trimmed.startsWith("> ")) {
                Surface(
                    color = Color(0xFFF59E0B).copy(alpha = 0.1f),
                    shape = RoundedCornerShape(8.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFF59E0B).copy(alpha = 0.4f)),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.Top
                    ) {
                        Icon(Icons.Default.FormatQuote, contentDescription = null, tint = Color(0xFFF59E0B))
                        RichTextWithLinks(trimmed.removePrefix("> "), onLinkClick)
                    }
                }
            }
            // Horizontal rule
            else if (trimmed == "---") {
                Divider(
                    color = MaterialTheme.colorScheme.outline.copy(alpha = 0.4f),
                    thickness = 1.dp,
                    modifier = Modifier.padding(vertical = 8.dp)
                )
            }
            // Standard Text Line
            else if (trimmed.isNotEmpty()) {
                RichTextWithLinks(trimmed, onLinkClick)
            }
        }
    }
}

@Composable
fun RichTextWithLinks(text: String, onLinkClick: (String) -> Unit) {
    // Parse [[Note Title]] links
    val linkPattern = Pattern.compile("\\[\\[(.*?)\\]\\]")
    val matcher = linkPattern.matcher(text)

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.Start,
        verticalAlignment = Alignment.CenterVertically
    ) {
        var lastEnd = 0
        while (matcher.find()) {
            val start = matcher.start()
            val end = matcher.end()
            val linkTitle = matcher.group(1) ?: ""

            if (start > lastEnd) {
                Text(
                    text = text.substring(lastEnd, start),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onBackground
                )
            }

            // Clickable Bi-directional Link Badge
            Surface(
                color = NeonCyan.copy(alpha = 0.15f),
                shape = RoundedCornerShape(6.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, NeonCyan.copy(alpha = 0.5f)),
                modifier = Modifier
                    .padding(horizontal = 2.dp)
                    .clickable { onLinkClick(linkTitle) }
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Icon(Icons.Default.Link, contentDescription = null, tint = NeonCyan, modifier = Modifier.size(12.dp))
                    Text(
                        text = linkTitle,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = NeonCyan
                    )
                }
            }

            lastEnd = end
        }

        if (lastEnd < text.length) {
            Text(
                text = text.substring(lastEnd),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onBackground
            )
        }
    }
}

@Composable
fun CodeBlockCard(code: String, language: String, onCopy: () -> Unit) {
    Surface(
        color = Color(0xFF0F172A),
        shape = RoundedCornerShape(10.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF334155)),
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = language.uppercase(),
                    style = MaterialTheme.typography.labelSmall,
                    color = NeonCyan,
                    fontWeight = FontWeight.Bold
                )
                IconButton(onClick = onCopy, modifier = Modifier.size(24.dp)) {
                    Icon(Icons.Default.ContentCopy, contentDescription = "Copy code", tint = Color.LightGray, modifier = Modifier.size(16.dp))
                }
            }
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = code,
                fontFamily = FontFamily.Monospace,
                fontSize = 13.sp,
                color = Color(0xFFE2E8F0),
                lineHeight = 18.sp
            )
        }
    }
}
