package com.example.ui.screens

import android.graphics.Bitmap
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Flip
import androidx.compose.material.icons.filled.RestartAlt
import androidx.compose.material.icons.filled.RotateRight
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.camera.CameraFilter
import com.example.camera.PhotoEditAdjustments
import com.example.data.MediaEntity
import java.io.File

enum class EditorTab(val label: String) {
    TUNE("Penyesuaian"),
    FILTER("Filter"),
    TRANSFORM("Putar & Balik")
}

enum class TuneProperty(val label: String) {
    BRIGHTNESS("Kecerahan"),
    CONTRAST("Kontras"),
    SATURATION("Saturasi"),
    WARMTH("Kehangatan"),
    VIGNETTE("Vignette")
}

@Composable
fun PhotoEditorScreen(
    media: MediaEntity,
    bitmap: Bitmap?,
    adjustments: PhotoEditAdjustments,
    onUpdateAdjustments: ((PhotoEditAdjustments) -> PhotoEditAdjustments) -> Unit,
    onSave: (saveAsNew: Boolean) -> Unit,
    onCancel: () -> Unit,
    modifier: Modifier = Modifier
) {
    var activeTab by remember { mutableStateOf(EditorTab.TUNE) }
    var activeTuneProp by remember { mutableStateOf(TuneProperty.BRIGHTNESS) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFF090B0E))
            .statusBarsPadding()
            .navigationBarsPadding()
    ) {
        // Top Bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick = onCancel,
                modifier = Modifier.testTag("editor_cancel_button")
            ) {
                Icon(imageVector = Icons.Default.Close, contentDescription = "Batal", tint = Color.White)
            }

            Text(
                text = "Editor Foto",
                color = Color.White,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold
            )

            Row {
                IconButton(
                    onClick = {
                        onUpdateAdjustments { PhotoEditAdjustments() }
                    },
                    modifier = Modifier.testTag("editor_reset_button")
                ) {
                    Icon(imageVector = Icons.Default.RestartAlt, contentDescription = "Reset", tint = Color(0xFF90A4AE))
                }

                Button(
                    onClick = { onSave(false) },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00E5FF)),
                    shape = RoundedCornerShape(18.dp),
                    modifier = Modifier.testTag("editor_save_button")
                ) {
                    Text("Simpan", color = Color.Black, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                }
            }
        }

        // Preview Canvas Area
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .padding(12.dp)
                .clip(RoundedCornerShape(16.dp))
                .background(Color(0xFF14171F)),
            contentAlignment = Alignment.Center
        ) {
            if (bitmap != null) {
                androidx.compose.foundation.Image(
                    bitmap = bitmap.asImageBitmap(),
                    contentDescription = "Foto Diedit",
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Fit
                )
            } else {
                AsyncImage(
                    model = File(media.filePath),
                    contentDescription = "Foto Asli",
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Fit
                )
            }
        }

        // Bottom Editor Panel
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color(0xFF10131A), RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp))
                .border(1.dp, Color(0x33FFFFFF), RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp))
                .padding(16.dp)
        ) {
            // Tab Selector
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                EditorTab.entries.forEach { tab ->
                    val selected = activeTab == tab
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .background(if (selected) Color(0x3300E5FF) else Color.Transparent)
                            .clickable { activeTab = tab }
                            .padding(horizontal = 14.dp, vertical = 6.dp)
                    ) {
                        Text(
                            text = tab.label,
                            color = if (selected) Color(0xFF00E5FF) else Color(0xFF90A4AE),
                            fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
                            fontSize = 13.sp
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            when (activeTab) {
                EditorTab.TUNE -> {
                    // Property Chips
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        TuneProperty.entries.forEach { prop ->
                            val isSel = activeTuneProp == prop
                            val valueText = when (prop) {
                                TuneProperty.BRIGHTNESS -> "${adjustments.brightness.toInt()}"
                                TuneProperty.CONTRAST -> "${adjustments.contrast.toInt()}"
                                TuneProperty.SATURATION -> "${adjustments.saturation.toInt()}"
                                TuneProperty.WARMTH -> "${adjustments.warmth.toInt()}"
                                TuneProperty.VIGNETTE -> "${adjustments.vignette.toInt()}"
                            }

                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(if (isSel) Color(0xFF00E5FF) else Color(0x22FFFFFF))
                                    .clickable { activeTuneProp = prop }
                                    .padding(horizontal = 12.dp, vertical = 6.dp)
                            ) {
                                Text(
                                    text = "${prop.label} ($valueText)",
                                    color = if (isSel) Color.Black else Color.White,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Slider for chosen property
                    val (currentVal, minVal, maxVal) = when (activeTuneProp) {
                        TuneProperty.BRIGHTNESS -> Triple(adjustments.brightness, -100f, 100f)
                        TuneProperty.CONTRAST -> Triple(adjustments.contrast, -100f, 100f)
                        TuneProperty.SATURATION -> Triple(adjustments.saturation, -100f, 100f)
                        TuneProperty.WARMTH -> Triple(adjustments.warmth, -100f, 100f)
                        TuneProperty.VIGNETTE -> Triple(adjustments.vignette, 0f, 100f)
                    }

                    Slider(
                        value = currentVal,
                        onValueChange = { newVal ->
                            onUpdateAdjustments { prev ->
                                when (activeTuneProp) {
                                    TuneProperty.BRIGHTNESS -> prev.copy(brightness = newVal)
                                    TuneProperty.CONTRAST -> prev.copy(contrast = newVal)
                                    TuneProperty.SATURATION -> prev.copy(saturation = newVal)
                                    TuneProperty.WARMTH -> prev.copy(warmth = newVal)
                                    TuneProperty.VIGNETTE -> prev.copy(vignette = newVal)
                                }
                            }
                        },
                        valueRange = minVal..maxVal,
                        colors = SliderDefaults.colors(
                            thumbColor = Color(0xFF00E5FF),
                            activeTrackColor = Color(0xFF00E5FF),
                            inactiveTrackColor = Color(0x33FFFFFF)
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                EditorTab.FILTER -> {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        CameraFilter.entries.forEach { f ->
                            val isChosen = adjustments.selectedFilter == f
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                modifier = Modifier
                                    .clickable {
                                        onUpdateAdjustments { it.copy(selectedFilter = f) }
                                    }
                                    .padding(4.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(50.dp)
                                        .clip(CircleShape)
                                        .background(Color(f.previewColor))
                                        .border(
                                            2.dp,
                                            if (isChosen) Color(0xFF00E5FF) else Color.Transparent,
                                            CircleShape
                                        )
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = f.displayName,
                                    color = if (isChosen) Color(0xFF00E5FF) else Color.White,
                                    fontSize = 11.sp,
                                    fontWeight = if (isChosen) FontWeight.Bold else FontWeight.Normal
                                )
                            }
                        }
                    }
                }

                EditorTab.TRANSFORM -> {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        Button(
                            onClick = {
                                onUpdateAdjustments {
                                    it.copy(rotationDegrees = (it.rotationDegrees + 90f) % 360f)
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0x33FFFFFF))
                        ) {
                            Icon(imageVector = Icons.Default.RotateRight, contentDescription = null, tint = Color.White)
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Putar 90°", color = Color.White)
                        }

                        Button(
                            onClick = {
                                onUpdateAdjustments {
                                    it.copy(flipHorizontal = !it.flipHorizontal)
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0x33FFFFFF))
                        ) {
                            Icon(imageVector = Icons.Default.Flip, contentDescription = null, tint = Color.White)
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Balik Horizontal", color = Color.White)
                        }
                    }
                }
            }
        }
    }
}
