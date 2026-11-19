package com.example.ai.services

import com.example.types.CreativeToolRequest
import com.example.types.CreativeToolResult
import com.example.types.GeneratedImageResult
import com.example.types.GeneratedVideoResult
import com.example.types.ImageGenerationRequest
import com.example.types.VideoGenerationRequest

data class AiProviderStatus(
    val isCloudConfigured: Boolean,
    val providerName: String,
    val statusHeadline: String,
    val statusDetail: String
)

interface ImageAiService {
    fun providerStatus(): AiProviderStatus
    suspend fun generateImage(
        request: ImageGenerationRequest,
        onProgress: (Float, String) -> Unit
    ): GeneratedImageResult
}

interface VideoAiService {
    fun providerStatus(): AiProviderStatus
    suspend fun generateVideo(
        request: VideoGenerationRequest,
        onProgress: (Float, String) -> Unit
    ): GeneratedVideoResult
}

interface CreativeTextAiService {
    fun providerStatus(): AiProviderStatus
    suspend fun runCreativeTool(
        request: CreativeToolRequest,
        onProgress: (Float, String) -> Unit
    ): CreativeToolResult
}
