package com.example.screens

import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.outlined.BookmarkBorder
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import com.example.components.AnimatedFramePlayer
import com.example.components.StudioChipRow
import com.example.components.StudioTopBar
import com.example.types.CreationHistoryItem
import com.example.types.CreationType
import com.example.types.DownloadState
import com.example.types.StudioAspectRatio
import com.example.ui.theme.CyanSpark
import com.example.ui.theme.EmeraldGlow
import com.example.ui.theme.IndigoElectric
import com.example.ui.theme.RoseNeon
import com.example.ui.theme.StudioCardBorder
import com.example.ui.theme.StudioObsidian
import com.example.ui.theme.StudioSurface
import com.example.ui.theme.StudioSurfaceElevated
import com.example.utilities.Formatters
import com.example.viewmodel.HistoryFilter
import com.example.viewmodel.HistoryViewModel
import java.io.File

@Composable
fun HistoryScreen(
    viewModel: HistoryViewModel,
    onBack: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val items by viewModel.items.collectAsStateWithLifecycle()
    val selectedFilter by viewModel.selectedFilter.collectAsStateWithLifecycle()
    val searchQuery by viewModel.searchQuery.collectAsStateWithLifecycle()
    val downloadState by viewModel.downloadState.collectAsStateWithLifecycle()
    var previewItem by remember { mutableStateOf<CreationHistoryItem?>(null) }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = StudioObsidian,
        topBar = {
            StudioTopBar(
                title = "Creation History",
                subtitle = "${items.size} local items",
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
                        value = searchQuery,
                        onValueChange = viewModel::setSearchQuery,
                        placeholder = { Text("Search prompts, styles, or creations...") },
                        leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = CyanSpark) },
                        trailingIcon = {
                            if (searchQuery.isNotEmpty()) {
                                IconButton(onClick = { viewModel.setSearchQuery("") }) {
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
                            .testTag("history_search_input")
                    )
                }

                item {
                    StudioChipRow(
                        items = HistoryFilter.entries,
                        selectedItem = selectedFilter,
                        labelProvider = { it.label },
                        onSelect = viewModel::setFilter,
                        testTagPrefix = "history_filter"
                    )
                }

                if (downloadState is DownloadState.Completed) {
                    val done = downloadState as DownloadState.Completed
                    item {
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = EmeraldGlow.copy(alpha = 0.14f),
                            border = BorderStroke(1.dp, EmeraldGlow.copy(alpha = 0.45f)),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.padding(12.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "Downloaded ${done.fileName}",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = EmeraldGlow,
                                    modifier = Modifier.weight(1f)
                                )
                                TextButton(onClick = viewModel::clearDownloadStatus) {
                                    Text("Dismiss")
                                }
                            }
                        }
                    }
                }

                if (items.isEmpty()) {
                    item {
                        Surface(
                            shape = MaterialTheme.shapes.large,
                            color = StudioSurface,
                            border = BorderStroke(1.dp, StudioCardBorder),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(
                                modifier = Modifier.padding(32.dp),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.History,
                                    contentDescription = null,
                                    tint = CyanSpark,
                                    modifier = Modifier.size(40.dp)
                                )
                                Text(
                                    text = "No creations yet",
                                    style = MaterialTheme.typography.titleMedium
                                )
                                Text(
                                    text = "Generate images, videos, or creative prompts and they will appear here locally without any account required.",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                } else {
                    items(items, key = { it.id }) { item ->
                        HistoryItemCard(
                            item = item,
                            onPreview = { previewItem = item },
                            onToggleSaved = { viewModel.toggleSaved(item) },
                            onDownload = { viewModel.downloadAgain(item) },
                            onShare = {
                                viewModel.shareItem(context, item).onFailure { err ->
                                    Toast.makeText(context, err.localizedMessage ?: "Share unavailable", Toast.LENGTH_SHORT).show()
                                }
                            },
                            onDelete = { viewModel.deleteItem(item) }
                        )
                    }
                }
            }
        }
    }

    previewItem?.let { item ->
        AlertDialog(
            onDismissRequest = { previewItem = null },
            containerColor = StudioSurface,
            title = { Text(item.title, style = MaterialTheme.typography.titleLarge) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    if (item.type == CreationType.VIDEO && item.secondaryMediaPaths.isNotEmpty()) {
                        val ratio = StudioAspectRatio.entries.firstOrNull { it.label == item.aspectRatioLabel }
                            ?: StudioAspectRatio.RATIO_16_9
                        AnimatedFramePlayer(
                            framePaths = item.secondaryMediaPaths,
                            posterPath = item.secondaryMediaPaths.first(),
                            durationSeconds = item.durationSeconds.coerceAtLeast(4),
                            aspectRatio = ratio
                        )
                    } else if (item.mediaPath.isNotBlank() && File(item.mediaPath).exists()) {
                        AsyncImage(
                            model = File(item.mediaPath),
                            contentDescription = item.title,
                            contentScale = ContentScale.Fit,
                            modifier = Modifier
                                .fillMaxWidth()
                                .aspectRatio(1f)
                                .clip(RoundedCornerShape(12.dp))
                                .background(StudioObsidian)
                        )
                    }
                    Text(
                        text = item.prompt,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = Formatters.formatTimestamp(item.createdAtMillis),
                        style = MaterialTheme.typography.labelSmall,
                        color = CyanSpark
                    )
                }
            },
            confirmButton = {
                TextButton(onClick = { previewItem = null }) {
                    Text("Close")
                }
            }
        )
    }
}

@Composable
private fun HistoryItemCard(
    item: CreationHistoryItem,
    onPreview: () -> Unit,
    onToggleSaved: () -> Unit,
    onDownload: () -> Unit,
    onShare: () -> Unit,
    onDelete: () -> Unit
) {
    val thumbPath = when {
        item.secondaryMediaPaths.isNotEmpty() -> item.secondaryMediaPaths.first()
        item.mediaPath.isNotBlank() && !item.mediaPath.endsWith(".mp4", true) -> item.mediaPath
        else -> null
    }

    Surface(
        shape = MaterialTheme.shapes.large,
        color = StudioSurface,
        border = BorderStroke(1.dp, StudioCardBorder),
        modifier = Modifier
            .fillMaxWidth()
            .clip(MaterialTheme.shapes.large)
            .clickable(onClick = onPreview)
            .testTag("history_card_${item.id}")
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (thumbPath != null && File(thumbPath).exists()) {
                    AsyncImage(
                        model = File(thumbPath),
                        contentDescription = item.title,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .size(72.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(StudioSurfaceElevated)
                    )
                } else {
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = IndigoElectric.copy(alpha = 0.2f),
                        modifier = Modifier.size(72.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Text(
                                text = item.type.displayName.uppercase(),
                                style = MaterialTheme.typography.labelSmall,
                                color = CyanSpark
                            )
                        }
                    }
                }

                Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = IndigoElectric.copy(alpha = 0.22f)
                        ) {
                            Text(
                                text = item.type.displayName,
                                style = MaterialTheme.typography.labelSmall,
                                color = CyanSpark,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                        Text(
                            text = item.aspectRatioLabel,
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Text(
                        text = item.prompt,
                        style = MaterialTheme.typography.titleSmall,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = Formatters.formatTimestamp(item.createdAtMillis),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    IconButton(
                        onClick = onToggleSaved,
                        modifier = Modifier.minimumInteractiveComponentSize()
                    ) {
                        Icon(
                            imageVector = if (item.isSaved) Icons.Default.Bookmark else Icons.Outlined.BookmarkBorder,
                            contentDescription = "Save creation",
                            tint = if (item.isSaved) EmeraldGlow else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    if (item.mediaPath.isNotBlank()) {
                        IconButton(
                            onClick = onDownload,
                            modifier = Modifier.minimumInteractiveComponentSize()
                        ) {
                            Icon(
                                imageVector = Icons.Default.Download,
                                contentDescription = "Download again",
                                tint = CyanSpark
                            )
                        }
                    }
                    IconButton(
                        onClick = onShare,
                        modifier = Modifier.minimumInteractiveComponentSize()
                    ) {
                        Icon(
                            imageVector = Icons.Default.Share,
                            contentDescription = "Share creation",
                            tint = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }

                IconButton(
                    onClick = onDelete,
                    modifier = Modifier.minimumInteractiveComponentSize()
                ) {
                    Icon(
                        imageVector = Icons.Default.DeleteOutline,
                        contentDescription = "Delete creation",
                        tint = RoseNeon
                    )
                }
            }
        }
    }
}
