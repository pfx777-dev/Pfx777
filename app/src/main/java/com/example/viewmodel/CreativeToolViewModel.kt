package com.example.viewmodel

import android.content.Context
import android.net.Uri
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.ai.services.CreativeTextAiService
import com.example.downloads.DownloadService
import com.example.downloads.ShareService
import com.example.storage.CreationRepository
import com.example.types.AiToolId
import com.example.types.CameraMotion
import com.example.types.CreationHistoryItem
import com.example.types.CreationType
import com.example.types.CreativeToolRequest
import com.example.types.CreativeToolResult
import com.example.types.DownloadState
import com.example.types.GenerationState
import com.example.types.StudioAspectRatio
import com.example.utilities.ErrorMapper
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeout

data class CreativeToolInputState(
    val toolId: AiToolId = AiToolId.PROMPT_ENHANCER,
    val prompt: String = "",
    val secondaryOption: String = "",
    val aspectRatio: StudioAspectRatio = StudioAspectRatio.RATIO_1_1,
    val sourceImageUri: Uri? = null
)

class CreativeToolViewModel(
    private val creativeTextAiService: CreativeTextAiService,
    private val repository: CreationRepository,
    private val downloadService: DownloadService
) : ViewModel() {

    private val _inputState = MutableStateFlow(CreativeToolInputState())
    val inputState: StateFlow<CreativeToolInputState> = _inputState.asStateFlow()

    private val _toolState = MutableStateFlow<GenerationState<CreativeToolResult>>(GenerationState.Idle)
    val toolState: StateFlow<GenerationState<CreativeToolResult>> = _toolState.asStateFlow()

    private val _downloadState = MutableStateFlow<DownloadState>(DownloadState.Idle)
    val downloadState: StateFlow<DownloadState> = _downloadState.asStateFlow()

    val providerStatus = creativeTextAiService.providerStatus()

    fun selectTool(toolId: AiToolId) {
        if (_inputState.value.toolId != toolId) {
            val defaultOption = defaultOptionFor(toolId)
            val defaultRatio = if (toolId == AiToolId.AI_THUMBNAIL_CREATOR) {
                StudioAspectRatio.RATIO_16_9
            } else {
                StudioAspectRatio.RATIO_1_1
            }
            _inputState.value = CreativeToolInputState(
                toolId = toolId,
                secondaryOption = defaultOption,
                aspectRatio = defaultRatio
            )
            _toolState.value = GenerationState.Idle
            _downloadState.value = DownloadState.Idle
        }
    }

    fun updatePrompt(prompt: String) { _inputState.update { it.copy(prompt = prompt) } }
    fun updateSecondaryOption(option: String) { _inputState.update { it.copy(secondaryOption = option) } }
    fun updateAspectRatio(ratio: StudioAspectRatio) { _inputState.update { it.copy(aspectRatio = ratio) } }

    fun updateSourceImage(context: Context, uri: Uri?) {
        if (uri == null) {
            _inputState.update { it.copy(sourceImageUri = null) }
            return
        }
        val err = ErrorMapper.validatePickedImageUri(context, uri)
        if (err != null) {
            _toolState.value = GenerationState.Error(err, canRetry = false)
            return
        }
        _inputState.update { it.copy(sourceImageUri = uri) }
        if (_toolState.value is GenerationState.Error) {
            _toolState.value = GenerationState.Idle
        }
    }

    fun runTool() {
        if (_toolState.value is GenerationState.Loading) return
        val input = _inputState.value

        if (!input.toolId.requiresImageInput && input.prompt.isBlank()) {
            _toolState.value = GenerationState.Error("Please enter a prompt or description first.", canRetry = false)
            return
        }
        if (input.toolId.requiresImageInput && input.sourceImageUri == null) {
            _toolState.value = GenerationState.Error("Please pick a source image using the Photo Picker.", canRetry = false)
            return
        }

        _downloadState.value = DownloadState.Idle
        viewModelScope.launch {
            _toolState.value = GenerationState.Loading(0.12f, "Starting ${input.toolId.title}...")
            try {
                val result = withTimeout(60_000L) {
                    creativeTextAiService.runCreativeTool(
                        CreativeToolRequest(
                            toolId = input.toolId,
                            prompt = input.prompt.ifBlank { input.toolId.title },
                            secondaryOption = input.secondaryOption,
                            aspectRatio = input.aspectRatio,
                            sourceImageUri = input.sourceImageUri
                        )
                    ) { progress, stage ->
                        _toolState.value = GenerationState.Loading(progress, stage)
                    }
                }
                _toolState.value = GenerationState.Success(result)

                repository.saveHistoryItem(
                    CreationHistoryItem(
                        id = result.id,
                        type = when {
                            result.generatedVideoFrames.isNotEmpty() -> CreationType.VIDEO
                            result.generatedImagePath != null -> CreationType.IMAGE
                            else -> CreationType.PROMPT_TOOL
                        },
                        title = input.toolId.title,
                        prompt = input.prompt.ifBlank { input.secondaryOption.ifBlank { input.toolId.title } },
                        styleLabel = input.secondaryOption.ifBlank { input.toolId.category.label },
                        aspectRatioLabel = result.aspectRatio.label,
                        mediaPath = result.generatedImagePath.orEmpty(),
                        secondaryMediaPaths = result.generatedVideoFrames,
                        durationSeconds = if (result.generatedVideoFrames.isNotEmpty()) 4 else 0,
                        isSaved = false,
                        createdAtMillis = result.createdAtMillis
                    )
                )
            } catch (e: TimeoutCancellationException) {
                Log.e(TAG, "Tool execution timed out", e)
                _toolState.value = GenerationState.Error("Operation timed out after 60 seconds. Please retry.")
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                Log.e(TAG, "Creative tool execution failed", e)
                _toolState.value = GenerationState.Error(ErrorMapper.toUserMessage(e))
            }
        }
    }

    fun downloadResult() {
        val current = (_toolState.value as? GenerationState.Success)?.data ?: return
        val path = current.generatedImagePath ?: return
        viewModelScope.launch {
            downloadService.downloadCreation(
                creationId = current.id,
                title = current.toolId.title,
                type = if (current.generatedVideoFrames.isNotEmpty()) CreationType.VIDEO else CreationType.IMAGE,
                sourcePath = path,
                isVideoMp4 = false
            ) { state ->
                _downloadState.value = state
            }
        }
    }

    fun shareResult(context: Context): Result<Unit> {
        val current = (_toolState.value as? GenerationState.Success)?.data
            ?: return Result.failure(IllegalStateException("Nothing to share yet."))
        val path = current.generatedImagePath
        return if (path != null) {
            ShareService.shareFile(context, path, "image/png", current.toolId.title)
        } else {
            ShareService.shareText(context, current.generatedText.orEmpty(), current.toolId.title)
        }
    }

    private fun defaultOptionFor(toolId: AiToolId): String = when (toolId) {
        AiToolId.STYLE_CHANGER -> "Anime"
        AiToolId.BACKGROUND_CHANGE -> "Cyberpunk Neon City"
        AiToolId.BACKGROUND_REMOVAL -> "Clean Transparent Checkerboard"
        AiToolId.IMAGE_UPSCALING -> "4x Ultra Sharp"
        AiToolId.AI_IMAGE_EDITOR -> "Cinematic Lighting Boost"
        AiToolId.IMAGE_TO_ANIMATION -> CameraMotion.DOLLY_IN.label
        AiToolId.PROMPT_ENHANCER,
        AiToolId.VIDEO_PROMPT_GENERATOR,
        AiToolId.CINEMATIC_PROMPT_GENERATOR,
        AiToolId.ANIME_PROMPT_GENERATOR,
        AiToolId.CARTOON_PROMPT_GENERATOR -> "Dramatic Lighting"
        else -> ""
    }

    companion object {
        private const val TAG = "CreativeToolViewModel"

        fun provideFactory(
            creativeTextAiService: CreativeTextAiService,
            repository: CreationRepository,
            downloadService: DownloadService
        ): ViewModelProvider.Factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                return CreativeToolViewModel(creativeTextAiService, repository, downloadService) as T
            }
        }
    }
}
