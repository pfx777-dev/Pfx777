package com.example.ai

import android.content.Context
import com.example.ai.services.CreativeTextAiService
import com.example.ai.services.ImageAiService
import com.example.ai.services.SampleCreativeTextAiService
import com.example.ai.services.SampleImageAiService
import com.example.ai.services.SampleVideoAiService
import com.example.ai.services.VideoAiService
import com.example.downloads.DownloadService
import com.example.downloads.VideoExportService
import com.example.storage.CreationRepository
import com.example.storage.SettingsDataStore
import com.example.storage.StudioDatabase

class StudioContainer(context: Context) {
    private val appContext = context.applicationContext

    val database: StudioDatabase by lazy { StudioDatabase.getInstance(appContext) }
    val creationRepository: CreationRepository by lazy { CreationRepository(database.creationDao()) }
    val settingsDataStore: SettingsDataStore by lazy { SettingsDataStore(appContext) }
    val videoExportService: VideoExportService by lazy { VideoExportService() }
    val downloadService: DownloadService by lazy { DownloadService(appContext, creationRepository) }

    val imageAiService: ImageAiService by lazy { SampleImageAiService(appContext) }
    val videoAiService: VideoAiService by lazy { SampleVideoAiService(appContext, videoExportService) }
    val creativeTextAiService: CreativeTextAiService by lazy {
        SampleCreativeTextAiService(appContext, imageAiService, videoAiService)
    }

    companion object {
        @Volatile
        private var INSTANCE: StudioContainer? = null

        fun getInstance(context: Context): StudioContainer {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: StudioContainer(context).also { INSTANCE = it }
            }
        }
    }
}
