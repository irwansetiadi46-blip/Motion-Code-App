package com.example.model

enum class AspectRatioCategory(val displayName: String) {
    LANDSCAPE("16:9 Landscape"),
    VERTICAL_9_16("9:16 Vertical (Reels / TikTok)"),
    VERTICAL_4_5("4:5 Vertical (Feed IG)"),
    VERTICAL_3_4("3:4 Vertical"),
    SQUARE("1:1 Square")
}

data class VideoResolution(
    val width: Int,
    val height: Int,
    val label: String,
    val category: AspectRatioCategory,
    val isDefault: Boolean = false
) {
    val aspectRatioString: String
        get() = "${width}x${height}"

    val aspectRatioFloat: Float
        get() = width.toFloat() / height.toFloat()

    companion object {
        val ALL = listOf(
            // 16:9
            VideoResolution(1920, 1080, "Full HD 1080p (1920×1080)", AspectRatioCategory.LANDSCAPE, isDefault = true),
            VideoResolution(2560, 1440, "2K QHD (2560×1440)", AspectRatioCategory.LANDSCAPE),
            VideoResolution(1280, 720, "HD 720p (1280×720)", AspectRatioCategory.LANDSCAPE),

            // 9:16
            VideoResolution(1080, 1920, "Full HD 9:16 (1080×1920)", AspectRatioCategory.VERTICAL_9_16),
            VideoResolution(720, 1280, "HD 9:16 (720×1280)", AspectRatioCategory.VERTICAL_9_16),
            VideoResolution(1440, 2560, "2K 9:16 (1440×2560)", AspectRatioCategory.VERTICAL_9_16),

            // 4:5
            VideoResolution(1080, 1350, "Full HD 4:5 (1080×1350)", AspectRatioCategory.VERTICAL_4_5),
            VideoResolution(1440, 1800, "2K 4:5 (1440×1800)", AspectRatioCategory.VERTICAL_4_5),

            // 3:4
            VideoResolution(1080, 1440, "Full HD 3:4 (1080×1440)", AspectRatioCategory.VERTICAL_3_4),
            VideoResolution(1440, 1920, "2K 3:4 (1440×1920)", AspectRatioCategory.VERTICAL_3_4),

            // 1:1
            VideoResolution(1080, 1080, "Square HD (1080×1080)", AspectRatioCategory.SQUARE),
            VideoResolution(1440, 1440, "Square 2K (1440×1440)", AspectRatioCategory.SQUARE)
        )
    }
}

data class QualityBitrate(
    val bitrateBps: Long,
    val label: String
) {
    companion object {
        val ALL = listOf(
            QualityBitrate(6_000_000L, "Standard (6 Mbps)"),
            QualityBitrate(10_000_000L, "High (10 Mbps)"),
            QualityBitrate(20_000_000L, "Ultra (20 Mbps)"),
            QualityBitrate(40_000_000L, "Extreme (40 Mbps)")
        )
    }
}

data class RenderConfig(
    val resolution: VideoResolution = VideoResolution.ALL.first { it.isDefault },
    val durationSeconds: Int = 6,
    val fps: Int = 60,
    val quality: QualityBitrate = QualityBitrate.ALL[2] // 20 Mbps Ultra
) {
    val totalFrames: Int
        get() = durationSeconds * fps

    val safeBitrateBps: Long
        get() {
            val pixelRate = resolution.width.toLong() * resolution.height.toLong() * fps
            val ceiling = when {
                pixelRate <= 1920L * 1080 * 30 -> 8_000_000L
                pixelRate <= 1920L * 1080 * 60 -> 14_000_000L
                pixelRate <= 2560L * 1440 * 30 -> 18_000_000L
                pixelRate <= 2560L * 1440 * 60 -> 24_000_000L
                else -> 32_000_000L
            }
            return minOf(quality.bitrateBps, ceiling)
        }

    val estimatedSizeBytes: Long
        get() = (safeBitrateBps / 8) * durationSeconds
}
