package com.example.camera

import androidx.compose.ui.graphics.ColorMatrix

enum class CameraFilter(
    val id: String,
    val displayName: String,
    val description: String,
    val previewColor: Long
) {
    NORMAL(
        id = "NORMAL",
        displayName = "Alami",
        description = "Warna alami sensor tanpa penyesuaian",
        previewColor = 0xFF4A90E2
    ),
    CINEMATIC(
        id = "CINEMATIC",
        displayName = "Teal & Orange",
        description = "Gaya sinematik film Hollywood modern",
        previewColor = 0xFF00897B
    ),
    PORTRA(
        id = "PORTRA",
        displayName = "Kodak Portra",
        description = "Warna kulit hangat dan highlight lembut ala 35mm",
        previewColor = 0xFFFFB74D
    ),
    FUJI_CHROME(
        id = "FUJI_CHROME",
        displayName = "Fuji Classic",
        description = "Kontras dokumenter dengan hijau dalam dan bayangan kaya",
        previewColor = 0xFF2E7D32
    ),
    GOLDEN_HOUR(
        id = "GOLDEN_HOUR",
        displayName = "Golden Hour",
        description = "Kilau cahaya senja matahari terbenam keemasan",
        previewColor = 0xFFFF9800
    ),
    CYBERPUNK(
        id = "CYBERPUNK",
        displayName = "Cyberpunk",
        description = "Estetika neon futuristik ungu dan toska cerah",
        previewColor = 0xFFE040FB
    ),
    PASTEL_DREAM(
        id = "PASTEL_DREAM",
        displayName = "Pastel Dream",
        description = "Nuansa lembut airy Korea dengan rona merah muda halus",
        previewColor = 0xFFF48FB1
    ),
    MOODY_EMERALD(
        id = "MOODY_EMERALD",
        displayName = "Moody Forest",
        description = "Hijau zamrud misterius dengan bayangan matte pudar",
        previewColor = 0xFF004D40
    ),
    VIVID(
        id = "VIVID",
        displayName = "Vivid HDR",
        description = "Saturasi dinamis dan rentang warna kaya",
        previewColor = 0xFFFF5722
    ),
    NOIR(
        id = "NOIR",
        displayName = "Noir B&W",
        description = "Monokrom klasik kontras tinggi jalanan",
        previewColor = 0xFF212121
    ),
    SILVER_MONO(
        id = "SILVER_MONO",
        displayName = "Silver Luxe",
        description = "Monokrom perak halus premium dengan gradasi abu-abu halus",
        previewColor = 0xFF90A4AE
    ),
    WARM(
        id = "WARM",
        displayName = "Hangat",
        description = "Nuansa amber ramah untuk potret santai",
        previewColor = 0xFFFFB300
    ),
    COOL(
        id = "COOL",
        displayName = "Nordic Cool",
        description = "Nuansa dingin nordic dengan aksen cyan jernih",
        previewColor = 0xFF00E5FF
    ),
    VINTAGE(
        id = "VINTAGE",
        displayName = "Retro 90s",
        description = "Gaya retro analog VHS hangat dan nostalgia",
        previewColor = 0xFF8D6E63
    ),
    SEPIA(
        id = "SEPIA",
        displayName = "Sepia Artisan",
        description = "Nuansa klasik kuno tembaga hangat abadi",
        previewColor = 0xFF795548
    ),
    INFRARED(
        id = "INFRARED",
        displayName = "Aerochrome",
        description = "Inframerah surealis dengan dedaunan magenta dan langit es",
        previewColor = 0xFFD81B60
    );

    fun toComposeColorMatrix(): ColorMatrix {
        return ColorMatrix(getColorMatrixArray())
    }

    fun toAndroidColorMatrix(): android.graphics.ColorMatrix {
        return android.graphics.ColorMatrix(getColorMatrixArray())
    }

    fun getColorMatrixArray(): FloatArray {
        return when (this) {
            NORMAL -> floatArrayOf(
                1f, 0f, 0f, 0f, 0f,
                0f, 1f, 0f, 0f, 0f,
                0f, 0f, 1f, 0f, 0f,
                0f, 0f, 0f, 1f, 0f
            )
            CINEMATIC -> floatArrayOf(
                1.22f, 0f, 0.05f, 0f, 15f,
                0f, 1.05f, 0.15f, 0f, 5f,
                -0.12f, 0.18f, 1.35f, 0f, 18f,
                0f, 0f, 0f, 1f, 0f
            )
            PORTRA -> floatArrayOf(
                1.18f, 0.05f, 0f, 0f, 14f,
                0.02f, 1.08f, 0f, 0f, 10f,
                -0.05f, 0.02f, 0.92f, 0f, -5f,
                0f, 0f, 0f, 1f, 0f
            )
            FUJI_CHROME -> floatArrayOf(
                1.08f, 0f, -0.05f, 0f, -2f,
                0f, 1.15f, 0.05f, 0f, 4f,
                -0.08f, -0.05f, 1.12f, 0f, 8f,
                0f, 0f, 0f, 1f, 0f
            )
            GOLDEN_HOUR -> floatArrayOf(
                1.30f, 0.10f, 0f, 0f, 25f,
                0.05f, 1.12f, 0f, 0f, 14f,
                -0.15f, -0.10f, 0.80f, 0f, -20f,
                0f, 0f, 0f, 1f, 0f
            )
            CYBERPUNK -> floatArrayOf(
                1.35f, 0f, 0.25f, 0f, 22f,
                -0.10f, 0.95f, 0.15f, 0f, -8f,
                0.20f, 0f, 1.45f, 0f, 25f,
                0f, 0f, 0f, 1f, 0f
            )
            PASTEL_DREAM -> floatArrayOf(
                1.15f, 0.08f, 0.08f, 0f, 20f,
                0.05f, 1.10f, 0.05f, 0f, 18f,
                0.08f, 0.05f, 1.18f, 0f, 22f,
                0f, 0f, 0f, 1f, 0f
            )
            MOODY_EMERALD -> floatArrayOf(
                0.90f, 0.02f, 0f, 0f, -10f,
                0.05f, 1.25f, 0.08f, 0f, 12f,
                -0.05f, 0.08f, 1.15f, 0f, 5f,
                0f, 0f, 0f, 1f, 0f
            )
            VIVID -> floatArrayOf(
                1.28f, 0f, 0f, 0f, 6f,
                0f, 1.28f, 0f, 0f, 6f,
                0f, 0f, 1.28f, 0f, 6f,
                0f, 0f, 0f, 1f, 0f
            )
            NOIR -> floatArrayOf(
                0.35f, 0.55f, 0.10f, 0f, -15f,
                0.35f, 0.55f, 0.10f, 0f, -15f,
                0.35f, 0.55f, 0.10f, 0f, -15f,
                0f, 0f, 0f, 1f, 0f
            )
            SILVER_MONO -> floatArrayOf(
                0.28f, 0.60f, 0.12f, 0f, 12f,
                0.28f, 0.60f, 0.12f, 0f, 12f,
                0.28f, 0.60f, 0.12f, 0f, 12f,
                0f, 0f, 0f, 1f, 0f
            )
            WARM -> floatArrayOf(
                1.20f, 0f, 0f, 0f, 18f,
                0f, 1.05f, 0f, 0f, 8f,
                0f, 0f, 0.85f, 0f, -15f,
                0f, 0f, 0f, 1f, 0f
            )
            COOL -> floatArrayOf(
                0.85f, 0f, 0f, 0f, -10f,
                0f, 1.05f, 0f, 0f, 10f,
                0f, 0f, 1.30f, 0f, 25f,
                0f, 0f, 0f, 1f, 0f
            )
            VINTAGE -> floatArrayOf(
                0.95f, 0.12f, 0.08f, 0f, 22f,
                0.08f, 0.88f, 0.08f, 0f, 18f,
                0.08f, 0.08f, 0.72f, 0f, 8f,
                0f, 0f, 0f, 1f, 0f
            )
            SEPIA -> floatArrayOf(
                0.393f, 0.769f, 0.189f, 0f, 10f,
                0.349f, 0.686f, 0.168f, 0f, 6f,
                0.272f, 0.534f, 0.131f, 0f, -10f,
                0f, 0f, 0f, 1f, 0f
            )
            INFRARED -> floatArrayOf(
                1.45f, -0.20f, 0.10f, 0f, 30f,
                -0.10f, 0.70f, 0.10f, 0f, -10f,
                0.15f, 0.10f, 1.35f, 0f, 15f,
                0f, 0f, 0f, 1f, 0f
            )
        }
    }
}
