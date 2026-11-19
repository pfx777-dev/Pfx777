package com.example.screens

import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import com.example.R
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
import com.example.types.GenerationState
import com.example.types.ImageMode
import com.example.types.ImageResolution
import com.example.types.ImageStyle
import com.example.ui.theme.CyanSpark
import com.example.ui.theme.IndigoElectric
import com.example.ui.theme.StudioCardBorder
import com.example.ui.theme.StudioObsidian
import com.example.ui.theme.StudioSurfaceElevated
import com.example.viewmodel.ImageStudioViewModel
import java.io.File

@Composable
fun ImageStudioScreen(
    viewModel: ImageStudioViewModel,
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
                title = "AI Image Studio",
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
                    .widthIn(max = 760.dp)
                    .testTag("image_studio_list"),
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                item {
                    FirebaseNoticeBanner(customMessage = viewModel.providerStatus.statusDetail)
                }

                // Mode & Source Picker
                item {
                    StudioCard {
                        StudioSectionHeader(title = "Workflow Mode")
                        StudioChipRow(
                            items = ImageMode.entries,
                            selectedItem = input.mode,
                            labelProvider = { it.label },
                            onSelect = viewModel::updateMode,
                            testTagPrefix = "image_mode"
                        )

                        AnimatedVisibility(visible = input.mode.requiresSourceImage) {
                            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                StudioSectionHeader(title = "Reference Photo")
                                SourceImagePickerCard(
                                    selectedUri = input.sourceImageUri,
                                    label = "Select photo for ${input.mode.label}",
                                    onImagePicked = { uri -> viewModel.updateSourceImage(context, uri) }
                                )
                            }
                        }
                    }
                }

                // Prompt & Negative Prompt
                item {
                    StudioCard {
                        StudioSectionHeader(title = "Creative Prompt")
                        OutlinedTextField(
                            value = input.prompt,
                            onValueChange = viewModel::updatePrompt,
                            label = { Text("Describe what you want to create or edit") },
                            placeholder = { Text("e.g., Futuristic cyber samurai under neon rain, volumetric light...") },
                            minLines = 3,
                            maxLines = 6,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedContainerColor = StudioSurfaceElevated,
                                unfocusedContainerColor = StudioSurfaceElevated,
                                focusedBorderColor = CyanSpark,
                                unfocusedBorderColor = StudioCardBorder
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("image_prompt_input")
                        )

                        OutlinedTextField(
                            value = input.negativePrompt,
                            onValueChange = viewModel::updateNegativePrompt,
                            label = { Text("Negative prompt (optional)") },
                            placeholder = { Text("blurry, distorted anatomy, watermark, low contrast") },
                            singleLine = true,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedContainerColor = StudioSurfaceElevated,
                                unfocusedContainerColor = StudioSurfaceElevated,
                                focusedBorderColor = IndigoElectric,
                                unfocusedBorderColor = StudioCardBorder
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("image_negative_prompt_input")
                        )
                    }
                }

                // Style, Aspect Ratio, Resolution
                item {
                    StudioCard {
                        StudioSectionHeader(title = "Artistic Style", badge = "${ImageStyle.entries.size} STYLES")
                        StudioChipRow(
                            items = ImageStyle.entries,
                            selectedItem = input.selectedStyle,
                            labelProvider = { it.label },
                            onSelect = viewModel::updateStyle,
                            testTagPrefix = "image_style"
                        )

                        AnimatedVisibility(visible = input.selectedStyle == ImageStyle.CUSTOM) {
                            OutlinedTextField(
                                value = input.customStyleText,
                                onValueChange = viewModel::updateCustomStyleText,
                                label = { Text("Custom style keywords") },
                                placeholder = { Text("e.g., synthwave watercolor, gold foil ink") },
                                singleLine = true,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("image_custom_style_input")
                            )
                        }

                        StudioSectionHeader(title = "Aspect Ratio")
                        AspectRatioSelector(
                            selectedRatio = input.aspectRatio,
                            onSelectRatio = viewModel::updateAspectRatio
                        )

                        StudioSectionHeader(title = "Resolution")
                        StudioChipRow(
                            items = ImageResolution.entries,
                            selectedItem = input.resolution,
                            labelProvider = { it.label },
                            onSelect = viewModel::updateResolution,
                            testTagPrefix = "image_res"
                        )
                    }
                }

                // Generate Button
                item {
                    Button(
                        onClick = viewModel::generateImage,
                        enabled = !isLoading,
                        colors = ButtonDefaults.buttonColors(containerColor = IndigoElectric),
                        shape = MaterialTheme.shapes.medium,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(54.dp)
                            .minimumInteractiveComponentSize()
                            .testTag("generate_image_button")
                    ) {
                        Icon(Icons.Default.AutoAwesome, contentDescription = null, modifier = Modifier.size(20.dp))
                        Spacer(Modifier.width(8.dp))
                        Text(
                            text = if (isLoading) "Generating..." else "Generate Image",
                            style = MaterialTheme.typography.titleMedium
                        )
                    }
                }

                // Output States
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
                                onRetry = viewModel::generateImage
                            )
                        }
                        is GenerationState.Success -> {
                            val result = state.data
                            val ratio = (result.aspectRatio.width.toFloat() / result.aspectRatio.height.toFloat())
                                .coerceIn(0.55f, 1.85f)
                            StudioCard(highlighted = true) {
                                StudioSectionHeader(
                                    title = "Generated Output",
                                    badge = "${result.widthPx}×${result.heightPx}"
                                )
                                AsyncImage(
                                    model = File(result.localFilePath),
                                    contentDescription = stringResource(R.string.cd_generated_preview),
                                    contentScale = ContentScale.Crop,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .aspectRatio(ratio)
                                        .clip(RoundedCornerShape(14.dp))
                                        .background(StudioObsidian)
                                        .testTag("generated_image_preview")
                                )
                                Text(
                                    text = "Effective Prompt: ${result.effectivePrompt}",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                if (result.isSamplePreview) {
                                    FirebaseNoticeBanner(customMessage = result.providerNotice)
                                }
                                CreationActionBar(
                                    isSavedLocally = input.isSavedLocally,
                                    downloadState = downloadState,
                                    onRegenerate = viewModel::generateImage,
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
