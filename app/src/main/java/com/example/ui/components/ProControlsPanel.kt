package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.camera.CameraState
import com.example.camera.WhiteBalanceSetting

enum class ProControlTab(val label: String) {
    EV("EV"),
    ISO("ISO"),
    SHUTTER("S"),
    WB("WB"),
    FOCUS("MF")
}

@Composable
fun ProControlsPanel(
    state: CameraState,
    onSetEv: (Float) -> Unit,
    onSetIso: (String) -> Unit,
    onSetShutter: (String) -> Unit,
    onSetWb: (WhiteBalanceSetting) -> Unit,
    onSetFocus: (Float) -> Unit,
    modifier: Modifier = Modifier
) {
    var activeTab by remember { mutableStateOf(ProControlTab.EV) }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(Color(0xE60A0C10), RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp))
            .border(1.dp, Color(0x3300E5FF), RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp))
            .padding(16.dp)
            .testTag("pro_controls_panel")
    ) {
        // Tab selectors
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceAround
        ) {
            ProControlTab.entries.forEach { tab ->
                val isSelected = activeTab == tab
                val valueText = when (tab) {
                    ProControlTab.EV -> "${if (state.exposureEv >= 0) "+" else ""}${String.format("%.1f", state.exposureEv)}"
                    ProControlTab.ISO -> state.isoValue
                    ProControlTab.SHUTTER -> state.shutterSpeed
                    ProControlTab.WB -> state.whiteBalance.label
                    ProControlTab.FOCUS -> if (state.focusDistance == 0f) "Auto" else "${(state.focusDistance * 100).toInt()}%"
                }

                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier
                        .clip(RoundedCornerShape(10.dp))
                        .background(if (isSelected) Color(0x3300E5FF) else Color.Transparent)
                        .clickable { activeTab = tab }
                        .padding(horizontal = 8.dp, vertical = 6.dp)
                ) {
                    Text(
                        text = tab.label,
                        color = if (isSelected) Color(0xFF00E5FF) else Color(0xFF90A4AE),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = valueText,
                        color = Color.White,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Content for active tab
        when (activeTab) {
            ProControlTab.EV -> {
                Column(modifier = Modifier.fillMaxWidth()) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Kompensasi Eksposur", color = Color(0xFFB0BEC5), fontSize = 12.sp)
                        Text(
                            "${if (state.exposureEv >= 0) "+" else ""}${String.format("%.1f", state.exposureEv)} EV",
                            color = Color(0xFF00E5FF),
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Slider(
                        value = state.exposureEv,
                        onValueChange = { onSetEv((Math.round(it * 2f) / 2f)) },
                        valueRange = -3.0f..3.0f,
                        steps = 11,
                        colors = SliderDefaults.colors(
                            thumbColor = Color(0xFF00E5FF),
                            activeTrackColor = Color(0xFF00E5FF),
                            inactiveTrackColor = Color(0x44FFFFFF)
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
            ProControlTab.ISO -> {
                val isoList = listOf("AUTO", "100", "200", "400", "800", "1600", "3200")
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    isoList.forEach { iso ->
                        val selected = state.isoValue == iso
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(if (selected) Color(0xFF00E5FF) else Color(0x22FFFFFF))
                                .clickable { onSetIso(iso) }
                                .padding(horizontal = 14.dp, vertical = 8.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = iso,
                                color = if (selected) Color.Black else Color.White,
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp
                            )
                        }
                    }
                }
            }
            ProControlTab.SHUTTER -> {
                val shutterList = listOf("AUTO", "1/1000s", "1/500s", "1/250s", "1/125s", "1/60s", "1/30s", "1/4s", "1s")
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    shutterList.forEach { speed ->
                        val selected = state.shutterSpeed == speed
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(if (selected) Color(0xFF00E5FF) else Color(0x22FFFFFF))
                                .clickable { onSetShutter(speed) }
                                .padding(horizontal = 12.dp, vertical = 8.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = speed,
                                color = if (selected) Color.Black else Color.White,
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp
                            )
                        }
                    }
                }
            }
            ProControlTab.WB -> {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    WhiteBalanceSetting.entries.forEach { wb ->
                        val selected = state.whiteBalance == wb
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(if (selected) Color(0xFFFFB300) else Color(0x22FFFFFF))
                                .clickable { onSetWb(wb) }
                                .padding(horizontal = 12.dp, vertical = 8.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(
                                    text = wb.label,
                                    color = if (selected) Color.Black else Color.White,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 12.sp
                                )
                                Text(
                                    text = wb.tempK,
                                    color = if (selected) Color(0xFF37474F) else Color(0xFFB0BEC5),
                                    fontSize = 10.sp
                                )
                            }
                        }
                    }
                }
            }
            ProControlTab.FOCUS -> {
                Column(modifier = Modifier.fillMaxWidth()) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Jarak Fokus Manual", color = Color(0xFFB0BEC5), fontSize = 12.sp)
                        Text(
                            if (state.focusDistance == 0f) "Auto Focus (AF)" else "MF: ${(state.focusDistance * 100).toInt()}%",
                            color = Color(0xFF00E5FF),
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Slider(
                        value = state.focusDistance,
                        onValueChange = onSetFocus,
                        valueRange = 0.0f..1.0f,
                        colors = SliderDefaults.colors(
                            thumbColor = Color(0xFF00E5FF),
                            activeTrackColor = Color(0xFF00E5FF),
                            inactiveTrackColor = Color(0x44FFFFFF)
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        }
    }
}
