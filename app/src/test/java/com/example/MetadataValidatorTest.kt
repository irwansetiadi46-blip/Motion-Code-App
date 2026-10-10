package com.example

import com.example.model.VideoMetadata
import com.example.network.GeminiMetadataService
import com.example.util.MetadataValidator
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class MetadataValidatorTest {

    @Test
    fun testValidMetadataPassesAllChecks() {
        val validKeywords = (1..35).joinToString(", ") { "keyword$it" }
        val metadata = VideoMetadata(
            title = "Digital Matrix Binary Code Stream Animation Loop",
            description = "Seamless glowing binary code stream falling continuously over dark grid.",
            keywords = validKeywords
        )

        val report = MetadataValidator.validate(metadata)
        assertTrue(report.isTitleValid)
        assertTrue(report.isDescValid)
        assertTrue(report.isKeywordsValid)
        assertTrue(report.isAllValid)
    }

    @Test
    fun testTitleRules() {
        // Less than 5 words fails
        val shortWords = VideoMetadata(title = "Only Four Words Here")
        val rep1 = MetadataValidator.validate(shortWords)
        assertFalse(rep1.isTitleValid)
        assertEquals(4, rep1.titleWordCount)

        // Exactly 5 words passes
        val fiveWords = VideoMetadata(title = "This title has five words")
        val rep2 = MetadataValidator.validate(fiveWords)
        assertTrue(rep2.isTitleValid)

        // Greater than 100 characters fails
        val tooLong = VideoMetadata(title = "A".repeat(101) + " five words here to test")
        val rep3 = MetadataValidator.validate(tooLong)
        assertFalse(rep3.isTitleValid)
    }

    @Test
    fun testDescriptionRules() {
        // Exceeding 150 characters fails
        val tooLongDesc = VideoMetadata(description = "This is a sentence that is deliberately written to exceed the one hundred and fifty character maximum length constraint set by the microstock platform rules and guidelines here.")
        val rep1 = MetadataValidator.validate(tooLongDesc)
        assertFalse(rep1.isDescValid)

        // Missing sentence punctuation fails
        val noSentenceEnd = VideoMetadata(description = "This has no punctuation at the end of the line")
        val rep2 = MetadataValidator.validate(noSentenceEnd)
        assertFalse(rep2.isDescValid)

        // Valid 1 sentence ending with dot passes
        val validDesc = VideoMetadata(description = "High tech digital matrix raining binary numbers in darkness.")
        val rep3 = MetadataValidator.validate(validDesc)
        assertTrue(rep3.isDescValid)
    }

    @Test
    fun testKeywordsRules() {
        // Less than 30 fails
        val twentyKeywords = (1..20).joinToString(", ") { "tag$it" }
        val rep1 = MetadataValidator.validate(VideoMetadata(keywords = twentyKeywords))
        assertFalse(rep1.isKeywordsValid)
        assertEquals(20, rep1.keywordCount)

        // Greater than 49 fails
        val fiftyKeywords = (1..50).joinToString(", ") { "tag$it" }
        val rep2 = MetadataValidator.validate(VideoMetadata(keywords = fiftyKeywords))
        assertFalse(rep2.isKeywordsValid)
        assertEquals(50, rep2.keywordCount)

        // Uppercase keywords fail
        val upperKeywords = (1..32).joinToString(", ") { if (it == 5) "UPPERCASE" else "tag$it" }
        val rep3 = MetadataValidator.validate(VideoMetadata(keywords = upperKeywords))
        assertFalse(rep3.isKeywordsValid)
        assertTrue(rep3.hasUppercase)

        // Duplicate keywords fail
        val duplicateKeywords = (1..35).map { "tag$it" }.toMutableList().apply {
            this[5] = "tag1" // Duplicate tag1
        }.joinToString(", ")
        val rep4 = MetadataValidator.validate(VideoMetadata(keywords = duplicateKeywords))
        assertFalse(rep4.isKeywordsValid)
        assertTrue(rep4.hasDuplicates)

        // 35 unique lowercase keywords pass
        val valid35 = (1..35).joinToString(", ") { "tag$it" }
        val rep5 = MetadataValidator.validate(VideoMetadata(keywords = valid35))
        assertTrue(rep5.isKeywordsValid)
    }

    @Test
    fun testAutoFormatAndDeduplicateKeywords() {
        val messy = "TAG1, tag1, Tag2 , TAG3, tag2, tag4"
        val cleaned = MetadataValidator.cleanAndDeduplicateKeywords(messy)
        assertEquals("tag1, tag2, tag3, tag4", cleaned)
    }

    @Test
    fun testFallbackGeneratorAdheresToAllRules() {
        val service = GeminiMetadataService()
        val fallback = service.generateFallbackMetadata("Matrix Rain", "console.log('matrix');")
        val report = MetadataValidator.validate(fallback)
        assertTrue("Fallback title must be valid: ${report.titleMessage}", report.isTitleValid)
        assertTrue("Fallback desc must be valid: ${report.descMessage}", report.isDescValid)
        assertTrue("Fallback keywords must be valid: ${report.keywordsMessage}", report.isKeywordsValid)
        assertTrue("Fallback overall must be valid", report.isAllValid)
    }

    @Test
    fun testCsvFormatting() {
        val meta = VideoMetadata(
            title = "Digital \"Matrix\" Animation",
            description = "A simple sentence.",
            keywords = "tag1, tag2"
        )
        val csv = meta.toFullCsv("test.mp4")
        assertTrue(csv.contains("Filename,Title,Description,Keywords"))
        assertTrue(csv.contains("\"test.mp4\""))
        assertTrue(csv.contains("\"Digital \"\"Matrix\"\" Animation\""))
    }
}
