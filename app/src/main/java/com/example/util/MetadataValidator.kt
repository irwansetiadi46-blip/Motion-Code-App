package com.example.util

import com.example.model.MetadataValidationReport
import com.example.model.VideoMetadata

object MetadataValidator {

    const val TITLE_MAX_CHARS = 100
    const val TITLE_MIN_WORDS = 5

    const val DESC_MAX_CHARS = 150
    const val DESC_MIN_SENTENCES = 1

    const val KEYWORDS_MIN = 30
    const val KEYWORDS_MAX = 49

    fun validate(metadata: VideoMetadata): MetadataValidationReport {
        // 1. Title Validation
        val trimmedTitle = metadata.title.trim()
        val titleWords = if (trimmedTitle.isEmpty()) emptyList() else trimmedTitle.split(Regex("\\s+")).filter { it.isNotBlank() }
        val titleWordCount = titleWords.size
        val titleLength = metadata.title.length

        val isTitleWordOk = titleWordCount >= TITLE_MIN_WORDS
        val isTitleCharOk = titleLength <= TITLE_MAX_CHARS && titleLength > 0
        val isTitleValid = isTitleWordOk && isTitleCharOk

        val titleMessage = when {
            trimmedTitle.isEmpty() -> "Title wajib diisi (Min $TITLE_MIN_WORDS kata, Max $TITLE_MAX_CHARS char)"
            titleWordCount < TITLE_MIN_WORDS -> "Title kurang kata: $titleWordCount / $TITLE_MIN_WORDS kata minimum"
            titleLength > TITLE_MAX_CHARS -> "Title terlalu panjang: $titleLength / $TITLE_MAX_CHARS char (lebih ${titleLength - TITLE_MAX_CHARS})"
            else -> "Title valid ($titleWordCount kata, $titleLength/$TITLE_MAX_CHARS char)"
        }

        // 2. Description Validation
        val trimmedDesc = metadata.description.trim()
        val descLength = metadata.description.length
        val hasTerminalPunctuation = trimmedDesc.endsWith('.') || trimmedDesc.endsWith('!') || trimmedDesc.endsWith('?')
        val descSentences = if (!hasTerminalPunctuation || trimmedDesc.isEmpty()) 0 else {
            trimmedDesc.split(Regex("[.!?]+")).filter { it.trim().isNotBlank() }.size
        }
        val isDescSentenceOk = hasTerminalPunctuation && descSentences >= DESC_MIN_SENTENCES
        val isDescCharOk = descLength <= DESC_MAX_CHARS && descLength > 0
        val isDescValid = isDescSentenceOk && isDescCharOk

        val descMessage = when {
            trimmedDesc.isEmpty() -> "Description wajib diisi (Min 1 kalimat, Max $DESC_MAX_CHARS char)"
            descLength > DESC_MAX_CHARS -> "Description terlalu panjang: $descLength / $DESC_MAX_CHARS char (lebih ${descLength - DESC_MAX_CHARS})"
            !hasTerminalPunctuation -> "Harus berupa kalimat lengkap yang diakhiri tanda titik (.)"
            !isDescSentenceOk -> "Minimal $DESC_MIN_SENTENCES kalimat lengkap"
            else -> "Description valid ($descLength/$DESC_MAX_CHARS char, $descSentences kalimat)"
        }

        // 3. Keywords Validation
        val rawKeywords = metadata.keywords.split(",").map { it.trim() }.filter { it.isNotEmpty() }
        val keywordCount = rawKeywords.size

        val lowercaseKeywords = rawKeywords.map { it.lowercase() }
        val uniqueKeywords = lowercaseKeywords.distinct()
        val uniqueKeywordCount = uniqueKeywords.size

        val hasDuplicates = keywordCount > uniqueKeywordCount
        val hasUppercase = rawKeywords.any { word -> word.any { ch -> ch.isUpperCase() } }

        val isCountOk = keywordCount in KEYWORDS_MIN..KEYWORDS_MAX
        val isKeywordsValid = isCountOk && !hasDuplicates && !hasUppercase

        val keywordsMessage = when {
            keywordCount == 0 -> "Keywords wajib diisi (Min $KEYWORDS_MIN, Max $KEYWORDS_MAX unik lowercase)"
            keywordCount < KEYWORDS_MIN -> "Jumlah keywords kurang: $keywordCount / $KEYWORDS_MIN minimum (kurang ${KEYWORDS_MIN - keywordCount})"
            keywordCount > KEYWORDS_MAX -> "Jumlah keywords melebihi batas: $keywordCount / $KEYWORDS_MAX maksimum (lebih ${keywordCount - KEYWORDS_MAX})"
            hasDuplicates -> "Terdapat keyword duplikat (unik: $uniqueKeywordCount dari $keywordCount)"
            hasUppercase -> "Ada huruf kapital (semua keyword harus lowercase)"
            else -> "Keywords valid ($keywordCount keyword unik lowercase)"
        }

        val isAllValid = isTitleValid && isDescValid && isKeywordsValid

        return MetadataValidationReport(
            titleLength = titleLength,
            titleWordCount = titleWordCount,
            isTitleValid = isTitleValid,
            titleMessage = titleMessage,
            descLength = descLength,
            descSentenceCount = descSentences,
            isDescValid = isDescValid,
            descMessage = descMessage,
            keywordCount = keywordCount,
            uniqueKeywordCount = uniqueKeywordCount,
            hasDuplicates = hasDuplicates,
            hasUppercase = hasUppercase,
            isKeywordsValid = isKeywordsValid,
            keywordsMessage = keywordsMessage,
            isAllValid = isAllValid
        )
    }

    /**
     * Cleans, deduplicates, and lowercases raw comma-separated keywords string.
     */
    fun cleanAndDeduplicateKeywords(raw: String): String {
        return raw.split(",")
            .map { it.trim().lowercase() }
            .filter { it.isNotEmpty() }
            .distinct()
            .joinToString(", ")
    }
}
