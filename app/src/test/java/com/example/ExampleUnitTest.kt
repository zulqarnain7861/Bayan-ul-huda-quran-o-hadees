package com.example

import com.example.ui.audio.availableReciters
import com.example.ui.quran.TajweedCorrectionEngine
import org.junit.Assert.*
import org.junit.Test

class ExampleUnitTest {

  @Test
  fun addition_isCorrect() {
    assertEquals(4, 2 + 2)
  }

  @Test
  fun testTajweedArabicNormalization() {
    val wordWithTashkeel = "بِسْمِ"
    val normalized = TajweedCorrectionEngine.normalizeArabicForComparison(wordWithTashkeel)
    assertEquals("بسم", normalized)

    val wordWithAlifVariations = "إِيَّاكَ"
    val normalizedAlif = TajweedCorrectionEngine.normalizeArabicForComparison(wordWithAlifVariations)
    assertEquals("اياك", normalizedAlif)
  }

  @Test
  fun testTajweedEvaluation_detectsMissedWordsInRed() {
    val canonical = "الْحَمْدُ لِلَّهِ رَبِّ الْعَالَمِينَ"
    // Reciter recited first 3 words, missed "العالمين"
    val spoken = "الحمد لله رب"

    val result = TajweedCorrectionEngine.evaluateRecitation(
      canonicalVerseText = canonical,
      recognizedSpokenArabic = spoken,
      surah = 1,
      ayah = 2
    )

    assertEquals(4, result.wordStatuses.size)
    // First 3 should match (green)
    assertTrue(result.wordStatuses[0].isMatched)
    assertFalse(result.wordStatuses[0].isMissedOrWrong)

    assertTrue(result.wordStatuses[1].isMatched)
    assertFalse(result.wordStatuses[1].isMissedOrWrong)

    assertTrue(result.wordStatuses[2].isMatched)
    assertFalse(result.wordStatuses[2].isMissedOrWrong)

    // 4th word was missed, so isMissedOrWrong MUST be true (rendered in RED)
    assertFalse(result.wordStatuses[3].isMatched)
    assertTrue(result.wordStatuses[3].isMissedOrWrong)
    assertEquals("الْعَالَمِينَ", result.wordStatuses[3].canonicalWord) // Zero alteration to original canonical text
  }

  @Test
  fun testIslamSobhiAndAbdulRehmanMossadRecitersConfigured() {
    val sobhi = availableReciters.find { it.id == "islam_sobhi" }
    assertNotNull("Islam Sobhi must be in available reciters", sobhi)
    assertTrue("Islam Sobhi recites full surah", sobhi!!.isSurahLevelReciter)

    val mossad = availableReciters.find { it.id == "abdul_rehman_mossad" }
    assertNotNull("Abdul Rehman Mossad must be in available reciters", mossad)
    assertTrue("Abdul Rehman Mossad recites full surah", mossad!!.isSurahLevelReciter)
  }

  @Test
  fun testTajweedEvaluation_threeTierGrading_greenYellowRed() {
    val canonical = "الْحَمْدُ لِلَّهِ رَبِّ الْعَالَمِينَ"
    // Spoken has:
    // 1. "الحمد" -> Exact match -> CORRECT (Green)
    // 2. "فلله" -> Minor prefix 'ف' variance -> SMALL_MISTAKE (Yellow)
    // 3. (nothing spoken for "رب" and "العالمين") -> WRONG_OR_MISSED (Red)
    val spoken = "الحمد فلله"

    val result = TajweedCorrectionEngine.evaluateRecitation(
      canonicalVerseText = canonical,
      recognizedSpokenArabic = spoken,
      surah = 1,
      ayah = 2
    )

    assertEquals(4, result.wordStatuses.size)

    // Word 0: "الْحَمْدُ" -> CORRECT (Green)
    assertEquals(com.example.ui.quran.TajweedWordGrade.CORRECT, result.wordStatuses[0].grade)
    assertTrue(result.wordStatuses[0].isMatched)
    assertFalse(result.wordStatuses[0].isSmallMistake)
    assertFalse(result.wordStatuses[0].isMissedOrWrong)

    // Word 1: "لِلَّهِ" -> recited as "فلله" -> SMALL_MISTAKE (Yellow)
    assertEquals(com.example.ui.quran.TajweedWordGrade.SMALL_MISTAKE, result.wordStatuses[1].grade)
    assertFalse(result.wordStatuses[1].isMatched)
    assertTrue(result.wordStatuses[1].isSmallMistake)
    assertFalse(result.wordStatuses[1].isMissedOrWrong)

    // Word 2: "رَبِّ" -> missed -> WRONG_OR_MISSED (Red)
    assertEquals(com.example.ui.quran.TajweedWordGrade.WRONG_OR_MISSED, result.wordStatuses[2].grade)
    assertFalse(result.wordStatuses[2].isMatched)
    assertFalse(result.wordStatuses[2].isSmallMistake)
    assertTrue(result.wordStatuses[2].isMissedOrWrong)

    // Word 3: "الْعَالَمِينَ" -> missed -> WRONG_OR_MISSED (Red)
    assertEquals(com.example.ui.quran.TajweedWordGrade.WRONG_OR_MISSED, result.wordStatuses[3].grade)
    assertTrue(result.wordStatuses[3].isMissedOrWrong)

    // Summary counts
    assertEquals(1, result.matchedCount)
    assertEquals(1, result.smallMistakeCount)
    assertEquals(2, result.wrongOrMissedCount)
  }
}
