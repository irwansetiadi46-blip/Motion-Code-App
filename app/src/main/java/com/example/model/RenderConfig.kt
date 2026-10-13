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

    val totalPixels: Long
        get() = width.toLong() * height.toLong()

    val megaPixels: Float
        get() = totalPixels / 1_000_000f

    val isMicrostockCompliant: Boolean
        get() = totalPixels >= 4_000_000L

    val cleanLabel: String
        get() = label.substringBefore(" (").trim()

    val imageLabel: String
        get() {
            val tier = when {
                totalPixels >= 8_000_000L -> "Ultra"
                totalPixels >= 5_500_000L -> "Medium"
                else -> "Small"
            }
            val mpInt = kotlin.math.round(megaPixels).toInt()
            val mpStr = if (kotlin.math.abs(megaPixels - mpInt) < 0.15f) {
                "${mpInt}Mp"
            } else {
                "${String.format(java.util.Locale.US, "%.1f", megaPixels)}Mp"
            }
            return "$tier ${width}x${height} $mpStr"
        }

    companion object {
        val ALL = listOf(
            // 16:9 Landscape
            VideoResolution(3840, 2160, "4K Ultra HD (3840×2160)", AspectRatioCategory.LANDSCAPE),
            VideoResolution(2670, 1500, "Microstock 4MP 16:9 (2670×1500)", AspectRatioCategory.LANDSCAPE),
            VideoResolution(2560, 1440, "2K QHD 1440p (2560×1440)", AspectRatioCategory.LANDSCAPE),
            VideoResolution(1920, 1080, "Full HD 1080p (1920×1080)", AspectRatioCategory.LANDSCAPE, isDefault = true),
            VideoResolution(1280, 720, "HD 720p (1280×720)", AspectRatioCategory.LANDSCAPE),

            // 9:16 Vertical
            VideoResolution(2160, 3840, "4K 9:16 (2160×3840)", AspectRatioCategory.VERTICAL_9_16),
            VideoResolution(1500, 2670, "Microstock 4MP 9:16 (1500×2670)", AspectRatioCategory.VERTICAL_9_16),
            VideoResolution(1440, 2560, "2K 9:16 (1440×2560)", AspectRatioCategory.VERTICAL_9_16),
            VideoResolution(1080, 1920, "Full HD 9:16 (1080×1920)", AspectRatioCategory.VERTICAL_9_16),
            VideoResolution(720, 1280, "HD 9:16 (720×1280)", AspectRatioCategory.VERTICAL_9_16),

            // 4:5 Vertical
            VideoResolution(2160, 2700, "4K 4:5 (2160×2700)", AspectRatioCategory.VERTICAL_4_5),
            VideoResolution(1792, 2240, "Microstock 4MP 4:5 (1792×2240)", AspectRatioCategory.VERTICAL_4_5),
            VideoResolution(1440, 1800, "2K 4:5 (1440×1800)", AspectRatioCategory.VERTICAL_4_5),
            VideoResolution(1080, 1350, "Full HD 4:5 (1080×1350)", AspectRatioCategory.VERTICAL_4_5),
            VideoResolution(720, 900, "HD 4:5 (720×900)", AspectRatioCategory.VERTICAL_4_5),

            // 3:4 Vertical
            VideoResolution(4000, 3000, "Ultra 12MP 4:3 (4000×3000)", AspectRatioCategory.VERTICAL_3_4),
            VideoResolution(2160, 2880, "4K 3:4 (2160×2880)", AspectRatioCategory.VERTICAL_3_4),
            VideoResolution(1732, 2310, "Microstock 4MP 3:4 (1732×2310)", AspectRatioCategory.VERTICAL_3_4),
            VideoResolution(1440, 1920, "2K 3:4 (1440×1920)", AspectRatioCategory.VERTICAL_3_4),
            VideoResolution(1080, 1440, "Full HD 3:4 (1080×1440)", AspectRatioCategory.VERTICAL_3_4),
            VideoResolution(720, 960, "HD 3:4 (720×960)", AspectRatioCategory.VERTICAL_3_4),

            // 1:1 Square
            VideoResolution(4000, 4000, "Ultra Square 16MP (4000×4000)", AspectRatioCategory.SQUARE),
            VideoResolution(3000, 3000, "Ultra Square 9MP (3000×3000)", AspectRatioCategory.SQUARE),
            VideoResolution(2160, 2160, "Square 4K (2160×2160)", AspectRatioCategory.SQUARE),
            VideoResolution(2000, 2000, "Microstock 4MP 1:1 (2000×2000)", AspectRatioCategory.SQUARE),
            VideoResolution(1440, 1440, "Square 2K (1440×1440)", AspectRatioCategory.SQUARE),
            VideoResolution(1080, 1080, "Square Full HD (1080×1080)", AspectRatioCategory.SQUARE),
            VideoResolution(720, 720, "Square HD (720×720)", AspectRatioCategory.SQUARE)
        )

        /**
         * Resolusi khusus untuk mode Ekspor Images.
         * Wajib memenuhi standar minimal 4.0 Mega Pixel (4,000,000 pixels) untuk microstock.
         */
        val IMAGE_RESOLUTIONS: List<VideoResolution>
            get() = ALL.filter { it.totalPixels >= 4_000_000L }
    }
}

data class QualityBitrate(
    val bitrateBps: Long,
    val label: String
) {
    companion object {
        val ALL = listOf(
            QualityBitrate(4_000_000L, "Hemat / Cepat (4 Mbps)"),
            QualityBitrate(8_000_000L, "Standar HD (8 Mbps)"),
            QualityBitrate(14_000_000L, "Tinggi (14 Mbps)"),
            QualityBitrate(20_000_000L, "Sangat Tinggi (20 Mbps)"),
            QualityBitrate(25_000_000L, "Maksimal (25 Mbps)")
        )
    }
}

data class RenderConfig(
    val resolution: VideoResolution = VideoResolution.ALL.first { it.isDefault },
    val durationSeconds: Int = 10,
    val fps: Int = 60,
    val quality: QualityBitrate = QualityBitrate.ALL[1] // 8 Mbps Standard HD
) {
    val totalFrames: Int
        get() = durationSeconds * fps

    val safeBitrateBps: Long
        get() {
            // Memberikan batas aman yang mendukung hingga 25 Mbps untuk bitrate maksimum yang dipilih user
            val ceiling = when {
                fps > 30 || resolution.width >= 1920 || resolution.height >= 1920 -> 25_000_000L
                resolution.width >= 1280 || resolution.height >= 1280 -> 20_000_000L
                else -> 12_000_000L
            }
            return minOf(quality.bitrateBps, ceiling)
        }

    val estimatedSizeBytes: Long
        get() = (safeBitrateBps / 8) * durationSeconds
}
