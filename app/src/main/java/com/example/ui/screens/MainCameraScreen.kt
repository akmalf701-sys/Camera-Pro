package com.example.ui.screens

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat
import com.example.camera.AspectRatioMode
import com.example.camera.CameraMode
import com.example.ui.MainCameraViewModel
import com.example.ui.components.CameraBottomDeck
import com.example.ui.components.CameraTopBar
import com.example.ui.components.CameraViewfinder
import com.example.ui.components.CloudSyncSheet
import com.example.ui.components.ExportDialog
import com.example.ui.components.FilterSelectorCarousel
import com.example.ui.components.ProControlsPanel

@Composable
fun MainCameraScreen(
    viewModel: MainCameraViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val cameraState by viewModel.cameraState.collectAsState()
    val allMedia by viewModel.allMedia.collectAsState()
    val cloudSyncState by viewModel.cloudSyncState.collectAsState()
    val editingMedia by viewModel.editingMedia.collectAsState()
    val editingBitmap by viewModel.editingBitmap.collectAsState()
    val editAdjustments by viewModel.editAdjustments.collectAsState()
    val detailMedia by viewModel.detailMedia.collectAsState()
    val showCloudSheet by viewModel.showCloudSheet.collectAsState()
    val exportingMedia by viewModel.exportingMedia.collectAsState()
    val isExporting by viewModel.isExporting.collectAsState()

    var hasCameraPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED
        )
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { perms ->
        hasCameraPermission = perms[Manifest.permission.CAMERA] == true
    }

    LaunchedEffect(Unit) {
        if (!hasCameraPermission) {
            permissionLauncher.launch(
                arrayOf(
                    Manifest.permission.CAMERA,
                    Manifest.permission.RECORD_AUDIO
                )
            )
        }
    }

    val pendingCount = allMedia.count { it.cloudSyncStatus != "SYNCED" }
    val latestMedia = allMedia.firstOrNull()

    // Route 1: Photo Editor Screen
    if (editingMedia != null) {
        PhotoEditorScreen(
            media = editingMedia!!,
            bitmap = editingBitmap,
            adjustments = editAdjustments,
            onUpdateAdjustments = { transform -> viewModel.updateEditAdjustments(transform) },
            onSave = { saveAsNew -> viewModel.saveEditedPhoto(saveAsNew) },
            onCancel = { viewModel.closePhotoEditor() },
            modifier = modifier
        )
        return
    }

    // Route 2: Gallery Screen
    if (cameraState.currentMode == CameraMode.GALLERY) {
        GalleryScreen(
            mediaList = allMedia,
            selectedMedia = detailMedia,
            onSelectMedia = { viewModel.showMediaDetail(it) },
            onEditPhoto = { viewModel.openPhotoEditor(it) },
            onOpenExportDialog = { viewModel.openExportDialog(it) },
            onDeleteMedia = { viewModel.deleteMedia(it) },
            onBack = { viewModel.setCameraMode(CameraMode.PHOTO) },
            onOpenCloudSync = { viewModel.openCloudSyncSheet() },
            modifier = modifier
        )

        // Cloud Sync Sheet in Gallery
        if (showCloudSheet) {
            CloudSyncSheet(
                status = cloudSyncState,
                pendingCount = pendingCount,
                totalCount = allMedia.size,
                onTriggerSync = { viewModel.triggerCloudSync() },
                onToggleAutoSyncWifiOnly = { viewModel.setAutoSyncWifiOnly(it) },
                onDismiss = { viewModel.closeCloudSyncSheet() }
            )
        }

        // Export Dialog in Gallery
        if (exportingMedia != null) {
            ExportDialog(
                media = exportingMedia!!,
                isExporting = isExporting,
                onExportProfileSelected = { profile ->
                    viewModel.exportMedia(profile, context)
                },
                onDismiss = { viewModel.closeExportDialog() }
            )
        }
        return
    }

    // Route 3: Main Camera Viewfinder View
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color.Black)
    ) {
        // Viewfinder
        CameraViewfinder(
            state = cameraState,
            hasCameraPermission = hasCameraPermission,
            onBindImageCapture = { captureUseCase ->
                viewModel.setImageCaptureUseCase(captureUseCase)
            },
            onZoomChange = { newZoom ->
                viewModel.setZoom(newZoom)
            },
            modifier = Modifier.fillMaxSize()
        )

        // Top Controls Overlay
        CameraTopBar(
            state = cameraState,
            cloudSyncStatus = cloudSyncState,
            pendingSyncCount = pendingCount,
            onCycleFlash = { viewModel.cycleFlashMode() },
            onCycleAspectRatio = {
                val nextRatio = when (cameraState.aspectRatio) {
                    AspectRatioMode.RATIO_4_3 -> AspectRatioMode.RATIO_16_9
                    AspectRatioMode.RATIO_16_9 -> AspectRatioMode.RATIO_1_1
                    AspectRatioMode.RATIO_1_1 -> AspectRatioMode.RATIO_FULL
                    AspectRatioMode.RATIO_FULL -> AspectRatioMode.RATIO_4_3
                }
                viewModel.setAspectRatio(nextRatio)
            },
            onToggleStabilizer = { viewModel.toggleStabilizer() },
            onToggleNightModeAuto = { viewModel.toggleNightModeAuto() },
            onCycleGrid = { viewModel.cycleGridType() },
            onOpenCloudSync = { viewModel.openCloudSyncSheet() },
            modifier = Modifier.align(Alignment.TopCenter)
        )

        // Bottom Deck & Controls Overlay
        Column(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
        ) {
            // Live Filter Carousel (toggleable)
            AnimatedVisibility(
                visible = cameraState.isFilterSelectorExpanded,
                enter = slideInVertically { it },
                exit = slideOutVertically { it }
            ) {
                FilterSelectorCarousel(
                    selectedFilter = cameraState.selectedFilter,
                    onSelectFilter = { viewModel.selectFilter(it) }
                )
            }

            // Pro Controls Panel (toggleable or when PRO mode selected)
            AnimatedVisibility(
                visible = cameraState.isProControlExpanded,
                enter = slideInVertically { it },
                exit = slideOutVertically { it }
            ) {
                ProControlsPanel(
                    state = cameraState,
                    onSetEv = { viewModel.setExposureEv(it) },
                    onSetIso = { viewModel.setIsoValue(it) },
                    onSetShutter = { viewModel.setShutterSpeed(it) },
                    onSetWb = { viewModel.setWhiteBalance(it) },
                    onSetFocus = { viewModel.setFocusDistance(it) }
                )
            }

            // Bottom Shutter Deck
            CameraBottomDeck(
                state = cameraState,
                latestMedia = latestMedia,
                onModeChange = { mode -> viewModel.setCameraMode(mode) },
                onShutterClick = {
                    if (cameraState.currentMode == CameraMode.VIDEO) {
                        viewModel.toggleVideoRecording()
                    } else {
                        viewModel.takePhoto(context)
                    }
                },
                onToggleFilterSelector = { viewModel.toggleFilterSelector() },
                onToggleProControls = { viewModel.toggleProControls() },
                onFlipCamera = { viewModel.toggleCameraFacing() },
                onOpenGallery = { viewModel.setCameraMode(CameraMode.GALLERY) }
            )
        }

        // Cloud Sync Sheet
        if (showCloudSheet) {
            CloudSyncSheet(
                status = cloudSyncState,
                pendingCount = pendingCount,
                totalCount = allMedia.size,
                onTriggerSync = { viewModel.triggerCloudSync() },
                onToggleAutoSyncWifiOnly = { viewModel.setAutoSyncWifiOnly(it) },
                onDismiss = { viewModel.closeCloudSyncSheet() }
            )
        }

        // Export Dialog
        if (exportingMedia != null) {
            ExportDialog(
                media = exportingMedia!!,
                isExporting = isExporting,
                onExportProfileSelected = { profile ->
                    viewModel.exportMedia(profile, context)
                },
                onDismiss = { viewModel.closeExportDialog() }
            )
        }
    }
}
