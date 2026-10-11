package com.example.model

import java.util.UUID

data class VideoMetadata(
    val filename: String = "",
    val title: String = "",
    val description: String = "",
    val keywords: String = "",
    val categories: String = "",
    val id: String = UUID.randomUUID().toString(),
    val createdAtMillis: Long = System.currentTimeMillis()
) {
    /**
     * Parses comma-separated keywords into a cleaned list of individual lowercase keywords.
     */
    fun parseKeywordsList(): List<String> {
        return keywords.split(",")
            .map { it.trim() }
            .filter { it.isNotEmpty() }
    }

    /**
     * Formats distinct lowercase keywords separated by comma and space.
     */
    fun cleanKeywordsString(): String {
        return parseKeywordsList()
            .map { it.lowercase() }
            .distinct()
            .joinToString(", ")
    }

    /**
     * Formats metadata as a standard CSV string row for microstock platforms.
     */
    fun toCsvRow(defaultFilename: String = "Vid-WM.mp4"): String {
        val chosenFilename = if (filename.isNotBlank()) {
            if (!filename.endsWith(".mp4", ignoreCase = true)) "$filename.mp4" else filename
        } else {
            defaultFilename
        }
        val safeFilename = escapeCsv(chosenFilename)
        val safeTitle = escapeCsv(title)
        val safeDesc = escapeCsv(description)
        val safeKeywords = escapeCsv(cleanKeywordsString())
        return "$safeFilename,$safeTitle,$safeDesc,$safeKeywords"
    }

    fun toFullCsv(defaultFilename: String = "Vid-WM.mp4"): String {
        val header = "Filename,Title,Description,Keywords"
        return "$header\n${toCsvRow(defaultFilename)}"
    }

    private fun escapeCsv(value: String): String {
        val escaped = value.replace("\"", "\"\"")
        return "\"$escaped\""
    }
}

data class MetadataValidationReport(
    val filenameWordCount: Int,
    val isFilenameValid: Boolean,
    val filenameMessage: String,

    val titleLength: Int,
    val titleWordCount: Int,
    val isTitleValid: Boolean,
    val titleMessage: String,

    val descLength: Int,
    val descSentenceCount: Int,
    val isDescValid: Boolean,
    val descMessage: String,

    val keywordCount: Int,
    val uniqueKeywordCount: Int,
    val hasDuplicates: Boolean,
    val hasUppercase: Boolean,
    val isKeywordsValid: Boolean,
    val keywordsMessage: String,

    val isAllValid: Boolean
)
