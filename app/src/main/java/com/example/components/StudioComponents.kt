package com.example.components

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.outlined.AddPhotoAlternate
import androidx.compose.material.icons.outlined.BookmarkBorder
import androidx.compose.material.icons.outlined.ErrorOutline
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.example.R
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StudioTopBar(
    title: String,
    subtitle: String? = null,
    onBack: (() -> Unit)? = null,
    actions: @Composable () -> Unit = {}
) {
    CenterAlignedTopAppBar(
        title = {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleLarge,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                if (!subtitle.isNullOrBlank()) {
                    Text(
                        text = subtitle,
                        style = MaterialTheme.typography.labelSmall,
                        color = CyanSpark,
                        maxLines = 1
                    )
                }
            }
        },
        navigationIcon = {
            if (onBack != null) {
                IconButton(
                    onClick = onBack,
                    modifier = Modifier
                        .minimumInteractiveComponentSize()
                        .testTag("top_bar_back_button")
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = stringResource(R.string.cd_back)
                    )
                }
            }
        },
        actions = { actions() },
        colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
            containerColor = StudioObsidian,
            titleContentColor = MaterialTheme.colorScheme.onBackground,
            navigationIconContentColor = MaterialTheme.colorScheme.onBackground
        )
    )
}

@Composable
fun StudioSectionHeader(
    title: String,
    badge: String? = null,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onBackground
        )
        if (!badge.isNullOrBlank()) {
            Surface(
                shape = RoundedCornerShape(999.dp),
                color = IndigoElectric.copy(alpha = 0.16f),
                border = BorderStroke(1.dp, IndigoElectric.copy(alpha = 0.35f))
            ) {
                Text(
                    text = badge,
                    style = MaterialTheme.typography.labelSmall,
                    color = CyanSpark,
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                )
            }
        }
    }
}

@Composable
fun StudioCard(
    modifier: Modifier = Modifier,
    highlighted: Boolean = false,
    content: @Composable ColumnScope.() -> Unit
) {
    val borderColor = if (highlighted) CyanSpark.copy(alpha = 0.45f) else StudioCardBorder
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.large,
        color = StudioSurface,
        border = BorderStroke(1.dp, borderColor),
        tonalElevation = 2.dp
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            content = content
        )
    }
}

@Composable
fun <T> StudioChipRow(
    items: List<T>,
    selectedItem: T,
    labelProvider: (T) -> String,
    onSelect: (T) -> Unit,
    testTagPrefix: String
) {
    LazyRow(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        contentPadding = PaddingValues(horizontal = 2.dp)
    ) {
        items(items) { item ->
            val selected = item == selectedItem
            FilterChip(
                selected = selected,
                onClick = { onSelect(item) },
                label = {
                    Text(
                        text = labelProvider(item),
                        style = MaterialTheme.typography.labelLarge
                    )
                },
                colors = FilterChipDefaults.filterChipColors(
                    containerColor = StudioSurfaceElevated,
                    labelColor = MaterialTheme.colorScheme.onSurfaceVariant,
                    selectedContainerColor = IndigoElectric.copy(alpha = 0.28f),
                    selectedLabelColor = CyanSpark
                ),
                border = FilterChipDefaults.filterChipBorder(
                    enabled = true,
                    selected = selected,
                    borderColor = StudioCardBorder,
                    selectedBorderColor = CyanSpark
                ),
                modifier = Modifier.testTag("${testTagPrefix}_${labelProvider(item).lowercase().replace(' ', '_')}")
            )
        }
    }
}

@Composable
fun AspectRatioSelector(
    selectedRatio: StudioAspectRatio,
    onSelectRatio: (StudioAspectRatio) -> Unit,
    modifier: Modifier = Modifier
) {
    LazyRow(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(StudioAspectRatio.entries) { ratio ->
            val selected = ratio == selectedRatio
            val previewW = when (ratio) {
                StudioAspectRatio.RATIO_1_1 -> 14.dp
                StudioAspectRatio.RATIO_16_9 -> 18.dp
                StudioAspectRatio.RATIO_9_16 -> 10.dp
                StudioAspectRatio.RATIO_4_3 -> 16.dp
                StudioAspectRatio.RATIO_3_4 -> 12.dp
            }
            val previewH = when (ratio) {
                StudioAspectRatio.RATIO_1_1 -> 14.dp
                StudioAspectRatio.RATIO_16_9 -> 10.dp
                StudioAspectRatio.RATIO_9_16 -> 18.dp
                StudioAspectRatio.RATIO_4_3 -> 12.dp
                StudioAspectRatio.RATIO_3_4 -> 16.dp
            }
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = if (selected) IndigoElectric.copy(alpha = 0.25f) else StudioSurfaceElevated,
                border = BorderStroke(1.dp, if (selected) CyanSpark else StudioCardBorder),
                modifier = Modifier
                    .minimumInteractiveComponentSize()
                    .clip(RoundedCornerShape(12.dp))
                    .clickable { onSelectRatio(ratio) }
                    .testTag("aspect_ratio_${ratio.label}")
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(width = previewW, height = previewH)
                            .border(
                                width = 1.5.dp,
                                color = if (selected) CyanSpark else MaterialTheme.colorScheme.onSurfaceVariant,
                                shape = RoundedCornerShape(3.dp)
                            )
                    )
                    Text(
                        text = ratio.label,
                        style = MaterialTheme.typography.labelMedium,
                        color = if (selected) CyanSpark else MaterialTheme.colorScheme.onSurface
                    )
                }
            }
        }
    }
}

@Composable
fun SourceImagePickerCard(
    selectedUri: Uri?,
    label: String,
    onImagePicked: (Uri?) -> Unit,
    modifier: Modifier = Modifier
) {
    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia(),
        onResult = { uri -> if (uri != null) onImagePicked(uri) }
    )

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .clip(MaterialTheme.shapes.medium)
            .clickable {
                photoPickerLauncher.launch(
                    PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                )
            }
            .testTag("source_image_picker"),
        shape = MaterialTheme.shapes.medium,
        color = StudioSurfaceElevated,
        border = BorderStroke(
            1.dp,
            if (selectedUri != null) CyanSpark.copy(alpha = 0.6f) else StudioCardBorder
        )
    ) {
        if (selectedUri != null) {
            Row(
                modifier = Modifier.padding(12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                AsyncImage(
                    model = selectedUri,
                    contentDescription = stringResource(R.string.cd_pick_source_image),
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .size(64.dp)
                        .clip(RoundedCornerShape(10.dp))
                )
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Reference image ready",
                        style = MaterialTheme.typography.titleSmall,
                        color = CyanSpark
                    )
                    Text(
                        text = "Tap to replace photo (max 20 MB)",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                OutlinedButton(
                    onClick = { onImagePicked(null) },
                    modifier = Modifier.testTag("clear_source_image_button")
                ) {
                    Text("Clear")
                }
            }
        } else {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(
                            Brush.linearGradient(
                                listOf(IndigoElectric.copy(alpha = 0.3f), CyanSpark.copy(alpha = 0.25f))
                            )
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Outlined.AddPhotoAlternate,
                        contentDescription = stringResource(R.string.cd_pick_source_image),
                        tint = CyanSpark
                    )
                }
                Column {
                    Text(
                        text = label,
                        style = MaterialTheme.typography.titleSmall,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "Uses Android Photo Picker • No storage permission needed",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

@Composable
fun GenerationProgressCard(
    progress: Float,
    stage: String,
    modifier: Modifier = Modifier
) {
    StudioCard(modifier = modifier, highlighted = true) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = stage,
                style = MaterialTheme.typography.titleSmall,
                color = CyanSpark
            )
            Text(
                text = "${(progress * 100).toInt()}%",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
        LinearProgressIndicator(
            progress = { progress.coerceIn(0f, 1f) },
            modifier = Modifier
                .fillMaxWidth()
                .height(8.dp)
                .clip(RoundedCornerShape(999.dp)),
            color = CyanSpark,
            trackColor = StudioSurfaceElevated
        )
    }
}

@Composable
fun ErrorFeedbackCard(
    message: String,
    canRetry: Boolean,
    onRetry: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.medium,
        color = RoseNeon.copy(alpha = 0.14f),
        border = BorderStroke(1.dp, RoseNeon.copy(alpha = 0.5f))
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Outlined.ErrorOutline,
                    contentDescription = null,
                    tint = RoseNeon
                )
                Text(
                    text = message,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onBackground
                )
            }
            if (canRetry) {
                Button(
                    onClick = onRetry,
                    colors = ButtonDefaults.buttonColors(containerColor = RoseNeon),
                    modifier = Modifier.testTag("retry_button")
                ) {
                    Icon(Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(6.dp))
                    Text("Retry")
                }
            }
        }
    }
}

@Composable
fun CreationActionBar(
    isSavedLocally: Boolean,
    downloadState: DownloadState,
    onRegenerate: () -> Unit,
    onSaveLocally: () -> Unit,
    onDownload: () -> Unit,
    onShare: () -> Unit,
    modifier: Modifier = Modifier
) {
    val isDownloading = downloadState is DownloadState.Downloading
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            OutlinedButton(
                onClick = onRegenerate,
                modifier = Modifier
                    .weight(1f)
                    .minimumInteractiveComponentSize()
                    .testTag("regenerate_button"),
                border = BorderStroke(1.dp, StudioCardBorder)
            ) {
                Icon(Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(6.dp))
                Text("Regenerate")
            }

            OutlinedButton(
                onClick = onSaveLocally,
                modifier = Modifier
                    .weight(1f)
                    .minimumInteractiveComponentSize()
                    .testTag("save_locally_button"),
                border = BorderStroke(1.dp, if (isSavedLocally) EmeraldGlow else StudioCardBorder)
            ) {
                Icon(
                    imageVector = if (isSavedLocally) Icons.Default.Bookmark else Icons.Outlined.BookmarkBorder,
                    contentDescription = null,
                    tint = if (isSavedLocally) EmeraldGlow else Color.Unspecified,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(Modifier.width(6.dp))
                Text(if (isSavedLocally) "Saved" else "Save")
            }
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Button(
                onClick = onDownload,
                enabled = !isDownloading,
                modifier = Modifier
                    .weight(1f)
                    .minimumInteractiveComponentSize()
                    .testTag("download_button"),
                colors = ButtonDefaults.buttonColors(containerColor = IndigoElectric)
            ) {
                Icon(Icons.Default.Download, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(6.dp))
                Text(if (isDownloading) "Saving..." else "Download")
            }

            OutlinedButton(
                onClick = onShare,
                modifier = Modifier
                    .weight(1f)
                    .minimumInteractiveComponentSize()
                    .testTag("share_button"),
                border = BorderStroke(1.dp, CyanSpark.copy(alpha = 0.5f))
            ) {
                Icon(Icons.Default.Share, contentDescription = null, tint = CyanSpark, modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(6.dp))
                Text("Share", color = CyanSpark)
            }
        }

        AnimatedVisibility(visible = downloadState !is DownloadState.Idle) {
            when (downloadState) {
                is DownloadState.Downloading -> {
                    LinearProgressIndicator(
                        progress = { downloadState.progress },
                        modifier = Modifier.fillMaxWidth(),
                        color = EmeraldGlow
                    )
                }
                is DownloadState.Completed -> {
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = EmeraldGlow.copy(alpha = 0.14f),
                        border = BorderStroke(1.dp, EmeraldGlow.copy(alpha = 0.45f)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = "Downloaded ${downloadState.fileName} to ${downloadState.displayLocation}",
                            style = MaterialTheme.typography.bodyMedium,
                            color = EmeraldGlow,
                            modifier = Modifier.padding(10.dp)
                        )
                    }
                }
                is DownloadState.Failed -> {
                    Text(
                        text = "Download failed: ${downloadState.message}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = RoseNeon
                    )
                }
                DownloadState.Idle -> Unit
            }
        }
    }
}
