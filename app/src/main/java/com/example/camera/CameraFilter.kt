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
        description = "Warna alami tanpa penyesuaian",
        previewColor = 0xFF4A90E2
    ),
    VIVID(
        id = "VIVID",
        displayName = "Vivid",
        description = "Saturasi tinggi dan kontras dinamis",
        previewColor = 0xFFFF5722
    ),
    NOIR(
        id = "NOIR",
        displayName = "Noir",
        description = "Monokrom klasik dengan kontras dramatis",
        previewColor = 0xFF424242
    ),
    WARM(
        id = "WARM",
        displayName = "Hangat",
        description = "Nuansa matahari terbenam keemasan",
        previewColor = 0xFFFFB300
    ),
    COOL(
        id = "COOL",
        displayName = "Sejuk",
        description = "Nuansa dingin nordic dengan aksen cyan",
        previewColor = 0xFF00E5FF
    ),
    CYBERPUNK(
        id = "CYBERPUNK",
        displayName = "Cyberpunk",
        description = "Estetika neon futuristik ungu dan toska",
        previewColor = 0xFFE040FB
    ),
    CINEMATIC(
        id = "CINEMATIC",
        displayName = "Sinema",
        description = "Tampilan film layar lebar teal & orange",
        previewColor = 0xFF00897B
    ),
    VINTAGE(
        id = "VINTAGE",
        displayName = "Vintage",
        description = "Gaya retro analog 70-an bernuansa hangat",
        previewColor = 0xFF8D6E63
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
            VIVID -> floatArrayOf(
                1.25f, 0f, 0f, 0f, 5f,
                0f, 1.25f, 0f, 0f, 5f,
                0f, 0f, 1.25f, 0f, 5f,
                0f, 0f, 0f, 1f, 0f
            )
            NOIR -> floatArrayOf(
                0.33f, 0.59f, 0.11f, 0f, -10f,
                0.33f, 0.59f, 0.11f, 0f, -10f,
                0.33f, 0.59f, 0.11f, 0f, -10f,
                0f, 0f, 0f, 1f, 0f
            )
            WARM -> floatArrayOf(
                1.2f, 0f, 0f, 0f, 18f,
                0f, 1.05f, 0f, 0f, 8f,
                0f, 0f, 0.85f, 0f, -15f,
                0f, 0f, 0f, 1f, 0f
            )
            COOL -> floatArrayOf(
                0.85f, 0f, 0f, 0f, -10f,
                0f, 1.05f, 0f, 0f, 10f,
                0f, 0f, 1.3f, 0f, 25f,
                0f, 0f, 0f, 1f, 0f
            )
            CYBERPUNK -> floatArrayOf(
                1.3f, 0f, 0.2f, 0f, 15f,
                0f, 0.9f, 0.1f, 0f, -5f,
                0.2f, 0f, 1.4f, 0f, 20f,
                0f, 0f, 0f, 1f, 0f
            )
            CINEMATIC -> floatArrayOf(
                1.15f, 0f, 0f, 0f, 12f,
                0f, 1.0f, 0.1f, 0f, 5f,
                -0.1f, 0.1f, 1.25f, 0f, 15f,
                0f, 0f, 0f, 1f, 0f
            )
            VINTAGE -> floatArrayOf(
                0.9f, 0.1f, 0.1f, 0f, 25f,
                0.1f, 0.85f, 0.1f, 0f, 20f,
                0.1f, 0.1f, 0.75f, 0f, 10f,
                0f, 0f, 0f, 1f, 0f
            )
        }
    }
}
