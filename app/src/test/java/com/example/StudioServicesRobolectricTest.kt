package com.example

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.example.ai.services.SampleCreativeTextAiService
import com.example.ai.services.SampleImageAiService
import com.example.ai.services.SampleVideoAiService
import com.example.downloads.DownloadService
import com.example.downloads.VideoExportService
import com.example.storage.CreationRepository
import com.example.storage.StudioDatabase
import com.example.types.AiToolId
import com.example.types.CameraMotion
import com.example.types.CreationHistoryItem
import com.example.types.CreationType
import com.example.types.CreativeToolRequest
import com.example.types.DownloadState
import com.example.types.ImageGenerationRequest
import com.example.types.ImageMode
import com.example.types.ImageResolution
import com.example.types.ImageStyle
import com.example.types.StudioAspectRatio
import com.example.types.VideoDuration
import com.example.types.VideoGenerationRequest
import com.example.types.VideoMode
import com.example.types.VideoQuality
import com.example.types.VideoStyle
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import java.io.File

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class StudioServicesRobolectricTest {

    private lateinit var context: Context
    private lateinit var database: StudioDatabase
    private lateinit var repository: CreationRepository
    private lateinit var imageService: SampleImageAiService
    private lateinit var videoService: SampleVideoAiService
    private lateinit var toolService: SampleCreativeTextAiService
    private lateinit var downloadService: DownloadService

    @Before
    fun setup() {
        context = ApplicationProvider.getApplicationContext()
        database = Room.inMemoryDatabaseBuilder(context, StudioDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        repository = CreationRepository(database.creationDao())
        imageService = SampleImageAiService(context)
        videoService = SampleVideoAiService(context, VideoExportService())
        toolService = SampleCreativeTextAiService(context, imageService, videoService)
        downloadService = DownloadService(context, repository)
    }

    @After
    fun tearDown() {
        database.close()
    }

    @Test
    fun imageGenerationCreatesValidFileAndDownloadSavesRecord() = runBlocking {
        val result = imageService.generateImage(
            ImageGenerationRequest(
                mode = ImageMode.TEXT_TO_IMAGE,
                prompt = "Cyberpunk skyline at sunset",
                style = ImageStyle.CINEMATIC,
                aspectRatio = StudioAspectRatio.RATIO_16_9,
                resolution = ImageResolution.RES_1K
            )
        ) { _, _ -> }

        val generatedFile = File(result.localFilePath)
        assertTrue(generatedFile.exists())
        assertTrue(generatedFile.length() > 0L)
        assertTrue(result.isSamplePreview)

        repository.saveHistoryItem(
            CreationHistoryItem(
                id = result.id,
                type = CreationType.IMAGE,
                title = result.styleLabel,
                prompt = result.prompt,
                styleLabel = result.styleLabel,
                aspectRatioLabel = result.aspectRatio.label,
                mediaPath = result.localFilePath,
                createdAtMillis = result.createdAtMillis
            )
        )
        assertEquals(1, repository.historyItems.first().size)

        val dlState = downloadService.downloadCreation(
            creationId = result.id,
            title = result.prompt,
            type = CreationType.IMAGE,
            sourcePath = result.localFilePath,
            isVideoMp4 = false
        ) {}
        assertTrue(dlState is DownloadState.Completed)
        assertEquals(1, repository.downloadedItems.first().size)
    }

    @Test
    fun videoGenerationCreatesFrameSequenceAndHandlesEmptyPrompt() = runBlocking {
        var failedOnBlank = false
        try {
            videoService.generateVideo(
                VideoGenerationRequest(
                    mode = VideoMode.TEXT_TO_VIDEO,
                    prompt = "   "
                )
            ) { _, _ -> }
        } catch (_: IllegalArgumentException) {
            failedOnBlank = true
        }
        assertTrue(failedOnBlank)

        val videoResult = videoService.generateVideo(
            VideoGenerationRequest(
                mode = VideoMode.TEXT_TO_VIDEO,
                prompt = "Golden phoenix rising over mountains",
                style = VideoStyle.FANTASY,
                cameraMotion = CameraMotion.CRANE_UP,
                aspectRatio = StudioAspectRatio.RATIO_16_9,
                duration = VideoDuration.SEC_4,
                quality = VideoQuality.HD_720
            )
        ) { _, _ -> }

        assertTrue(File(videoResult.posterFilePath).exists())
        assertTrue(videoResult.frameFilePaths.isNotEmpty())
        assertTrue(videoResult.frameFilePaths.all { File(it).exists() })
    }

    @Test
    fun creativeToolEnhancesPromptAndDeletesHistoryCleanly() = runBlocking {
        val toolResult = toolService.runCreativeTool(
            CreativeToolRequest(
                toolId = AiToolId.CINEMATIC_PROMPT_GENERATOR,
                prompt = "Lone astronaut walking on Mars",
                secondaryOption = "Golden Hour"
            )
        ) { _, _ -> }

        assertFalse(toolResult.generatedText.isNullOrBlank())
        assertTrue(toolResult.generatedText!!.contains("astronaut"))

        val historyItem = CreationHistoryItem(
            id = "test_delete_id",
            type = CreationType.PROMPT_TOOL,
            title = AiToolId.CINEMATIC_PROMPT_GENERATOR.title,
            prompt = toolResult.generatedText.orEmpty(),
            styleLabel = "Golden Hour",
            aspectRatioLabel = "1:1",
            mediaPath = "",
            createdAtMillis = System.currentTimeMillis()
        )
        repository.saveHistoryItem(historyItem)
        assertEquals(1, repository.historyItems.first().size)
        repository.deleteHistoryItem(historyItem)
        assertEquals(0, repository.historyItems.first().size)
    }
}
