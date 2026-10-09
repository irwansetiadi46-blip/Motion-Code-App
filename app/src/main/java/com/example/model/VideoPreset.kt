package com.example.model

data class VideoPreset(
    val id: String,
    val name: String,
    val category: String,
    val description: String,
    val code: String,
    val defaultAspect: String = "16:9",
    val defaultFps: Int = 60,
    val defaultDuration: Int = 6,
    val tags: List<String> = emptyList()
)
