package com.example.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.downloads.DownloadService
import com.example.downloads.ShareService
import com.example.storage.CreationRepository
import com.example.types.CreationHistoryItem
import com.example.types.CreationType
import com.example.types.DownloadState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

enum class HistoryFilter(val label: String) {
    ALL("All"),
    IMAGES("Images"),
    VIDEOS("Videos"),
    SAVED("Saved"),
    TOOLS("AI Tools")
}

class HistoryViewModel(
    private val repository: CreationRepository,
    private val downloadService: DownloadService
) : ViewModel() {

    private val _selectedFilter = MutableStateFlow(HistoryFilter.ALL)
    val selectedFilter: StateFlow<HistoryFilter> = _selectedFilter

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery

    private val _downloadState = MutableStateFlow<DownloadState>(DownloadState.Idle)
    val downloadState: StateFlow<DownloadState> = _downloadState

    val items: StateFlow<List<CreationHistoryItem>> = combine(
        repository.historyItems,
        _selectedFilter,
        _searchQuery
    ) { allItems, filter, query ->
        allItems.filter { item ->
            val matchesFilter = when (filter) {
                HistoryFilter.ALL -> true
                HistoryFilter.IMAGES -> item.type == CreationType.IMAGE
                HistoryFilter.VIDEOS -> item.type == CreationType.VIDEO
                HistoryFilter.SAVED -> item.isSaved
                HistoryFilter.TOOLS -> item.type == CreationType.PROMPT_TOOL
            }
            val matchesQuery = query.isBlank() ||
                item.prompt.contains(query, ignoreCase = true) ||
                item.title.contains(query, ignoreCase = true) ||
                item.styleLabel.contains(query, ignoreCase = true)
            matchesFilter && matchesQuery
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun setFilter(filter: HistoryFilter) {
        _selectedFilter.value = filter
    }

    fun setSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun toggleSaved(item: CreationHistoryItem) {
        viewModelScope.launch {
            repository.toggleSaved(item.id, item.isSaved)
        }
    }

    fun deleteItem(item: CreationHistoryItem) {
        viewModelScope.launch {
            repository.deleteHistoryItem(item)
        }
    }

    fun downloadAgain(item: CreationHistoryItem) {
        if (item.mediaPath.isBlank()) return
        viewModelScope.launch {
            val isMp4 = item.type == CreationType.VIDEO && item.mediaPath.endsWith(".mp4", ignoreCase = true)
            downloadService.downloadCreation(
                creationId = item.id,
                title = item.title,
                type = item.type,
                sourcePath = item.mediaPath,
                isVideoMp4 = isMp4
            ) { state ->
                _downloadState.value = state
            }
        }
    }

    fun shareItem(context: Context, item: CreationHistoryItem): Result<Unit> {
        return if (item.mediaPath.isNotBlank()) {
            val mime = if (item.mediaPath.endsWith(".mp4", true)) "video/mp4" else "image/png"
            ShareService.shareFile(context, item.mediaPath, mime, item.title)
        } else {
            ShareService.shareText(context, item.prompt, item.title)
        }
    }

    fun clearDownloadStatus() {
        _downloadState.value = DownloadState.Idle
    }

    companion object {
        fun provideFactory(
            repository: CreationRepository,
            downloadService: DownloadService
        ): ViewModelProvider.Factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                return HistoryViewModel(repository, downloadService) as T
            }
        }
    }
}
