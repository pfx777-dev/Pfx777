package com.example.screens

import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MovieCreation
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.components.AnimatedFramePlayer
import com.example.components.AspectRatioSelector
import com.example.components.CreationActionBar
import com.example.components.ErrorFeedbackCard
import com.example.components.FirebaseNoticeBanner
import com.example.components.GenerationProgressCard
import com.example.components.SourceImagePickerCard
import com.example.components.StudioCard
import com.example.components.StudioChipRow
import com.example.components.StudioSectionHeader
import com.example.components.StudioTopBar
import com.example.types.CameraMotion
import com.example.types.GenerationState
import com.example.types.VideoDuration
import com.example.types.VideoMode
import com.example.types.VideoQuality
import com.example.types.VideoStyle
import com.example.ui.theme.CyanSpark
import com.example.ui.theme.IndigoElectric
import com.example.ui.theme.StudioCardBorder
import com.example.ui.theme.StudioObsidian
import com.example.ui.theme.StudioSurfaceElevated
import com.example.ui.theme.StudioVoid
import com.example.viewmodel.VideoStudioViewModel

@Composable
fun VideoStudioScreen(
    viewModel: VideoStudioViewModel,
    onBack: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val input by viewModel.inputState.collectAsStateWithLifecycle()
    val genState by viewModel.generationState.collectAsStateWithLifecycle()
    val downloadState by viewModel.downloadState.collectAsStateWithLifecycle()
    val isLoading = genState is GenerationState.Loading

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = StudioObsidian,
        topBar = {
            StudioTopBar(
                title = "AI Video Studio",
                subtitle = viewModel.providerStatus.statusHeadline,
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
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                item {
                    FirebaseNoticeBanner(customMessage = viewModel.providerStatus.statusDetail)
                }

                // Video Mode + Source Picker
                item {
                    StudioCard {
                        StudioSectionHeader(title = "Video Mode")
                        StudioChipRow(
                            items = VideoMode.entries,
                            selectedItem = input.mode,
                            labelProvider = { it.label },
                            onSelect = viewModel::updateMode,
                            testTagPrefix = "video_mode"
                        )
                        AnimatedVisibility(visible = input.mode.requiresSourceImage) {
                            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                StudioSectionHeader(title = "Keyframe Image")
                                SourceImagePickerCard(
                                    selectedUri = input.sourceImageUri,
                                    label = "Pick keyframe image to animate",
                                    onImagePicked = { uri -> viewModel.updateSourceImage(context, uri) }
                                )
                            }
                        }
                    }
                }

                // Scene & Motion Prompts
                item {
                    StudioCard {
                        StudioSectionHeader(title = "Scene & Motion Prompts")
                        OutlinedTextField(
                            value = input.prompt,
                            onValueChange = viewModel::updatePrompt,
                            label = { Text("Scene prompt") },
                            placeholder = { Text("e.g., Golden dragon soaring through glowing storm clouds...") },
                            minLines = 3,
                            maxLines = 5,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedContainerColor = StudioSurfaceElevated,
                                unfocusedContainerColor = StudioSurfaceElevated,
                                focusedBorderColor = CyanSpark,
                                unfocusedBorderColor = StudioCardBorder
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("video_prompt_input")
                        )

                        OutlinedTextField(
                            value = input.motionPrompt,
                            onValueChange = viewModel::updateMotionPrompt,
                            label = { Text("Custom motion prompt (optional)") },
                            placeholder = { Text("e.g., billowing mist, slow cloth flutter, glowing embers") },
                            singleLine = true,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedContainerColor = StudioSurfaceElevated,
                                unfocusedContainerColor = StudioSurfaceElevated,
                                focusedBorderColor = IndigoElectric,
                                unfocusedBorderColor = StudioCardBorder
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("video_motion_prompt_input")
                        )
                    }
                }

                // Style, Camera Motion, Aspect Ratio, Duration, Quality
                item {
                    StudioCard {
                        StudioSectionHeader(title = "Video Style")
                        StudioChipRow(
                            items = VideoStyle.entries,
                            selectedItem = input.selectedStyle,
                            labelProvider = { it.label },
                            onSelect = viewModel::updateStyle,
                            testTagPrefix = "video_style"
                        )

                        AnimatedVisibility(visible = input.selectedStyle == VideoStyle.CUSTOM) {
                            OutlinedTextField(
                                value = input.customStyleText,
                                onValueChange = viewModel::updateCustomStyle,
                                label = { Text("Custom video aesthetic") },
                                singleLine = true,
                                modifier = Modifier.fillMaxWidth()
                            )
                        }

                        StudioSectionHeader(title = "Camera Motion")
                        StudioChipRow(
                            items = CameraMotion.entries,
                            selectedItem = input.cameraMotion,
                            labelProvider = { it.label },
                            onSelect = viewModel::updateCameraMotion,
                            testTagPrefix = "camera_motion"
                        )

                        StudioSectionHeader(title = "Aspect Ratio")
                        AspectRatioSelector(
                            selectedRatio = input.aspectRatio,
                            onSelectRatio = viewModel::updateAspectRatio
                        )

                        StudioSectionHeader(title = "Duration & Quality")
                        StudioChipRow(
                            items = VideoDuration.entries,
                            selectedItem = input.duration,
                            labelProvider = { it.label },
                            onSelect = viewModel::updateDuration,
                            testTagPrefix = "video_duration"
                        )
                        StudioChipRow(
                            items = VideoQuality.entries,
                            selectedItem = input.quality,
                            labelProvider = { it.label },
                            onSelect = viewModel::updateQuality,
                            testTagPrefix = "video_quality"
                        )
                        Text(
                            text = "No app-side daily lockouts. Cloud provider quotas and model capacities apply when connected.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                // Generate Button
                item {
                    Button(
                        onClick = viewModel::generateVideo,
                        enabled = !isLoading,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = CyanSpark,
                            contentColor = StudioVoid
                        ),
                        shape = MaterialTheme.shapes.medium,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(54.dp)
                            .minimumInteractiveComponentSize()
                            .testTag("generate_video_button")
                    ) {
                        Icon(Icons.Default.MovieCreation, contentDescription = null, modifier = Modifier.size(20.dp))
                        Spacer(Modifier.width(8.dp))
                        Text(
                            text = if (isLoading) "Rendering Motion..." else "Generate Video",
                            style = MaterialTheme.typography.titleMedium
                        )
                    }
                }

                // Video Result / States
                item {
                    when (val state = genState) {
                        is GenerationState.Idle -> Unit
                        is GenerationState.Loading -> {
                            GenerationProgressCard(progress = state.progress, stage = state.stage)
                        }
                        is GenerationState.Error -> {
                            ErrorFeedbackCard(
                                message = state.message,
                                canRetry = state.canRetry,
                                onRetry = viewModel::generateVideo
                            )
                        }
                        is GenerationState.Success -> {
                            val result = state.data
                            StudioCard(highlighted = true) {
                                StudioSectionHeader(
                                    title = "Video Preview",
                                    badge = "${result.durationSeconds}s • ${result.quality.label}"
                                )
                                AnimatedFramePlayer(
                                    framePaths = result.frameFilePaths,
                                    posterPath = result.posterFilePath,
                                    durationSeconds = result.durationSeconds,
                                    aspectRatio = result.aspectRatio,
                                    modifier = Modifier.testTag("generated_video_preview")
                                )
                                Text(
                                    text = if (result.exportedMp4Path != null) {
                                        "Export format: H.264 MP4 + Poster ready"
                                    } else {
                                        "Export format: High-Res Keyframe Poster PNG (hardware AVC codec unavailable)"
                                    },
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = CyanSpark
                                )
                                if (result.isSamplePreview) {
                                    FirebaseNoticeBanner(customMessage = result.providerNotice)
                                }
                                CreationActionBar(
                                    isSavedLocally = input.isSavedLocally,
                                    downloadState = downloadState,
                                    onRegenerate = viewModel::generateVideo,
                                    onSaveLocally = viewModel::saveCurrentLocally,
                                    onDownload = viewModel::downloadCurrent,
                                    onShare = {
                                        viewModel.shareCurrent(context).onFailure { err ->
                                            Toast.makeText(context, err.localizedMessage ?: "Share unavailable", Toast.LENGTH_SHORT).show()
                                        }
                                    }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
