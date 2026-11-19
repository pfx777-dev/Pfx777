package com.example.storage

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "creations")
data class CreationEntity(
    @PrimaryKey val id: String,
    val typeName: String,
    val title: String,
    val prompt: String,
    val styleLabel: String,
    val aspectRatioLabel: String,
    val mediaPath: String,
    val secondaryMediaPathsJoined: String = "",
    val durationSeconds: Int = 0,
    val isSaved: Boolean = false,
    val isDownloaded: Boolean = false,
    val downloadedDisplayPath: String? = null,
    val createdAtMillis: Long
)

@Entity(tableName = "downloads")
data class DownloadEntity(
    @PrimaryKey val id: String,
    val title: String,
    val typeName: String,
    val fileName: String,
    val filePath: String,
    val contentUriString: String?,
    val fileSizeBytes: Long,
    val downloadedAtMillis: Long
)
