package com.example.viewmodel

import android.content.Context
import android.net.Uri
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.ai.services.AiProviderStatus
import com.example.ai.services.VideoAiService
import com.example.downloads.DownloadService
import com.example.downloads.ShareService
import com.example.storage.CreationRepository
import com.example.storage.SettingsDataStore
import com.example.types.CameraMotion
import com.example.types.CreationHistoryItem
import com.example.types.CreationType
import com.example.types.DownloadState
import com.example.types.GeneratedVideoResult
import com.example.types.GenerationState
import com.example.types.StudioAspectRatio
import com.example.types.VideoDuration
import com.example.types.VideoGenerationRequest
import com.example.types.VideoMode
import com.example.types.VideoQuality
import com.example.types.VideoStyle
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

data class VideoStudioInputState(
    val mode: VideoMode = VideoMode.TEXT_TO_VIDEO,
    val prompt: String = "",
    val motionPrompt: String = "",
    val selectedStyle: VideoStyle = VideoStyle.CINEMATIC,
    val customStyleText: String = "",
    val cameraMotion: CameraMotion = CameraMotion.DOLLY_IN,
    val aspectRatio: StudioAspectRatio = StudioAspectRatio.RATIO_16_9,
    val duration: VideoDuration = VideoDuration.SEC_4,
    val quality: VideoQuality = VideoQuality.HD_720,
    val sourceImageUri: Uri? = null,
    val isSavedLocally: Boolean = false
)

class VideoStudioViewModel(
    private val videoAiService: VideoAiService,
    private val repository: CreationRepository,
    private val downloadService: DownloadService,
    private val settingsDataStore: SettingsDataStore
) : ViewModel() {

    private val _inputState = MutableStateFlow(VideoStudioInputState())
    val inputState: StateFlow<VideoStudioInputState> = _inputState.asStateFlow()

    private val _generationState = MutableStateFlow<GenerationState<GeneratedVideoResult>>(GenerationState.Idle)
    val generationState: StateFlow<GenerationState<GeneratedVideoResult>> = _generationState.asStateFlow()

    private val _downloadState = MutableStateFlow<DownloadState>(DownloadState.Idle)
    val downloadState: StateFlow<DownloadState> = _downloadState.asStateFlow()

    val providerStatus: AiProviderStatus = videoAiService.providerStatus()

    init {
        viewModelScope.launch {
            val settings = settingsDataStore.settingsFlow.first()
            _inputState.update {
                it.copy(
                    aspectRatio = settings.defaultVideoAspectRatio,
                    duration = settings.defaultVideoDuration,
                    quality = settings.defaultVideoQuality
                )
            }
        }
    }

    fun applyPrefill(prompt: String?, mode: VideoMode? = null, style: VideoStyle? = null) {
        _inputState.update { current ->
            current.copy(
                prompt = prompt?.takeIf { it.isNotBlank() } ?: current.prompt,
                mode = mode ?: current.mode,
                selectedStyle = style ?: current.selectedStyle
            )
        }
    }

    fun updateMode(mode: VideoMode) { _inputState.update { it.copy(mode = mode) } }
    fun updatePrompt(prompt: String) { _inputState.update { it.copy(prompt = prompt) } }
    fun updateMotionPrompt(motion: String) { _inputState.update { it.copy(motionPrompt = motion) } }
    fun updateStyle(style: VideoStyle) { _inputState.update { it.copy(selectedStyle = style) } }
    fun updateCustomStyle(text: String) { _inputState.update { it.copy(customStyleText = text) } }
    fun updateCameraMotion(motion: CameraMotion) { _inputState.update { it.copy(cameraMotion = motion) } }
    fun updateAspectRatio(ratio: StudioAspectRatio) { _inputState.update { it.copy(aspectRatio = ratio) } }
    fun updateDuration(duration: VideoDuration) { _inputState.update { it.copy(duration = duration) } }
    fun updateQuality(quality: VideoQuality) { _inputState.update { it.copy(quality = quality) } }

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

    fun generateVideo() {
        if (_generationState.value is GenerationState.Loading) return
        val input = _inputState.value
        if (input.prompt.isBlank()) {
            _generationState.value = GenerationState.Error("Please enter a video scene prompt.", canRetry = false)
            return
        }
        if (input.mode.requiresSourceImage && input.sourceImageUri == null) {
            _generationState.value = GenerationState.Error("Please select a reference image for ${input.mode.label}.", canRetry = false)
            return
        }

        _downloadState.value = DownloadState.Idle
        _inputState.update { it.copy(isSavedLocally = false) }

        viewModelScope.launch {
            _generationState.value = GenerationState.Loading(0.08f, "Initializing motion pipeline...")
            try {
                val request = VideoGenerationRequest(
                    mode = input.mode,
                    prompt = input.prompt,
                    motionPrompt = input.motionPrompt,
                    style = input.selectedStyle,
                    customStyleText = input.customStyleText,
                    cameraMotion = input.cameraMotion,
                    aspectRatio = input.aspectRatio,
                    duration = input.duration,
                    quality = input.quality,
                    sourceImageUri = input.sourceImageUri
                )
                val result = withTimeout(60_000L) {
                    videoAiService.generateVideo(request) { progress, stage ->
                        _generationState.value = GenerationState.Loading(progress, stage)
                    }
                }
                _generationState.value = GenerationState.Success(result)

                val autoSave = settingsDataStore.settingsFlow.first().autoSaveToHistory
                if (autoSave) {
                    persistToHistory(result, isSaved = false)
                }
            } catch (e: TimeoutCancellationException) {
                Log.e(TAG, "Video generation timed out", e)
                _generationState.value = GenerationState.Error("Video generation timed out after 60 seconds. Please retry.")
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                Log.e(TAG, "Video generation failed", e)
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
            val exportPath = current.exportedMp4Path ?: current.posterFilePath
            val isMp4 = current.exportedMp4Path != null
            downloadService.downloadCreation(
                creationId = current.id,
                title = current.prompt.take(40),
                type = CreationType.VIDEO,
                sourcePath = exportPath,
                isVideoMp4 = isMp4
            ) { state ->
                _downloadState.value = state
            }
        }
    }

    fun shareCurrent(context: Context): Result<Unit> {
        val current = (_generationState.value as? GenerationState.Success)?.data
            ?: return Result.failure(IllegalStateException("No generated video to share."))
        val path = current.exportedMp4Path ?: current.posterFilePath
        val mime = if (current.exportedMp4Path != null) "video/mp4" else "image/png"
        return ShareService.shareFile(
            context = context,
            filePath = path,
            mimeType = mime,
            title = "VARDAAN CREATION — ${current.styleLabel}"
        )
    }

    fun dismissDownloadNotice() {
        _downloadState.value = DownloadState.Idle
    }

    private suspend fun persistToHistory(result: GeneratedVideoResult, isSaved: Boolean) {
        repository.saveHistoryItem(
            CreationHistoryItem(
                id = result.id,
                type = CreationType.VIDEO,
                title = "${result.styleLabel} • ${result.cameraMotion.label}",
                prompt = result.prompt,
                styleLabel = result.styleLabel,
                aspectRatioLabel = result.aspectRatio.label,
                mediaPath = result.exportedMp4Path ?: result.posterFilePath,
                secondaryMediaPaths = result.frameFilePaths,
                durationSeconds = result.durationSeconds,
                isSaved = isSaved,
                createdAtMillis = result.createdAtMillis
            )
        )
    }

    companion object {
        private const val TAG = "VideoStudioViewModel"

        fun provideFactory(
            videoAiService: VideoAiService,
            repository: CreationRepository,
            downloadService: DownloadService,
            settingsDataStore: SettingsDataStore
        ): ViewModelProvider.Factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                return VideoStudioViewModel(videoAiService, repository, downloadService, settingsDataStore) as T
            }
        }
    }
}
