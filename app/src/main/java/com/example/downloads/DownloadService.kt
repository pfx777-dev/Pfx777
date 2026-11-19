package com.example.downloads

import android.content.ContentValues
import android.content.Context
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import com.example.storage.CreationRepository
import com.example.types.CreationType
import com.example.types.DownloadState
import com.example.types.DownloadedCreationItem
import com.example.utilities.Formatters
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.util.UUID

class DownloadService(
    private val context: Context,
    private val repository: CreationRepository
) {

    suspend fun downloadCreation(
        creationId: String,
        title: String,
        type: CreationType,
        sourcePath: String,
        isVideoMp4: Boolean,
        onProgress: (DownloadState) -> Unit
    ): DownloadState = withContext(Dispatchers.IO) {
        try {
            val sourceFile = File(sourcePath)
            if (!sourceFile.exists() || sourceFile.length() == 0L) {
                val failed = DownloadState.Failed("Source file is missing or empty.")
                onProgress(failed)
                return@withContext failed
            }

            onProgress(DownloadState.Downloading(0.2f))

            val extension = if (isVideoMp4 && sourcePath.endsWith(".mp4", ignoreCase = true)) "mp4" else "png"
            val mimeType = if (extension == "mp4") "video/mp4" else "image/png"
            val slug = Formatters.slugify(title)
            val baseFileName = "vardaan_${slug}_${creationId.take(6)}.$extension"

            // Avoid duplicate download files
            if (repository.hasDownloadWithFileName(baseFileName)) {
                val completed = DownloadState.Completed(
                    fileName = baseFileName,
                    displayLocation = if (extension == "mp4") "Movies/VARDAAN CREATION" else "Pictures/VARDAAN CREATION"
                )
                onProgress(completed)
                return@withContext completed
            }

            // Copy into app downloads directory for deterministic local preview & share
            val appDownloadsDir = File(context.filesDir, "downloads").apply { mkdirs() }
            val localCopy = File(appDownloadsDir, baseFileName)
            FileInputStream(sourceFile).use { input ->
                FileOutputStream(localCopy).use { output ->
                    input.copyTo(output)
                }
            }

            onProgress(DownloadState.Downloading(0.65f))

            var mediaUri: Uri? = null
            var displayLocation: String

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                val collection = if (extension == "mp4") {
                    MediaStore.Video.Media.getContentUri(MediaStore.VOLUME_EXTERNAL_PRIMARY)
                } else {
                    MediaStore.Images.Media.getContentUri(MediaStore.VOLUME_EXTERNAL_PRIMARY)
                }
                val relativeDir = if (extension == "mp4") {
                    "${Environment.DIRECTORY_MOVIES}/VARDAAN CREATION"
                } else {
                    "${Environment.DIRECTORY_PICTURES}/VARDAAN CREATION"
                }
                val values = ContentValues().apply {
                    put(MediaStore.MediaColumns.DISPLAY_NAME, baseFileName)
                    put(MediaStore.MediaColumns.MIME_TYPE, mimeType)
                    put(MediaStore.MediaColumns.RELATIVE_PATH, relativeDir)
                    put(MediaStore.MediaColumns.IS_PENDING, 1)
                }
                val resolver = context.contentResolver
                mediaUri = resolver.insert(collection, values)
                if (mediaUri != null) {
                    resolver.openOutputStream(mediaUri)?.use { out ->
                        FileInputStream(localCopy).use { it.copyTo(out) }
                    }
                    values.clear()
                    values.put(MediaStore.MediaColumns.IS_PENDING, 0)
                    resolver.update(mediaUri, values, null, null)
                    displayLocation = relativeDir
                } else {
                    displayLocation = "App Downloads (${localCopy.name})"
                }
            } else {
                val dirType = if (extension == "mp4") Environment.DIRECTORY_MOVIES else Environment.DIRECTORY_PICTURES
                val extDir = File(context.getExternalFilesDir(dirType), "VARDAAN CREATION").apply { mkdirs() }
                val extFile = File(extDir, baseFileName)
                FileInputStream(localCopy).use { input ->
                    FileOutputStream(extFile).use { output ->
                        input.copyTo(output)
                    }
                }
                displayLocation = extFile.absolutePath
            }

            onProgress(DownloadState.Downloading(0.92f))

            val downloadRecord = DownloadedCreationItem(
                id = UUID.randomUUID().toString(),
                title = title,
                type = type,
                fileName = baseFileName,
                filePath = localCopy.absolutePath,
                contentUriString = mediaUri?.toString(),
                fileSizeBytes = localCopy.length(),
                downloadedAtMillis = System.currentTimeMillis()
            )
            repository.recordDownload(downloadRecord)
            repository.markDownloaded(creationId, displayLocation)

            val done = DownloadState.Completed(
                fileName = baseFileName,
                displayLocation = displayLocation
            )
            onProgress(done)
            done
        } catch (e: Exception) {
            val failed = DownloadState.Failed(e.localizedMessage ?: "Failed to save download.")
            onProgress(failed)
            failed
        }
    }
}
