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
        description = "Warna murni alami sensor tanpa pemrosesan warna",
        previewColor = 0xFF4A90E2
    ),
    RETRO_90S(
        id = "RETRO_90S",
        displayName = "Retro 90s",
        description = "Gaya analog kaset VHS & film 90-an hangat bernostalgia",
        previewColor = 0xFFD35400
    ),
    VINTAGE_FILM(
        id = "VINTAGE_FILM",
        displayName = "Vintage Film",
        description = "Warna film 35mm jadul era 80-an dengan bayangan matte hangat",
        previewColor = 0xFF8D6E63
    ),
    POLAROID(
        id = "POLAROID",
        displayName = "Polaroid Jadul",
        description = "Cetak instan Polaroid klasik dengan highlight lembut & rona krem",
        previewColor = 0xFFFFCA28
    ),
    CINEMATIC(
        id = "CINEMATIC",
        displayName = "Teal & Orange",
        description = "Grading bioskop Hollywood modern dengan kontras dramatis",
        previewColor = 0xFF00897B
    ),
    KODAK_CHROME(
        id = "KODAK_CHROME",
        displayName = "Kodak Chrome",
        description = "Karakter slide film legendaris warna merah pekat dan langit jernih",
        previewColor = 0xFFE65100
    ),
    FUJI_CLASSIC(
        id = "FUJI_CLASSIC",
        displayName = "Fuji Nature",
        description = "Kontras dokumenter khas film Jepang dengan hijau zamrud dalam",
        previewColor = 0xFF2E7D32
    ),
    GOLDEN_HOUR(
        id = "GOLDEN_HOUR",
        displayName = "Golden Sunset",
        description = "Kilau cahaya senja matahari terbenam keemasan yang menenangkan",
        previewColor = 0xFFFF9800
    ),
    CYBERPUNK(
        id = "CYBERPUNK",
        displayName = "Cyberpunk",
        description = "Nuansa malam neon futuristik ultraviolet dan cyan elektrik",
        previewColor = 0xFFE040FB
    ),
    PASTEL_DREAM(
        id = "PASTEL_DREAM",
        displayName = "Pastel Dream",
        description = "Estetika lembut dreamy Korea dengan kontras rendah & rona manis",
        previewColor = 0xFFF48FB1
    ),
    NOIR_CLASSIC(
        id = "NOIR_CLASSIC",
        displayName = "Noir Klasik",
        description = "Monokrom film noir 1950s kontras tinggi dengan bayangan pekat",
        previewColor = 0xFF212121
    ),
    SILVER_MONO(
        id = "SILVER_MONO",
        displayName = "Silver Luxe",
        description = "Monokrom perak halus premium dengan gradasi abu-abu jernih",
        previewColor = 0xFF90A4AE
    ),
    MOODY_EMERALD(
        id = "MOODY_EMERALD",
        displayName = "Moody Forest",
        description = "Hijau hutan lebat misterius dengan bayangan matte pudar",
        previewColor = 0xFF004D40
    ),
    WARM_AMBER(
        id = "WARM_AMBER",
        displayName = "Warm Amber",
        description = "Sentuhan cahaya tembaga hangat nyaman untuk potret manusia",
        previewColor = 0xFFFFB300
    ),
    COOL_NORDIC(
        id = "COOL_NORDIC",
        displayName = "Nordic Blue",
        description = "Nuansa es skandinavia sejuk dengan aksen biru langit murni",
        previewColor = 0xFF00E5FF
    ),
    SEPIA(
        id = "SEPIA",
        displayName = "Sepia Tempo Dulu",
        description = "Foto antik abad lampau dengan rona tembaga hangat abadi",
        previewColor = 0xFF6D4C41
    ),
    INFRARED(
        id = "INFRARED",
        displayName = "Aerochrome",
        description = "Inframerah surealis dengan tanaman merah delima dan langit es",
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
            RETRO_90S -> floatArrayOf(
                1.18f, 0.10f, 0.02f, 0f, 18f,
                0.05f, 0.96f, 0.05f, 0f, 12f,
                -0.08f, 0.05f, 0.76f, 0f, -6f,
                0f, 0f, 0f, 1f, 0f
            )
            VINTAGE_FILM -> floatArrayOf(
                1.08f, 0.12f, 0.05f, 0f, 22f,
                0.06f, 0.94f, 0.06f, 0f, 16f,
                0.04f, 0.06f, 0.74f, 0f, 6f,
                0f, 0f, 0f, 1f, 0f
            )
            POLAROID -> floatArrayOf(
                1.15f, 0.08f, 0.04f, 0f, 20f,
                0.04f, 1.05f, 0.04f, 0f, 16f,
                0.02f, 0.04f, 0.90f, 0f, 12f,
                0f, 0f, 0f, 1f, 0f
            )
            CINEMATIC -> floatArrayOf(
                1.24f, 0f, 0.05f, 0f, 15f,
                0f, 1.05f, 0.15f, 0f, 5f,
                -0.12f, 0.18f, 1.35f, 0f, 18f,
                0f, 0f, 0f, 1f, 0f
            )
            KODAK_CHROME -> floatArrayOf(
                1.25f, 0.05f, 0f, 0f, 16f,
                0.02f, 1.10f, 0f, 0f, 10f,
                -0.08f, 0.02f, 0.88f, 0f, -10f,
                0f, 0f, 0f, 1f, 0f
            )
            FUJI_CLASSIC -> floatArrayOf(
                1.06f, 0f, -0.05f, 0f, -2f,
                0f, 1.20f, 0.05f, 0f, 8f,
                -0.08f, -0.05f, 1.10f, 0f, 6f,
                0f, 0f, 0f, 1f, 0f
            )
            GOLDEN_HOUR -> floatArrayOf(
                1.32f, 0.12f, 0f, 0f, 26f,
                0.06f, 1.14f, 0f, 0f, 14f,
                -0.16f, -0.10f, 0.78f, 0f, -22f,
                0f, 0f, 0f, 1f, 0f
            )
            CYBERPUNK -> floatArrayOf(
                1.38f, 0f, 0.28f, 0f, 24f,
                -0.10f, 0.92f, 0.15f, 0f, -10f,
                0.22f, 0f, 1.50f, 0f, 28f,
                0f, 0f, 0f, 1f, 0f
            )
            PASTEL_DREAM -> floatArrayOf(
                1.14f, 0.08f, 0.08f, 0f, 22f,
                0.05f, 1.08f, 0.05f, 0f, 20f,
                0.08f, 0.05f, 1.16f, 0f, 24f,
                0f, 0f, 0f, 1f, 0f
            )
            NOIR_CLASSIC -> floatArrayOf(
                0.36f, 0.58f, 0.10f, 0f, -18f,
                0.36f, 0.58f, 0.10f, 0f, -18f,
                0.36f, 0.58f, 0.10f, 0f, -18f,
                0f, 0f, 0f, 1f, 0f
            )
            SILVER_MONO -> floatArrayOf(
                0.28f, 0.60f, 0.12f, 0f, 12f,
                0.28f, 0.60f, 0.12f, 0f, 12f,
                0.28f, 0.60f, 0.12f, 0f, 12f,
                0f, 0f, 0f, 1f, 0f
            )
            MOODY_EMERALD -> floatArrayOf(
                0.88f, 0.02f, 0f, 0f, -12f,
                0.05f, 1.28f, 0.08f, 0f, 14f,
                -0.05f, 0.08f, 1.12f, 0f, 4f,
                0f, 0f, 0f, 1f, 0f
            )
            WARM_AMBER -> floatArrayOf(
                1.22f, 0f, 0f, 0f, 20f,
                0f, 1.06f, 0f, 0f, 10f,
                0f, 0f, 0.82f, 0f, -18f,
                0f, 0f, 0f, 1f, 0f
            )
            COOL_NORDIC -> floatArrayOf(
                0.84f, 0f, 0f, 0f, -12f,
                0f, 1.04f, 0f, 0f, 8f,
                0f, 0f, 1.35f, 0f, 28f,
                0f, 0f, 0f, 1f, 0f
            )
            SEPIA -> floatArrayOf(
                0.393f, 0.769f, 0.189f, 0f, 12f,
                0.349f, 0.686f, 0.168f, 0f, 8f,
                0.272f, 0.534f, 0.131f, 0f, -8f,
                0f, 0f, 0f, 1f, 0f
            )
            INFRARED -> floatArrayOf(
                1.48f, -0.22f, 0.10f, 0f, 32f,
                -0.10f, 0.68f, 0.10f, 0f, -12f,
                0.15f, 0.10f, 1.38f, 0f, 16f,
                0f, 0f, 0f, 1f, 0f
            )
        }
    }
}
