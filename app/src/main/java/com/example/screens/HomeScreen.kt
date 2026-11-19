package com.example.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.AutoFixHigh
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.filled.Videocam
import androidx.compose.material.icons.outlined.AutoAwesome
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.R
import com.example.components.StudioSectionHeader
import com.example.types.AiToolId
import com.example.ui.theme.AmberGold
import com.example.ui.theme.CyanSpark
import com.example.ui.theme.EmeraldGlow
import com.example.ui.theme.IndigoElectric
import com.example.ui.theme.StudioCardBorder
import com.example.ui.theme.StudioObsidian
import com.example.ui.theme.StudioSurface
import com.example.ui.theme.StudioSurfaceElevated

@Composable
fun HomeScreen(
    onOpenImageStudio: () -> Unit,
    onOpenImageEditing: () -> Unit,
    onOpenVideoStudio: () -> Unit,
    onOpenAiToolsHub: () -> Unit,
    onOpenTool: (AiToolId) -> Unit,
    onOpenHistory: () -> Unit,
    onOpenDownloads: () -> Unit,
    onOpenSettings: () -> Unit,
    modifier: Modifier = Modifier
) {
    var searchQuery by rememberSaveable { mutableStateOf("") }
    val matchingTools = if (searchQuery.isBlank()) {
        emptyList()
    } else {
        AiToolId.entries.filter {
            it.title.contains(searchQuery, ignoreCase = true) ||
                it.subtitle.contains(searchQuery, ignoreCase = true) ||
                it.category.label.contains(searchQuery, ignoreCase = true)
        }.take(6)
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(StudioObsidian),
        contentAlignment = Alignment.TopCenter
    ) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .widthIn(max = 760.dp),
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 14.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Top Brand Bar
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Image(
                            painter = painterResource(id = R.drawable.launcher_emblem),
                            contentDescription = "VARDAAN CREATION logo",
                            contentScale = ContentScale.Crop,
                            modifier = Modifier
                                .size(44.dp)
                                .clip(RoundedCornerShape(12.dp))
                        )
                        Column {
                            Text(
                                text = stringResource(R.string.app_name),
                                style = MaterialTheme.typography.titleLarge,
                                color = MaterialTheme.colorScheme.onBackground
                            )
                            Text(
                                text = stringResource(R.string.studio_tagline),
                                style = MaterialTheme.typography.labelSmall,
                                color = CyanSpark
                            )
                        }
                    }

                    IconButton(
                        onClick = onOpenSettings,
                        modifier = Modifier
                            .minimumInteractiveComponentSize()
                            .background(StudioSurfaceElevated, CircleShape)
                            .testTag("home_settings_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Settings,
                            contentDescription = stringResource(R.string.nav_settings),
                            tint = MaterialTheme.colorScheme.onBackground
                        )
                    }
                }
            }

            // Hero Studio Banner
            item {
                Surface(
                    shape = MaterialTheme.shapes.extraLarge,
                    color = StudioSurface,
                    border = BorderStroke(1.dp, IndigoElectric.copy(alpha = 0.45f)),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(186.dp)
                ) {
                    Box(modifier = Modifier.fillMaxSize()) {
                        Image(
                            painter = painterResource(id = R.drawable.img_studio_hero),
                            contentDescription = stringResource(R.string.cd_studio_hero),
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize()
                        )
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(
                                    Brush.verticalGradient(
                                        listOf(
                                            StudioObsidian.copy(alpha = 0.25f),
                                            StudioObsidian.copy(alpha = 0.88f)
                                        )
                                    )
                                )
                        )
                        Column(
                            modifier = Modifier
                                .align(Alignment.BottomStart)
                                .padding(18.dp),
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Surface(
                                shape = RoundedCornerShape(999.dp),
                                color = CyanSpark.copy(alpha = 0.18f),
                                border = BorderStroke(1.dp, CyanSpark.copy(alpha = 0.45f))
                            ) {
                                Text(
                                    text = "CREATIVE SUITE • NO SIGN-IN",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = CyanSpark,
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                                )
                            }
                            Text(
                                text = "Create images, motion & prompts in one dark studio",
                                style = MaterialTheme.typography.headlineSmall,
                                color = Color.White
                            )
                        }
                    }
                }
            }

            // Search Tools Bar
            item {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    placeholder = { Text("Search 17 AI tools (enhancer, anime, upscale...)") },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.Search,
                            contentDescription = stringResource(R.string.cd_search_tools),
                            tint = CyanSpark
                        )
                    },
                    trailingIcon = {
                        if (searchQuery.isNotEmpty()) {
                            IconButton(onClick = { searchQuery = "" }) {
                                Icon(Icons.Default.Close, contentDescription = "Clear search")
                            }
                        }
                    },
                    singleLine = true,
                    shape = MaterialTheme.shapes.medium,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = StudioSurface,
                        unfocusedContainerColor = StudioSurface,
                        focusedBorderColor = CyanSpark,
                        unfocusedBorderColor = StudioCardBorder
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("home_search_tools_input")
                )

                AnimatedVisibility(visible = matchingTools.isNotEmpty()) {
                    Column(
                        modifier = Modifier.padding(top = 8.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        matchingTools.forEach { tool ->
                            Surface(
                                shape = MaterialTheme.shapes.medium,
                                color = StudioSurfaceElevated,
                                border = BorderStroke(1.dp, StudioCardBorder),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(MaterialTheme.shapes.medium)
                                    .clickable {
                                        searchQuery = ""
                                        onOpenTool(tool)
                                    }
                                    .testTag("search_result_${tool.name.lowercase()}")
                            ) {
                                Row(
                                    modifier = Modifier.padding(12.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = tool.title,
                                            style = MaterialTheme.typography.titleSmall,
                                            color = MaterialTheme.colorScheme.onSurface
                                        )
                                        Text(
                                            text = tool.subtitle,
                                            style = MaterialTheme.typography.bodyMedium,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                    }
                                    Icon(
                                        imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                                        contentDescription = null,
                                        tint = CyanSpark
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // Primary Studios (Image & Video)
            item {
                StudioSectionHeader(title = "Primary Studios", badge = "2 WORKSPACES")
                Spacer(Modifier.height(10.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    StudioFeaturedTile(
                        title = "Image Studio",
                        subtitle = "11 styles, Text & Photo edit",
                        coverRes = R.drawable.img_image_studio_cover,
                        accent = IndigoElectric,
                        onClick = onOpenImageStudio,
                        modifier = Modifier
                            .weight(1f)
                            .testTag("home_image_studio_card")
                    )
                    StudioFeaturedTile(
                        title = "Video Studio",
                        subtitle = "Motion, Camera & 1080p",
                        coverRes = R.drawable.img_video_studio_cover,
                        accent = CyanSpark,
                        onClick = onOpenVideoStudio,
                        modifier = Modifier
                            .weight(1f)
                            .testTag("home_video_studio_card")
                    )
                }
            }

            // Quick Studio Shortcuts
            item {
                StudioSectionHeader(title = "Studio Shortcuts")
                Spacer(Modifier.height(10.dp))
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        QuickShortcutCard(
                            title = "Image Editing",
                            subtitle = "Restyle & edit photos",
                            icon = Icons.Default.Tune,
                            accent = AmberGold,
                            onClick = onOpenImageEditing,
                            modifier = Modifier
                                .weight(1f)
                                .testTag("home_image_editing_shortcut")
                        )
                        QuickShortcutCard(
                            title = "AI Tools",
                            subtitle = "17 creative utilities",
                            icon = Icons.Default.AutoFixHigh,
                            accent = CyanSpark,
                            onClick = onOpenAiToolsHub,
                            modifier = Modifier
                                .weight(1f)
                                .testTag("home_ai_tools_shortcut")
                        )
                    }
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        QuickShortcutCard(
                            title = "Creation History",
                            subtitle = "View & reuse outputs",
                            icon = Icons.Default.History,
                            accent = IndigoElectric,
                            onClick = onOpenHistory,
                            modifier = Modifier
                                .weight(1f)
                                .testTag("home_history_shortcut")
                        )
                        QuickShortcutCard(
                            title = "Downloads",
                            subtitle = "Saved device files",
                            icon = Icons.Default.Download,
                            accent = EmeraldGlow,
                            onClick = onOpenDownloads,
                            modifier = Modifier
                                .weight(1f)
                                .testTag("home_downloads_shortcut")
                        )
                    }
                }
            }

            // Popular Creative Tools Preview
            item {
                StudioSectionHeader(title = "Featured AI Tools", badge = "SAMPLE PREVIEW")
                Spacer(Modifier.height(10.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    CompactToolTile(
                        title = "Prompt Enhancer",
                        icon = Icons.Outlined.AutoAwesome,
                        onClick = { onOpenTool(AiToolId.PROMPT_ENHANCER) },
                        modifier = Modifier.weight(1f)
                    )
                    CompactToolTile(
                        title = "Style Changer",
                        icon = Icons.Default.Image,
                        onClick = { onOpenTool(AiToolId.STYLE_CHANGER) },
                        modifier = Modifier.weight(1f)
                    )
                    CompactToolTile(
                        title = "Image-to-Anim",
                        icon = Icons.Default.Videocam,
                        onClick = { onOpenTool(AiToolId.IMAGE_TO_ANIMATION) },
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }
    }
}

@Composable
private fun StudioFeaturedTile(
    title: String,
    subtitle: String,
    coverRes: Int,
    accent: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        shape = MaterialTheme.shapes.large,
        color = StudioSurface,
        border = BorderStroke(1.dp, accent.copy(alpha = 0.45f)),
        modifier = modifier
            .height(168.dp)
            .clip(MaterialTheme.shapes.large)
            .clickable(onClick = onClick)
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            Image(
                painter = painterResource(id = coverRes),
                contentDescription = title,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.verticalGradient(
                            listOf(Color.Transparent, StudioObsidian.copy(alpha = 0.92f))
                        )
                    )
            )
            Column(
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .padding(14.dp),
                verticalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    color = Color.White
                )
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodyMedium,
                    color = accent,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

@Composable
private fun QuickShortcutCard(
    title: String,
    subtitle: String,
    icon: ImageVector,
    accent: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        shape = MaterialTheme.shapes.medium,
        color = StudioSurface,
        border = BorderStroke(1.dp, StudioCardBorder),
        modifier = modifier
            .minimumInteractiveComponentSize()
            .clip(MaterialTheme.shapes.medium)
            .clickable(onClick = onClick)
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(accent.copy(alpha = 0.16f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(imageVector = icon, contentDescription = null, tint = accent)
            }
            Column {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

@Composable
private fun CompactToolTile(
    title: String,
    icon: ImageVector,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        shape = MaterialTheme.shapes.medium,
        color = StudioSurfaceElevated,
        border = BorderStroke(1.dp, StudioCardBorder),
        modifier = modifier
            .minimumInteractiveComponentSize()
            .clip(MaterialTheme.shapes.medium)
            .clickable(onClick = onClick)
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Icon(imageVector = icon, contentDescription = null, tint = CyanSpark)
            Text(
                text = title,
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}
