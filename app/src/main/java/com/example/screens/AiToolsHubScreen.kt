package com.example.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Animation
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.AutoFixHigh
import androidx.compose.material.icons.filled.Brush
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Face
import androidx.compose.material.icons.filled.Hd
import androidx.compose.material.icons.filled.Landscape
import androidx.compose.material.icons.filled.LayersClear
import androidx.compose.material.icons.filled.Movie
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.filled.Wallpaper
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import com.example.components.StudioChipRow
import com.example.components.StudioTopBar
import com.example.types.AiToolCategory
import com.example.types.AiToolId
import com.example.ui.theme.CyanSpark
import com.example.ui.theme.IndigoElectric
import com.example.ui.theme.StudioCardBorder
import com.example.ui.theme.StudioObsidian
import com.example.ui.theme.StudioSurface
import com.example.ui.theme.StudioSurfaceElevated

@Composable
fun AiToolsHubScreen(
    onOpenTool: (AiToolId) -> Unit,
    onBack: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    var query by rememberSaveable { mutableStateOf("") }
    var selectedCategory by rememberSaveable { mutableStateOf<String>("All") }
    val categories = listOf("All") + AiToolCategory.entries.map { it.label }

    val filteredTools = AiToolId.entries.filter { tool ->
        val matchesCategory = selectedCategory == "All" || tool.category.label == selectedCategory
        val matchesQuery = query.isBlank() ||
            tool.title.contains(query, ignoreCase = true) ||
            tool.subtitle.contains(query, ignoreCase = true)
        matchesCategory && matchesQuery
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = StudioObsidian,
        topBar = {
            StudioTopBar(
                title = "AI Creative Tools",
                subtitle = "${AiToolId.entries.size} Studio Utilities",
                onBack = onBack
            )
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentAlignment = Alignment.TopCenter
        ) {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .widthIn(max = 760.dp),
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                item {
                    OutlinedTextField(
                        value = query,
                        onValueChange = { query = it },
                        placeholder = { Text("Filter tools by name or capability...") },
                        leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = CyanSpark) },
                        trailingIcon = {
                            if (query.isNotEmpty()) {
                                IconButton(onClick = { query = "" }) {
                                    Icon(Icons.Default.Close, contentDescription = "Clear")
                                }
                            }
                        },
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedContainerColor = StudioSurface,
                            unfocusedContainerColor = StudioSurface,
                            focusedBorderColor = CyanSpark,
                            unfocusedBorderColor = StudioCardBorder
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("tools_hub_search")
                    )
                }

                item {
                    StudioChipRow(
                        items = categories,
                        selectedItem = selectedCategory,
                        labelProvider = { it },
                        onSelect = { selectedCategory = it },
                        testTagPrefix = "tool_cat"
                    )
                }

                items(filteredTools, key = { it.name }) { tool ->
                    Surface(
                        shape = MaterialTheme.shapes.large,
                        color = StudioSurface,
                        border = BorderStroke(1.dp, StudioCardBorder),
                        modifier = Modifier
                            .fillMaxWidth()
                            .minimumInteractiveComponentSize()
                            .clip(MaterialTheme.shapes.large)
                            .clickable { onOpenTool(tool) }
                            .testTag("tool_card_${tool.name.lowercase()}")
                    ) {
                        Row(
                            modifier = Modifier.padding(14.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = IndigoElectric.copy(alpha = 0.18f),
                                modifier = Modifier.size(46.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        imageVector = iconForTool(tool),
                                        contentDescription = null,
                                        tint = CyanSpark
                                    )
                                }
                            }
                            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(3.dp)) {
                                Text(
                                    text = tool.title,
                                    style = MaterialTheme.typography.titleMedium,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = tool.subtitle,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Text(
                                    text = tool.category.label.uppercase(),
                                    style = MaterialTheme.typography.labelSmall,
                                    color = CyanSpark
                                )
                            }
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                                contentDescription = "Open ${tool.title}",
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        }
    }
}

private fun iconForTool(tool: AiToolId): ImageVector = when (tool) {
    AiToolId.PROMPT_ENHANCER -> Icons.Default.AutoAwesome
    AiToolId.STYLE_CHANGER -> Icons.Default.Palette
    AiToolId.AI_IMAGE_EDITOR -> Icons.Default.Tune
    AiToolId.BACKGROUND_CHANGE -> Icons.Default.Wallpaper
    AiToolId.BACKGROUND_REMOVAL -> Icons.Default.LayersClear
    AiToolId.IMAGE_UPSCALING -> Icons.Default.Hd
    AiToolId.AI_ART_GENERATOR -> Icons.Default.Brush
    AiToolId.AI_CHARACTER_CREATOR -> Icons.Default.Face
    AiToolId.IMAGE_TO_ANIMATION -> Icons.Default.Animation
    AiToolId.VIDEO_PROMPT_GENERATOR,
    AiToolId.CINEMATIC_PROMPT_GENERATOR -> Icons.Default.Movie
    AiToolId.ANIME_PROMPT_GENERATOR,
    AiToolId.CARTOON_PROMPT_GENERATOR -> Icons.Default.AutoFixHigh
    AiToolId.AI_SCENE_CREATOR -> Icons.Default.Landscape
    AiToolId.AI_THUMBNAIL_CREATOR -> Icons.Default.PhotoLibrary
    AiToolId.TEXT_TO_IMAGE_TEMPLATES,
    AiToolId.TEXT_TO_VIDEO_TEMPLATES -> Icons.Default.AutoAwesome
}
