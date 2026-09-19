package com.example.camera

enum class CameraMode(val displayName: String) {
    PHOTO("FOTO"),
    VIDEO("VIDEO"),
    PRO("PRO"),
    NIGHT("MALAM"),
    GALLERY("GALERI")
}

enum class AspectRatioMode(val label: String, val ratioValue: Float) {
    RATIO_4_3("4:3", 4f / 3f),
    RATIO_16_9("16:9", 16f / 9f),
    RATIO_1_1("1:1", 1f),
    RATIO_FULL("FULL", 20f / 9f)
}

enum class GridType(val label: String) {
    NONE("Mati"),
    RULE_OF_THIRDS("3x3"),
    GOLDEN_RATIO("Golden"),
    CROSSHAIR("Pusat")
}

enum class FlashMode(val label: String) {
    OFF("Mati"),
    AUTO("Auto"),
    ON("Nyala"),
    TORCH("Senter")
}

enum class WhiteBalanceSetting(val label: String, val tempK: String) {
    AUTO("Auto", "Auto"),
    DAYLIGHT("Siang", "5500K"),
    CLOUDY("Mendung", "6500K"),
    TUNGSTEN("Pijar", "3200K"),
    FLUORESCENT("Neon", "4000K")
}

enum class VideoQualityOption(val label: String, val resolutionText: String) {
    UHD_4K("4K", "3840x2160 • 60fps"),
    FHD_1080P("FHD", "1920x1080 • 60fps"),
    HD_720P("HD", "1280x720 • 30fps")
}

data class CameraState(
    val currentMode: CameraMode = CameraMode.PHOTO,
    val selectedFilter: CameraFilter = CameraFilter.NORMAL,
    val isStabilizerEnabled: Boolean = true,
    val isNightModeAuto: Boolean = true,
    val isLowLightDetected: Boolean = false,
    val ambientLux: Float = 120f,
    val isCapturing: Boolean = false,
    val captureProgress: Float = 0f,
    val isRecordingVideo: Boolean = false,
    val videoElapsedSeconds: Int = 0,
    val videoQuality: VideoQualityOption = VideoQualityOption.FHD_1080P,
    val aspectRatio: AspectRatioMode = AspectRatioMode.RATIO_4_3,
    val gridType: GridType = GridType.RULE_OF_THIRDS,
    val flashMode: FlashMode = FlashMode.AUTO,
    val timerSeconds: Int = 0,
    val isBackCamera: Boolean = true,
    val zoomRatio: Float = 1.0f,
    
    // Pro manual controls
    val exposureEv: Float = 0.0f, // -3.0f to +3.0f
    val isoValue: String = "AUTO", // AUTO, 100, 200, 400, 800, 1600, 3200
    val shutterSpeed: String = "AUTO", // AUTO, 1/1000s, 1/500s, 1/250s, 1/125s, 1/60s, 1/30s, 1/4s, 1s
    val whiteBalance: WhiteBalanceSetting = WhiteBalanceSetting.AUTO,
    val focusDistance: Float = 0.0f, // 0.0f (Auto/Infinity) to 1.0f (Macro)
    val isProControlExpanded: Boolean = false,
    val isFilterSelectorExpanded: Boolean = false,

    // Horizon / Level stabilization angles
    val pitchAngle: Float = 0.0f,
    val rollAngle: Float = 0.0f,
    val isLevelStable: Boolean = true
)
