package com.example.ui.screens

import android.content.Intent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
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
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.CloudDone
import androidx.compose.material.icons.filled.CloudUpload
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.FileDownload
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.PlayCircle
import androidx.compose.material.icons.filled.Replay
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.FileProvider
import coil.compose.AsyncImage
import com.example.data.MediaEntity
import kotlinx.coroutines.delay
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

enum class GalleryFilterTab(val label: String) {
    ALL("Semua"),
    PHOTOS("Foto"),
    VIDEOS("Video"),
    CLOUD_SYNCED("Awan")
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GalleryScreen(
    mediaList: List<MediaEntity>,
    selectedMedia: MediaEntity?,
    onSelectMedia: (MediaEntity?) -> Unit,
    onEditPhoto: (MediaEntity) -> Unit,
    onOpenExportDialog: (MediaEntity) -> Unit,
    onDeleteMedia: (MediaEntity) -> Unit,
    onBack: () -> Unit,
    onOpenCloudSync: () -> Unit,
    modifier: Modifier = Modifier
) {
    var activeTab by remember { mutableStateOf(GalleryFilterTab.ALL) }
    var showInfoSheet by remember { mutableStateOf(false) }
    var showDeleteConfirmDialog by remember { mutableStateOf(false) }
    val context = LocalContext.current

    val filteredList = remember(mediaList, activeTab) {
        when (activeTab) {
            GalleryFilterTab.ALL -> mediaList
            GalleryFilterTab.PHOTOS -> mediaList.filter { it.mediaType == "PHOTO" }
            GalleryFilterTab.VIDEOS -> mediaList.filter { it.mediaType == "VIDEO" }
            GalleryFilterTab.CLOUD_SYNCED -> mediaList.filter { it.cloudSyncStatus == "SYNCED" }
        }
    }

    // Direct Fullscreen Media Viewer
    if (selectedMedia != null) {
        val currentIndex = mediaList.indexOfFirst { it.id == selectedMedia.id }
        val prevMedia = if (currentIndex > 0) mediaList[currentIndex - 1] else null
        val nextMedia = if (currentIndex >= 0 && currentIndex < mediaList.size - 1) mediaList[currentIndex + 1] else null

        FullscreenMediaViewer(
            media = selectedMedia,
            hasPrevious = prevMedia != null,
            hasNext = nextMedia != null,
            onPrevious = { if (prevMedia != null) onSelectMedia(prevMedia) },
            onNext = { if (nextMedia != null) onSelectMedia(nextMedia) },
            onClose = { onSelectMedia(null) },
            onEdit = {
                onSelectMedia(null)
                onEditPhoto(selectedMedia)
            },
            onExport = { onOpenExportDialog(selectedMedia) },
            onShare = {
                val file = File(selectedMedia.filePath)
                if (file.exists()) {
                    val uri = FileProvider.getUriForFile(
                        context,
                        "${context.packageName}.provider",
                        file
                    )
                    val sendIntent = Intent(Intent.ACTION_SEND).apply {
                        type = if (selectedMedia.mediaType == "PHOTO") "image/jpeg" else "video/mp4"
                        putExtra(Intent.EXTRA_STREAM, uri)
                        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                    }
                    context.startActivity(Intent.createChooser(sendIntent, "Bagikan Media"))
                }
            },
            onShowInfo = { showInfoSheet = true },
            onRequestDelete = { showDeleteConfirmDialog = true },
            modifier = modifier
        )

        // Metadata Info Sheet
        if (showInfoSheet) {
            ModalBottomSheet(
                onDismissRequest = { showInfoSheet = false },
                containerColor = Color(0xFF141923),
                contentColor = Color.White
            ) {
                MediaDetailSheetContent(
                    media = selectedMedia,
                    onDismiss = { showInfoSheet = false }
                )
            }
        }

        // Delete Confirmation Dialog
        if (showDeleteConfirmDialog) {
            AlertDialog(
                onDismissRequest = { showDeleteConfirmDialog = false },
                confirmButton = {
                    TextButton(
                        onClick = {
                            showDeleteConfirmDialog = false
                            onDeleteMedia(selectedMedia)
                            onSelectMedia(null)
                        }
                    ) {
                        Text("Hapus", color = Color(0xFFE53935), fontWeight = FontWeight.Bold)
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showDeleteConfirmDialog = false }) {
                        Text("Batal", color = Color.White)
                    }
                },
                title = { Text("Hapus Media?") },
                text = { Text("File \"${selectedMedia.fileName}\" akan dihapus secara permanen dari perangkat.") },
                containerColor = Color(0xFF1B2230),
                titleContentColor = Color.White,
                textContentColor = Color(0xFFB0BEC5)
            )
        }

        return
    }

    // Standard Grid View
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFF090C11))
            .statusBarsPadding()
            .navigationBarsPadding()
    ) {
        // Top Toolbar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(
                    onClick = onBack,
                    modifier = Modifier.testTag("gallery_back_button")
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Kembali ke Kamera",
                        tint = Color.White
                    )
                }
                Spacer(modifier = Modifier.width(8.dp))
                Column {
                    Text(
                        text = "Galeri Foto & Video",
                        color = Color.White,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "${mediaList.size} file tersimpan • Ketuk untuk lihat langsung",
                        color = Color(0xFF90A4AE),
                        fontSize = 11.sp
                    )
                }
            }

            IconButton(
                onClick = onOpenCloudSync,
                modifier = Modifier
                    .clip(CircleShape)
                    .background(Color(0x3300E5FF))
                    .testTag("gallery_cloud_sync_button")
            ) {
                Icon(
                    imageVector = Icons.Default.Sync,
                    contentDescription = "Sinkronisasi Awan",
                    tint = Color(0xFF00E5FF)
                )
            }
        }

        // Category Filter Tabs
        TabRow(
            selectedTabIndex = activeTab.ordinal,
            containerColor = Color.Transparent,
            contentColor = Color(0xFF00E5FF),
            indicator = { tabPositions ->
                TabRowDefaults.SecondaryIndicator(
                    modifier = Modifier.tabIndicatorOffset(tabPositions[activeTab.ordinal]),
                    color = Color(0xFF00E5FF)
                )
            },
            modifier = Modifier.padding(horizontal = 8.dp)
        ) {
            GalleryFilterTab.entries.forEach { tab ->
                val selected = activeTab == tab
                Tab(
                    selected = selected,
                    onClick = { activeTab = tab },
                    text = {
                        Text(
                            text = tab.label,
                            color = if (selected) Color(0xFF00E5FF) else Color(0xFF90A4AE),
                            fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal,
                            fontSize = 13.sp
                        )
                    }
                )
            }
        }

        // Media Grid
        if (filteredList.isEmpty()) {
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "Belum ada media di kategori ini",
                        color = Color(0xFF78909C),
                        fontSize = 14.sp
                    )
                }
            }
        } else {
            LazyVerticalGrid(
                columns = GridCells.Fixed(3),
                contentPadding = PaddingValues(6.dp),
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp),
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
            ) {
                items(filteredList, key = { it.id }) { item ->
                    MediaGridCard(
                        media = item,
                        onClick = { onSelectMedia(item) }
                    )
                }
            }
        }
    }
}

@Composable
fun FullscreenMediaViewer(
    media: MediaEntity,
    hasPrevious: Boolean,
    hasNext: Boolean,
    onPrevious: () -> Unit,
    onNext: () -> Unit,
    onClose: () -> Unit,
    onEdit: () -> Unit,
    onExport: () -> Unit,
    onShare: () -> Unit,
    onShowInfo: () -> Unit,
    onRequestDelete: () -> Unit,
    modifier: Modifier = Modifier
) {
    var scale by remember(media.id) { mutableFloatStateOf(1f) }
    var offset by remember(media.id) { mutableStateOf(Offset.Zero) }
    var showControls by remember { mutableStateOf(true) }

    // Video Playback Simulation State
    val isVideo = media.mediaType == "VIDEO"
    val totalSeconds = maxOf(1, media.durationSeconds)
    var isPlaying by remember(media.id) { mutableStateOf(true) }
    var currentSeconds by remember(media.id) { mutableIntStateOf(0) }

    LaunchedEffect(isPlaying, media.id) {
        if (isVideo && isPlaying) {
            while (isPlaying && currentSeconds < totalSeconds) {
                delay(1000)
                currentSeconds++
                if (currentSeconds >= totalSeconds) {
                    isPlaying = false
                }
            }
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color.Black)
            .statusBarsPadding()
            .navigationBarsPadding()
            .pointerInput(media.id) {
                detectTapGestures(
                    onDoubleTap = {
                        scale = if (scale > 1.2f) 1f else 2.5f
                        offset = Offset.Zero
                    },
                    onTap = {
                        showControls = !showControls
                    }
                )
            }
    ) {
        // Main Visual Surface (Photo or Video)
        Box(
            modifier = Modifier
                .fillMaxSize()
                .pointerInput(media.id) {
                    detectTransformGestures { _, pan, zoom, _ ->
                        scale = (scale * zoom).coerceIn(1f, 5f)
                        if (scale > 1f) {
                            val maxOffset = 400f * (scale - 1f)
                            offset = Offset(
                                x = (offset.x + pan.x).coerceIn(-maxOffset, maxOffset),
                                y = (offset.y + pan.y).coerceIn(-maxOffset, maxOffset)
                            )
                        } else {
                            offset = Offset.Zero
                        }
                    }
                },
            contentAlignment = Alignment.Center
        ) {
            AsyncImage(
                model = File(media.filePath),
                contentDescription = media.fileName,
                contentScale = ContentScale.Fit,
                modifier = Modifier
                    .fillMaxSize()
                    .graphicsLayer(
                        scaleX = scale,
                        scaleY = scale,
                        translationX = offset.x,
                        translationY = offset.y
                    )
            )

            // Video Play / Pause Overlay Center
            if (isVideo) {
                AnimatedVisibility(
                    visible = showControls || !isPlaying,
                    enter = fadeIn(),
                    exit = fadeOut(),
                    modifier = Modifier.align(Alignment.Center)
                ) {
                    Box(
                        modifier = Modifier
                            .size(72.dp)
                            .clip(CircleShape)
                            .background(Color(0x99000000))
                            .border(1.5.dp, Color(0xFF00E5FF), CircleShape)
                            .clickable {
                                if (currentSeconds >= totalSeconds) {
                                    currentSeconds = 0
                                    isPlaying = true
                                } else {
                                    isPlaying = !isPlaying
                                }
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = when {
                                currentSeconds >= totalSeconds -> Icons.Default.Replay
                                isPlaying -> Icons.Default.Pause
                                else -> Icons.Default.PlayArrow
                            },
                            contentDescription = "Putar / Jeda Video",
                            tint = Color(0xFF00E5FF),
                            modifier = Modifier.size(38.dp)
                        )
                    }
                }
            }
        }

        // Top Overlay Header Bar
        AnimatedVisibility(
            visible = showControls,
            enter = fadeIn(),
            exit = fadeOut(),
            modifier = Modifier.align(Alignment.TopCenter)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xCC000000))
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(onClick = onClose) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Tutup",
                            tint = Color.White
                        )
                    }
                    Spacer(modifier = Modifier.width(6.dp))
                    Column {
                        Text(
                            text = media.fileName,
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                            maxLines = 1
                        )
                        val dateStr = SimpleDateFormat("dd MMM yyyy, HH:mm", Locale.getDefault()).format(Date(media.timestamp))
                        Text(
                            text = "$dateStr • ${media.width}x${media.height}",
                            color = Color(0xFFB0BEC5),
                            fontSize = 11.sp
                        )
                    }
                }

                // Filter & Super Steady Tag
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    if (media.isStabilized) {
                        Box(
                            modifier = Modifier
                                .background(Color(0x3300E676), RoundedCornerShape(12.dp))
                                .border(1.dp, Color(0xFF00E676), RoundedCornerShape(12.dp))
                                .padding(horizontal = 8.dp, vertical = 3.dp)
                        ) {
                            Text(
                                text = "SUPER STEADY",
                                color = Color(0xFF00E676),
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                    if (media.filterUsed != "NORMAL") {
                        Box(
                            modifier = Modifier
                                .background(Color(0x3300E5FF), RoundedCornerShape(12.dp))
                                .border(1.dp, Color(0xFF00E5FF), RoundedCornerShape(12.dp))
                                .padding(horizontal = 8.dp, vertical = 3.dp)
                        ) {
                            Text(
                                text = media.filterUsed,
                                color = Color(0xFF00E5FF),
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }

        // Left Navigation Arrow (Previous Media)
        if (hasPrevious) {
            AnimatedVisibility(
                visible = showControls,
                enter = fadeIn(),
                exit = fadeOut(),
                modifier = Modifier
                    .align(Alignment.CenterStart)
                    .padding(start = 12.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .clip(CircleShape)
                        .background(Color(0x77000000))
                        .clickable { onPrevious() },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.ChevronLeft,
                        contentDescription = "Sebelumnya",
                        tint = Color.White,
                        modifier = Modifier.size(32.dp)
                    )
                }
            }
        }

        // Right Navigation Arrow (Next Media)
        if (hasNext) {
            AnimatedVisibility(
                visible = showControls,
                enter = fadeIn(),
                exit = fadeOut(),
                modifier = Modifier
                    .align(Alignment.CenterEnd)
                    .padding(end = 12.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .clip(CircleShape)
                        .background(Color(0x77000000))
                        .clickable { onNext() },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.ChevronRight,
                        contentDescription = "Berikutnya",
                        tint = Color.White,
                        modifier = Modifier.size(32.dp)
                    )
                }
            }
        }

        // Bottom Deck: Video Timeline & Action Buttons
        AnimatedVisibility(
            visible = showControls,
            enter = fadeIn(),
            exit = fadeOut(),
            modifier = Modifier.align(Alignment.BottomCenter)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xE60D1117))
                    .padding(vertical = 10.dp)
            ) {
                // Video Progress Slider (if video)
                if (isVideo) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 20.dp, vertical = 2.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = String.format(Locale.US, "%02d:%02d", currentSeconds / 60, currentSeconds % 60),
                                color = Color.White,
                                fontSize = 11.sp
                            )
                            Text(
                                text = "Anti-Goyang Stabil (60 FPS)",
                                color = Color(0xFF00E676),
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                            Text(
                                text = String.format(Locale.US, "%02d:%02d", totalSeconds / 60, totalSeconds % 60),
                                color = Color(0xFFB0BEC5),
                                fontSize = 11.sp
                            )
                        }
                        Slider(
                            value = currentSeconds.toFloat(),
                            onValueChange = { currentSeconds = it.toInt() },
                            valueRange = 0f..totalSeconds.toFloat(),
                            colors = SliderDefaults.colors(
                                thumbColor = Color(0xFF00E5FF),
                                activeTrackColor = Color(0xFF00E5FF),
                                inactiveTrackColor = Color(0x44FFFFFF)
                            ),
                            modifier = Modifier.height(28.dp)
                        )
                    }
                }

                // Action Bar
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 4.dp),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (media.mediaType == "PHOTO") {
                        ViewerActionButton(
                            icon = Icons.Default.Edit,
                            label = "Edit",
                            color = Color(0xFF00E5FF),
                            onClick = onEdit
                        )
                    }

                    ViewerActionButton(
                        icon = Icons.Default.Share,
                        label = "Bagikan",
                        color = Color.White,
                        onClick = onShare
                    )

                    ViewerActionButton(
                        icon = Icons.Default.FileDownload,
                        label = "Ekspor",
                        color = Color(0xFFFFB300),
                        onClick = onExport
                    )

                    ViewerActionButton(
                        icon = Icons.Default.Info,
                        label = "Info",
                        color = Color(0xFF80CBC4),
                        onClick = onShowInfo
                    )

                    ViewerActionButton(
                        icon = Icons.Default.Delete,
                        label = "Hapus",
                        color = Color(0xFFEF5350),
                        onClick = onRequestDelete
                    )
                }
            }
        }
    }
}

@Composable
fun ViewerActionButton(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    color: Color,
    onClick: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .clickable { onClick() }
            .padding(horizontal = 10.dp, vertical = 4.dp)
    ) {
        Box(
            modifier = Modifier
                .size(42.dp)
                .clip(CircleShape)
                .background(color.copy(alpha = 0.15f))
                .border(1.dp, color.copy(alpha = 0.35f), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = color,
                modifier = Modifier.size(20.dp)
            )
        }
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = label,
            color = Color.White,
            fontSize = 10.sp,
            fontWeight = FontWeight.Medium
        )
    }
}

@Composable
fun MediaGridCard(
    media: MediaEntity,
    onClick: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(10.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF161B26)),
        modifier = Modifier
            .aspectRatio(1f)
            .clickable { onClick() }
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            AsyncImage(
                model = File(media.filePath),
                contentDescription = media.fileName,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )

            // Video indicator overlay
            if (media.mediaType == "VIDEO") {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color(0x33000000)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.PlayCircle,
                        contentDescription = "Video",
                        tint = Color.White,
                        modifier = Modifier.size(32.dp)
                    )
                }
                Text(
                    text = "${media.durationSeconds}s",
                    color = Color.White,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier
                        .align(Alignment.BottomStart)
                        .padding(4.dp)
                        .background(Color(0xCC000000), RoundedCornerShape(4.dp))
                        .padding(horizontal = 4.dp, vertical = 2.dp)
                )
            }

            // Cloud status icon
            val isSynced = media.cloudSyncStatus == "SYNCED"
            Icon(
                imageVector = if (isSynced) Icons.Default.CloudDone else Icons.Default.CloudUpload,
                contentDescription = null,
                tint = if (isSynced) Color(0xFF00E676) else Color(0xFFFFB300),
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(4.dp)
                    .size(14.dp)
            )
        }
    }
}

@Composable
fun MediaDetailSheetContent(
    media: MediaEntity,
    onDismiss: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 12.dp)
    ) {
        Text(
            text = media.fileName,
            color = Color.White,
            fontWeight = FontWeight.Bold,
            fontSize = 16.sp
        )

        val dateStr = SimpleDateFormat("dd MMMM yyyy, HH:mm:ss", Locale.getDefault()).format(Date(media.timestamp))
        Text(text = dateStr, color = Color(0xFF90A4AE), fontSize = 12.sp)

        Spacer(modifier = Modifier.height(16.dp))

        // Metadata chips
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            MetaChip(label = "Resolusi", value = "${media.width} x ${media.height}")
            MetaChip(label = "Filter", value = media.filterUsed)
            MetaChip(label = "ISO", value = "${media.iso}")
            MetaChip(label = "Kecepatan", value = media.shutterSpeed)
        }

        Spacer(modifier = Modifier.height(8.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            MetaChip(
                label = "Stabilizer",
                value = if (media.isStabilized) "Super Steady (Anti-Goyang)" else "Normal",
                accentColor = if (media.isStabilized) Color(0xFF00E676) else Color.White
            )
            MetaChip(
                label = "Ukuran File",
                value = String.format(Locale.US, "%.1f MB", media.fileSize / (1024f * 1024f))
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        TextButton(
            onClick = onDismiss,
            modifier = Modifier.align(Alignment.End)
        ) {
            Text("Tutup", color = Color(0xFF00E5FF))
        }

        Spacer(modifier = Modifier.height(16.dp))
    }
}

@Composable
fun MetaChip(label: String, value: String, accentColor: Color = Color.White) {
    Box(
        modifier = Modifier
            .background(Color(0x22FFFFFF), RoundedCornerShape(8.dp))
            .padding(horizontal = 10.dp, vertical = 6.dp)
    ) {
        Column {
            Text(text = label, color = Color(0xFF78909C), fontSize = 10.sp)
            Text(text = value, color = accentColor, fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
        }
    }
}
