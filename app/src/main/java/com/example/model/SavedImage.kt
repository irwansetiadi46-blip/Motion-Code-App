package com.example.model

import java.io.File

data class SavedImage(
    val id: String,
    val file: File,
    val title: String,
    val width: Int,
    val height: Int,
    val format: ImageFormat,
    val fileSizeBytes: Long,
    val createdAtMillis: Long
)
