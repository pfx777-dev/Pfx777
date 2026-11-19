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
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.DownloadDone
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import com.example.components.StudioTopBar
import com.example.types.DownloadedCreationItem
import com.example.ui.theme.CyanSpark
import com.example.ui.theme.EmeraldGlow
import com.example.ui.theme.RoseNeon
import com.example.ui.theme.StudioCardBorder
import com.example.ui.theme.StudioObsidian
import com.example.ui.theme.StudioSurface
import com.example.ui.theme.StudioSurfaceElevated
import com.example.utilities.Formatters
import com.example.viewmodel.DownloadsViewModel
import java.io.File

@Composable
fun DownloadsScreen(
    viewModel: DownloadsViewModel,
    onBack: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val downloads by viewModel.downloads.collectAsStateWithLifecycle()
    var previewItem by remember { mutableStateOf<DownloadedCreationItem?>(null) }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = StudioObsidian,
        topBar = {
            StudioTopBar(
                title = "Downloaded Creations",
                subtitle = "${downloads.size} exported files",
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
                if (downloads.isEmpty()) {
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
                                    imageVector = Icons.Default.DownloadDone,
                                    contentDescription = null,
                                    tint = EmeraldGlow,
                                    modifier = Modifier.size(40.dp)
                                )
                                Text("No downloaded files yet", style = MaterialTheme.typography.titleMedium)
                                Text(
                                    text = "Tap Download on any generated image or video to export it cleanly to Pictures/VARDAAN CREATION or Movies/VARDAAN CREATION.",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                } else {
                    items(downloads, key = { it.id }) { item ->
                        Surface(
                            shape = MaterialTheme.shapes.large,
                            color = StudioSurface,
                            border = BorderStroke(1.dp, StudioCardBorder),
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(MaterialTheme.shapes.large)
                                .clickable { previewItem = item }
                                .testTag("download_card_${item.id}")
                        ) {
                            Row(
                                modifier = Modifier.padding(14.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                val file = File(item.filePath)
                                if (file.exists() && !item.fileName.endsWith(".mp4", true)) {
                                    AsyncImage(
                                        model = file,
                                        contentDescription = item.fileName,
                                        contentScale = ContentScale.Crop,
                                        modifier = Modifier
                                            .size(64.dp)
                                            .clip(RoundedCornerShape(12.dp))
                                            .background(StudioSurfaceElevated)
                                    )
                                } else {
                                    Surface(
                                        shape = RoundedCornerShape(12.dp),
                                        color = EmeraldGlow.copy(alpha = 0.16f),
                                        modifier = Modifier.size(64.dp)
                                    ) {
                                        Box(contentAlignment = Alignment.Center) {
                                            Text(
                                                text = if (item.fileName.endsWith(".mp4", true)) "MP4" else "PNG",
                                                style = MaterialTheme.typography.labelLarge,
                                                color = EmeraldGlow
                                            )
                                        }
                                    }
                                }

                                Column(
                                    modifier = Modifier.weight(1f),
                                    verticalArrangement = Arrangement.spacedBy(3.dp)
                                ) {
                                    Text(
                                        text = item.fileName,
                                        style = MaterialTheme.typography.titleSmall,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                    Text(
                                        text = "${item.type.displayName} • ${Formatters.formatFileSize(item.fileSizeBytes)}",
                                        style = MaterialTheme.typography.labelMedium,
                                        color = CyanSpark
                                    )
                                    Text(
                                        text = Formatters.formatTimestamp(item.downloadedAtMillis),
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }

                                IconButton(
                                    onClick = {
                                        viewModel.shareDownload(context, item).onFailure { err ->
                                            Toast.makeText(context, err.localizedMessage ?: "Share unavailable", Toast.LENGTH_SHORT).show()
                                        }
                                    },
                                    modifier = Modifier.minimumInteractiveComponentSize()
                                ) {
                                    Icon(Icons.Default.Share, contentDescription = "Share downloaded file", tint = CyanSpark)
                                }

                                IconButton(
                                    onClick = { viewModel.deleteDownload(item) },
                                    modifier = Modifier.minimumInteractiveComponentSize()
                                ) {
                                    Icon(Icons.Default.DeleteOutline, contentDescription = "Delete downloaded file", tint = RoseNeon)
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    previewItem?.let { item ->
        AlertDialog(
            onDismissRequest = { previewItem = null },
            containerColor = StudioSurface,
            title = { Text(item.fileName, style = MaterialTheme.typography.titleMedium) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    val file = File(item.filePath)
                    if (file.exists() && !item.fileName.endsWith(".mp4", true)) {
                        AsyncImage(
                            model = file,
                            contentDescription = item.fileName,
                            contentScale = ContentScale.Fit,
                            modifier = Modifier
                                .fillMaxWidth()
                                .aspectRatio(1f)
                                .clip(RoundedCornerShape(12.dp))
                                .background(StudioObsidian)
                        )
                    }
                    Text(
                        text = "Stored in ${if (item.fileName.endsWith(".mp4", true)) "Movies/VARDAAN CREATION" else "Pictures/VARDAAN CREATION"} (${Formatters.formatFileSize(item.fileSizeBytes)})",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            },
            confirmButton = {
                TextButton(onClick = { previewItem = null }) { Text("Close") }
            }
        )
    }
}
