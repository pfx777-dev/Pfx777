package com.example.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.components.AspectRatioSelector
import com.example.components.FirebaseNoticeBanner
import com.example.components.StudioCard
import com.example.components.StudioChipRow
import com.example.components.StudioSectionHeader
import com.example.components.StudioTopBar
import com.example.types.ImageResolution
import com.example.types.VideoDuration
import com.example.types.VideoQuality
import com.example.ui.theme.CyanSpark
import com.example.ui.theme.EmeraldGlow
import com.example.ui.theme.IndigoElectric
import com.example.ui.theme.RoseNeon
import com.example.ui.theme.StudioObsidian
import com.example.ui.theme.StudioSurface
import com.example.viewmodel.SettingsViewModel

@Composable
fun SettingsScreen(
    viewModel: SettingsViewModel,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val settings by viewModel.settings.collectAsStateWithLifecycle()
    val statusMessage by viewModel.statusMessage.collectAsStateWithLifecycle()
    var showClearConfirm by remember { mutableStateOf(false) }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = StudioObsidian,
        topBar = {
            StudioTopBar(
                title = "Studio Settings",
                subtitle = "VARDAAN CREATION",
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
                    StudioCard(highlighted = true) {
                        StudioSectionHeader(title = "AI Service Configuration", badge = "SAMPLE PREVIEW")
                        FirebaseNoticeBanner(
                            customMessage = "Cloud AI generation via Gemini in Android apps requires Firebase AI Logic to be enabled (with App Check). Currently active: ${viewModel.imageProviderStatus.providerName}."
                        )
                        Text(
                            text = "Image Provider: ${viewModel.imageProviderStatus.statusHeadline}\nVideo Provider: ${viewModel.videoProviderStatus.statusHeadline}\nAuthentication: No login or signup required.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                item {
                    StudioCard {
                        StudioSectionHeader(title = "Image Studio Defaults")
                        AspectRatioSelector(
                            selectedRatio = settings.defaultImageAspectRatio,
                            onSelectRatio = viewModel::setDefaultImageRatio
                        )
                        StudioChipRow(
                            items = ImageResolution.entries,
                            selectedItem = settings.defaultImageResolution,
                            labelProvider = { it.label },
                            onSelect = viewModel::setDefaultImageResolution,
                            testTagPrefix = "settings_img_res"
                        )
                    }
                }

                item {
                    StudioCard {
                        StudioSectionHeader(title = "Video Studio Defaults")
                        AspectRatioSelector(
                            selectedRatio = settings.defaultVideoAspectRatio,
                            onSelectRatio = viewModel::setDefaultVideoRatio
                        )
                        StudioChipRow(
                            items = VideoDuration.entries,
                            selectedItem = settings.defaultVideoDuration,
                            labelProvider = { it.label },
                            onSelect = viewModel::setDefaultVideoDuration,
                            testTagPrefix = "settings_vid_dur"
                        )
                        StudioChipRow(
                            items = VideoQuality.entries,
                            selectedItem = settings.defaultVideoQuality,
                            labelProvider = { it.label },
                            onSelect = viewModel::setDefaultVideoQuality,
                            testTagPrefix = "settings_vid_qual"
                        )
                    }
                }

                item {
                    StudioCard {
                        StudioSectionHeader(title = "Local Storage & History")
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "Auto-save creations to History",
                                    style = MaterialTheme.typography.titleSmall
                                )
                                Text(
                                    text = "Stores generated previews on-device in Room",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            Switch(
                                checked = settings.autoSaveToHistory,
                                onCheckedChange = viewModel::setAutoSaveHistory,
                                colors = SwitchDefaults.colors(
                                    checkedThumbColor = CyanSpark,
                                    checkedTrackColor = IndigoElectric
                                ),
                                modifier = Modifier.testTag("settings_autosave_switch")
                            )
                        }

                        OutlinedButton(
                            onClick = { showClearConfirm = true },
                            border = BorderStroke(1.dp, RoseNeon.copy(alpha = 0.6f)),
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = RoseNeon),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("settings_clear_history_button")
                        ) {
                            Text("Clear Local Generation History")
                        }

                        if (!statusMessage.isNullOrBlank()) {
                            Text(
                                text = statusMessage.orEmpty(),
                                style = MaterialTheme.typography.bodyMedium,
                                color = EmeraldGlow
                            )
                        }
                    }
                }
            }
        }
    }

    if (showClearConfirm) {
        AlertDialog(
            onDismissRequest = { showClearConfirm = false },
            containerColor = StudioSurface,
            title = { Text("Clear local history?") },
            text = { Text("This removes all saved creation history records from this device. Downloaded gallery files remain untouched.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.clearHistory()
                        showClearConfirm = false
                    }
                ) { Text("Clear", color = RoseNeon) }
            },
            dismissButton = {
                TextButton(onClick = { showClearConfirm = false }) { Text("Cancel") }
            }
        )
    }
}
