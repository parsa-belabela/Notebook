package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

@Composable
fun GlassCard(
    modifier: Modifier = Modifier,
    cornerRadius: Dp = 16.dp,
    borderColor: Color = MaterialTheme.colorScheme.outline,
    backgroundColor: Color = MaterialTheme.colorScheme.surface.copy(alpha = 0.85f),
    onClick: (() -> Unit)? = null,
    content: @Composable BoxScope.() -> Unit
) {
    val shape = RoundedCornerShape(cornerRadius)
    val clickableModifier = if (onClick != null) {
        modifier
            .clip(shape)
            .clickable(onClick = onClick)
    } else {
        modifier.clip(shape)
    }

    Box(
        modifier = clickableModifier
            .background(backgroundColor, shape = shape)
            .border(width = 1.dp, color = borderColor, shape = shape)
            .padding(16.dp)
    ) {
        content()
    }
}

@Composable
fun GlowingGlassCard(
    modifier: Modifier = Modifier,
    cornerRadius: Dp = 16.dp,
    glowColor: Color = MaterialTheme.colorScheme.primary,
    onClick: (() -> Unit)? = null,
    content: @Composable BoxScope.() -> Unit
) {
    val shape = RoundedCornerShape(cornerRadius)
    val borderGradient = Brush.linearGradient(
        colors = listOf(glowColor.copy(alpha = 0.8f), MaterialTheme.colorScheme.secondary.copy(alpha = 0.5f), Color.Transparent)
    )

    val clickableModifier = if (onClick != null) {
        modifier
            .clip(shape)
            .clickable(onClick = onClick)
    } else {
        modifier.clip(shape)
    }

    Box(
        modifier = clickableModifier
            .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.9f), shape = shape)
            .border(width = 1.5.dp, brush = borderGradient, shape = shape)
            .padding(16.dp)
    ) {
        content()
    }
}
