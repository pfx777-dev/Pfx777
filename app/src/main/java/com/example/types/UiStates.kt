package com.example.types

sealed interface GenerationState<out T> {
    data object Idle : GenerationState<Nothing>
    data class Loading(val progress: Float, val stage: String) : GenerationState<Nothing>
    data class Success<T>(val data: T) : GenerationState<T>
    data class Error(val message: String, val canRetry: Boolean = true) : GenerationState<Nothing>
}

sealed interface DownloadState {
    data object Idle : DownloadState
    data class Downloading(val progress: Float) : DownloadState
    data class Completed(val fileName: String, val displayLocation: String) : DownloadState
    data class Failed(val message: String) : DownloadState
}
