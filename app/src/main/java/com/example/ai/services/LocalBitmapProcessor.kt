package com.example.ai.services

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.ColorMatrix
import android.graphics.ColorMatrixColorFilter
import android.graphics.LinearGradient
import android.graphics.Matrix
import android.graphics.Paint
import android.graphics.RadialGradient
import android.graphics.RectF
import android.graphics.Shader
import android.net.Uri
import com.example.types.CameraMotion
import com.example.types.StudioAspectRatio
import java.io.File
import java.io.FileOutputStream
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.max
import kotlin.math.sin

object LocalBitmapProcessor {

    fun loadScaledBitmap(context: Context, uri: Uri, maxDimension: Int = 1080): Bitmap? {
        val resolver = context.contentResolver
        val boundsOptions = BitmapFactory.Options().apply { inJustDecodeBounds = true }
        resolver.openInputStream(uri)?.use { stream ->
            BitmapFactory.decodeStream(stream, null, boundsOptions)
        }
        val origW = boundsOptions.outWidth
        val origH = boundsOptions.outHeight
        if (origW <= 0 || origH <= 0) return null

        var sampleSize = 1
        while (origW / sampleSize > maxDimension || origH / sampleSize > maxDimension) {
            sampleSize *= 2
        }
        val decodeOptions = BitmapFactory.Options().apply {
            inSampleSize = sampleSize
            inPreferredConfig = Bitmap.Config.ARGB_8888
        }
        return resolver.openInputStream(uri)?.use { stream ->
            BitmapFactory.decodeStream(stream, null, decodeOptions)
        }
    }

    fun renderSampleCreativeBitmap(
        width: Int,
        height: Int,
        prompt: String,
        styleLabel: String,
        aspectLabel: String,
        seedHash: Int,
        sourceBitmap: Bitmap? = null,
        filterType: String = styleLabel
    ): Bitmap {
        val safeW = width.coerceIn(320, 1440)
        val safeH = height.coerceIn(320, 1440)
        val output = Bitmap.createBitmap(safeW, safeH, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(output)

        if (sourceBitmap != null) {
            drawTransformedSource(canvas, sourceBitmap, safeW, safeH, filterType)
        } else {
            drawProceduralStudioPreview(canvas, safeW, safeH, seedHash, styleLabel)
        }

        // Subtle studio metadata frame overlay so sample preview is unmistakable
        val badgePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.argb(175, 9, 11, 18)
        }
        val borderPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            style = Paint.Style.STROKE
            strokeWidth = 2f
            color = Color.argb(160, 34, 211, 238)
        }
        val cardMargin = safeW * 0.04f
        val barHeight = (safeH * 0.14f).coerceIn(72f, 130f)
        val rect = RectF(cardMargin, safeH - barHeight - cardMargin, safeW - cardMargin, safeH - cardMargin)
        canvas.drawRoundRect(rect, 20f, 20f, badgePaint)
        canvas.drawRoundRect(rect, 20f, 20f, borderPaint)

        val titlePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.parseColor("#22D3EE")
            textSize = (safeW * 0.026f).coerceIn(18f, 30f)
            isFakeBoldText = true
        }
        val subPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.parseColor("#F8FAFC")
            textSize = (safeW * 0.023f).coerceIn(15f, 26f)
        }
        val noticePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.parseColor("#FBBF24")
            textSize = (safeW * 0.019f).coerceIn(13f, 22f)
        }
        val padX = rect.left + 20f
        canvas.drawText(
            "VARDAAN CREATION • $styleLabel • $aspectLabel",
            padX,
            rect.top + rect.height() * 0.30f,
            titlePaint
        )
        val trimmedPrompt = if (prompt.length > 52) prompt.take(49) + "..." else prompt
        canvas.drawText(
            trimmedPrompt,
            padX,
            rect.top + rect.height() * 0.58f,
            subPaint
        )
        canvas.drawText(
            "SAMPLE PREVIEW • Requires Firebase AI Logic for cloud rendering",
            padX,
            rect.top + rect.height() * 0.84f,
            noticePaint
        )
        return output
    }

    fun renderVideoMotionFrames(
        context: Context,
        creationId: String,
        baseBitmap: Bitmap,
        frameCount: Int,
        cameraMotion: CameraMotion,
        aspectRatio: StudioAspectRatio,
        styleLabel: String
    ): List<String> {
        val framesDir = File(context.filesDir, "creations/frames_$creationId").apply { mkdirs() }
        val targetW = (aspectRatio.width * 0.55f).toInt().coerceIn(360, 720)
        val targetH = (aspectRatio.height * 0.55f).toInt().coerceIn(360, 720)
        val paths = mutableListOf<String>()

        for (i in 0 until frameCount) {
            val t = if (frameCount <= 1) 0f else i.toFloat() / (frameCount - 1).toFloat()
            val frameBmp = Bitmap.createBitmap(targetW, targetH, Bitmap.Config.ARGB_8888)
            val canvas = Canvas(frameBmp)

            val matrix = Matrix()
            val baseScale = max(targetW.toFloat() / baseBitmap.width, targetH.toFloat() / baseBitmap.height)
            var zoom = 1.0f
            var tx = 0f
            var ty = 0f
            var rot = 0f

            when (cameraMotion) {
                CameraMotion.STATIC -> {
                    zoom = 1.02f
                }
                CameraMotion.PAN_LEFT -> {
                    zoom = 1.16f
                    tx = (0.5f - t) * targetW * 0.10f
                }
                CameraMotion.PAN_RIGHT -> {
                    zoom = 1.16f
                    tx = (t - 0.5f) * targetW * 0.10f
                }
                CameraMotion.DOLLY_IN -> {
                    zoom = 1.0f + t * 0.22f
                }
                CameraMotion.ORBIT -> {
                    zoom = 1.14f
                    tx = sin(t * 2.0 * PI).toFloat() * targetW * 0.05f
                    ty = cos(t * 2.0 * PI).toFloat() * targetH * 0.03f
                    rot = sin(t * 2.0 * PI).toFloat() * 1.6f
                }
                CameraMotion.CRANE_UP -> {
                    zoom = 1.15f
                    ty = (0.5f - t) * targetH * 0.10f
                }
                CameraMotion.DYNAMIC -> {
                    zoom = 1.12f + sin(t * 3.0 * PI).toFloat() * 0.05f
                    tx = sin(t * 4.0 * PI).toFloat() * targetW * 0.03f
                    ty = cos(t * 3.0 * PI).toFloat() * targetH * 0.025f
                }
            }

            val cx = baseBitmap.width / 2f
            val cy = baseBitmap.height / 2f
            matrix.postTranslate(-cx, -cy)
            matrix.postScale(baseScale * zoom, baseScale * zoom)
            matrix.postRotate(rot)
            matrix.postTranslate(targetW / 2f + tx, targetH / 2f + ty)

            val drawPaint = Paint(Paint.ANTI_ALIAS_FLAG or Paint.FILTER_BITMAP_FLAG)
            canvas.drawColor(Color.parseColor("#090B12"))
            canvas.drawBitmap(baseBitmap, matrix, drawPaint)

            // Subtle animated light-particle sweep per frame
            val particlePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                color = Color.argb(65, 34, 211, 238)
            }
            for (p in 0..8) {
                val px = ((p * 0.13f + t * 0.35f) % 1.0f) * targetW
                val py = ((p * 0.21f + sin((t + p) * PI).toFloat() * 0.08f + 1f) % 1.0f) * targetH
                canvas.drawCircle(px, py, 3f + (p % 3) * 1.5f, particlePaint)
            }

            // Motion badge top-left
            val hudPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                color = Color.argb(150, 9, 11, 18)
            }
            val hudText = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                color = Color.parseColor("#22D3EE")
                textSize = 18f
                isFakeBoldText = true
            }
            canvas.drawRoundRect(16f, 16f, 250f, 52f, 14f, 14f, hudPaint)
            canvas.drawText(
                "$styleLabel • ${cameraMotion.label}",
                28f,
                40f,
                hudText
            )

            val file = File(framesDir, "frame_${i.toString().padStart(3, '0')}.jpg")
            FileOutputStream(file).use { out ->
                frameBmp.compress(Bitmap.CompressFormat.JPEG, 88, out)
            }
            frameBmp.recycle()
            paths.add(file.absolutePath)
        }
        return paths
    }

    fun saveBitmapToCreationsDir(context: Context, bitmap: Bitmap, fileName: String): String {
        val dir = File(context.filesDir, "creations").apply { mkdirs() }
        val file = File(dir, fileName)
        FileOutputStream(file).use { out ->
            bitmap.compress(Bitmap.CompressFormat.PNG, 95, out)
        }
        return file.absolutePath
    }

    private fun drawTransformedSource(
        canvas: Canvas,
        source: Bitmap,
        targetW: Int,
        targetH: Int,
        filterType: String
    ) {
        val scale = max(targetW.toFloat() / source.width, targetH.toFloat() / source.height)
        val matrix = Matrix().apply {
            postTranslate(-source.width / 2f, -source.height / 2f)
            postScale(scale, scale)
            postTranslate(targetW / 2f, targetH / 2f)
        }

        val cm = ColorMatrix()
        when {
            filterType.contains("Anime", ignoreCase = true) -> {
                cm.setSaturation(1.45f)
                val contrast = ColorMatrix(
                    floatArrayOf(
                        1.18f, 0f, 0f, 0f, -12f,
                        0f, 1.18f, 0f, 0f, -8f,
                        0f, 0f, 1.24f, 0f, 6f,
                        0f, 0f, 0f, 1f, 0f
                    )
                )
                cm.postConcat(contrast)
            }
            filterType.contains("Cartoon", ignoreCase = true) -> {
                cm.setSaturation(1.65f)
            }
            filterType.contains("Cinematic", ignoreCase = true) -> {
                val grade = ColorMatrix(
                    floatArrayOf(
                        1.12f, 0f, 0f, 0f, -10f,
                        0f, 1.05f, 0f, 0f, -4f,
                        0f, 0f, 1.15f, 0f, 8f,
                        0f, 0f, 0f, 1f, 0f
                    )
                )
                cm.postConcat(grade)
            }
            filterType.contains("Background Removal", ignoreCase = true) -> {
                // Checkerboard backdrop behind centered subject card
                val checker = Paint()
                val cell = 28f
                for (y in 0..(targetH / cell).toInt()) {
                    for (x in 0..(targetW / cell).toInt()) {
                        checker.color = if ((x + y) % 2 == 0) Color.parseColor("#1E2436") else Color.parseColor("#141927")
                        canvas.drawRect(x * cell, y * cell, (x + 1) * cell, (y + 1) * cell, checker)
                    }
                }
            }
            filterType.contains("Background Change", ignoreCase = true) -> {
                val bgPaint = Paint().apply {
                    shader = LinearGradient(
                        0f, 0f, targetW.toFloat(), targetH.toFloat(),
                        intArrayOf(Color.parseColor("#311042"), Color.parseColor("#0F172A"), Color.parseColor("#0E7490")),
                        null, Shader.TileMode.CLAMP
                    )
                }
                canvas.drawRect(0f, 0f, targetW.toFloat(), targetH.toFloat(), bgPaint)
            }
            filterType.contains("Fantasy", ignoreCase = true) -> {
                cm.setSaturation(1.3f)
                val fantasy = ColorMatrix(
                    floatArrayOf(
                        1.05f, 0f, 0.1f, 0f, 0f,
                        0f, 1.0f, 0.12f, 0f, 0f,
                        0.08f, 0f, 1.25f, 0f, 12f,
                        0f, 0f, 0f, 1f, 0f
                    )
                )
                cm.postConcat(fantasy)
            }
        }

        val paint = Paint(Paint.ANTI_ALIAS_FLAG or Paint.FILTER_BITMAP_FLAG).apply {
            colorFilter = ColorMatrixColorFilter(cm)
        }
        if (filterType.contains("Background", ignoreCase = true)) {
            // Draw slightly inset framed subject so replaced/removed backdrop is clearly visible
            val insetMatrix = Matrix().apply {
                val insetScale = minOf(targetW * 0.84f / source.width, targetH * 0.78f / source.height)
                postTranslate(-source.width / 2f, -source.height / 2f)
                postScale(insetScale, insetScale)
                postTranslate(targetW / 2f, targetH * 0.45f)
            }
            canvas.drawBitmap(source, insetMatrix, paint)
        } else {
            canvas.drawBitmap(source, matrix, paint)
        }
    }

    private fun drawProceduralStudioPreview(
        canvas: Canvas,
        width: Int,
        height: Int,
        seedHash: Int,
        styleLabel: String
    ) {
        val palettes = listOf(
            intArrayOf(Color.parseColor("#090B12"), Color.parseColor("#312E81"), Color.parseColor("#0891B2")),
            intArrayOf(Color.parseColor("#090B12"), Color.parseColor("#4C1D95"), Color.parseColor("#DB2777")),
            intArrayOf(Color.parseColor("#090B12"), Color.parseColor("#0F766E"), Color.parseColor("#F59E0B")),
            intArrayOf(Color.parseColor("#090B12"), Color.parseColor("#1E3A8A"), Color.parseColor("#22D3EE"))
        )
        val palette = palettes[(seedHash.ushr(1)) % palettes.size]
        val bgPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            shader = LinearGradient(
                0f, 0f, width.toFloat(), height.toFloat(),
                palette, null, Shader.TileMode.CLAMP
            )
        }
        canvas.drawRect(0f, 0f, width.toFloat(), height.toFloat(), bgPaint)

        // Luminous orb & prism ribbons keyed by prompt hash
        val orbX = width * (0.28f + ((seedHash and 0xFF) / 255f) * 0.44f)
        val orbY = height * (0.24f + (((seedHash ushr 8) and 0xFF) / 255f) * 0.34f)
        val orbRadius = minOf(width, height) * 0.42f
        val orbPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            shader = RadialGradient(
                orbX, orbY, orbRadius,
                intArrayOf(Color.argb(210, 34, 211, 238), Color.argb(120, 99, 102, 241), Color.TRANSPARENT),
                floatArrayOf(0f, 0.55f, 1f),
                Shader.TileMode.CLAMP
            )
        }
        canvas.drawCircle(orbX, orbY, orbRadius, orbPaint)

        val accentOrbPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            shader = RadialGradient(
                width - orbX, height * 0.58f, orbRadius * 0.7f,
                intArrayOf(Color.argb(170, 245, 158, 11), Color.TRANSPARENT),
                null, Shader.TileMode.CLAMP
            )
        }
        canvas.drawCircle(width - orbX, height * 0.58f, orbRadius * 0.7f, accentOrbPaint)

        // Subtle geometric studio grid lines
        val gridPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.argb(32, 248, 250, 252)
            strokeWidth = 1.5f
        }
        val step = width / 6f
        for (i in 1..5) {
            canvas.drawLine(i * step, 0f, i * step, height.toFloat(), gridPaint)
        }
        val stepY = height / 6f
        for (i in 1..5) {
            canvas.drawLine(0f, i * stepY, width.toFloat(), i * stepY, gridPaint)
        }

        // Center stylized motif badge
        val motifPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            style = Paint.Style.STROKE
            strokeWidth = 3f
            color = Color.argb(140, 248, 250, 252)
        }
        canvas.drawCircle(width / 2f, height * 0.42f, minOf(width, height) * 0.16f, motifPaint)
        val styleTagPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.argb(210, 248, 250, 252)
            textSize = (width * 0.032f).coerceIn(20f, 36f)
            textAlign = Paint.Align.CENTER
            isFakeBoldText = true
        }
        canvas.drawText(styleLabel.uppercase(), width / 2f, height * 0.43f, styleTagPaint)
    }
}
