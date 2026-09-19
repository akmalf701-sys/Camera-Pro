package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AspectRatio
import androidx.compose.material.icons.filled.Bedtime
import androidx.compose.material.icons.filled.CloudDone
import androidx.compose.material.icons.filled.CloudUpload
import androidx.compose.material.icons.filled.FlashAuto
import androidx.compose.material.icons.filled.FlashOff
import androidx.compose.material.icons.filled.FlashOn
import androidx.compose.material.icons.filled.GridOn
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.camera.AspectRatioMode
import com.example.camera.CameraState
import com.example.camera.FlashMode
import com.example.camera.GridType
import com.example.data.CloudSyncStatus

@Composable
fun CameraTopBar(
    state: CameraState,
    cloudSyncStatus: CloudSyncStatus,
    pendingSyncCount: Int,
    onCycleFlash: () -> Unit,
    onCycleAspectRatio: () -> Unit,
    onToggleStabilizer: () -> Unit,
    onToggleNightModeAuto: () -> Unit,
    onCycleGrid: () -> Unit,
    onOpenCloudSync: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .statusBarsPadding()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Flash Mode
        IconButton(
            onClick = onCycleFlash,
            modifier = Modifier
                .size(40.dp)
                .clip(CircleShape)
                .background(Color(0x55000000))
                .testTag("flash_toggle_button")
        ) {
            val (icon, color) = when (state.flashMode) {
                FlashMode.AUTO -> Icons.Default.FlashAuto to Color(0xFFFFCA28)
                FlashMode.ON -> Icons.Default.FlashOn to Color(0xFFFFD54F)
                FlashMode.TORCH -> Icons.Default.FlashOn to Color(0xFFFF9100)
                FlashMode.OFF -> Icons.Default.FlashOff to Color.White.copy(alpha = 0.7f)
            }
            Icon(imageVector = icon, contentDescription = "Flash", tint = color, modifier = Modifier.size(20.dp))
        }

        // Aspect Ratio
        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(16.dp))
                .background(Color(0x55000000))
                .clickable { onCycleAspectRatio() }
                .padding(horizontal = 10.dp, vertical = 6.dp)
                .testTag("aspect_ratio_button"),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = state.aspectRatio.label,
                color = Color.White,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold
            )
        }

        // Stabilizer Toggle
        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(16.dp))
                .background(if (state.isStabilizerEnabled) Color(0x3300E676) else Color(0x55000000))
                .border(
                    1.dp,
                    if (state.isStabilizerEnabled) Color(0xFF00E676) else Color.Transparent,
                    RoundedCornerShape(16.dp)
                )
                .clickable { onToggleStabilizer() }
                .padding(horizontal = 10.dp, vertical = 6.dp)
                .testTag("stabilizer_toggle_button"),
            contentAlignment = Alignment.Center
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.Security,
                    contentDescription = "Stabilizer",
                    tint = if (state.isStabilizerEnabled) Color(0xFF00E676) else Color.White.copy(alpha = 0.6f),
                    modifier = Modifier.size(14.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = "OIS",
                    color = if (state.isStabilizerEnabled) Color(0xFF00E676) else Color.White.copy(alpha = 0.6f),
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        // Night Mode Auto Toggle
        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(16.dp))
                .background(if (state.isNightModeAuto) Color(0x33FFB300) else Color(0x55000000))
                .border(
                    1.dp,
                    if (state.isNightModeAuto) Color(0xFFFFB300) else Color.Transparent,
                    RoundedCornerShape(16.dp)
                )
                .clickable { onToggleNightModeAuto() }
                .padding(horizontal = 10.dp, vertical = 6.dp)
                .testTag("night_mode_toggle_button"),
            contentAlignment = Alignment.Center
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.Bedtime,
                    contentDescription = "Night Mode",
                    tint = if (state.isNightModeAuto) Color(0xFFFFB300) else Color.White.copy(alpha = 0.6f),
                    modifier = Modifier.size(14.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = if (state.isNightModeAuto) "AUTO" else "OFF",
                    color = if (state.isNightModeAuto) Color(0xFFFFE082) else Color.White.copy(alpha = 0.6f),
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        // Grid Switcher
        IconButton(
            onClick = onCycleGrid,
            modifier = Modifier
                .size(40.dp)
                .clip(CircleShape)
                .background(Color(0x55000000))
                .testTag("grid_toggle_button")
        ) {
            Icon(
                imageVector = Icons.Default.GridOn,
                contentDescription = "Grid",
                tint = if (state.gridType != GridType.NONE) Color(0xFF00E5FF) else Color.White.copy(alpha = 0.6f),
                modifier = Modifier.size(18.dp)
            )
        }

        // Cloud Sync Indicator / Button
        BadgedBox(
            badge = {
                if (pendingSyncCount > 0 && !cloudSyncStatus.isSyncing) {
                    Badge(
                        containerColor = Color(0xFFFF9100),
                        contentColor = Color.Black
                    ) {
                        Text(text = "$pendingSyncCount", fontSize = 9.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        ) {
            IconButton(
                onClick = onOpenCloudSync,
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(Color(0x55000000))
                    .testTag("cloud_sync_button")
            ) {
                val cloudIcon = when {
                    cloudSyncStatus.isSyncing -> Icons.Default.Sync
                    pendingSyncCount == 0 -> Icons.Default.CloudDone
                    else -> Icons.Default.CloudUpload
                }
                val cloudColor = when {
                    cloudSyncStatus.isSyncing -> Color(0xFF00E5FF)
                    pendingSyncCount == 0 -> Color(0xFF00E676)
                    else -> Color(0xFFFFB300)
                }
                Icon(
                    imageVector = cloudIcon,
                    contentDescription = "Sinkronisasi Awan",
                    tint = cloudColor,
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}
