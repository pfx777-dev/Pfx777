package com.example.types

import android.net.Uri

data class ImageGenerationRequest(
    val mode: ImageMode,
    val prompt: String,
    val negativePrompt: String = "",
    val style: ImageStyle = ImageStyle.CINEMATIC,
    val customStyleText: String = "",
    val aspectRatio: StudioAspectRatio = StudioAspectRatio.RATIO_1_1,
    val resolution: ImageResolution = ImageResolution.RES_1K,
    val sourceImageUri: Uri? = null
)

data class GeneratedImageResult(
    val id: String,
    val localFilePath: String,
    val prompt: String,
    val effectivePrompt: String,
    val styleLabel: String,
    val aspectRatio: StudioAspectRatio,
    val resolution: ImageResolution,
    val widthPx: Int,
    val heightPx: Int,
    val createdAtMillis: Long,
    val isSamplePreview: Boolean,
    val providerNotice: String
)

data class VideoGenerationRequest(
    val mode: VideoMode,
    val prompt: String,
    val motionPrompt: String = "",
    val style: VideoStyle = VideoStyle.CINEMATIC,
    val customStyleText: String = "",
    val cameraMotion: CameraMotion = CameraMotion.DOLLY_IN,
    val aspectRatio: StudioAspectRatio = StudioAspectRatio.RATIO_16_9,
    val duration: VideoDuration = VideoDuration.SEC_4,
    val quality: VideoQuality = VideoQuality.HD_720,
    val sourceImageUri: Uri? = null
)

data class GeneratedVideoResult(
    val id: String,
    val posterFilePath: String,
    val frameFilePaths: List<String>,
    val exportedMp4Path: String?,
    val prompt: String,
    val effectivePrompt: String,
    val styleLabel: String,
    val cameraMotion: CameraMotion,
    val aspectRatio: StudioAspectRatio,
    val durationSeconds: Int,
    val quality: VideoQuality,
    val createdAtMillis: Long,
    val isSamplePreview: Boolean,
    val providerNotice: String
)

data class CreativeToolRequest(
    val toolId: AiToolId,
    val prompt: String,
    val secondaryOption: String = "",
    val aspectRatio: StudioAspectRatio = StudioAspectRatio.RATIO_1_1,
    val sourceImageUri: Uri? = null
)

data class CreativeToolResult(
    val id: String,
    val toolId: AiToolId,
    val generatedText: String? = null,
    val generatedImagePath: String? = null,
    val generatedVideoFrames: List<String> = emptyList(),
    val cameraMotion: CameraMotion = CameraMotion.DOLLY_IN,
    val aspectRatio: StudioAspectRatio = StudioAspectRatio.RATIO_1_1,
    val isSamplePreview: Boolean = true,
    val providerNotice: String,
    val createdAtMillis: Long = System.currentTimeMillis()
)

data class CreationHistoryItem(
    val id: String,
    val type: CreationType,
    val title: String,
    val prompt: String,
    val styleLabel: String,
    val aspectRatioLabel: String,
    val mediaPath: String,
    val secondaryMediaPaths: List<String> = emptyList(),
    val durationSeconds: Int = 0,
    val isSaved: Boolean = false,
    val isDownloaded: Boolean = false,
    val downloadedDisplayPath: String? = null,
    val createdAtMillis: Long
)

data class DownloadedCreationItem(
    val id: String,
    val title: String,
    val type: CreationType,
    val fileName: String,
    val filePath: String,
    val contentUriString: String?,
    val fileSizeBytes: Long,
    val downloadedAtMillis: Long
)

data class StudioSettings(
    val defaultImageAspectRatio: StudioAspectRatio = StudioAspectRatio.RATIO_1_1,
    val defaultImageResolution: ImageResolution = ImageResolution.RES_1K,
    val defaultVideoAspectRatio: StudioAspectRatio = StudioAspectRatio.RATIO_16_9,
    val defaultVideoDuration: VideoDuration = VideoDuration.SEC_4,
    val defaultVideoQuality: VideoQuality = VideoQuality.HD_720,
    val autoSaveToHistory: Boolean = true
)
