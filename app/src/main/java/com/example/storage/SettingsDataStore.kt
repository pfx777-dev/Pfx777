package com.example.storage

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.example.types.ImageResolution
import com.example.types.StudioAspectRatio
import com.example.types.StudioSettings
import com.example.types.VideoDuration
import com.example.types.VideoQuality
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.studioDataStore: DataStore<Preferences> by preferencesDataStore(name = "vardaan_settings")

class SettingsDataStore(private val context: Context) {
    private object Keys {
        val DEFAULT_IMAGE_RATIO = stringPreferencesKey("default_image_ratio")
        val DEFAULT_IMAGE_RES = stringPreferencesKey("default_image_res")
        val DEFAULT_VIDEO_RATIO = stringPreferencesKey("default_video_ratio")
        val DEFAULT_VIDEO_DURATION = stringPreferencesKey("default_video_duration")
        val DEFAULT_VIDEO_QUALITY = stringPreferencesKey("default_video_quality")
        val AUTO_SAVE_HISTORY = booleanPreferencesKey("auto_save_history")
    }

    val settingsFlow: Flow<StudioSettings> = context.studioDataStore.data.map { prefs ->
        StudioSettings(
            defaultImageAspectRatio = prefs[Keys.DEFAULT_IMAGE_RATIO]
                ?.let { runCatching { StudioAspectRatio.valueOf(it) }.getOrNull() }
                ?: StudioAspectRatio.RATIO_1_1,
            defaultImageResolution = prefs[Keys.DEFAULT_IMAGE_RES]
                ?.let { runCatching { ImageResolution.valueOf(it) }.getOrNull() }
                ?: ImageResolution.RES_1K,
            defaultVideoAspectRatio = prefs[Keys.DEFAULT_VIDEO_RATIO]
                ?.let { runCatching { StudioAspectRatio.valueOf(it) }.getOrNull() }
                ?: StudioAspectRatio.RATIO_16_9,
            defaultVideoDuration = prefs[Keys.DEFAULT_VIDEO_DURATION]
                ?.let { runCatching { VideoDuration.valueOf(it) }.getOrNull() }
                ?: VideoDuration.SEC_4,
            defaultVideoQuality = prefs[Keys.DEFAULT_VIDEO_QUALITY]
                ?.let { runCatching { VideoQuality.valueOf(it) }.getOrNull() }
                ?: VideoQuality.HD_720,
            autoSaveToHistory = prefs[Keys.AUTO_SAVE_HISTORY] ?: true
        )
    }

    suspend fun updateImageAspectRatio(ratio: StudioAspectRatio) {
        context.studioDataStore.edit { it[Keys.DEFAULT_IMAGE_RATIO] = ratio.name }
    }

    suspend fun updateImageResolution(resolution: ImageResolution) {
        context.studioDataStore.edit { it[Keys.DEFAULT_IMAGE_RES] = resolution.name }
    }

    suspend fun updateVideoAspectRatio(ratio: StudioAspectRatio) {
        context.studioDataStore.edit { it[Keys.DEFAULT_VIDEO_RATIO] = ratio.name }
    }

    suspend fun updateVideoDuration(duration: VideoDuration) {
        context.studioDataStore.edit { it[Keys.DEFAULT_VIDEO_DURATION] = duration.name }
    }

    suspend fun updateVideoQuality(quality: VideoQuality) {
        context.studioDataStore.edit { it[Keys.DEFAULT_VIDEO_QUALITY] = quality.name }
    }

    suspend fun updateAutoSaveHistory(enabled: Boolean) {
        context.studioDataStore.edit { it[Keys.AUTO_SAVE_HISTORY] = enabled }
    }
}
