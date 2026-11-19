package com.example.ai.services

import android.content.Context
import com.example.downloads.VideoExportService
import com.example.types.AiToolId
import com.example.types.CameraMotion
import com.example.types.CreativeToolRequest
import com.example.types.CreativeToolResult
import com.example.types.ImageGenerationRequest
import com.example.types.ImageMode
import com.example.types.ImageResolution
import com.example.types.ImageStyle
import com.example.types.VideoDuration
import com.example.types.VideoGenerationRequest
import com.example.types.VideoMode
import com.example.types.VideoQuality
import com.example.types.VideoStyle
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import java.util.UUID

class SampleCreativeTextAiService(
    private val context: Context,
    private val imageAiService: ImageAiService,
    private val videoAiService: VideoAiService
) : CreativeTextAiService {

    override fun providerStatus(): AiProviderStatus {
        return AiProviderStatus(
            isCloudConfigured = false,
            providerName = "Sample Creative Engine (Firebase AI Logic Required)",
            statusHeadline = "Sample Preview Mode",
            statusDetail = "Cloud LLM prompt crafting & AI editing require Firebase AI Logic. Structured sample templates and local image filters are active."
        )
    }

    override suspend fun runCreativeTool(
        request: CreativeToolRequest,
        onProgress: (Float, String) -> Unit
    ): CreativeToolResult = withContext(Dispatchers.Default) {
        val cleanInput = request.prompt.trim()
        if (!request.toolId.requiresImageInput && cleanInput.isBlank()) {
            throw IllegalArgumentException("Please enter a prompt or concept to run ${request.toolId.title}.")
        }
        if (request.toolId.requiresImageInput && request.sourceImageUri == null) {
            throw IllegalArgumentException("Please select an image first using the Photo Picker.")
        }

        val id = UUID.randomUUID().toString()
        val notice = "Sample Preview Mode: Cloud AI enhancement requires Firebase AI Logic."

        when {
            request.toolId.outputsVideoPreview -> {
                val motion = CameraMotion.entries.firstOrNull {
                    it.label.equals(request.secondaryOption, ignoreCase = true)
                } ?: CameraMotion.DOLLY_IN
                val videoResult = videoAiService.generateVideo(
                    VideoGenerationRequest(
                        mode = VideoMode.IMAGE_TO_VIDEO,
                        prompt = cleanInput.ifBlank { "Animate uploaded image smoothly" },
                        style = VideoStyle.CREATIVE,
                        cameraMotion = motion,
                        aspectRatio = request.aspectRatio,
                        duration = VideoDuration.SEC_4,
                        quality = VideoQuality.HD_720,
                        sourceImageUri = request.sourceImageUri
                    ),
                    onProgress = onProgress
                )
                CreativeToolResult(
                    id = id,
                    toolId = request.toolId,
                    generatedText = "Generated ${videoResult.frameFilePaths.size}-frame ${motion.label} animation preview.",
                    generatedImagePath = videoResult.posterFilePath,
                    generatedVideoFrames = videoResult.frameFilePaths,
                    cameraMotion = motion,
                    aspectRatio = request.aspectRatio,
                    isSamplePreview = true,
                    providerNotice = notice
                )
            }

            request.toolId.outputsImage -> {
                val style = when (request.toolId) {
                    AiToolId.STYLE_CHANGER -> ImageStyle.entries.firstOrNull {
                        it.label.equals(request.secondaryOption, ignoreCase = true)
                    } ?: ImageStyle.ANIME
                    AiToolId.AI_ART_GENERATOR -> ImageStyle.FANTASY_ART
                    AiToolId.AI_CHARACTER_CREATOR -> ImageStyle.PORTRAIT_ART
                    AiToolId.AI_SCENE_CREATOR -> ImageStyle.LANDSCAPE_ART
                    AiToolId.AI_THUMBNAIL_CREATOR -> ImageStyle.CINEMATIC
                    else -> ImageStyle.CUSTOM
                }
                val customStyle = when (request.toolId) {
                    AiToolId.BACKGROUND_CHANGE -> "Background Change"
                    AiToolId.BACKGROUND_REMOVAL -> "Background Removal"
                    AiToolId.IMAGE_UPSCALING -> "4x Upscaled Clarity"
                    AiToolId.AI_IMAGE_EDITOR -> "AI Image Editor"
                    else -> request.secondaryOption
                }
                val resolution = if (request.toolId == AiToolId.IMAGE_UPSCALING) {
                    ImageResolution.RES_4K
                } else {
                    ImageResolution.RES_1K
                }
                val mode = if (request.toolId.requiresImageInput) ImageMode.IMAGE_EDIT else ImageMode.TEXT_TO_IMAGE
                val promptText = cleanInput.ifBlank { "${request.toolId.title}: ${request.secondaryOption}" }

                val imgResult = imageAiService.generateImage(
                    ImageGenerationRequest(
                        mode = mode,
                        prompt = promptText,
                        style = style,
                        customStyleText = customStyle,
                        aspectRatio = request.aspectRatio,
                        resolution = resolution,
                        sourceImageUri = request.sourceImageUri
                    ),
                    onProgress = onProgress
                )
                CreativeToolResult(
                    id = id,
                    toolId = request.toolId,
                    generatedText = "Rendered ${imgResult.widthPx}x${imgResult.heightPx} preview (${imgResult.styleLabel}).",
                    generatedImagePath = imgResult.localFilePath,
                    aspectRatio = request.aspectRatio,
                    isSamplePreview = true,
                    providerNotice = notice
                )
            }

            else -> {
                onProgress(0.35f, "Structuring studio prompt blueprint...")
                delay(240)
                onProgress(0.85f, "Polishing lighting, lens & composition tags...")
                delay(180)
                val crafted = buildStructuredSamplePrompt(request.toolId, cleanInput, request.secondaryOption)
                onProgress(1.0f, "Prompt ready")
                CreativeToolResult(
                    id = id,
                    toolId = request.toolId,
                    generatedText = crafted,
                    aspectRatio = request.aspectRatio,
                    isSamplePreview = true,
                    providerNotice = notice
                )
            }
        }
    }

    private fun buildStructuredSamplePrompt(toolId: AiToolId, input: String, option: String): String {
        val tone = option.ifBlank { "Dramatic Studio" }
        return when (toolId) {
            AiToolId.PROMPT_ENHANCER -> buildString {
                appendLine("[SAMPLE ENHANCED PROMPT]")
                appendLine("Subject: $input")
                appendLine("Visual Direction ($tone): crisp focal hierarchy, volumetric rim lighting, atmospheric depth, balanced negative space, ultra-clean surface textures, sharp 85mm optical clarity.")
            }
            AiToolId.VIDEO_PROMPT_GENERATOR -> buildString {
                appendLine("[SAMPLE VIDEO PROMPT]")
                appendLine("Scene: $input")
                appendLine("Motion & Pacing: Smooth 24fps stabilized dolly-in over 4 seconds, subtle foreground parallax, natural wind & light particle drift, $tone color grade.")
            }
            AiToolId.CINEMATIC_PROMPT_GENERATOR -> buildString {
                appendLine("[SAMPLE CINEMATIC PROMPT]")
                appendLine("Frame: $input")
                appendLine("Cinematography: Shot on 35mm anamorphic lens, shallow f/1.8 depth of field, golden-amber key light against deep indigo shadows, subtle film halation, $tone mood.")
            }
            AiToolId.ANIME_PROMPT_GENERATOR -> buildString {
                appendLine("[SAMPLE ANIME PROMPT]")
                appendLine("Key Visual: $input")
                appendLine("Anime Art Direction: Crisp cel-shaded linework, luminous sky gradient, expressive windswept hair & fabric motion, glowing rim highlights, $tone palette.")
            }
            AiToolId.CARTOON_PROMPT_GENERATOR -> buildString {
                appendLine("[SAMPLE CARTOON PROMPT]")
                appendLine("Character & Scene: $input")
                appendLine("Toon Style: Bold expressive silhouettes, playful squash-and-stretch energy, saturated complementary colors, clean studio vector finish ($tone).")
            }
            else -> input
        }.trim()
    }
}
