package com.example.screens

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Videocam
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import com.example.components.AnimatedFramePlayer
import com.example.components.AspectRatioSelector
import com.example.components.ErrorFeedbackCard
import com.example.components.FirebaseNoticeBanner
import com.example.components.GenerationProgressCard
import com.example.components.SourceImagePickerCard
import com.example.components.StudioCard
import com.example.components.StudioChipRow
import com.example.components.StudioSectionHeader
import com.example.components.StudioTopBar
import com.example.types.AiToolId
import com.example.types.CameraMotion
import com.example.types.DownloadState
import com.example.types.GenerationState
import com.example.ui.theme.CyanSpark
import com.example.ui.theme.EmeraldGlow
import com.example.ui.theme.IndigoElectric
import com.example.ui.theme.RoseNeon
import com.example.ui.theme.StudioCardBorder
import com.example.ui.theme.StudioObsidian
import com.example.ui.theme.StudioSurfaceElevated
import com.example.utilities.PromptTemplates
import com.example.viewmodel.CreativeToolViewModel
import java.io.File

@Composable
fun CreativeToolScreen(
    toolId: AiToolId,
    viewModel: CreativeToolViewModel,
    onBack: () -> Unit,
    onSendToImageStudio: (String) -> Unit,
    onSendToVideoStudio: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    LaunchedEffect(toolId) {
        viewModel.selectTool(toolId)
    }

    val input by viewModel.inputState.collectAsStateWithLifecycle()
    val toolState by viewModel.toolState.collectAsStateWithLifecycle()
    val downloadState by viewModel.downloadState.collectAsStateWithLifecycle()
    val isLoading = toolState is GenerationState.Loading

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = StudioObsidian,
        topBar = {
            StudioTopBar(
                title = toolId.title,
                subtitle = toolId.category.label,
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

                if (toolId == AiToolId.TEXT_TO_IMAGE_TEMPLATES || toolId == AiToolId.TEXT_TO_VIDEO_TEMPLATES) {
                    val templates = if (toolId == AiToolId.TEXT_TO_IMAGE_TEMPLATES) {
                        PromptTemplates.imageTemplates
                    } else {
                        PromptTemplates.videoTemplates
                    }
                    items(templates, key = { it.id }) { tpl ->
                        StudioCard {
                            StudioSectionHeader(title = tpl.title, badge = tpl.recommendedAspect)
                            Text(
                                text = "${tpl.category} • ${tpl.styleHint}",
                                style = MaterialTheme.typography.labelLarge,
                                color = CyanSpark
                            )
                            Text(
                                text = tpl.prompt,
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                OutlinedButton(
                                    onClick = {
                                        copyText(context, tpl.prompt)
                                    },
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Icon(Icons.Default.ContentCopy, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Spacer(Modifier.width(6.dp))
                                    Text("Copy")
                                }
                                Button(
                                    onClick = {
                                        if (toolId == AiToolId.TEXT_TO_IMAGE_TEMPLATES) {
                                            onSendToImageStudio(tpl.prompt)
                                        } else {
                                            onSendToVideoStudio(tpl.prompt)
                                        }
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = IndigoElectric),
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Icon(
                                        imageVector = if (toolId == AiToolId.TEXT_TO_IMAGE_TEMPLATES) Icons.Default.Image else Icons.Default.Videocam,
                                        contentDescription = null,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(Modifier.width(6.dp))
                                    Text("Open Studio")
                                }
                            }
                        }
                    }
                } else {
                    if (toolId.requiresImageInput) {
                        item {
                            StudioCard {
                                StudioSectionHeader(title = "Source Image")
                                SourceImagePickerCard(
                                    selectedUri = input.sourceImageUri,
                                    label = "Select photo for ${toolId.title}",
                                    onImagePicked = { uri -> viewModel.updateSourceImage(context, uri) }
                                )
                            }
                        }
                    }

                    val presets = optionsForTool(toolId)
                    if (presets.isNotEmpty()) {
                        item {
                            StudioCard {
                                StudioSectionHeader(title = "Preset Configuration")
                                StudioChipRow(
                                    items = presets,
                                    selectedItem = input.secondaryOption,
                                    labelProvider = { it },
                                    onSelect = viewModel::updateSecondaryOption,
                                    testTagPrefix = "tool_option"
                                )
                            }
                        }
                    }

                    item {
                        StudioCard {
                            StudioSectionHeader(
                                title = if (toolId.requiresImageInput) "Instructions (Optional)" else "Your Concept"
                            )
                            OutlinedTextField(
                                value = input.prompt,
                                onValueChange = viewModel::updatePrompt,
                                label = { Text("Enter prompt or edit instructions") },
                                placeholder = { Text(placeholderForTool(toolId)) },
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
                                    .testTag("tool_prompt_input")
                            )

                            if (toolId.outputsImage || toolId.outputsVideoPreview) {
                                StudioSectionHeader(title = "Aspect Ratio")
                                AspectRatioSelector(
                                    selectedRatio = input.aspectRatio,
                                    onSelectRatio = viewModel::updateAspectRatio
                                )
                            }
                        }
                    }

                    item {
                        Button(
                            onClick = viewModel::runTool,
                            enabled = !isLoading,
                            colors = ButtonDefaults.buttonColors(containerColor = IndigoElectric),
                            shape = MaterialTheme.shapes.medium,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(54.dp)
                                .minimumInteractiveComponentSize()
                                .testTag("run_tool_button")
                        ) {
                            Icon(Icons.Default.AutoAwesome, contentDescription = null, modifier = Modifier.size(20.dp))
                            Spacer(Modifier.width(8.dp))
                            Text(if (isLoading) "Processing..." else "Run ${toolId.title}")
                        }
                    }

                    item {
                        when (val state = toolState) {
                            is GenerationState.Idle -> Unit
                            is GenerationState.Loading -> {
                                GenerationProgressCard(progress = state.progress, stage = state.stage)
                            }
                            is GenerationState.Error -> {
                                ErrorFeedbackCard(
                                    message = state.message,
                                    canRetry = state.canRetry,
                                    onRetry = viewModel::runTool
                                )
                            }
                            is GenerationState.Success -> {
                                val result = state.data
                                StudioCard(highlighted = true) {
                                    StudioSectionHeader(title = "Tool Output")

                                    if (result.generatedVideoFrames.isNotEmpty() && result.generatedImagePath != null) {
                                        AnimatedFramePlayer(
                                            framePaths = result.generatedVideoFrames,
                                            posterPath = result.generatedImagePath,
                                            durationSeconds = 4,
                                            aspectRatio = result.aspectRatio
                                        )
                                    } else if (result.generatedImagePath != null) {
                                        val ratio = (result.aspectRatio.width.toFloat() / result.aspectRatio.height.toFloat())
                                            .coerceIn(0.55f, 1.85f)
                                        AsyncImage(
                                            model = File(result.generatedImagePath),
                                            contentDescription = "Tool generated image",
                                            contentScale = ContentScale.Crop,
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .aspectRatio(ratio)
                                                .clip(RoundedCornerShape(14.dp))
                                                .background(StudioObsidian)
                                                .testTag("tool_output_image")
                                        )
                                    }

                                    if (!result.generatedText.isNullOrBlank()) {
                                        Text(
                                            text = result.generatedText,
                                            style = MaterialTheme.typography.bodyLarge,
                                            color = MaterialTheme.colorScheme.onSurface,
                                            modifier = Modifier.testTag("tool_output_text")
                                        )
                                    }

                                    FirebaseNoticeBanner(customMessage = result.providerNotice)

                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        if (result.generatedImagePath != null) {
                                            Button(
                                                onClick = viewModel::downloadResult,
                                                enabled = downloadState !is DownloadState.Downloading,
                                                colors = ButtonDefaults.buttonColors(containerColor = IndigoElectric),
                                                modifier = Modifier
                                                    .weight(1f)
                                                    .testTag("tool_download_button")
                                            ) {
                                                Icon(Icons.Default.Download, contentDescription = null, modifier = Modifier.size(18.dp))
                                                Spacer(Modifier.width(6.dp))
                                                Text("Download")
                                            }
                                        } else if (!result.generatedText.isNullOrBlank()) {
                                            OutlinedButton(
                                                onClick = { copyText(context, result.generatedText) },
                                                modifier = Modifier.weight(1f)
                                            ) {
                                                Icon(Icons.Default.ContentCopy, contentDescription = null, modifier = Modifier.size(16.dp))
                                                Spacer(Modifier.width(6.dp))
                                                Text("Copy")
                                            }
                                            val isVideoTool = toolId == AiToolId.VIDEO_PROMPT_GENERATOR
                                            Button(
                                                onClick = {
                                                    if (isVideoTool) onSendToVideoStudio(result.generatedText)
                                                    else onSendToImageStudio(result.generatedText)
                                                },
                                                colors = ButtonDefaults.buttonColors(containerColor = IndigoElectric),
                                                modifier = Modifier.weight(1f)
                                            ) {
                                                Text(if (isVideoTool) "Send to Video" else "Send to Image")
                                            }
                                        }

                                        OutlinedButton(
                                            onClick = {
                                                viewModel.shareResult(context).onFailure { err ->
                                                    Toast.makeText(context, err.localizedMessage ?: "Share failed", Toast.LENGTH_SHORT).show()
                                                }
                                            },
                                            border = BorderStroke(1.dp, CyanSpark.copy(alpha = 0.5f)),
                                            modifier = Modifier.weight(1f)
                                        ) {
                                            Icon(Icons.Default.Share, contentDescription = null, tint = CyanSpark, modifier = Modifier.size(18.dp))
                                            Spacer(Modifier.width(6.dp))
                                            Text("Share", color = CyanSpark)
                                        }
                                    }

                                    when (val ds = downloadState) {
                                        is DownloadState.Completed -> Text(
                                            text = "Saved ${ds.fileName} to ${ds.displayLocation}",
                                            style = MaterialTheme.typography.bodyMedium,
                                            color = EmeraldGlow
                                        )
                                        is DownloadState.Failed -> Text(
                                            text = ds.message,
                                            style = MaterialTheme.typography.bodyMedium,
                                            color = RoseNeon
                                        )
                                        else -> Unit
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

private fun copyText(context: Context, text: String) {
    val cm = context.getSystemService(Context.CLIPBOARD_SERVICE) as? ClipboardManager
    cm?.setPrimaryClip(ClipData.newPlainText("VARDAAN CREATION Prompt", text))
    Toast.makeText(context, "Prompt copied to clipboard", Toast.LENGTH_SHORT).show()
}

private fun optionsForTool(toolId: AiToolId): List<String> = when (toolId) {
    AiToolId.STYLE_CHANGER -> listOf("Anime", "Cartoon", "Cinematic", "Fantasy Art", "3D Art")
    AiToolId.BACKGROUND_CHANGE -> listOf("Cyberpunk Neon City", "Golden Sunset Beach", "Minimalist Dark Studio", "Enchanted Forest")
    AiToolId.BACKGROUND_REMOVAL -> listOf("Clean Transparent Checkerboard", "Pure Obsidian Cutout")
    AiToolId.IMAGE_UPSCALING -> listOf("2x Crisp Detail", "4x Ultra Sharp")
    AiToolId.AI_IMAGE_EDITOR -> listOf("Cinematic Lighting Boost", "Anime Color Pop", "Moody Noir Grade")
    AiToolId.IMAGE_TO_ANIMATION -> CameraMotion.entries.map { it.label }
    AiToolId.PROMPT_ENHANCER,
    AiToolId.VIDEO_PROMPT_GENERATOR,
    AiToolId.CINEMATIC_PROMPT_GENERATOR,
    AiToolId.ANIME_PROMPT_GENERATOR,
    AiToolId.CARTOON_PROMPT_GENERATOR -> listOf("Dramatic Lighting", "Soft Pastel", "Neon Cyberpunk", "Golden Hour")
    else -> emptyList()
}

private fun placeholderForTool(toolId: AiToolId): String = when (toolId) {
    AiToolId.AI_CHARACTER_CREATOR -> "e.g., Cybernetic archer with obsidian cloak and glowing cyan bow"
    AiToolId.AI_SCENE_CREATOR -> "e.g., Ancient misty temple floating above emerald waterfalls"
    AiToolId.AI_THUMBNAIL_CREATOR -> "e.g., Shocked gamer reaction, neon lightning split background, bold energy"
    AiToolId.VIDEO_PROMPT_GENERATOR -> "e.g., Drone flying through neon skyscrapers in rain"
    else -> "Describe your creative idea..."
}
