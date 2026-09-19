package com.example.ui.components

import android.graphics.ColorMatrixColorFilter
import android.graphics.Paint
import android.view.ViewGroup
import androidx.camera.core.Camera
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageCapture
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bedtime
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Videocam
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import com.example.camera.AspectRatioMode
import com.example.camera.CameraFilter
import com.example.camera.CameraMode
import com.example.camera.CameraState
import com.example.camera.GridType
import com.example.camera.ImageProcessor
import kotlin.math.abs

@Composable
fun CameraViewfinder(
    state: CameraState,
    hasCameraPermission: Boolean,
    onBindImageCapture: (ImageCapture) -> Unit,
    onZoomChange: (Float) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    var previewView by remember { mutableStateOf<PreviewView?>(null) }
    var cameraProvider by remember { mutableStateOf<ProcessCameraProvider?>(null) }
    var activeCamera by remember { mutableStateOf<Camera?>(null) }

    // Retrieve ProcessCameraProvider safely
    LaunchedEffect(context) {
        try {
            val future = ProcessCameraProvider.getInstance(context)
            cameraProvider = future.get()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    // Safely unbind all when disposing
    DisposableEffect(lifecycleOwner, cameraProvider) {
        onDispose {
            try {
                cameraProvider?.unbindAll()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    // Bind camera lifecycle reactively
    LaunchedEffect(cameraProvider, previewView, state.isBackCamera, hasCameraPermission) {
        val provider = cameraProvider ?: return@LaunchedEffect
        val pView = previewView ?: return@LaunchedEffect
        if (!hasCameraPermission) return@LaunchedEffect

        try {
            provider.unbindAll()

            val preview = Preview.Builder().build().also {
                it.setSurfaceProvider(pView.surfaceProvider)
            }

            val imageCapture = ImageCapture.Builder()
                .setCaptureMode(ImageCapture.CAPTURE_MODE_MAXIMIZE_QUALITY)
                .build()
            onBindImageCapture(imageCapture)

            val cameraSelector = if (state.isBackCamera) {
                CameraSelector.DEFAULT_BACK_CAMERA
            } else {
                CameraSelector.DEFAULT_FRONT_CAMERA
            }

            val camera = provider.bindToLifecycle(
                lifecycleOwner,
                cameraSelector,
                preview,
                imageCapture
            )
            activeCamera = camera
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    // Reactively apply zoom to hardware CameraControl
    LaunchedEffect(state.zoomRatio, activeCamera) {
        val cam = activeCamera ?: return@LaunchedEffect
        try {
            val zoomState = cam.cameraInfo.zoomState.value
            val minZ = zoomState?.minZoomRatio ?: 1.0f
            val maxZ = zoomState?.maxZoomRatio ?: 8.0f
            val hardwareRatio = state.zoomRatio.coerceIn(minZ, maxZ)
            cam.cameraControl.setZoomRatio(hardwareRatio)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    // Fallback sample bitmap for preview overlay or emulator simulation
    val sampleBitmap = remember { ImageProcessor.createSampleSceneBitmap(1080, 1440) }

    val filterColorMatrix = remember(state.selectedFilter) {
        state.selectedFilter.toComposeColorMatrix()
    }

    BoxWithConstraints(
        modifier = modifier
            .background(Color.Black)
            .pointerInput(Unit) {
                detectTransformGestures { _, _, zoom, _ ->
                    val newRatio = (state.zoomRatio * zoom).coerceIn(0.5f, 100.0f)
                    onZoomChange(Math.round(newRatio * 10f) / 10f)
                }
            },
        contentAlignment = Alignment.Center
    ) {
        val containerWidth = maxWidth
        val containerHeight = maxHeight

        val targetRatio = when (state.aspectRatio) {
            AspectRatioMode.RATIO_4_3 -> 3f / 4f
            AspectRatioMode.RATIO_16_9 -> 9f / 16f
            AspectRatioMode.RATIO_1_1 -> 1f
            AspectRatioMode.RATIO_FULL -> containerWidth.value / containerHeight.value
        }

        Box(
            modifier = Modifier
                .aspectRatio(targetRatio, matchHeightConstraintsFirst = true)
                .fillMaxSize()
                .clip(RoundedCornerShape(8.dp))
                .background(Color(0xFF0F1117))
        ) {
            if (hasCameraPermission) {
                // Calculate extra digital magnification for preview if zoom exceeds camera hardware max
                val hwMax = activeCamera?.cameraInfo?.zoomState?.value?.maxZoomRatio ?: 8.0f
                val digitalScale = if (state.zoomRatio > hwMax) (state.zoomRatio / hwMax).coerceAtMost(12.5f) else 1.0f

                AndroidView(
                    factory = { ctx ->
                        PreviewView(ctx).apply {
                            layoutParams = ViewGroup.LayoutParams(
                                ViewGroup.LayoutParams.MATCH_PARENT,
                                ViewGroup.LayoutParams.MATCH_PARENT
                            )
                            scaleType = PreviewView.ScaleType.FILL_CENTER
                            // Use COMPATIBLE (TextureView) to prevent BufferQueue abandonment in Compose/emulator
                            implementationMode = PreviewView.ImplementationMode.COMPATIBLE
                            previewView = this
                        }
                    },
                    update = { view ->
                        previewView = view
                    },
                    modifier = Modifier
                        .fillMaxSize()
                        .graphicsLayer(
                            scaleX = digitalScale,
                            scaleY = digitalScale
                        )
                        .drawWithContent {
                            drawContent()
                            // Apply real-time live filter overlay over camera preview!
                            if (state.selectedFilter != CameraFilter.NORMAL) {
                                val androidMatrix = state.selectedFilter.toAndroidColorMatrix()
                                val paint = Paint().apply {
                                    colorFilter = ColorMatrixColorFilter(androidMatrix)
                                }
                                // Subtle blend overlay to tint preview accurately
                                drawRect(
                                    color = Color(state.selectedFilter.previewColor).copy(alpha = 0.15f)
                                )
                            }
                        }
                )
            } else {
                // Fallback interactive synthetic viewfinder with live filter and 0.5x - 100x zoom rendering
                Canvas(
                    modifier = Modifier.fillMaxSize()
                ) {
                    val filterMatrix = state.selectedFilter.toAndroidColorMatrix()
                    val paint = Paint(Paint.ANTI_ALIAS_FLAG or Paint.FILTER_BITMAP_FLAG).apply {
                        colorFilter = ColorMatrixColorFilter(filterMatrix)
                    }

                    val zoom = state.zoomRatio.coerceIn(0.5f, 100.0f)
                    val cropW = (sampleBitmap.width / zoom).coerceIn(12f, sampleBitmap.width.toFloat())
                    val cropH = (sampleBitmap.height / zoom).coerceIn(16f, sampleBitmap.height.toFloat())
                    val left = ((sampleBitmap.width - cropW) / 2f).toInt().coerceAtLeast(0)
                    val top = ((sampleBitmap.height - cropH) / 2f).toInt().coerceAtLeast(0)
                    val right = (left + cropW.toInt()).coerceAtMost(sampleBitmap.width)
                    val bottom = (top + cropH.toInt()).coerceAtMost(sampleBitmap.height)

                    val srcRect = android.graphics.Rect(left, top, right, bottom)
                    val dstRect = android.graphics.RectF(0f, 0f, size.width, size.height)
                    drawContext.canvas.nativeCanvas.drawBitmap(sampleBitmap, srcRect, dstRect, paint)
                }
            }

            // Real-Time Grid Line Overlay
            if (state.gridType != GridType.NONE) {
                GridOverlay(gridType = state.gridType, modifier = Modifier.fillMaxSize())
            }

            // Stabilizer Gyroscope / Horizon Level Overlay
            if (state.isStabilizerEnabled) {
                HorizonLevelOverlay(
                    rollAngle = state.rollAngle,
                    isLevelStable = state.isLevelStable,
                    modifier = Modifier.fillMaxSize()
                )
            }

            // Super Zoom Target Locator (Mini-PIP Overview Window for >= 15x Zoom)
            if (state.zoomRatio >= 15f) {
                Box(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(top = 48.dp, end = 12.dp)
                        .size(width = 68.dp, height = 90.dp)
                        .background(Color(0xDD000000), RoundedCornerShape(8.dp))
                        .border(1.5.dp, Color(0xFFFFB300), RoundedCornerShape(8.dp))
                        .clip(RoundedCornerShape(8.dp))
                ) {
                    Image(
                        bitmap = sampleBitmap.asImageBitmap(),
                        contentDescription = "Pemandangan Utuh Zoom",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .fillMaxSize()
                            .alpha(0.65f)
                    )
                    // Magnification target reticle box
                    val reticleFraction = (1.0f / (state.zoomRatio / 4f)).coerceIn(0.12f, 0.55f)
                    Box(
                        modifier = Modifier
                            .align(Alignment.Center)
                            .size((68 * reticleFraction).dp, (90 * reticleFraction).dp)
                            .border(1.5.dp, Color(0xFFFFD600))
                    )
                    Text(
                        text = "${state.zoomRatio.toInt()}x",
                        color = Color(0xFFFFD600),
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier
                            .align(Alignment.BottomCenter)
                            .background(Color(0xBB000000), RoundedCornerShape(4.dp))
                            .padding(horizontal = 4.dp, vertical = 1.dp)
                    )
                }
            }

            // Low Light / Auto Night Mode Indicator Badge
            val showNightBadge = state.currentMode == CameraMode.NIGHT ||
                    (state.isNightModeAuto && state.isLowLightDetected)

            AnimatedVisibility(
                visible = showNightBadge,
                enter = fadeIn(),
                exit = fadeOut(),
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .padding(12.dp)
            ) {
                Row(
                    modifier = Modifier
                        .background(Color(0xCC000000), RoundedCornerShape(20.dp))
                        .border(1.dp, Color(0xFFFFB300), RoundedCornerShape(20.dp))
                        .padding(horizontal = 10.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Bedtime,
                        contentDescription = "Mode Malam",
                        tint = Color(0xFFFFB300),
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = if (state.currentMode == CameraMode.NIGHT) "Mode Malam • Eksposur Tinggi" else "Malam Otomatis (${state.ambientLux.toInt()} lux)",
                        color = Color(0xFFFFE082),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }

            // Stabilizer Active Badge
            if (state.isStabilizerEnabled) {
                Row(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(12.dp)
                        .background(Color(0xBB000000), RoundedCornerShape(20.dp))
                        .border(
                            1.dp,
                            if (state.isLevelStable) Color(0xFF00E676) else Color(0x66FFFFFF),
                            RoundedCornerShape(20.dp)
                        )
                        .padding(horizontal = 10.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .clip(CircleShape)
                            .background(if (state.isLevelStable) Color(0xFF00E676) else Color(0xFFFF9100))
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = if (state.isLevelStable) "Stabilizer: Sejajar" else "Stabilizer: EIS Aktif",
                        color = Color.White,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }

            // Video Recording Timer & Audio Meter
            if (state.isRecordingVideo) {
                Row(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(bottom = 24.dp)
                        .background(Color(0xD9B71C1C), RoundedCornerShape(24.dp))
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(10.dp)
                            .clip(CircleShape)
                            .background(Color.White)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    val mins = state.videoElapsedSeconds / 60
                    val secs = state.videoElapsedSeconds % 60
                    Text(
                        text = String.format("REC %02d:%02d • %s", mins, secs, state.videoQuality.label),
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp
                    )
                }
            }

            // Long Exposure / Night Capture Progress Modal
            if (state.isCapturing && state.captureProgress > 0f) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color(0xCC000000)),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.padding(24.dp)
                    ) {
                        CircularProgressIndicator(
                            progress = { state.captureProgress },
                            color = Color(0xFFFFB300),
                            trackColor = Color(0x33FFFFFF),
                            strokeWidth = 4.dp,
                            modifier = Modifier.size(64.dp)
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "Mengumpulkan Cahaya...",
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Tahan kamera tetap stabil (${(state.captureProgress * 100).toInt()}%)",
                            color = Color(0xFFB0BEC5),
                            fontSize = 12.sp
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun GridOverlay(gridType: GridType, modifier: Modifier = Modifier) {
    Canvas(modifier = modifier) {
        val stroke = Stroke(width = 1.dp.toPx())
        val color = Color(0x66FFFFFF)

        when (gridType) {
            GridType.RULE_OF_THIRDS -> {
                // Horizontal lines
                drawLine(color, Offset(0f, size.height / 3f), Offset(size.width, size.height / 3f), strokeWidth = stroke.width)
                drawLine(color, Offset(0f, size.height * 2f / 3f), Offset(size.width, size.height * 2f / 3f), strokeWidth = stroke.width)
                // Vertical lines
                drawLine(color, Offset(size.width / 3f, 0f), Offset(size.width / 3f, size.height), strokeWidth = stroke.width)
                drawLine(color, Offset(size.width * 2f / 3f, 0f), Offset(size.width * 2f / 3f, size.height), strokeWidth = stroke.width)
            }
            GridType.GOLDEN_RATIO -> {
                val phi = 0.618f
                val h1 = size.height * (1f - phi)
                val h2 = size.height * phi
                val w1 = size.width * (1f - phi)
                val w2 = size.width * phi

                drawLine(color, Offset(0f, h1), Offset(size.width, h1), strokeWidth = stroke.width)
                drawLine(color, Offset(0f, h2), Offset(size.width, h2), strokeWidth = stroke.width)
                drawLine(color, Offset(w1, 0f), Offset(w1, size.height), strokeWidth = stroke.width)
                drawLine(color, Offset(w2, 0f), Offset(w2, size.height), strokeWidth = stroke.width)
            }
            GridType.CROSSHAIR -> {
                val cx = size.width / 2f
                val cy = size.height / 2f
                val arm = 30.dp.toPx()
                drawLine(color, Offset(cx - arm, cy), Offset(cx + arm, cy), strokeWidth = stroke.width * 1.5f)
                drawLine(color, Offset(cx, cy - arm), Offset(cx, cy + arm), strokeWidth = stroke.width * 1.5f)
                drawCircle(color, radius = 12.dp.toPx(), style = stroke)
            }
            GridType.NONE -> {}
        }
    }
}

@Composable
fun HorizonLevelOverlay(
    rollAngle: Float,
    isLevelStable: Boolean,
    modifier: Modifier = Modifier
) {
    Canvas(modifier = modifier) {
        val cx = size.width / 2f
        val cy = size.height / 2f
        val barLength = 100.dp.toPx()
        val accentColor = if (isLevelStable) Color(0xFF00E676) else Color(0xFFFFD54F)

        rotate(degrees = -rollAngle, pivot = Offset(cx, cy)) {
            // Left balance tick
            drawLine(
                color = accentColor,
                start = Offset(cx - barLength, cy),
                end = Offset(cx - 24.dp.toPx(), cy),
                strokeWidth = if (isLevelStable) 2.5.dp.toPx() else 1.5.dp.toPx()
            )
            // Right balance tick
            drawLine(
                color = accentColor,
                start = Offset(cx + 24.dp.toPx(), cy),
                end = Offset(cx + barLength, cy),
                strokeWidth = if (isLevelStable) 2.5.dp.toPx() else 1.5.dp.toPx()
            )
            // Center ring indicator
            drawCircle(
                color = accentColor,
                radius = 6.dp.toPx(),
                center = Offset(cx, cy),
                style = Stroke(width = if (isLevelStable) 2.5.dp.toPx() else 1.5.dp.toPx())
            )
        }

        // Center fixed reference horizon notch
        drawLine(
            color = Color(0x66FFFFFF),
            start = Offset(cx - 4.dp.toPx(), cy),
            end = Offset(cx + 4.dp.toPx(), cy),
            strokeWidth = 2.dp.toPx()
        )
    }
}
