package com.example.storage

import com.example.types.CreationHistoryItem
import com.example.types.CreationType
import com.example.types.DownloadedCreationItem
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import java.io.File

class CreationRepository(
    private val dao: CreationDao
) {
    val historyItems: Flow<List<CreationHistoryItem>> = dao.observeAllCreations().map { list ->
        list.map { it.toDomain() }
    }

    val downloadedItems: Flow<List<DownloadedCreationItem>> = dao.observeAllDownloads().map { list ->
        list.map { it.toDomain() }
    }

    suspend fun saveHistoryItem(item: CreationHistoryItem) {
        dao.insertCreation(item.toEntity())
    }

    suspend fun toggleSaved(id: String, currentSaved: Boolean) {
        dao.updateSavedState(id, !currentSaved)
    }

    suspend fun setSaved(id: String, saved: Boolean) {
        dao.updateSavedState(id, saved)
    }

    suspend fun markDownloaded(id: String, displayPath: String) {
        dao.markCreationDownloaded(id, displayPath)
    }

    suspend fun deleteHistoryItem(item: CreationHistoryItem) {
        dao.deleteCreationById(item.id)
        withContext(Dispatchers.IO) {
            runCatching {
                if (item.mediaPath.isNotBlank()) File(item.mediaPath).delete()
                item.secondaryMediaPaths.forEach { path -> File(path).delete() }
            }
        }
    }

    suspend fun clearAllHistory() {
        dao.clearAllCreations()
    }

    suspend fun recordDownload(item: DownloadedCreationItem) {
        dao.insertDownload(item.toEntity())
    }

    suspend fun hasDownloadWithFileName(fileName: String): Boolean {
        return dao.findDownloadByFileName(fileName) != null
    }

    suspend fun deleteDownloadRecord(item: DownloadedCreationItem) {
        dao.deleteDownloadById(item.id)
        withContext(Dispatchers.IO) {
            runCatching {
                if (item.filePath.isNotBlank()) {
                    val file = File(item.filePath)
                    if (file.exists()) file.delete()
                }
            }
        }
    }

    private fun CreationEntity.toDomain(): CreationHistoryItem {
        val type = runCatching { CreationType.valueOf(typeName) }.getOrDefault(CreationType.IMAGE)
        val secondary = secondaryMediaPathsJoined
            .split("|")
            .filter { it.isNotBlank() }
        return CreationHistoryItem(
            id = id,
            type = type,
            title = title,
            prompt = prompt,
            styleLabel = styleLabel,
            aspectRatioLabel = aspectRatioLabel,
            mediaPath = mediaPath,
            secondaryMediaPaths = secondary,
            durationSeconds = durationSeconds,
            isSaved = isSaved,
            isDownloaded = isDownloaded,
            downloadedDisplayPath = downloadedDisplayPath,
            createdAtMillis = createdAtMillis
        )
    }

    private fun CreationHistoryItem.toEntity(): CreationEntity {
        return CreationEntity(
            id = id,
            typeName = type.name,
            title = title,
            prompt = prompt,
            styleLabel = styleLabel,
            aspectRatioLabel = aspectRatioLabel,
            mediaPath = mediaPath,
            secondaryMediaPathsJoined = secondaryMediaPaths.joinToString("|"),
            durationSeconds = durationSeconds,
            isSaved = isSaved,
            isDownloaded = isDownloaded,
            downloadedDisplayPath = downloadedDisplayPath,
            createdAtMillis = createdAtMillis
        )
    }

    private fun DownloadEntity.toDomain(): DownloadedCreationItem {
        val type = runCatching { CreationType.valueOf(typeName) }.getOrDefault(CreationType.IMAGE)
        return DownloadedCreationItem(
            id = id,
            title = title,
            type = type,
            fileName = fileName,
            filePath = filePath,
            contentUriString = contentUriString,
            fileSizeBytes = fileSizeBytes,
            downloadedAtMillis = downloadedAtMillis
        )
    }

    private fun DownloadedCreationItem.toEntity(): DownloadEntity {
        return DownloadEntity(
            id = id,
            title = title,
            typeName = type.name,
            fileName = fileName,
            filePath = filePath,
            contentUriString = contentUriString,
            fileSizeBytes = fileSizeBytes,
            downloadedAtMillis = downloadedAtMillis
        )
    }
}
