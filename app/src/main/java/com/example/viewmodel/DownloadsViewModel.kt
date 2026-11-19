package com.example.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.downloads.ShareService
import com.example.storage.CreationRepository
import com.example.types.DownloadedCreationItem
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class DownloadsViewModel(
    private val repository: CreationRepository
) : ViewModel() {

    val downloads: StateFlow<List<DownloadedCreationItem>> = repository.downloadedItems
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun deleteDownload(item: DownloadedCreationItem) {
        viewModelScope.launch {
            repository.deleteDownloadRecord(item)
        }
    }

    fun shareDownload(context: Context, item: DownloadedCreationItem): Result<Unit> {
        val mime = if (item.fileName.endsWith(".mp4", ignoreCase = true)) "video/mp4" else "image/png"
        return ShareService.shareFile(context, item.filePath, mime, item.title)
    }

    companion object {
        fun provideFactory(repository: CreationRepository): ViewModelProvider.Factory =
            object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    return DownloadsViewModel(repository) as T
                }
            }
    }
}
