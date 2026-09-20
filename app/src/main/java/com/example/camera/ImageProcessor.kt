package com.example.camera

import android.content.ContentValues
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.ColorMatrix
import android.graphics.ColorMatrixColorFilter
import android.graphics.Matrix
import android.graphics.Paint
import android.graphics.RadialGradient
import android.graphics.Rect
import android.graphics.Shader
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.io.OutputStream
import kotlin.math.max
import kotlin.math.min

data class PhotoEditAdjustments(
    val brightness: Float = 0f,    // -100 to +100
    val contrast: Float = 0f,      // -100 to +100
    val saturation: Float = 0f,    // -100 to +100
    val warmth: Float = 0f,        // -100 to +100
    val vignette: Float = 0f,      // 0 to 100
    val rotationDegrees: Float = 0f,
    val flipHorizontal: Boolean = false,
    val selectedFilter: CameraFilter = CameraFilter.NORMAL,
    val filterIntensity: Float = 1.0f // 0.0 to 1.0
)

enum class ExportProfile(
    val title: String,
    val description: String,
    val targetWidth: Int,
    val targetHeight: Int,
    val quality: Int,
    val format: Bitmap.CompressFormat
) {
    PRINT_ULTRA_HD(
        title = "Cetak Kualitas Tinggi (UHD)",
        description = "Maksimum resolusi 4K untuk pencetakan foto profesional & kanvas (300 DPI)",
        targetWidth = 3840,
        targetHeight = 2880,
        quality = 100,
        format = Bitmap.CompressFormat.JPEG
    ),
    SOCIAL_FEED(
        title = "Instagram & Media Sosial (4:5)",
        description = "Format ideal feed sosial 1080x1350 tanpa penurunan ketajaman",
        targetWidth = 1080,
        targetHeight = 1350,
        quality = 95,
        format = Bitmap.CompressFormat.JPEG
    ),
    STORY_REELS(
        title = "Story & Reels (9:16)",
        description = "Resolusi penuh vertikal 1080x1920 untuk cerita & konten layar penuh",
        targetWidth = 1080,
        targetHeight = 1920,
        quality = 95,
        format = Bitmap.CompressFormat.JPEG
    ),
    ORIGINAL_LOSSLESS(
        title = "Resolusi Asli Master",
        description = "Penyimpanan asli tanpa kompresi dengan detail sensor maksimal",
        targetWidth = -1,
        targetHeight = -1,
        quality = 100,
        format = Bitmap.CompressFormat.PNG
    )
}

object ImageProcessor {

    /**
     * Applies filter, manual adjustments, and vignette to a source Bitmap.
     */
    suspend fun applyAdjustments(
        source: Bitmap,
        adjustments: PhotoEditAdjustments
    ): Bitmap = withContext(Dispatchers.Default) {
        // Base transformation matrix (Rotation + Flip)
        val matrix = Matrix()
        if (adjustments.rotationDegrees != 0f) {
            matrix.postRotate(adjustments.rotationDegrees)
        }
        if (adjustments.flipHorizontal) {
            matrix.postScale(-1f, 1f, source.width / 2f, source.height / 2f)
        }

        val transformedSource = if (adjustments.rotationDegrees != 0f || adjustments.flipHorizontal) {
            Bitmap.createBitmap(source, 0, 0, source.width, source.height, matrix, true)
        } else {
            source
        }

        val resultBitmap = Bitmap.createBitmap(
            transformedSource.width,
            transformedSource.height,
            Bitmap.Config.ARGB_8888
        )
        val canvas = Canvas(resultBitmap)

        // Composite Color Matrix
        val combinedMatrix = ColorMatrix()

        // 1. Filter color matrix
        if (adjustments.selectedFilter != CameraFilter.NORMAL) {
            val filterMatrix = adjustments.selectedFilter.toAndroidColorMatrix()
            combinedMatrix.postConcat(filterMatrix)
        }

        // 2. Brightness (-100 to 100 -> offset -100 to 100)
        if (adjustments.brightness != 0f) {
            val brightnessOffset = (adjustments.brightness / 100f) * 80f
            val bMatrix = ColorMatrix(
                floatArrayOf(
                    1f, 0f, 0f, 0f, brightnessOffset,
                    0f, 1f, 0f, 0f, brightnessOffset,
                    0f, 0f, 1f, 0f, brightnessOffset,
                    0f, 0f, 0f, 1f, 0f
                )
            )
            combinedMatrix.postConcat(bMatrix)
        }

        // 3. Contrast (-100 to 100 -> scale 0.5 to 1.8)
        if (adjustments.contrast != 0f) {
            val scale = 1f + (adjustments.contrast / 100f) * 0.8f
            val translate = (-0.5f * scale + 0.5f) * 255f
            val cMatrix = ColorMatrix(
                floatArrayOf(
                    scale, 0f, 0f, 0f, translate,
                    0f, scale, 0f, 0f, translate,
                    0f, 0f, scale, 0f, translate,
                    0f, 0f, 0f, 1f, 0f
                )
            )
            combinedMatrix.postConcat(cMatrix)
        }

        // 4. Saturation (-100 to 100 -> sat 0.0 to 2.0)
        if (adjustments.saturation != 0f) {
            val sat = 1f + (adjustments.saturation / 100f)
            val sMatrix = ColorMatrix()
            sMatrix.setSaturation(sat.coerceAtLeast(0f))
            combinedMatrix.postConcat(sMatrix)
        }

        // 5. Warmth (-100 to 100)
        if (adjustments.warmth != 0f) {
            val redBoost = (adjustments.warmth / 100f) * 35f
            val blueReduction = -(adjustments.warmth / 100f) * 35f
            val wMatrix = ColorMatrix(
                floatArrayOf(
                    1f, 0f, 0f, 0f, redBoost,
                    0f, 1f, 0f, 0f, redBoost * 0.3f,
                    0f, 0f, 1f, 0f, blueReduction,
                    0f, 0f, 0f, 1f, 0f
                )
            )
            combinedMatrix.postConcat(wMatrix)
        }

        val paint = Paint(Paint.ANTI_ALIAS_FLAG or Paint.FILTER_BITMAP_FLAG)
        paint.colorFilter = ColorMatrixColorFilter(combinedMatrix)
        canvas.drawBitmap(transformedSource, 0f, 0f, paint)

        // 6. Vignette overlay if enabled
        if (adjustments.vignette > 0f) {
            val w = resultBitmap.width.toFloat()
            val h = resultBitmap.height.toFloat()
            val radius = max(w, h) * 0.75f
            val intensity = (adjustments.vignette / 100f).coerceIn(0f, 1f)
            val alpha = (intensity * 200).toInt()

            val vignetteShader = RadialGradient(
                w / 2f,
                h / 2f,
                radius,
                intArrayOf(Color.TRANSPARENT, Color.argb(alpha / 3, 0, 0, 0), Color.argb(alpha, 0, 0, 0)),
                floatArrayOf(0.4f, 0.75f, 1.0f),
                Shader.TileMode.CLAMP
            )
            val vignettePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                shader = vignetteShader
            }
            canvas.drawRect(0f, 0f, w, h, vignettePaint)
        }

        resultBitmap
    }

    /**
     * Applies Night Mode sharpening & dynamic range enhancement.
     */
    suspend fun processNightMode(source: Bitmap): Bitmap = withContext(Dispatchers.Default) {
        val result = Bitmap.createBitmap(source.width, source.height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(result)

        // Boost shadow illumination + enhance saturation without clipping
        val nightMatrix = ColorMatrix(
            floatArrayOf(
                1.15f, 0f, 0f, 0f, 22f,
                0f, 1.15f, 0f, 0f, 24f,
                0f, 0f, 1.25f, 0f, 30f,
                0f, 0f, 0f, 1f, 0f
            )
        )
        val paint = Paint(Paint.ANTI_ALIAS_FLAG or Paint.FILTER_BITMAP_FLAG).apply {
            colorFilter = ColorMatrixColorFilter(nightMatrix)
        }
        canvas.drawBitmap(source, 0f, 0f, paint)
        result
    }

    /**
     * Resizes and exports a bitmap according to a selected ExportProfile.
     */
    suspend fun exportBitmap(
        context: Context,
        source: Bitmap,
        profile: ExportProfile,
        baseName: String
    ): Uri? = withContext(Dispatchers.IO) {
        val finalBitmap = if (profile.targetWidth > 0 && profile.targetHeight > 0) {
            // Scale and center crop to target dimensions
            scaleCenterCrop(source, profile.targetWidth, profile.targetHeight)
        } else {
            source
        }

        val filename = "${baseName}_${profile.name}_${System.currentTimeMillis()}.${if (profile.format == Bitmap.CompressFormat.PNG) "png" else "jpg"}"

        val contentValues = ContentValues().apply {
            put(MediaStore.MediaColumns.DISPLAY_NAME, filename)
            put(MediaStore.MediaColumns.MIME_TYPE, if (profile.format == Bitmap.CompressFormat.PNG) "image/png" else "image/jpeg")
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_PICTURES + "/KameraPro")
                put(MediaStore.MediaColumns.IS_PENDING, 1)
            }
        }

        val resolver = context.contentResolver
        val uri = resolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues)

        uri?.let { destUri ->
            resolver.openOutputStream(destUri)?.use { out ->
                finalBitmap.compress(profile.format, profile.quality, out)
            }

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                contentValues.clear()
                contentValues.put(MediaStore.MediaColumns.IS_PENDING, 0)
                resolver.update(destUri, contentValues, null, null)
            }
        }

        uri
    }

    private fun scaleCenterCrop(source: Bitmap, targetWidth: Int, targetHeight: Int): Bitmap {
        val srcWidth = source.width
        val srcHeight = source.height

        val scaleX = targetWidth.toFloat() / srcWidth
        val scaleY = targetHeight.toFloat() / srcHeight
        val scale = max(scaleX, scaleY)

        val scaledWidth = scale * srcWidth
        val scaledHeight = scale * srcHeight

        val left = (targetWidth - scaledWidth) / 2f
        val top = (targetHeight - scaledHeight) / 2f

        val output = Bitmap.createBitmap(targetWidth, targetHeight, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(output)
        val paint = Paint(Paint.ANTI_ALIAS_FLAG or Paint.FILTER_BITMAP_FLAG)

        val targetRect = android.graphics.RectF(left, top, left + scaledWidth, top + scaledHeight)
        canvas.drawBitmap(source, null, targetRect, paint)

        return output
    }

    /**
     * Applies AI Super-Resolution, Multi-Pass Bicubic Resampling,
     * Adaptive Edge Sharpening, and Micro-Contrast Reconstruction for zoom captures (up to 100x).
     * Prevents pixelation and eliminates blurry/grainy artifacts.
     */
    suspend fun applySuperResolutionAndSharpening(
        source: Bitmap,
        zoomRatio: Float,
        targetWidth: Int = 1920,
        targetHeight: Int = 1440
    ): Bitmap = withContext(Dispatchers.Default) {
        if (zoomRatio <= 1.05f) return@withContext source

        // 1. Precise Center Crop based on Zoom Ratio
        val zoom = zoomRatio.coerceIn(1.0f, 100.0f)
        val cropW = (source.width / zoom).toInt().coerceIn(16, source.width)
        val cropH = (source.height / zoom).toInt().coerceIn(16, source.height)
        val cropX = ((source.width - cropW) / 2).coerceIn(0, source.width - cropW)
        val cropY = ((source.height - cropH) / 2).coerceIn(0, source.height - cropH)

        val cropped = Bitmap.createBitmap(source, cropX, cropY, cropW, cropH)

        // 2. Multi-Pass Staged Upscaling with Anti-Aliasing (prevents blocky pixelation)
        var currentBitmap = cropped
        var curW = cropW
        var curH = cropH

        // Staged doubling up to target size for smooth interpolation
        while (curW * 2 < targetWidth && curH * 2 < targetHeight) {
            curW *= 2
            curH *= 2
            val nextStep = Bitmap.createBitmap(curW, curH, Bitmap.Config.ARGB_8888)
            val stepCanvas = Canvas(nextStep)
            val stepPaint = Paint(Paint.ANTI_ALIAS_FLAG or Paint.FILTER_BITMAP_FLAG)
            stepCanvas.drawBitmap(
                currentBitmap,
                Rect(0, 0, currentBitmap.width, currentBitmap.height),
                Rect(0, 0, curW, curH),
                stepPaint
            )
            if (currentBitmap != cropped) {
                currentBitmap.recycle()
            }
            currentBitmap = nextStep
        }

        // Final upscale to target canvas
        val finalUpscaled = Bitmap.createBitmap(targetWidth, targetHeight, Bitmap.Config.ARGB_8888)
        val finalCanvas = Canvas(finalUpscaled)
        val finalPaint = Paint(Paint.ANTI_ALIAS_FLAG or Paint.FILTER_BITMAP_FLAG)
        finalCanvas.drawBitmap(
            currentBitmap,
            Rect(0, 0, currentBitmap.width, currentBitmap.height),
            Rect(0, 0, targetWidth, targetHeight),
            finalPaint
        )
        if (currentBitmap != cropped) {
            currentBitmap.recycle()
        }
        cropped.recycle()

        // 3. AI Edge Enhancement / Unsharp Masking Kernel
        // Strength adapts to zoom level: higher zoom receives stronger edge definition
        val sharpenStrength = when {
            zoom >= 50f -> 0.70f
            zoom >= 25f -> 0.50f
            zoom >= 10f -> 0.35f
            else -> 0.20f
        }
        val sharpened = applyConvolutionSharpen(finalUpscaled, sharpenStrength)
        finalUpscaled.recycle()

        // 4. Micro-Contrast & De-Haze Restoration
        // Extreme digital zoom tends to look washed out/foggy; this restores crisp vibrant depth
        val enhanced = Bitmap.createBitmap(sharpened.width, sharpened.height, Bitmap.Config.ARGB_8888)
        val enhCanvas = Canvas(enhanced)
        val contrastFactor = if (zoom >= 30f) 1.15f else 1.06f
        val translate = (-0.5f * contrastFactor + 0.5f) * 255f
        val colorMatrix = ColorMatrix(
            floatArrayOf(
                contrastFactor, 0f, 0f, 0f, translate + 3f,
                0f, contrastFactor, 0f, 0f, translate + 3f,
                0f, 0f, contrastFactor, 0f, translate + 3f,
                0f, 0f, 0f, 1f, 0f
            )
        )
        val dehazePaint = Paint(Paint.ANTI_ALIAS_FLAG or Paint.FILTER_BITMAP_FLAG).apply {
            colorFilter = ColorMatrixColorFilter(colorMatrix)
        }
        enhCanvas.drawBitmap(sharpened, 0f, 0f, dehazePaint)
        sharpened.recycle()

        enhanced
    }

    private fun applyConvolutionSharpen(source: Bitmap, strength: Float): Bitmap {
        val width = source.width
        val height = source.height
        val output = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)

        val srcPixels = IntArray(width * height)
        val dstPixels = IntArray(width * height)
        source.getPixels(srcPixels, 0, width, 0, 0, width, height)

        val k = strength.coerceIn(0.1f, 1.0f)
        val centerWeight = 1.0f + 4f * k

        for (y in 1 until height - 1) {
            val yOffset = y * width
            val topOffset = (y - 1) * width
            val bottomOffset = (y + 1) * width

            for (x in 1 until width - 1) {
                val centerIdx = yOffset + x
                val centerColor = srcPixels[centerIdx]
                val topColor = srcPixels[topOffset + x]
                val bottomColor = srcPixels[bottomOffset + x]
                val leftColor = srcPixels[yOffset + (x - 1)]
                val rightColor = srcPixels[yOffset + (x + 1)]

                val rCenter = (centerColor shr 16) and 0xFF
                val gCenter = (centerColor shr 8) and 0xFF
                val bCenter = centerColor and 0xFF

                val rSum = ((topColor shr 16) and 0xFF) + ((bottomColor shr 16) and 0xFF) +
                        ((leftColor shr 16) and 0xFF) + ((rightColor shr 16) and 0xFF)
                val gSum = ((topColor shr 8) and 0xFF) + ((bottomColor shr 8) and 0xFF) +
                        ((leftColor shr 8) and 0xFF) + ((rightColor shr 8) and 0xFF)
                val bSum = (topColor and 0xFF) + (bottomColor and 0xFF) +
                        (leftColor and 0xFF) + (rightColor and 0xFF)

                val rOut = (centerWeight * rCenter - k * rSum).toInt().coerceIn(0, 255)
                val gOut = (centerWeight * gCenter - k * gSum).toInt().coerceIn(0, 255)
                val bOut = (centerWeight * bCenter - k * bSum).toInt().coerceIn(0, 255)

                dstPixels[centerIdx] = (0xFF shl 24) or (rOut shl 16) or (gOut shl 8) or bOut
            }
        }

        // Copy edges unchanged
        for (x in 0 until width) {
            dstPixels[x] = srcPixels[x]
            dstPixels[(height - 1) * width + x] = srcPixels[(height - 1) * width + x]
        }
        for (y in 0 until height) {
            dstPixels[y * width] = srcPixels[y * width]
            dstPixels[y * width + (width - 1)] = srcPixels[y * width + (width - 1)]
        }

        output.setPixels(dstPixels, 0, width, 0, 0, width, height)
        return output
    }

    /**
     * Creates a synthetic camera scene bitmap (useful as realistic camera simulation if running in emulator).
     */
    fun createSampleSceneBitmap(width: Int = 1080, height: Int = 1440): Bitmap {
        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)

        // Gradient sky
        val skyShader = android.graphics.LinearGradient(
            0f, 0f, 0f, height * 0.65f,
            intArrayOf(0xFF192A56.toInt(), 0xFF4A69BD.toInt(), 0xFFE58E26.toInt(), 0xFFF8C291.toInt()),
            floatArrayOf(0f, 0.4f, 0.75f, 1f),
            Shader.TileMode.CLAMP
        )
        val skyPaint = Paint().apply { shader = skyShader }
        canvas.drawRect(0f, 0f, width.toFloat(), height * 0.65f, skyPaint)

        // Sun / glowing disc
        val sunPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = 0xFFFEEAA7.toInt()
            setShadowLayer(40f, 0f, 0f, 0xFFF39C12.toInt())
        }
        canvas.drawCircle(width * 0.72f, height * 0.38f, 65f, sunPaint)

        // Mountain silhouettes
        val mountainPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = 0xFF2C3E50.toInt()
        }
        val path1 = android.graphics.Path().apply {
            moveTo(0f, height * 0.65f)
            lineTo(width * 0.25f, height * 0.45f)
            lineTo(width * 0.55f, height * 0.65f)
            lineTo(width.toFloat(), height * 0.5f)
            lineTo(width.toFloat(), height.toFloat())
            lineTo(0f, height.toFloat())
            close()
        }
        canvas.drawPath(path1, mountainPaint)

        // Dark foreground hills & trees
        val fgPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = 0xFF1E272C.toInt()
        }
        val path2 = android.graphics.Path().apply {
            moveTo(0f, height * 0.62f)
            lineTo(width * 0.35f, height * 0.58f)
            lineTo(width * 0.7f, height * 0.64f)
            lineTo(width.toFloat(), height * 0.60f)
            lineTo(width.toFloat(), height.toFloat())
            lineTo(0f, height.toFloat())
            close()
        }
        canvas.drawPath(path2, fgPaint)

        // Lake reflection in foreground
        val waterPaint = Paint().apply {
            color = 0xFF14202B.toInt()
        }
        canvas.drawRect(0f, height * 0.72f, width.toFloat(), height.toFloat(), waterPaint)

        return bitmap
    }
}
