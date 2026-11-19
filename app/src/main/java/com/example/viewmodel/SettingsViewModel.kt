package com.example.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.ai.services.ImageAiService
import com.example.ai.services.VideoAiService
import com.example.storage.CreationRepository
import com.example.storage.SettingsDataStore
import com.example.types.ImageResolution
import com.example.types.StudioAspectRatio
import com.example.types.StudioSettings
import com.example.types.VideoDuration
import com.example.types.VideoQuality
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class SettingsViewModel(
    private val settingsDataStore: SettingsDataStore,
    private val repository: CreationRepository,
    imageAiService: ImageAiService,
    videoAiService: VideoAiService
) : ViewModel() {

    val settings: StateFlow<StudioSettings> = settingsDataStore.settingsFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), StudioSettings())

    val imageProviderStatus = imageAiService.providerStatus()
    val videoProviderStatus = videoAiService.providerStatus()

    private val _statusMessage = MutableStateFlow<String?>(null)
    val statusMessage: StateFlow<String?> = _statusMessage

    fun setDefaultImageRatio(ratio: StudioAspectRatio) {
        viewModelScope.launch { settingsDataStore.updateImageAspectRatio(ratio) }
    }

    fun setDefaultImageResolution(res: ImageResolution) {
        viewModelScope.launch { settingsDataStore.updateImageResolution(res) }
    }

    fun setDefaultVideoRatio(ratio: StudioAspectRatio) {
        viewModelScope.launch { settingsDataStore.updateVideoAspectRatio(ratio) }
    }

    fun setDefaultVideoDuration(duration: VideoDuration) {
        viewModelScope.launch { settingsDataStore.updateVideoDuration(duration) }
    }

    fun setDefaultVideoQuality(quality: VideoQuality) {
        viewModelScope.launch { settingsDataStore.updateVideoQuality(quality) }
    }

    fun setAutoSaveHistory(enabled: Boolean) {
        viewModelScope.launch { settingsDataStore.updateAutoSaveHistory(enabled) }
    }

    fun clearHistory() {
        viewModelScope.launch {
            repository.clearAllHistory()
            _statusMessage.value = "Local generation history cleared."
        }
    }

    fun clearStatusMessage() {
        _statusMessage.value = null
    }

    companion object {
        fun provideFactory(
            settingsDataStore: SettingsDataStore,
            repository: CreationRepository,
            imageAiService: ImageAiService,
            videoAiService: VideoAiService
        ): ViewModelProvider.Factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                return SettingsViewModel(settingsDataStore, repository, imageAiService, videoAiService) as T
            }
        }
    }
}
