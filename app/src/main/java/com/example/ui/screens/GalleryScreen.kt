package com.example.ui.screens

import android.content.Intent
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CloudDone
import androidx.compose.material.icons.filled.CloudUpload
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.FileDownload
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.PlayCircle
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.FileProvider
import coil.compose.AsyncImage
import com.example.data.MediaEntity
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
    val context = LocalContext.current

    val filteredList = remember(mediaList, activeTab) {
        when (activeTab) {
            GalleryFilterTab.ALL -> mediaList
            GalleryFilterTab.PHOTOS -> mediaList.filter { it.mediaType == "PHOTO" }
            GalleryFilterTab.VIDEOS -> mediaList.filter { it.mediaType == "VIDEO" }
            GalleryFilterTab.CLOUD_SYNCED -> mediaList.filter { it.cloudSyncStatus == "SYNCED" }
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFF0A0C10))
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
                IconButton(onClick = onBack, modifier = Modifier.testTag("gallery_back_button")) {
                    Icon(imageVector = Icons.Default.ArrowBack, contentDescription = "Kembali", tint = Color.White)
                }
                Spacer(modifier = Modifier.width(8.dp))
                Column {
                    Text(
                        text = "Galeri Media",
                        color = Color.White,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "${mediaList.size} file tersimpan",
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
                contentPadding = PaddingValues(4.dp),
                horizontalArrangement = Arrangement.spacedBy(4.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp),
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

    // Media Detail Modal Bottom Sheet
    if (selectedMedia != null) {
        ModalBottomSheet(
            onDismissRequest = { onSelectMedia(null) },
            containerColor = Color(0xFF12151D),
            contentColor = Color.White
        ) {
            MediaDetailSheetContent(
                media = selectedMedia,
                onEdit = {
                    onSelectMedia(null)
                    onEditPhoto(selectedMedia)
                },
                onExport = {
                    onOpenExportDialog(selectedMedia)
                },
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
                onDelete = {
                    onDeleteMedia(selectedMedia)
                }
            )
        }
    }
}

@Composable
fun MediaGridCard(
    media: MediaEntity,
    onClick: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF1E2330)),
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
                        .background(Color(0xAA000000), RoundedCornerShape(4.dp))
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
    onEdit: () -> Unit,
    onExport: () -> Unit,
    onShare: () -> Unit,
    onDelete: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 12.dp)
    ) {
        // Preview Header
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(240.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(Color.Black),
            contentAlignment = Alignment.Center
        ) {
            AsyncImage(
                model = File(media.filePath),
                contentDescription = media.fileName,
                contentScale = ContentScale.Fit,
                modifier = Modifier.fillMaxSize()
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Info Grid
        Text(
            text = media.fileName,
            color = Color.White,
            fontWeight = FontWeight.Bold,
            fontSize = 16.sp
        )

        val dateStr = SimpleDateFormat("dd MMM yyyy, HH:mm", Locale.getDefault()).format(Date(media.timestamp))
        Text(text = dateStr, color = Color(0xFF90A4AE), fontSize = 12.sp)

        Spacer(modifier = Modifier.height(12.dp))

        // Metadata specs chips
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            MetaChip(label = "Resolusi", value = "${media.width} x ${media.height}")
            MetaChip(label = "Filter", value = media.filterUsed)
            MetaChip(label = "ISO", value = "${media.iso}")
            MetaChip(label = "Speed", value = media.shutterSpeed)
        }

        Spacer(modifier = Modifier.height(8.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            MetaChip(
                label = "Stabilizer",
                value = if (media.isStabilized) "OIS Aktif" else "Normal",
                accentColor = if (media.isStabilized) Color(0xFF00E676) else Color.White
            )
            MetaChip(
                label = "Awan",
                value = if (media.cloudSyncStatus == "SYNCED") "Tersinkron" else "Tertunda",
                accentColor = if (media.cloudSyncStatus == "SYNCED") Color(0xFF00E676) else Color(0xFFFFB300)
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Action Buttons Row
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            if (media.mediaType == "PHOTO") {
                ActionButton(
                    icon = Icons.Default.Edit,
                    label = "Edit Foto",
                    color = Color(0xFF00E5FF),
                    onClick = onEdit
                )
            }

            ActionButton(
                icon = Icons.Default.FileDownload,
                label = "Ekspor High-Res",
                color = Color(0xFFFFB300),
                onClick = onExport
            )

            ActionButton(
                icon = Icons.Default.Share,
                label = "Bagikan",
                color = Color.White,
                onClick = onShare
            )

            ActionButton(
                icon = Icons.Default.Delete,
                label = "Hapus",
                color = Color(0xFFE53935),
                onClick = onDelete
            )
        }

        Spacer(modifier = Modifier.height(20.dp))
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

@Composable
fun ActionButton(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    color: Color,
    onClick: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .clickable { onClick() }
            .padding(8.dp)
    ) {
        Box(
            modifier = Modifier
                .size(48.dp)
                .clip(CircleShape)
                .background(color.copy(alpha = 0.15f))
                .border(1.dp, color.copy(alpha = 0.4f), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(imageVector = icon, contentDescription = label, tint = color, modifier = Modifier.size(22.dp))
        }
        Spacer(modifier = Modifier.height(6.dp))
        Text(text = label, color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Medium)
    }
}
