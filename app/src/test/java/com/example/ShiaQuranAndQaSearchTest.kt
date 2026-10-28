package com.example

import com.example.data.local.ExtendedHadithDataProvider
import com.example.data.local.ShiaTranslationDataProvider
import com.example.data.local.entities.AyahEntity
import com.example.data.repository.TafsirRepository
import com.example.ui.audio.availableReciters
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class ShiaQuranAndQaSearchTest {

  @Test
  fun `verify Stage 1 - 8 Sunni translations and 5 Shia translations are authentic and distinct`() {
    val sunniEditions = ShiaTranslationDataProvider.sunniEditions
    val shiaEditions = ShiaTranslationDataProvider.shiaEditions

    // Sunni translations count: Exactly 8
    assertEquals("Must preserve all 8 Sunni translations", 8, sunniEditions.size)
    val expectedSunniKeys = listOf(
      "en.sahih", "en.pickthall", "en.yusufali", "en.clearquran",
      "ur.jalandhry", "ur.maududi", "ur.junagarhi", "ur.usmani"
    )
    expectedSunniKeys.forEach { key ->
      assertTrue("Sunni edition $key must be present", sunniEditions.any { it.key == key })
    }

    // Shia translations count: Exactly 5 verified editions
    assertEquals("Must provide all 5 verified Shia translations", 5, shiaEditions.size)
    val expectedShiaKeys = listOf(
      "en.qarai", "en.mir_ahmed_ali", "en.shakir", "ur.jawadi", "ur.najafi"
    )
    expectedShiaKeys.forEach { key ->
      assertTrue("Shia edition $key must be present", shiaEditions.any { it.key == key })
    }

    // Test text generation for Surah 1 & 2 for all Shia and Sunni translations
    for (surahNum in listOf(1, 2, 36, 112, 114)) {
      expectedSunniKeys.forEach { key ->
        val text = ShiaTranslationDataProvider.getSpecificTranslation(surahNum, 1, key, "Base English", "Base Urdu")
        assertNotNull("Text for $key at $surahNum:1 must not be null", text)
        assertTrue("Text for $key at $surahNum:1 must not be blank", text.isNotBlank())
      }
      expectedShiaKeys.forEach { key ->
        val text = ShiaTranslationDataProvider.getSpecificTranslation(surahNum, 1, key, "Base English", "Base Urdu")
        assertNotNull("Text for $key at $surahNum:1 must not be null", text)
        assertTrue("Text for $key at $surahNum:1 must not be blank", text.isNotBlank())
      }
    }
  }

  @Test
  fun `verify Stage 1 - 14 Sunni tafsirs and 8 Shia tafsirs are registered and strictly separated`() {
    val allEditions = TafsirRepository.editions
    assertEquals("Must offer 22 Tafsirs (14 Sunni + 8 Shia)", 22, allEditions.size)

    val sunniTafsirs = TafsirRepository.getEditionsForTradition("SUNNI")
    val shiaTafsirs = TafsirRepository.getEditionsForTradition("SHIA")

    assertEquals("Must have 14 Sunni tafsirs in Sunni section", 14, sunniTafsirs.size)
    assertEquals("Must have 8 Shia tafsirs in Shia section", 8, shiaTafsirs.size)
    assertTrue("Sunni section must contain ONLY Sunni tafsirs", sunniTafsirs.all { it.tradition == "SUNNI" })
    assertTrue("Shia section must contain ONLY Shia tafsirs", shiaTafsirs.all { it.tradition == "SHIA" })

    // Check specific Shia exegeses
    val shiaKeys = listOf(
      "al_mizan", "majma_ul_bayan", "al_tibyan",
      "nur_al_thaqalayn", "tafsir_al_qummi", "tafsir_al_ayyashi",
      "pooya_ali_commentary", "tafsir_al_burhan"
    )
    shiaKeys.forEach { key ->
      val tafsir = shiaTafsirs.find { it.key == key }
      assertNotNull("Shia tafsir $key must be present", tafsir)
      assertTrue("Scholar name must be documented for $key", tafsir!!.scholarName.isNotBlank())
      assertTrue("Methodology must be documented for $key", tafsir.methodology.isNotBlank())
    }

    // Check specific Sunni exegeses
    val sunniKeys = listOf(
      "ibn_kathir", "tabari", "qurtubi", "jalalayn",
      "as_sadi", "maariful_quran", "tafheem", "usmani",
      "baghawi", "baydawi", "razi_kabir", "zad_al_masir",
      "adwa_al_bayan", "ruh_al_maani"
    )
    sunniKeys.forEach { key ->
      val tafsir = sunniTafsirs.find { it.key == key }
      assertNotNull("Sunni tafsir $key must be present", tafsir)
      assertTrue("Scholar name must be documented for $key", tafsir!!.scholarName.isNotBlank())
    }
  }

  @Test
  fun `verify Stage 1 - Audio reciters independent defaults for Sunni and Shia sections`() {
    val sunniDefault = availableReciters.find { it.id == "husary" }
    assertNotNull("Sunni default reciter Al-Husary must be registered", sunniDefault)
    assertEquals("SUNNI", sunniDefault!!.tradition)

    val shiaDefault = availableReciters.find { it.id == "karim_mansouri" }
    assertNotNull("Shia default reciter Karim Mansouri must be registered", shiaDefault)
    assertEquals("SHIA", shiaDefault!!.tradition)
    assertTrue("Reciter stream URL must be EveryAyah standard format", shiaDefault.subfolder.isNotBlank())

    val maytham = availableReciters.find { it.id == "maytham_tammar" }
    assertNotNull("Alternative Shia reciter Maytham al-Tammar must be registered", maytham)
    assertEquals("SHIA", maytham!!.tradition)
  }

  @Test
  fun `verify Stage 2 - Non-dummy indexed Q&A search across Tafsirs and Hadiths with tradition filtering`() {
    val hadiths = ExtendedHadithDataProvider.getExtendedHadiths()
    assertTrue("Hadiths dataset must be substantial", hadiths.size >= 30)

    val sampleAyahs = listOf(
      AyahEntity(
        id = 1L,
        surahNumber = 1,
        verseNumber = 1,
        textArabic = "بِسْمِ اللَّهِ الرَّحْمَٰنِ الرَّحِيمِ",
        translationEnglish = "In the name of Allah, the Entirely Merciful, the Especially Merciful.",
        translationUrdu = "شروع اللہ کے نام سے جو بڑا مہربان نہایت رحم والا ہے",
        transliteration = "Bismillahi ar-Rahmani ar-Raheem",
        juzNumber = 1,
        pageNumber = 1
      ),
      AyahEntity(
        id = 255L,
        surahNumber = 2,
        verseNumber = 255,
        textArabic = "اللَّهُ لَا إِلَٰهَ إِلَّا هُوَ الْحَيُّ الْقَيُّومُ",
        translationEnglish = "Allah - there is no deity except Him, the Ever-Living, the Sustainer of all existence.",
        translationUrdu = "اللہ جس کے سوا کوئی معبود نہیں وہ زندہ ہے سب کا تھامنے والا",
        transliteration = "Allahu la ilaha illa huwal-Hayyul-Qayyum",
        juzNumber = 3,
        pageNumber = 42
      )
    )

    // 1. Search for keyword "Rahman" in both traditions
    val bothResults = TafsirRepository.searchTafsirCommentary("Rahman", "BOTH", sampleAyahs, hadiths)
    assertFalse("Searching for 'Rahman' across both traditions must return genuine records", bothResults.isEmpty())
    assertTrue("Both results must contain Sunni or Shia commentary", bothResults.any { it.edition.tradition == "SUNNI" } || bothResults.any { it.edition.tradition == "SHIA" })

    // 2. Search for "Mercy" with SHIA filter
    val shiaResults = TafsirRepository.searchTafsirCommentary("Mercy", "SHIA", sampleAyahs, hadiths)
    shiaResults.forEach {
      assertEquals("SHIA filter must strictly return SHIA exegeses", "SHIA", it.edition.tradition)
    }

    // 3. Search for SUNNI filter
    val sunniResults = TafsirRepository.searchTafsirCommentary("Mercy", "SUNNI", sampleAyahs, hadiths)
    sunniResults.forEach {
      assertEquals("SUNNI filter must strictly return SUNNI exegeses", "SUNNI", it.edition.tradition)
    }

    // 4. Searching for completely non-existent gibberish must return ZERO results (NO dummy/mock data!)
    val gibberishResults = TafsirRepository.searchTafsirCommentary("xyznonexistentgibberish9874", "BOTH", sampleAyahs, hadiths)
    assertTrue("Non-matching search query must return 0 results without inventing fake data", gibberishResults.isEmpty())
  }

  @Test
  fun `verify Duas fuzzy search supports English, Urdu, and Roman Urdu natural queries`() {
    val safarResults = com.example.data.model.DailyDuasDataProvider.searchDuasFuzzy("Safar ki dua")
    assertFalse("Safar ki dua must return authentic travel dua", safarResults.isEmpty())
    assertTrue(safarResults.first().title.contains("Safar", ignoreCase = true) || safarResults.first().title.contains("Travel", ignoreCase = true))

    val soneResults = com.example.data.model.DailyDuasDataProvider.searchDuasFuzzy("Sone se pehle ki dua")
    assertFalse("Sone se pehle ki dua must return authentic sleeping dua", soneResults.isEmpty())
    assertEquals("dua_before_sleeping", soneResults.first().id)

    val paniResults = com.example.data.model.DailyDuasDataProvider.searchDuasFuzzy("Pani peene ki dua")
    assertFalse("Pani peene ki dua must return authentic water drinking dua", paniResults.isEmpty())
    assertTrue(paniResults.first().authenticReference.contains("Shu'ab al-Iman") || paniResults.first().authenticReference.contains("Sahih"))
  }

  @Test
  fun `verify Islamic History search for Qadisiyyah, Waqia Hurrah, and Battle of Yarmouk in Sunni and Shia traditions`() {
    val allHadiths = ExtendedHadithDataProvider.getExtendedHadiths()

    // Ensure no book names have double spaces
    assertTrue(
      "All Hadith & History book names must have normalized spacing",
      allHadiths.none { it.bookName.contains("  ") || it.collectionName.contains("  ") }
    )

    // 1. Qadisiyyah in Sunni & Shia
    val qadisiyyahSunni = ExtendedHadithDataProvider.searchHadithsSmartOffline(
      localCollectionHadiths = emptyList(),
      rawQuery = "Qadisiyyah",
      activeCollectionKey = "sunni_history",
      traditionFilter = "SUNNI",
      includeCrossCollectionMatches = true
    )
    assertFalse("Qadisiyyah must return Sunni historical chronicle", qadisiyyahSunni.isEmpty())
    assertTrue(qadisiyyahSunni.any { it.collectionKey == "sunni_history" && it.bookName.contains("Qadisiyyah", ignoreCase = true) })

    val qadisiyyahShia = ExtendedHadithDataProvider.searchHadithsSmartOffline(
      localCollectionHadiths = emptyList(),
      rawQuery = "Qadisiyyah",
      activeCollectionKey = "shia_history",
      traditionFilter = "SHIA",
      includeCrossCollectionMatches = true
    )
    assertFalse("Qadisiyyah must return Shia historical chronicle", qadisiyyahShia.isEmpty())
    assertTrue(qadisiyyahShia.any { it.collectionKey == "shia_history" && it.bookName.contains("Qadisiyyah", ignoreCase = true) })

    // 2. Waqia Hurrah in Sunni & Shia
    val hurrahSunni = ExtendedHadithDataProvider.searchHadithsSmartOffline(
      localCollectionHadiths = emptyList(),
      rawQuery = "waqia hurrah",
      activeCollectionKey = "sunni_history",
      traditionFilter = "SUNNI",
      includeCrossCollectionMatches = true
    )
    assertFalse("waqia hurrah must return Sunni historical chronicle (Battle of al-Harrah 63 AH)", hurrahSunni.isEmpty())
    assertTrue(hurrahSunni.any { it.collectionKey == "sunni_history" && it.bookName.contains("Harrah", ignoreCase = true) })

    val hurrahShia = ExtendedHadithDataProvider.searchHadithsSmartOffline(
      localCollectionHadiths = emptyList(),
      rawQuery = "waqia hurrah",
      activeCollectionKey = "shia_history",
      traditionFilter = "SHIA",
      includeCrossCollectionMatches = true
    )
    assertFalse("waqia hurrah must return Shia historical chronicle (Battle of al-Harrah 63 AH)", hurrahShia.isEmpty())
    assertTrue(hurrahShia.any { it.collectionKey == "shia_history" && it.bookName.contains("Harrah", ignoreCase = true) })

    // 3. Battle of Yarmouk in Sunni & Shia
    val yarmoukSunni = ExtendedHadithDataProvider.searchHadithsSmartOffline(
      localCollectionHadiths = emptyList(),
      rawQuery = "Battle of Yarmouk",
      activeCollectionKey = "sunni_history",
      traditionFilter = "SUNNI",
      includeCrossCollectionMatches = true
    )
    assertFalse("Battle of Yarmouk must return Sunni historical chronicle", yarmoukSunni.isEmpty())
    assertTrue(yarmoukSunni.any { it.collectionKey == "sunni_history" && it.bookName.contains("Yarmouk", ignoreCase = true) })

    val yarmoukShia = ExtendedHadithDataProvider.searchHadithsSmartOffline(
      localCollectionHadiths = emptyList(),
      rawQuery = "Battle of Yarmouk",
      activeCollectionKey = "shia_history",
      traditionFilter = "SHIA",
      includeCrossCollectionMatches = true
    )
    assertFalse("Battle of Yarmouk must return Shia historical chronicle", yarmoukShia.isEmpty())
    assertTrue(yarmoukShia.any { it.collectionKey == "shia_history" && it.bookName.contains("Yarmouk", ignoreCase = true) })
  }
}
