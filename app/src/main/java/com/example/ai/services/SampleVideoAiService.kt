package com.example.ai.services

import android.content.Context
import android.graphics.BitmapFactory
import com.example.downloads.VideoExportService
import com.example.types.GeneratedVideoResult
import com.example.types.VideoGenerationRequest
import com.example.types.VideoStyle
import com.example.utilities.ErrorMapper
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import java.io.File
import java.util.UUID

class SampleVideoAiService(
    private val context: Context,
    private val videoExportService: VideoExportService
) : VideoAiService {

    override fun providerStatus(): AiProviderStatus {
        return AiProviderStatus(
            isCloudConfigured = false,
            providerName = "Sample Motion Preview Engine (Cloud Video API / Firebase AI Required)",
            statusHeadline = "Sample Motion Preview Mode",
            statusDetail = "Cloud AI video rendering requires a connected video provider or Firebase AI Logic. Local animated motion previews & MP4 exports are active."
        )
    }

    override suspend fun generateVideo(
        request: VideoGenerationRequest,
        onProgress: (Float, String) -> Unit
    ): GeneratedVideoResult = withContext(Dispatchers.Default) {
        val cleanPrompt = request.prompt.trim()
        require(cleanPrompt.isNotBlank()) { "Please enter a video scene prompt before generating." }

        if (request.mode.requiresSourceImage) {
            val sourceUri = request.sourceImageUri
                ?: throw IllegalArgumentException("Please select a source image for ${request.mode.label}.")
            val validationError = ErrorMapper.validatePickedImageUri(context, sourceUri)
            if (validationError != null) {
                throw IllegalArgumentException(validationError)
            }
        }

        val resolvedStyle = if (request.style == VideoStyle.CUSTOM && request.customStyleText.isNotBlank()) {
            request.customStyleText.trim()
        } else {
            request.style.label
        }

        val effectivePrompt = buildString {
            append(cleanPrompt)
            if (request.motionPrompt.isNotBlank()) {
                append(" | Motion: ").append(request.motionPrompt.trim())
            }
            append(" | Camera: ").append(request.cameraMotion.label)
            if (request.style.promptModifier.isNotBlank()) {
                append(" | ").append(request.style.promptModifier)
            }
        }

        onProgress(0.18f, "Composing ${request.cameraMotion.label} motion path...")
        delay(220)

        val sourceBitmap = request.sourceImageUri?.let { uri ->
            LocalBitmapProcessor.loadScaledBitmap(context, uri, 960)
                ?: throw IllegalArgumentException("Unable to decode the selected image.")
        }

        val id = UUID.randomUUID().toString()
        val baseBitmap = LocalBitmapProcessor.renderSampleCreativeBitmap(
            width = request.aspectRatio.width,
            height = request.aspectRatio.height,
            prompt = cleanPrompt,
            styleLabel = resolvedStyle,
            aspectLabel = "${request.aspectRatio.label} • ${request.duration.label}",
            seedHash = effectivePrompt.hashCode(),
            sourceBitmap = sourceBitmap,
            filterType = resolvedStyle
        )
        sourceBitmap?.recycle()

        val posterPath = LocalBitmapProcessor.saveBitmapToCreationsDir(
            context = context,
            bitmap = baseBitmap,
            fileName = "vid_poster_${id}.png"
        )

        onProgress(0.48f, "Synthesizing ${request.duration.seconds}s animated sequence...")
        val frameCount = (request.duration.seconds * 4).coerceIn(12, 24)
        val framePaths = LocalBitmapProcessor.renderVideoMotionFrames(
            context = context,
            creationId = id,
            baseBitmap = baseBitmap,
            frameCount = frameCount,
            cameraMotion = request.cameraMotion,
            aspectRatio = request.aspectRatio,
            styleLabel = resolvedStyle
        )
        baseBitmap.recycle()

        onProgress(0.80f, "Encoding local preview clip...")
        val mp4File = File(context.filesDir, "creations/vid_${id}.mp4")
        val exportedMp4 = runCatching {
            val bitmaps = framePaths.mapNotNull { BitmapFactory.decodeFile(it) }
            try {
                if (bitmaps.isNotEmpty()) {
                    videoExportService.encodeBitmapsToMp4(bitmaps, mp4File, fps = 6)
                } else null
            } finally {
                bitmaps.forEach { it.recycle() }
            }
        }.getOrNull()

        onProgress(1.0f, "Motion preview ready")

        GeneratedVideoResult(
            id = id,
            posterFilePath = posterPath,
            frameFilePaths = framePaths,
            exportedMp4Path = exportedMp4?.absolutePath,
            prompt = cleanPrompt,
            effectivePrompt = effectivePrompt,
            styleLabel = resolvedStyle,
            cameraMotion = request.cameraMotion,
            aspectRatio = request.aspectRatio,
            durationSeconds = request.duration.seconds,
            quality = request.quality,
            createdAtMillis = System.currentTimeMillis(),
            isSamplePreview = true,
            providerNotice = "Sample Motion Preview: Full cloud video generation requires a connected cloud AI video provider / Firebase AI Logic."
        )
    }
}
