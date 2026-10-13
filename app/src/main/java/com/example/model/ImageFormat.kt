package com.example.model

enum class ImageFormat(
    val extension: String,
    val mimeType: String,
    val displayName: String
) {
    JPG("jpg", "image/jpeg", "JPG"),
    PNG("png", "image/png", "PNG")
}
