package com.example.model

import java.io.File

data class SavedVideo(
    val id: String,
    val file: File,
    val title: String,
    val width: Int,
    val height: Int,
    val durationSeconds: Float,
    val fileSizeBytes: Long,
    val createdAtMillis: Long
)
