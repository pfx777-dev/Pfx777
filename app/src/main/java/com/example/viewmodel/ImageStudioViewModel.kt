package com.example.viewmodel

import android.content.Context
import android.net.Uri
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.ai.services.AiProviderStatus
import com.example.ai.services.ImageAiService
import com.example.downloads.DownloadService
import com.example.downloads.ShareService
import com.example.storage.CreationRepository
import com.example.storage.SettingsDataStore
import com.example.types.CreationHistoryItem
import com.example.types.CreationType
import com.example.types.DownloadState
import com.example.types.GeneratedImageResult
import com.example.types.GenerationState
import com.example.types.ImageGenerationRequest
import com.example.types.ImageMode
import com.example.types.ImageResolution
import com.example.types.ImageStyle
import com.example.types.StudioAspectRatio
import com.example.utilities.ErrorMapper
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeout

data class ImageStudioInputState(
    val mode: ImageMode = ImageMode.TEXT_TO_IMAGE,
    val prompt: String = "",
    val negativePrompt: String = "",
    val selectedStyle: ImageStyle = ImageStyle.CINEMATIC,
    val customStyleText: String = "",
    val aspectRatio: StudioAspectRatio = StudioAspectRatio.RATIO_1_1,
    val resolution: ImageResolution = ImageResolution.RES_1K,
    val sourceImageUri: Uri? = null,
    val isSavedLocally: Boolean = false
)

class ImageStudioViewModel(
    private val imageAiService: ImageAiService,
    private val repository: CreationRepository,
    private val downloadService: DownloadService,
    private val settingsDataStore: SettingsDataStore
) : ViewModel() {

    private val _inputState = MutableStateFlow(ImageStudioInputState())
    val inputState: StateFlow<ImageStudioInputState> = _inputState.asStateFlow()

    private val _generationState = MutableStateFlow<GenerationState<GeneratedImageResult>>(GenerationState.Idle)
    val generationState: StateFlow<GenerationState<GeneratedImageResult>> = _generationState.asStateFlow()

    private val _downloadState = MutableStateFlow<DownloadState>(DownloadState.Idle)
    val downloadState: StateFlow<DownloadState> = _downloadState.asStateFlow()

    val providerStatus: AiProviderStatus = imageAiService.providerStatus()

    init {
        viewModelScope.launch {
            val settings = settingsDataStore.settingsFlow.first()
            _inputState.update {
                it.copy(
                    aspectRatio = settings.defaultImageAspectRatio,
                    resolution = settings.defaultImageResolution
                )
            }
        }
    }

    fun applyPrefill(prompt: String?, mode: ImageMode? = null, style: ImageStyle? = null) {
        _inputState.update { current ->
            current.copy(
                prompt = prompt?.takeIf { it.isNotBlank() } ?: current.prompt,
                mode = mode ?: current.mode,
                selectedStyle = style ?: current.selectedStyle
            )
        }
    }

    fun updateMode(mode: ImageMode) {
        _inputState.update { it.copy(mode = mode) }
    }

    fun updatePrompt(prompt: String) {
        _inputState.update { it.copy(prompt = prompt) }
    }

    fun updateNegativePrompt(negativePrompt: String) {
        _inputState.update { it.copy(negativePrompt = negativePrompt) }
    }

    fun updateStyle(style: ImageStyle) {
        _inputState.update { it.copy(selectedStyle = style) }
    }

    fun updateCustomStyleText(text: String) {
        _inputState.update { it.copy(customStyleText = text) }
    }

    fun updateAspectRatio(ratio: StudioAspectRatio) {
        _inputState.update { it.copy(aspectRatio = ratio) }
    }

    fun updateResolution(resolution: ImageResolution) {
        _inputState.update { it.copy(resolution = resolution) }
    }

    fun updateSourceImage(context: Context, uri: Uri?) {
        if (uri == null) {
            _inputState.update { it.copy(sourceImageUri = null) }
            return
        }
        val err = ErrorMapper.validatePickedImageUri(context, uri)
        if (err != null) {
            _generationState.value = GenerationState.Error(err, canRetry = false)
            return
        }
        _inputState.update { it.copy(sourceImageUri = uri) }
        if (_generationState.value is GenerationState.Error) {
            _generationState.value = GenerationState.Idle
        }
    }

    fun generateImage() {
        if (_generationState.value is GenerationState.Loading) return

        val input = _inputState.value
        if (input.prompt.isBlank()) {
            _generationState.value = GenerationState.Error("Please enter a prompt to generate an image.", canRetry = false)
            return
        }
        if (input.mode.requiresSourceImage && input.sourceImageUri == null) {
            _generationState.value = GenerationState.Error("Please pick a source image for ${input.mode.label}.", canRetry = false)
            return
        }

        _downloadState.value = DownloadState.Idle
        _inputState.update { it.copy(isSavedLocally = false) }

        viewModelScope.launch {
            _generationState.value = GenerationState.Loading(0.08f, "Preparing studio canvas...")
            try {
                val request = ImageGenerationRequest(
                    mode = input.mode,
                    prompt = input.prompt,
                    negativePrompt = input.negativePrompt,
                    style = input.selectedStyle,
                    customStyleText = input.customStyleText,
                    aspectRatio = input.aspectRatio,
                    resolution = input.resolution,
                    sourceImageUri = input.sourceImageUri
                )
                val result = withTimeout(60_000L) {
                    imageAiService.generateImage(request) { progress, stage ->
                        _generationState.value = GenerationState.Loading(progress, stage)
                    }
                }
                _generationState.value = GenerationState.Success(result)

                val autoSave = settingsDataStore.settingsFlow.first().autoSaveToHistory
                if (autoSave) {
                    persistToHistory(result, isSaved = false)
                }
            } catch (e: TimeoutCancellationException) {
                Log.e(TAG, "Image generation timed out", e)
                _generationState.value = GenerationState.Error("Image generation timed out after 60 seconds. Please retry.")
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                Log.e(TAG, "Image generation failed", e)
                _generationState.value = GenerationState.Error(ErrorMapper.toUserMessage(e))
            }
        }
    }

    fun saveCurrentLocally() {
        val current = (_generationState.value as? GenerationState.Success)?.data ?: return
        viewModelScope.launch {
            persistToHistory(current, isSaved = true)
            _inputState.update { it.copy(isSavedLocally = true) }
        }
    }

    fun downloadCurrent() {
        if (_downloadState.value is DownloadState.Downloading) return
        val current = (_generationState.value as? GenerationState.Success)?.data ?: return
        viewModelScope.launch {
            persistToHistory(current, isSaved = _inputState.value.isSavedLocally)
            downloadService.downloadCreation(
                creationId = current.id,
                title = current.prompt.take(40),
                type = CreationType.IMAGE,
                sourcePath = current.localFilePath,
                isVideoMp4 = false
            ) { state ->
                _downloadState.value = state
            }
        }
    }

    fun shareCurrent(context: Context): Result<Unit> {
        val current = (_generationState.value as? GenerationState.Success)?.data
            ?: return Result.failure(IllegalStateException("No generated image to share."))
        return ShareService.shareFile(
            context = context,
            filePath = current.localFilePath,
            mimeType = "image/png",
            title = "VARDAAN CREATION — ${current.styleLabel}"
        )
    }

    fun dismissDownloadNotice() {
        _downloadState.value = DownloadState.Idle
    }

    private suspend fun persistToHistory(result: GeneratedImageResult, isSaved: Boolean) {
        repository.saveHistoryItem(
            CreationHistoryItem(
                id = result.id,
                type = CreationType.IMAGE,
                title = "${result.styleLabel} • ${result.aspectRatio.label}",
                prompt = result.prompt,
                styleLabel = result.styleLabel,
                aspectRatioLabel = result.aspectRatio.label,
                mediaPath = result.localFilePath,
                isSaved = isSaved,
                createdAtMillis = result.createdAtMillis
            )
        )
    }

    companion object {
        private const val TAG = "ImageStudioViewModel"

        fun provideFactory(
            imageAiService: ImageAiService,
            repository: CreationRepository,
            downloadService: DownloadService,
            settingsDataStore: SettingsDataStore
        ): ViewModelProvider.Factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                return ImageStudioViewModel(imageAiService, repository, downloadService, settingsDataStore) as T
            }
        }
    }
}
