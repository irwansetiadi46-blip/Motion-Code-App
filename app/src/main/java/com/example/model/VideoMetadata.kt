package com.example.model

data class VideoMetadata(
    val title: String = "",
    val description: String = "",
    val keywords: String = ""
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
    fun toCsvRow(filename: String = "video.mp4"): String {
        val safeTitle = escapeCsv(title)
        val safeDesc = escapeCsv(description)
        val safeKeywords = escapeCsv(cleanKeywordsString())
        val safeFilename = escapeCsv(filename)
        return "$safeFilename,$safeTitle,$safeDesc,$safeKeywords"
    }

    fun toFullCsv(filename: String = "video.mp4"): String {
        val header = "Filename,Title,Description,Keywords"
        return "$header\n${toCsvRow(filename)}"
    }

    private fun escapeCsv(value: String): String {
        val escaped = value.replace("\"", "\"\"")
        return "\"$escaped\""
    }
}

data class MetadataValidationReport(
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
