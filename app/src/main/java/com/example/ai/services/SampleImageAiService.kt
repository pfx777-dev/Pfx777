package com.example.ai.services

import android.content.Context
import com.example.types.GeneratedImageResult
import com.example.types.ImageGenerationRequest
import com.example.types.ImageStyle
import com.example.utilities.ErrorMapper
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import java.util.UUID

class SampleImageAiService(
    private val context: Context
) : ImageAiService {

    override fun providerStatus(): AiProviderStatus {
        return AiProviderStatus(
            isCloudConfigured = false,
            providerName = "Sample Preview Engine (Firebase AI Logic Required)",
            statusHeadline = "Sample Preview Mode",
            statusDetail = "Cloud AI image generation requires Firebase AI Logic to be enabled. Local preview rendering and photo transformations are active."
        )
    }

    override suspend fun generateImage(
        request: ImageGenerationRequest,
        onProgress: (Float, String) -> Unit
    ): GeneratedImageResult = withContext(Dispatchers.Default) {
        val cleanPrompt = request.prompt.trim()
        require(cleanPrompt.isNotBlank()) { "Please enter a creative prompt before generating." }

        if (request.mode.requiresSourceImage) {
            val sourceUri = request.sourceImageUri
                ?: throw IllegalArgumentException("Please select a source image for ${request.mode.label}.")
            val validationError = ErrorMapper.validatePickedImageUri(context, sourceUri)
            if (validationError != null) {
                throw IllegalArgumentException(validationError)
            }
        }

        val resolvedStyle = if (request.style == ImageStyle.CUSTOM && request.customStyleText.isNotBlank()) {
            request.customStyleText.trim()
        } else {
            request.style.label
        }

        val effectivePrompt = buildString {
            append(cleanPrompt)
            val modifier = if (request.style == ImageStyle.CUSTOM) request.customStyleText else request.style.promptModifier
            if (modifier.isNotBlank()) {
                append(", ").append(modifier)
            }
            if (request.negativePrompt.isNotBlank()) {
                append(" | Avoid: ").append(request.negativePrompt.trim())
            }
        }

        onProgress(0.22f, "Validating studio parameters...")
        delay(220)

        val sourceBitmap = request.sourceImageUri?.let { uri ->
            LocalBitmapProcessor.loadScaledBitmap(context, uri, 1080)
                ?: throw IllegalArgumentException("Unable to decode the selected image file.")
        }

        onProgress(0.55f, "Rendering $resolvedStyle preview (${request.aspectRatio.label})...")
        delay(260)

        val targetW = (request.aspectRatio.width * request.resolution.scaleFactor).toInt().coerceAtMost(1920)
        val targetH = (request.aspectRatio.height * request.resolution.scaleFactor).toInt().coerceAtMost(1920)
        val id = UUID.randomUUID().toString()

        val rendered = LocalBitmapProcessor.renderSampleCreativeBitmap(
            width = targetW,
            height = targetH,
            prompt = cleanPrompt,
            styleLabel = resolvedStyle,
            aspectLabel = request.aspectRatio.label,
            seedHash = effectivePrompt.hashCode(),
            sourceBitmap = sourceBitmap,
            filterType = resolvedStyle
        )
        sourceBitmap?.recycle()

        onProgress(0.88f, "Saving preview to studio workspace...")
        val savedPath = LocalBitmapProcessor.saveBitmapToCreationsDir(
            context = context,
            bitmap = rendered,
            fileName = "img_${id}.png"
        )
        rendered.recycle()

        onProgress(1.0f, "Preview ready")

        GeneratedImageResult(
            id = id,
            localFilePath = savedPath,
            prompt = cleanPrompt,
            effectivePrompt = effectivePrompt,
            styleLabel = resolvedStyle,
            aspectRatio = request.aspectRatio,
            resolution = request.resolution,
            widthPx = targetW,
            heightPx = targetH,
            createdAtMillis = System.currentTimeMillis(),
            isSamplePreview = true,
            providerNotice = "Sample Preview Mode: Cloud AI image synthesis requires Firebase AI Logic."
        )
    }
}
