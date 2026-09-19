package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Cameraswitch
import androidx.compose.material.icons.filled.Collections
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.camera.CameraMode
import com.example.camera.CameraState
import com.example.data.MediaEntity
import java.io.File

@Composable
fun CameraBottomDeck(
    state: CameraState,
    latestMedia: MediaEntity?,
    onModeChange: (CameraMode) -> Unit,
    onShutterClick: () -> Unit,
    onToggleFilterSelector: () -> Unit,
    onToggleProControls: () -> Unit,
    onFlipCamera: () -> Unit,
    onOpenGallery: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(Color.Black)
            .navigationBarsPadding()
            .padding(bottom = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Mode Selector Row (FOTO, VIDEO, PRO, MALAM, GALERI)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 12.dp)
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            CameraMode.entries.forEach { mode ->
                val isSelected = state.currentMode == mode
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .background(if (isSelected) Color(0x33FFB300) else Color.Transparent)
                        .clickable { onModeChange(mode) }
                        .padding(horizontal = 14.dp, vertical = 6.dp)
                        .testTag("camera_mode_${mode.name.lowercase()}")
                ) {
                    Text(
                        text = mode.displayName,
                        color = if (isSelected) Color(0xFFFFCA28) else Color(0xFF90A4AE),
                        fontSize = 13.sp,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Main Shutter and control deck
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Left: Gallery Thumbnail / Quick Link
            Box(
                modifier = Modifier
                    .size(54.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(Color(0x33FFFFFF))
                    .border(1.5.dp, Color(0x66FFFFFF), RoundedCornerShape(14.dp))
                    .clickable { onOpenGallery() }
                    .testTag("gallery_thumbnail_button"),
                contentAlignment = Alignment.Center
            ) {
                if (latestMedia != null && File(latestMedia.filePath).exists()) {
                    AsyncImage(
                        model = File(latestMedia.filePath),
                        contentDescription = "Thumbnail Terakhir",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxWidth()
                    )
                } else {
                    Icon(
                        imageVector = Icons.Default.Collections,
                        contentDescription = "Galeri",
                        tint = Color.White,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }

            // Quick Filter / Pro toggle
            IconButton(
                onClick = {
                    if (state.currentMode == CameraMode.PRO) {
                        onToggleProControls()
                    } else {
                        onToggleFilterSelector()
                    }
                },
                modifier = Modifier
                    .size(46.dp)
                    .clip(CircleShape)
                    .background(
                        if (state.isFilterSelectorExpanded || state.isProControlExpanded)
                            Color(0x4400E5FF)
                        else Color(0x22FFFFFF)
                    )
                    .testTag("toggle_effects_button")
            ) {
                Icon(
                    imageVector = if (state.currentMode == CameraMode.PRO) Icons.Default.Tune else Icons.Default.AutoAwesome,
                    contentDescription = "Filter / Kontrol",
                    tint = if (state.isFilterSelectorExpanded || state.isProControlExpanded) Color(0xFF00E5FF) else Color.White,
                    modifier = Modifier.size(22.dp)
                )
            }

            // Central Shutter Button
            ShutterButton(
                mode = state.currentMode,
                isRecording = state.isRecordingVideo,
                isCapturing = state.isCapturing,
                onClick = onShutterClick
            )

            // Camera Lens Switcher (Front/Back)
            IconButton(
                onClick = onFlipCamera,
                modifier = Modifier
                    .size(54.dp)
                    .clip(CircleShape)
                    .background(Color(0x22FFFFFF))
                    .testTag("flip_camera_button")
            ) {
                Icon(
                    imageVector = Icons.Default.Cameraswitch,
                    contentDescription = "Ganti Lensa Kamera",
                    tint = Color.White,
                    modifier = Modifier.size(26.dp)
                )
            }
        }
    }
}

@Composable
fun ShutterButton(
    mode: CameraMode,
    isRecording: Boolean,
    isCapturing: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(if (isPressed) 0.92f else 1.0f, label = "shutter_scale")

    val outerRingColor = when {
        mode == CameraMode.VIDEO -> Color(0xFFE53935)
        mode == CameraMode.NIGHT -> Color(0xFFFFB300)
        mode == CameraMode.PRO -> Color(0xFF00E5FF)
        else -> Color.White
    }

    Box(
        modifier = modifier
            .size(80.dp)
            .scale(scale)
            .border(4.dp, outerRingColor, CircleShape)
            .padding(6.dp)
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = onClick
            )
            .testTag("camera_shutter_button"),
        contentAlignment = Alignment.Center
    ) {
        val innerShape = if (mode == CameraMode.VIDEO && isRecording) {
            RoundedCornerShape(8.dp)
        } else {
            CircleShape
        }

        val innerSize by animateDpAsState(
            targetValue = if (mode == CameraMode.VIDEO && isRecording) 30.dp else 60.dp,
            label = "shutter_inner_size"
        )

        val innerColor = when {
            mode == CameraMode.VIDEO -> Color(0xFFE53935)
            mode == CameraMode.NIGHT -> Color(0xFFFFB300)
            mode == CameraMode.PRO -> Color(0xFF00E5FF)
            else -> Color.White
        }

        Box(
            modifier = Modifier
                .size(innerSize)
                .clip(innerShape)
                .background(innerColor)
        )
    }
}
