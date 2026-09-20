package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.ZoomIn
import androidx.compose.material3.Icon
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlin.math.ln
import kotlin.math.exp

private val ZOOM_PRESETS = listOf(0.5f, 1.0f, 2.0f, 5.0f, 10.0f, 30.0f, 100.0f)

@Composable
fun ZoomControlDeck(
    currentZoom: Float,
    onZoomChange: (Float) -> Unit,
    modifier: Modifier = Modifier
) {
    var showFineSlider by remember { mutableStateOf(false) }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 6.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Continuous slider for fine precision tuning between 0.5x and 100x
        AnimatedVisibility(
            visible = showFineSlider,
            enter = fadeIn() + slideInVertically { it / 2 },
            exit = fadeOut() + slideOutVertically { it / 2 }
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xDD12141A), RoundedCornerShape(16.dp))
                    .border(1.dp, Color(0x33FFB300), RoundedCornerShape(16.dp))
                    .padding(horizontal = 16.dp, vertical = 10.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "0.5x Ultra-Wide",
                        color = Color(0xFF90A4AE),
                        fontSize = 11.sp
                    )
                    Text(
                        text = formatZoomText(currentZoom),
                        color = Color(0xFFFFC107),
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "100x Super-Zoom",
                        color = Color(0xFF90A4AE),
                        fontSize = 11.sp
                    )
                }

                // Logarithmic mapped slider for smooth control from 0.5 to 100
                val minLog = ln(0.5f)
                val maxLog = ln(100.0f)
                val currentLog = ln(currentZoom.coerceIn(0.5f, 100.0f))
                val sliderValue = (currentLog - minLog) / (maxLog - minLog)

                Slider(
                    value = sliderValue.coerceIn(0f, 1f),
                    onValueChange = { norm ->
                        val targetLog = minLog + norm * (maxLog - minLog)
                        val ratio = exp(targetLog)
                        val formatted = (Math.round(ratio * 10f) / 10f).coerceIn(0.5f, 100.0f)
                        onZoomChange(formatted)
                    },
                    colors = SliderDefaults.colors(
                        thumbColor = if (currentZoom >= 15f) Color(0xFF00E5FF) else Color(0xFFFFC107),
                        activeTrackColor = if (currentZoom >= 15f) Color(0xFF00E5FF) else Color(0xFFFFB300),
                        inactiveTrackColor = Color(0x44FFFFFF)
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("zoom_fine_slider")
                )

                if (currentZoom >= 15f) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(top = 2.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.AutoAwesome,
                            contentDescription = null,
                            tint = Color(0xFF00E5FF),
                            modifier = Modifier.size(13.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = if (currentZoom >= 50f) "AI Super-Resolution 100x Aktif (Anti-Pecah & Rekonstruksi Tepi)" else "AI Ultra Clarity Aktif",
                            color = Color(0xFF80D8FF),
                            fontSize = 10.5.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(4.dp))

        // Preset Pills Deck [0.5x] [1x] [2x] [5x] [10x] [30x] [100x]
        Row(
            modifier = Modifier
                .background(Color(0xCC000000), RoundedCornerShape(24.dp))
                .border(1.dp, Color(0x33FFFFFF), RoundedCornerShape(24.dp))
                .padding(horizontal = 6.dp, vertical = 4.dp)
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            ZOOM_PRESETS.forEach { preset ->
                val isSelected = Math.abs(currentZoom - preset) < 0.2f
                val pillText = if (preset < 1.0f) "${preset}x" else "${preset.toInt()}x"

                Box(
                    modifier = Modifier
                        .clip(CircleShape)
                        .background(
                            if (isSelected) Color(0xFFFFB300) else Color(0x22FFFFFF)
                        )
                        .clickable {
                            onZoomChange(preset)
                        }
                        .padding(horizontal = 10.dp, vertical = 6.dp)
                        .testTag("zoom_preset_${pillText}"),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = pillText,
                        color = if (isSelected) Color.Black else Color.White,
                        fontSize = 12.sp,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                    )
                }
            }

            // Toggle Slider expansion button
            Box(
                modifier = Modifier
                    .clip(CircleShape)
                    .background(if (showFineSlider) Color(0xFF37474F) else Color.Transparent)
                    .clickable { showFineSlider = !showFineSlider }
                    .padding(horizontal = 8.dp, vertical = 6.dp)
                    .testTag("zoom_toggle_slider_btn"),
                contentAlignment = Alignment.Center
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.ZoomIn,
                        contentDescription = "Slider Zoom Presisi",
                        tint = if (showFineSlider) Color(0xFFFFC107) else Color(0xFFB0BEC5),
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(2.dp))
                    Text(
                        text = formatZoomText(currentZoom),
                        color = if (showFineSlider) Color(0xFFFFC107) else Color(0xFFECEFF1),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

private fun formatZoomText(ratio: Float): String {
    return if (ratio < 1.0f) {
        String.format("%.1fx", ratio)
    } else if (ratio < 10.0f) {
        String.format("%.1fx", ratio)
    } else {
        String.format("%dx", ratio.toInt())
    }
}
