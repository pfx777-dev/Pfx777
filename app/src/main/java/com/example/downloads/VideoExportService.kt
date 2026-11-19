package com.example.downloads

import android.graphics.Bitmap
import android.media.MediaCodec
import android.media.MediaCodecInfo
import android.media.MediaCodecList
import android.media.MediaFormat
import android.media.MediaMuxer
import android.util.Log
import java.io.File
import java.nio.ByteBuffer

class VideoExportService {

    private data class DrainResult(
        val trackIndex: Int,
        val muxerStarted: Boolean,
        val wroteSample: Boolean
    )

    fun encodeBitmapsToMp4(
        frames: List<Bitmap>,
        outputFile: File,
        fps: Int = 6
    ): File? {
        if (frames.isEmpty()) return null
        val first = frames.first()
        // H.264 requires even dimensions
        val width = (first.width / 2) * 2
        val height = (first.height / 2) * 2
        if (width < 64 || height < 64) return null

        val encoderSelection = findSupportedAvcEncoder() ?: return null
        val codecInfo = encoderSelection.first
        val colorFormat = encoderSelection.second

        var codec: MediaCodec? = null
        var muxer: MediaMuxer? = null
        var codecStarted = false
        var muxerStarted = false
        var wroteSample = false

        return try {
            val format = MediaFormat.createVideoFormat(MediaFormat.MIMETYPE_VIDEO_AVC, width, height).apply {
                setInteger(MediaFormat.KEY_COLOR_FORMAT, colorFormat)
                setInteger(MediaFormat.KEY_BIT_RATE, width * height * 4)
                setInteger(MediaFormat.KEY_FRAME_RATE, fps)
                setInteger(MediaFormat.KEY_I_FRAME_INTERVAL, 1)
            }

            codec = MediaCodec.createByCodecName(codecInfo.name)
            codec.configure(format, null, null, MediaCodec.CONFIGURE_FLAG_ENCODE)
            codec.start()
            codecStarted = true

            muxer = MediaMuxer(outputFile.absolutePath, MediaMuxer.OutputFormat.MUXER_OUTPUT_MPEG_4)
            var trackIndex = -1
            val bufferInfo = MediaCodec.BufferInfo()
            val frameDurationUs = 1_000_000L / fps

            // Repeat frames gently so output clip lasts smoothly
            val repeatedFrames = buildList {
                frames.forEach { bmp ->
                    add(bmp)
                    add(bmp)
                }
            }

            val useSemiPlanar = colorFormat == MediaCodecInfo.CodecCapabilities.COLOR_FormatYUV420SemiPlanar

            for ((index, frame) in repeatedFrames.withIndex()) {
                val inputIndex = codec.dequeueInputBuffer(10_000L)
                if (inputIndex >= 0) {
                    val inputBuffer: ByteBuffer = codec.getInputBuffer(inputIndex) ?: continue
                    inputBuffer.clear()
                    val scaled = if (frame.width != width || frame.height != height) {
                        Bitmap.createScaledBitmap(frame, width, height, true)
                    } else frame
                    val yuv = if (useSemiPlanar) {
                        bitmapToNv12(scaled, width, height)
                    } else {
                        bitmapToI420(scaled, width, height)
                    }
                    if (scaled !== frame) scaled.recycle()
                    val bytesToWrite = minOf(inputBuffer.remaining(), yuv.size)
                    inputBuffer.put(yuv, 0, bytesToWrite)
                    codec.queueInputBuffer(inputIndex, 0, bytesToWrite, index * frameDurationUs, 0)
                }
                val res = drainEncoder(
                    codec = codec,
                    muxer = muxer,
                    bufferInfo = bufferInfo,
                    endOfStream = false,
                    existingTrack = trackIndex,
                    alreadyStarted = muxerStarted,
                    alreadyWroteSample = wroteSample
                )
                trackIndex = res.trackIndex
                muxerStarted = res.muxerStarted
                wroteSample = res.wroteSample
            }

            val eosIndex = codec.dequeueInputBuffer(10_000L)
            if (eosIndex >= 0) {
                codec.queueInputBuffer(
                    eosIndex, 0, 0, repeatedFrames.size * frameDurationUs,
                    MediaCodec.BUFFER_FLAG_END_OF_STREAM
                )
            }
            val finalRes = drainEncoder(
                codec = codec,
                muxer = muxer,
                bufferInfo = bufferInfo,
                endOfStream = true,
                existingTrack = trackIndex,
                alreadyStarted = muxerStarted,
                alreadyWroteSample = wroteSample
            )
            trackIndex = finalRes.trackIndex
            muxerStarted = finalRes.muxerStarted
            wroteSample = finalRes.wroteSample

            if (codecStarted) {
                runCatching { codec.stop() }
                codecStarted = false
            }

            if (muxerStarted && wroteSample) {
                muxer.stop()
                muxerStarted = false
            } else {
                muxerStarted = false
                runCatching { outputFile.delete() }
                return null
            }

            if (outputFile.exists() && outputFile.length() > 0L) outputFile else null
        } catch (e: Exception) {
            Log.w(TAG, "MP4 encoding unavailable in this environment: ${e.message}")
            runCatching { outputFile.delete() }
            null
        } finally {
            if (codecStarted) {
                runCatching { codec?.stop() }
            }
            runCatching { codec?.release() }
            if (muxerStarted && wroteSample) {
                runCatching { muxer?.stop() }
            }
            runCatching { muxer?.release() }
        }
    }

    private fun drainEncoder(
        codec: MediaCodec,
        muxer: MediaMuxer,
        bufferInfo: MediaCodec.BufferInfo,
        endOfStream: Boolean,
        existingTrack: Int,
        alreadyStarted: Boolean,
        alreadyWroteSample: Boolean
    ): DrainResult {
        var trackIndex = existingTrack
        var muxerStarted = alreadyStarted
        var wroteSample = alreadyWroteSample
        var attempts = 0
        while (attempts < 60) {
            attempts++
            val outputIndex = codec.dequeueOutputBuffer(bufferInfo, 10_000L)
            when {
                outputIndex == MediaCodec.INFO_TRY_AGAIN_LATER -> {
                    if (!endOfStream) break
                }
                outputIndex == MediaCodec.INFO_OUTPUT_FORMAT_CHANGED -> {
                    if (!muxerStarted) {
                        trackIndex = muxer.addTrack(codec.outputFormat)
                        muxer.start()
                        muxerStarted = true
                    }
                }
                outputIndex >= 0 -> {
                    val encodedData = codec.getOutputBuffer(outputIndex)
                    if (encodedData != null && bufferInfo.size > 0 &&
                        (bufferInfo.flags and MediaCodec.BUFFER_FLAG_CODEC_CONFIG) == 0 &&
                        muxerStarted && trackIndex >= 0
                    ) {
                        encodedData.position(bufferInfo.offset)
                        encodedData.limit(bufferInfo.offset + bufferInfo.size)
                        muxer.writeSampleData(trackIndex, encodedData, bufferInfo)
                        wroteSample = true
                    }
                    codec.releaseOutputBuffer(outputIndex, false)
                    if ((bufferInfo.flags and MediaCodec.BUFFER_FLAG_END_OF_STREAM) != 0) {
                        break
                    }
                }
            }
        }
        return DrainResult(trackIndex, muxerStarted, wroteSample)
    }

    private fun findSupportedAvcEncoder(): Pair<MediaCodecInfo, Int>? {
        val list = MediaCodecList(MediaCodecList.REGULAR_CODECS)
        val candidates = list.codecInfos.filter { info ->
            info.isEncoder && info.supportedTypes.any { it.equals(MediaFormat.MIMETYPE_VIDEO_AVC, true) }
        }
        val preferredOrder = listOf(
            MediaCodecInfo.CodecCapabilities.COLOR_FormatYUV420Planar,
            MediaCodecInfo.CodecCapabilities.COLOR_FormatYUV420SemiPlanar,
            MediaCodecInfo.CodecCapabilities.COLOR_FormatYUV420Flexible
        )
        for (info in candidates) {
            val caps = runCatching { info.getCapabilitiesForType(MediaFormat.MIMETYPE_VIDEO_AVC) }.getOrNull()
                ?: continue
            for (fmt in preferredOrder) {
                if (caps.colorFormats.contains(fmt)) {
                    return info to fmt
                }
            }
        }
        return null
    }

    private fun bitmapToI420(bitmap: Bitmap, width: Int, height: Int): ByteArray {
        val argb = IntArray(width * height)
        bitmap.getPixels(argb, 0, width, 0, 0, width, height)
        val frameSize = width * height
        val yuv = ByteArray(frameSize * 3 / 2)
        var yIndex = 0
        var uIndex = frameSize
        var vIndex = frameSize + frameSize / 4
        var index = 0
        for (j in 0 until height) {
            for (i in 0 until width) {
                val c = argb[index++]
                val r = (c shr 16) and 0xFF
                val g = (c shr 8) and 0xFF
                val b = c and 0xFF
                val y = ((66 * r + 129 * g + 25 * b + 128) shr 8) + 16
                yuv[yIndex++] = y.coerceIn(0, 255).toByte()
                if (j % 2 == 0 && i % 2 == 0 && uIndex < frameSize + frameSize / 4 && vIndex < yuv.size) {
                    val u = ((-38 * r - 74 * g + 112 * b + 128) shr 8) + 128
                    val v = ((112 * r - 94 * g - 18 * b + 128) shr 8) + 128
                    yuv[uIndex++] = u.coerceIn(0, 255).toByte()
                    yuv[vIndex++] = v.coerceIn(0, 255).toByte()
                }
            }
        }
        return yuv
    }

    private fun bitmapToNv12(bitmap: Bitmap, width: Int, height: Int): ByteArray {
        val argb = IntArray(width * height)
        bitmap.getPixels(argb, 0, width, 0, 0, width, height)
        val yuv = ByteArray(width * height * 3 / 2)
        var yIndex = 0
        var uvIndex = width * height
        var index = 0
        for (j in 0 until height) {
            for (i in 0 until width) {
                val c = argb[index++]
                val r = (c shr 16) and 0xFF
                val g = (c shr 8) and 0xFF
                val b = c and 0xFF
                val y = ((66 * r + 129 * g + 25 * b + 128) shr 8) + 16
                yuv[yIndex++] = y.coerceIn(0, 255).toByte()
                if (j % 2 == 0 && i % 2 == 0 && uvIndex + 1 < yuv.size) {
                    val u = ((-38 * r - 74 * g + 112 * b + 128) shr 8) + 128
                    val v = ((112 * r - 94 * g - 18 * b + 128) shr 8) + 128
                    yuv[uvIndex++] = u.coerceIn(0, 255).toByte()
                    yuv[uvIndex++] = v.coerceIn(0, 255).toByte()
                }
            }
        }
        return yuv
    }

    companion object {
        private const val TAG = "VideoExportService"
    }
}
