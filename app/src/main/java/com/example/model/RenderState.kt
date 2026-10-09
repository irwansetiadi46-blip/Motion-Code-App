package com.example.model

import java.io.File

sealed interface RenderState {
    data object Idle : RenderState

    data class Preparing(val message: String = "Menyiapkan encoder...") : RenderState

    data class Rendering(
        val currentFrame: Int,
        val totalFrames: Int,
        val progress: Float,
        val statusText: String
    ) : RenderState

    data class Finalizing(val message: String = "Menyelesaikan file MP4...") : RenderState

    data class Success(
        val videoFile: File,
        val width: Int,
        val height: Int,
        val durationSeconds: Float,
        val fileSizeBytes: Long
    ) : RenderState

    data class Error(val message: String) : RenderState
}
